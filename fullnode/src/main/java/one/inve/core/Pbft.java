package one.inve.core;
import one.inve.bean.node.BaseNode;
import one.inve.node.GeneralNode;
import one.inve.util.HnKeyUtils;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

public class Pbft {
    public static Logger logger = Logger.getLogger("Pbft.class");

    GeneralNode node;
    boolean leader = false;

    Sender sender = null;
    private long sequenceNo;
    public int sequenceLength = 30000;
    ArrayList<PrevoteMessage> prevoteStore = null;

    public Pbft(GeneralNode node) {
        this.node = node;
        this.sender = new Sender(node);
    }

    /**
     * The row 15 to 27 from the algorithm given in the pdf over pbft.
     */
    public String normalFunction(String data) throws Exception {
        logger.info("normalFunction()...");
        cleanUp();
        long newSeq = calculateSequenceNumber();        // currentTimeMillis / 5000
        if (this.sequenceNo >= newSeq) {
            logger.info("Sequence number is equal. old " + this.sequenceNo + " and new " + newSeq);
        }
        while (this.sequenceNo > newSeq) {
            synchronized (this) {
                this.wait(this.sequenceNo-newSeq);
                newSeq = calculateSequenceNumber();
            }
        }
        this.node.setCurrentSequenceNo(this.sequenceNo);
        logger.info("Changed sequence " + this.sequenceNo + " to " + newSeq);
        this.sequenceNo = newSeq;
        logger.info(" SEQ_NO: " + sequenceNo);

        ArrayList<Message> voteStore = new ArrayList<>();
        int state = 0;
        this.leader = HnKeyUtils.getString4PublicKey(this.node.publicKey).equals(this.node.getSeedPubkey());
        while(true){
            if((System.currentTimeMillis()/ sequenceLength) > this.sequenceNo){
                logger.info("time out...");
                throw new TimeoutException();
            }
            synchronized (this) {
                if(state == 0) {
                    if(this.leader) {  // are we the leader?
                        sender.sendMessage(
                                new ProposeMessage(
                                        this.sequenceNo,
                                        this.node.publicKey,
                                        this.leader,
                                        Crypto.sign(data.getBytes(), this.node.privateKey),
                                        data.getBytes()
                                )
                        );
                        state = 1;
                        logger.info("state = 1");
                    } else {
                        state = 1;
                        logger.info("state = 1");
                    }
                }
                if (state == 1 && !node.mq.proposeM.isEmpty() ) {
                    ProposeMessage proposeMessage = (ProposeMessage) node.mq.proposeM.take();
                    if (verifyProposeSign(proposeMessage)) {
                        logger.info("Got ProposeMessage");
                        sender.sendMessage(
                                new PrevoteMessage(
                                        this.sequenceNo,
                                        this.node.publicKey,
                                        this.leader,
                                        proposeMessage.data
                                )
                        );
                        state = 2;
                        logger.info("state = 2");
                    }
                }
                if (state == 2 && !node.mq.prevoteM.isEmpty()) {
                    logger.info("Got PrevoteMessage");
                    prevoteStore.add((PrevoteMessage) node.mq.prevoteM.take());
                    logger.info("=== prevoteStore size: " + prevoteStore.size());
                    VerifyAgreementResult agreement = checkAgreement(prevoteStore.stream().collect(Collectors.toList()));
                    if (agreement.bool) {
                        sender.sendMessage(new VoteMessage(this.sequenceNo,
                                this.node.publicKey, this.leader, agreement.data.getBytes()));
                        state=3;
                        logger.info("state = "+ state);
                    } else {
                        logger.info("checkAgreement failed.");
                    }
                }
                if ((state == 2 || state == 3) && !node.mq.voteM.isEmpty()) {//abfrage dessen das der median bei genügend node gleich ist und sequenznr stimmt fehlt
                    logger.info("Got VoteMessage");
                    voteStore.add((VoteMessage) node.mq.voteM.take());
                    logger.info("=== voteStore size: " + voteStore.size());
                    VerifyAgreementResult agreement = checkAgreement(voteStore);
                    if (agreement.bool) {
                        logger.info("state = DONE! (now doing cleanup)");
                        this.cleanUp();
                        return agreement.data;
                    }
                } else {
                    Thread.sleep(1000);  // waits for a new message to allow all 3 ifs to check, but otherwise block.
                }
            }
        }
    }

    private long calculateSequenceNumber() {
        return System.currentTimeMillis() / sequenceLength;     // 5秒一个seqNo
    }

    /**
     * Verify the calculated median for the result.
     * @param store
     * @return a tuple as VerifyAgreementResult
     */
    public VerifyAgreementResult checkAgreement(List<Message> store) {
        byte[] pubkey = null;
        String data = "";
        for (Message e : store){
            if (e instanceof PrevoteMessage) {
                data = new String(((PrevoteMessage) e).data);
                pubkey = ((PrevoteMessage) e).pubkey;
            } else if (e instanceof VoteMessage) {
                data = new String(((VoteMessage) e).data);
                pubkey = ((VoteMessage) e).pubkey;
            } else {
                logger.error("Needs type PrevoteMessage or VoteMessage.");
                throw new IllegalArgumentException("Needs type PrevoteMessage or VoteMessage.");
            }
            int count = 0;
//            logger.info("<<<< data: " + data + ", \n pubkey:" + pubkey);
            for (Message i : store){
                if ( (i instanceof PrevoteMessage && data.equals(new String(((PrevoteMessage) i).data)))
                  || (i instanceof VoteMessage && data.equals(new String(((VoteMessage) i).data))) ){
                    count++;
                }
            }
            logger.info("need count more than " + muchMoreThenHalf() + ", agreement count: " + count);
            if (count > muchMoreThenHalf()){
                return new VerifyAgreementResult(true, data, pubkey);
            }
        }
        return new VerifyAgreementResult(false, data, pubkey);
    }

    /**
     * Check that more than the half of the group members approve the result.
     * @return
     */
    double muchMoreThenHalf() {
        return (this.getTotalNodeCount() + getFaultyNodeCount())/2;
    }

    /**
     * Verify the propose message.
     * @return
     */
    public static boolean verifyProposeSign(ProposeMessage msg){
        if (Crypto.verifySignature(msg.data, msg.sign, msg.pubkey)) {
            logger.info("verifyProposeSign success.");
            return true;
        } else {
            logger.error("verifyProposeSign failed.");
            return false;
        }
    }

    /**
     * Calculates the faulty nodes.
     * @return count of possible faulty nodes.
     * @throws InterruptedException
     */
    public double getFaultyNodeCount() {
        return (this.getTotalNodeCount() -  1)/3;
    }

    /**
     * Retrieves the total amount of nodes.
     * @return count of total nodes in the system.
     * @throws InterruptedException
     */
    public double getTotalNodeCount() {
        return this.node.getFullNodeList().values().stream()
                .filter(BaseNode::checkIfConsensusNode).count();
    }

    /**
     * Clean the memory for a new sequence number.
     */
    public void cleanUp() {
        logger.info("clean up...");
        this.sequenceNo = calculateSequenceNumber();
        if (this.prevoteStore != null) {
            this.prevoteStore = this.prevoteStore.stream()
                .filter(i -> i.sequence_no >= this.sequenceNo)
                .collect(Collectors.toCollection(ArrayList::new));
        } else {
            this.prevoteStore = new ArrayList<>();
        }
    }

    public void wholeCleanUp() {
        logger.info("whole clean up...");
        this.sequenceNo = calculateSequenceNumber();
        this.prevoteStore = new ArrayList<>();
        node.mq.prevoteM.clear();
        node.mq.proposeM.clear();
        node.mq.voteM.clear();
    }

    /**
     * A class for return tuple in java.
     */
    class VerifyAgreementResult {
        public String data = "";
        public byte[] pubkey = null;
        public final boolean bool;

        public VerifyAgreementResult(boolean bool, String data, byte[] pubkey) {
            this.bool = bool;
            this.data = data;
            this.pubkey = pubkey;
        }
    }
}

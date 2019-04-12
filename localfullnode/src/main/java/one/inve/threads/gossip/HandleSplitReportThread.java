package one.inve.threads.gossip;

import one.inve.core.Config;
import one.inve.core.Cryptos;
import one.inve.node.GeneralNode;
import one.inve.node.Main;
import one.inve.node.RpcConnectionService;
import one.inve.rpc.localfullnode.Local2localPrx;
import one.inve.transport.Address;
import one.inve.util.HnKeyUtils;
import one.inve.utils.DSA;

import java.util.HashMap;

/**
 * @author liux
 */
public class HandleSplitReportThread extends Thread {
	
	private GeneralNode node;
	private int shardId;
	private long creatorId;
	private long creatorSeq;
	private boolean isFirst;
	private String eventHash;

    public HandleSplitReportThread(GeneralNode node,int shardId,long creatorId,long creatorSeq,String eventHash,boolean isFirst) {
        this.node = node;
        this.shardId = shardId;
        this.creatorId = creatorId;
        this.creatorSeq = creatorSeq;
        this.isFirst = isFirst;
        this.eventHash = eventHash;
    }
    
    @Override
	public void run() {
		String data = Config.GOSSIP_SIGNATURE_DATA;
		byte[] sig = Cryptos.sign(data.getBytes(), node.privateKey);

		HashMap<Address, Local2localPrx> prxMap = new HashMap<>();
		prxMap.putAll(((Main)node).getConsensusThread().getPrxMap());
		prxMap.putAll(((Main)node).getSyncThread().getPrxMap());
		String pubkey = node.publicKey==null ? "" : HnKeyUtils.getString4PublicKey(node.publicKey);
		for(Address address:prxMap.keySet()) {
			prxMap.get(address).gossip4SplitDelAsync(pubkey, sig==null ? "" : DSA.encryptBASE64(sig),
					data, this.shardId, this.creatorId, this.creatorSeq, this.eventHash, isFirst);
		}
		Local2localPrx prx = RpcConnectionService.buildConnection2localFullNode(
				node.getCommunicator(),
				node.nodeParameters.selfGossipAddress.pubIP,
				node.nodeParameters.selfGossipAddress.rpcPort);
		prx.gossip4SplitDelAsync(pubkey, sig==null ? "" : DSA.encryptBASE64(sig),
				data, this.shardId, this.creatorId, this.creatorSeq, this.eventHash, isFirst);
	}
}

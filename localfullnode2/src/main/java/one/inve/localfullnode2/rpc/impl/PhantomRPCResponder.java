package one.inve.localfullnode2.rpc.impl;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zeroc.Ice.Current;

import one.inve.localfullnode2.rpc.GossipObj;

public class PhantomRPCResponder extends DefLocal2localImpl {

	private static final Logger logger = LoggerFactory.getLogger(PhantomRPCResponder.class);

	@Override
	public GossipObj gossipMyMaxSeqList4Consensus(String pubkey, String sig, String snapVersion, String snapHash,
			long[] seqs, Current current) {
		// logger.info("gossipMyMaxSeqList4Consensus Current:{}",
		// JSON.toJSONString(current));

		Random r = new Random();
		int next = r.nextInt(60);

		GossipObj gossipObj = new GossipObj();
		gossipObj.snapVersion = String.valueOf(next);
		gossipObj.snapHash = pubkey.getBytes();

		// attempt to delay the process
		try {
			TimeUnit.SECONDS.sleep(next);

			logger.info("consuming {} seconds to do nothing({}) at all", next, pubkey);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return gossipObj;

	}

}

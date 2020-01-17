package one.inve.localfullnode2.rpc.impl;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zeroc.Ice.Current;

import one.inve.localfullnode2.rpc.GossipObj;

public class PhantomRPCResponder extends DefLocal2localImpl {

	private static final Logger logger = LoggerFactory.getLogger(PhantomRPCResponder.class);

	private ChunkSize csize;

	public enum ChunkSize {
		K(1024l), M(1024l * 1024l), M10(10 * 1024l * 1024l), M100(100 * 1024l * 1024l);

		private long sz;

		private ChunkSize(long sz) {
			this.sz = sz;
		}

		public long sz() {
			return this.sz;
		}
	}

	public PhantomRPCResponder() {
		this.csize = ChunkSize.M;
	}

	public PhantomRPCResponder(ChunkSize csize) {
		this.csize = csize;
	}

	private byte[] inflate(byte[] bytes) {
		int initBytesSize = bytes.length;
		int bytesLen = initBytesSize;
		byte[] bt3 = null;

		while (bytesLen < csize.sz()) {
			bytesLen += initBytesSize;
			bt3 = new byte[bytesLen];
			System.arraycopy(bytes, 0, bt3, 0, bytes.length);
		}

		return bt3;
	}

	@Override
	public GossipObj gossipMyMaxSeqList4Consensus(String pubkey, String sig, String snapVersion, String snapHash,
			long[] seqs, Current current) {
		// logger.info("gossipMyMaxSeqList4Consensus Current:{}",
		// JSON.toJSONString(current));

		Random r = new Random();
		int next = r.nextInt(60);

		GossipObj gossipObj = new GossipObj();
		gossipObj.snapVersion = String.valueOf(next);
		// gossipObj.snapHash = pubkey.getBytes();
		gossipObj.snapHash = inflate(pubkey.getBytes());

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

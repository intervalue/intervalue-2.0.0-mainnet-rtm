package one.inve.localfullnode2.nodes;

/**
 * 
 * Copyright © INVE FOUNDATION. All rights reserved.
 * 
 * @ClassName: exploit {@code WithSeed} boilerplate
 * @Description: TODO
 * @author Francis.Deng [francis_xiiiv@163.com]
 * @date Jan 17, 2020
 *
 */
public class ChronicleWithSeed extends ChronicleSkeleton {
	private WithSeed withSeed;

	public ChronicleWithSeed(WithSeed withSeed) {
		super();
		this.withSeed = withSeed;
	}

	@Override
	public void asLocalFullNode(String seedPubIP, String seedRpcPort) {
		withSeed.asLocalFullNode(seedPubIP, seedRpcPort);

	}

	@Override
	public void shardInfo(String seedPubIP, String seedRpcPort) {
		withSeed.shardInfo(seedPubIP, seedRpcPort);

	}

	@Override
	public void allLocalFullNodeList(String seedPubIP, String seedRpcPort) {
		withSeed.allLocalFullNodeList(seedPubIP, seedRpcPort);
	}

}

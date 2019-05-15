package one.inve.localfullnode2;

/**
 * 
 * 
 * Copyright © CHXX Co.,Ltd. All rights reserved.
 * 
 * @Description: The impl encapsulates all interaction with seed/administrator
 *               node
 * @author: Francis.Deng
 * @date: May 14, 2019 11:26:51 PM
 * @version: V1.0
 */
public interface NodeEnrolled {
	/**
	 * ask for becoming a member of localfullnode
	 */
	void asLocalFullNode(String seedPubIP, String seedRpcPort);

	/**
	 * ask for sharding information
	 */
	void shardInfo(String seedPubIP, String seedRpcPort);

	/**
	 * ask for the entire localfullnode list
	 */
	void allLocalFullNodeList(String seedPubIP, String seedRpcPort);
}

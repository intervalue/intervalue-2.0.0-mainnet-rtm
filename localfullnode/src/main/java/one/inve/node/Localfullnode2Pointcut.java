package one.inve.node;

import java.util.List;

import one.inve.bean.node.LocalFullNode;
import one.inve.localfullnode2.dep.DepItemsManager;
import one.inve.localfullnode2.dep.DepItemsManagerial;

/**
 * 
 * 
 * Copyright © CHXX Co.,Ltd. All rights reserved.
 * 
 * @Description: The localfullnode and localfullnode2 would be working together
 *               for a long while.The pointcut offers rich localfullnode data if
 *               you want.
 * @author: Francis.Deng
 * @date: Jun 8, 2019 3:58:44 AM
 * @version: V1.0
 */
public abstract class Localfullnode2Pointcut extends GeneralNode {
	private DepItemsManagerial depItemsManager = DepItemsManager.getInstance();

	public void setDepItemsManager(DepItemsManagerial depItemsManager) {
		this.depItemsManager = depItemsManager;
	}

	protected void unwareOfWhenSetting() {
		depItemsManager.attachDBId(null).set(nodeParameters.dbId);
		depItemsManager.attachMnemonic(null).set(nodeParameters.mnemonic);
		depItemsManager.attachPublicKey(null).set(publicKey);
	}

	@Override
	public void setShardId(int shardId) {
		depItemsManager.attachShardId(null).set(shardId);
		super.setShardId(shardId);
	}

	@Override
	public void setShardCount(int shardCount) {
		depItemsManager.attachShardCount(null).set(shardCount);
		super.setShardCount(shardCount);
	}

	@Override
	public void setnValue(int nValue) {
		depItemsManager.attachNValue(null).set(nValue);
		super.setnValue(nValue);

		// randomly choose the place
		unwareOfWhenSetting();
	}

	@Override
	public void setLocalFullNodes(List<LocalFullNode> localFullNodes) {
		depItemsManager.attachLocalFullNodes(null).set(localFullNodes);
		super.setLocalFullNodes(localFullNodes);
	}

	// create all deps and register them focused on items
	protected void newDepsAndRegisterThem() {

	}

//	@Override
//	public void setCreatorId(long creatorId) {
//		depItemsManager.atta
//		super.setCreatorId(creatorId);
//	}

}

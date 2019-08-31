package one.inve.localfullnode2.sync.rpc;

import one.inve.localfullnode2.nodes.LocalFullNode1GeneralNode;
import one.inve.localfullnode2.sync.rpc.DataSynchronizationZerocImpl.IDataSynchronization;

/**
 * 
 * Copyright © INVE FOUNDATION. All rights reserved.
 * 
 * @Description: assemble DataSynchronization
 * @author: Francis.Deng [francis_xiiiv@163.com]
 * @date: Aug 29, 2019 11:06:18 PM
 * @version: V1.0
 */
public class DataSynchronizationFactory {
	public static IDataSynchronization getDataSynchronizationImpl(LocalFullNode1GeneralNode node) {
		return new DataSynchronizationCore(node);
	}
}

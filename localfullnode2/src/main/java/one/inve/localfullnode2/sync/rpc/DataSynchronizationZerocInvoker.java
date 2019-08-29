package one.inve.localfullnode2.sync.rpc;

import java.util.concurrent.CompletableFuture;

import com.zeroc.Ice.Communicator;

import one.inve.localfullnode2.rpc.RpcConnectionService;
import one.inve.localfullnode2.sync.rpc.gen.DataSynchronizationPrx;
import one.inve.localfullnode2.sync.rpc.gen.DistributedEventObjects;

/**
 * 
 * Copyright © INVE FOUNDATION. All rights reserved.
 * 
 * @ClassName: DataSynchronizationZerocInvoker
 * @Description: wrap up the invocation to zeroc
 * @author Francis.Deng [francis_xiiiv@163.com]
 * @date Aug 27, 2019
 *
 */
public class DataSynchronizationZerocInvoker {

	public static CompletableFuture<DistributedEventObjects> invokeGetNotInDistributionEventsAsync(
			Communicator communicator, String ip, int port, String distJson) {
		DataSynchronizationPrx proxy = RpcConnectionService.buildDataSynchronizationProxy(communicator, ip, port);
		CompletableFuture<DistributedEventObjects> f = proxy.getNotInDistributionEventsAsync(distJson);

		return f;
	}
}

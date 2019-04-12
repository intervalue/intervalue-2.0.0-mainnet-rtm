package one.inve.node;

import one.inve.contract.Contract;
import one.inve.contract.provider.RepositoryProvider;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 
 * Copyright © CHXX Co.,Ltd. All rights reserved.
 * 
 * @Description: attempt to simulate the process of accepting the contract
 *               message and fetching contract from queue after reaching a
 *               consensus.See {@code LocalFullNode}
 * @author: Francis.Deng
 * @date: 2018年11月8日 下午2:24:50
 * @version: V1.0
 */
public class MockContractNode extends GeneralNode {
	private static final ReentrantLock LOCK = new ReentrantLock();
	private static final Condition STOP = LOCK.newCondition();
	// public static MVMHandler mvmHandler = new MVMHandler();

	public static void main(String[] args) {
		RepositoryProvider.getTrack("0_0");

		// 2. bootstrap http server
		GeneralNode node = new GeneralNode();
		node.nodeParameters = new NodeParameters();
		node.nodeParameters.dbId = "0_0";
		node.setShardId(0);
		node.setCreatorId(0);
		Contract.getInstance().getShell().boostrapHttpService(node);
		addHook();

		try {
			LOCK.lock();
			STOP.await();
		} catch (InterruptedException e) {
			System.out.println("service stopped, interrupted by other thread!/n" + e);
		} finally {
			LOCK.unlock();
		}
	}

	private static void addHook() {
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {

			try {
				Contract.getInstance(null).dispose();

				LOCK.lock();
				STOP.signal();
			} finally {
				LOCK.unlock();
			}
		}, "StartMain-shutdown-hook"));
	}

	// public interface MVMGetter {

	// 	MVMHandlerAuxiliary getMVM();
	// }
}

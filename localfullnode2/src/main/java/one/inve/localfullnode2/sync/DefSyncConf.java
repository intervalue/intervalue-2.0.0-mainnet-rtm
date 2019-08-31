package one.inve.localfullnode2.sync;

import com.zeroc.Ice.Communicator;

import one.inve.localfullnode2.dep.DepItemsManager;
import one.inve.localfullnode2.sync.ISyncContext.DefSyncContext;

/**
 * 
 * 
 * Copyright © INVE FOUNDATION. All rights reserved.
 * 
 * @Description: worked as xml,json configuration file
 * @author: Francis.Deng [francis_xiiiv@163.com]
 * @date: Aug 30, 2019 1:02:43 AM
 * @version: V1.0
 */
public class DefSyncConf implements ISyncConf {

	private final ISyncContext syncContext;

	private static class SingletonHelper {
		private static final DefSyncConf INSTANCE = new DefSyncConf();
	}

	public static DefSyncConf getInstance() {
		return SingletonHelper.INSTANCE;
	}

	private DefSyncConf() {
		syncContext = new DefSyncContext(this);
	}

	@Override
	public ISyncContext getDefaultContext() {
		return syncContext;
	}

	// depending on {@code LocalFullNodeSkeleton}'s initialization
	@Override
	public Communicator getCommunicator() {
		return DepItemsManager.getInstance().attachDirectCommunicator(null).get();
	}

	@Override
	public String getSynchronizationInitializerClassName() {
		return "one.inve.localfullnode2.sync.work.SyncInitializer";
	}

	@Override
	public String getSyncSourceProxyClassName() {
		return "one.inve.localfullnode2.sync.source.ProxiedSyncSource";
	}

	@Override
	public String[] getSynchronizationWorkClassNames() {
		return new String[] { "one.inve.localfullnode2.sync.work.EventSynchronizer",
				"one.inve.localfullnode2.sync.work.MessageSynchronizer",
				"one.inve.localfullnode2.sync.work.SystemMessageSynchronizer" };
	}

	@Override
	public String[] getLFNHostList() {
		return new String[] { "192.168.207.129:35792", "192.168.207.129:35795" };
	}

}

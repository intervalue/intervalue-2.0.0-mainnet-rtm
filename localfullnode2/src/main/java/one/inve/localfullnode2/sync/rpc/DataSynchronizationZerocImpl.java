package one.inve.localfullnode2.sync.rpc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zeroc.Ice.Current;

import one.inve.localfullnode2.nodes.LocalFullNode1GeneralNode;
import one.inve.localfullnode2.sync.rpc.gen.DataSynchronization;
import one.inve.localfullnode2.sync.rpc.gen.DistributedEventObjects;
import one.inve.localfullnode2.sync.rpc.gen.Localfullnode2InstanceProfile;

/**
 * 
 * Copyright © INVE FOUNDATION. All rights reserved.
 * 
 * @ClassName: DataSynchronizationZerocImpl
 * @Description: Extract expected data from rocksdb,It works as zeroc server.
 * @author Francis.Deng [francis_xiiiv@163.com]
 * @date Aug 26, 2019
 * @see DataSynchronizationCore
 *
 */
public class DataSynchronizationZerocImpl implements DataSynchronization {
	private static final Logger logger = LoggerFactory.getLogger(DataSynchronizationZerocImpl.class);

	private IDataSynchronization dataSynchronizationCore;// delegate to another core class

	// ensuring that {@code probeFirstSeqs} is executed before.
	public DataSynchronizationZerocImpl(LocalFullNode1GeneralNode node) {
		dataSynchronizationCore = DataSynchronizationFactory.getDataSynchronizationImpl(node);
	}

	@Override
	public DistributedEventObjects getNotInDistributionEvents(String distJson, Current current) {
		return dataSynchronizationCore.getNotInDistributionEvents(distJson);
	}

	// expose dataSynchronization core specification
	public static interface IDataSynchronization {
		DistributedEventObjects getNotInDistributionEvents(String distJson);

		Localfullnode2InstanceProfile getLocalfullnode2InstanceProfile();
	}

	@Override
	public Localfullnode2InstanceProfile getLocalfullnode2InstanceProfile(Current current) {
		return dataSynchronizationCore.getLocalfullnode2InstanceProfile();
	}

}

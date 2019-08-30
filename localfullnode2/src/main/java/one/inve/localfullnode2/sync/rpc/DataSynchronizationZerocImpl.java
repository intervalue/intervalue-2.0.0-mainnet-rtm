package one.inve.localfullnode2.sync.rpc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zeroc.Ice.Current;

import one.inve.localfullnode2.nodes.LocalFullNode1GeneralNode;
import one.inve.localfullnode2.sync.rpc.gen.DataSynchronization;
import one.inve.localfullnode2.sync.rpc.gen.DistributedEventObjects;

/**
 * 
 * Copyright © INVE FOUNDATION. All rights reserved.
 * 
 * @ClassName: DataSynchronizationZerocImpl
 * @Description: extract expected data from rocksdb
 * @author Francis.Deng [francis_xiiiv@163.com]
 * @date Aug 26, 2019
 * @see DataSynchronizationImpl
 *
 */
public class DataSynchronizationZerocImpl implements DataSynchronization {
	private static final Logger logger = LoggerFactory.getLogger(DataSynchronizationZerocImpl.class);

	private DataSynchronizationImpl dataSynchronization;

	// ensuring that {@code probeFirstSeqs} is executed before.
	public DataSynchronizationZerocImpl(LocalFullNode1GeneralNode node) {
		dataSynchronization = new DataSynchronizationImpl(node);
	}

	@Override
	public DistributedEventObjects getNotInDistributionEvents(String distJson, Current current) {
		return dataSynchronization.getNotInDistributionEvents(distJson);
	}

	public static interface IDataSynchronization {
		DistributedEventObjects getNotInDistributionEvents(String distJson);
	}

}

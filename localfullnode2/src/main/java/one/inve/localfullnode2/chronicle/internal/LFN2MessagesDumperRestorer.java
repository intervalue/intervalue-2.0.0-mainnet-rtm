package one.inve.localfullnode2.chronicle.internal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import one.inve.localfullnode2.dep.DepItemsManager;
import one.inve.localfullnode2.sync.msg.MsgIntrospectorDependency;
import one.inve.localfullnode2.sync.msg.MsgIntrospectorDependent;

/**
 * 
 * Copyright © INVE FOUNDATION. All rights reserved.
 * 
 * @ClassName: LFN2MessagesDumperRestorer
 * @Description: TODO
 * @author Francis.Deng [francis_xiiiv@163.com]
 * @date Jan 8, 2020
 *
 */
public class LFN2MessagesDumperRestorer extends LFN2MessagesDumper implements IChronicleDumperRestorer {
	private static final Logger logger = LoggerFactory.getLogger(LFN2MessagesDumperRestorer.class);

	@Override
	public void persist(byte[][] messagesByes) {
		MsgIntrospectorDependent dep = DepItemsManager.getInstance().getItemConcerned(MsgIntrospectorDependency.class);

	}

	@Override
	public void persistSys(byte[][] messagesByes) {
		// TODO Auto-generated method stub

	}

}

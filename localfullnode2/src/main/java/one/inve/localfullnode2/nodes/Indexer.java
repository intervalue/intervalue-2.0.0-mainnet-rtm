package one.inve.localfullnode2.nodes;

import one.inve.localfullnode2.dep.DepItemsManager;
import one.inve.localfullnode2.sync.msg.MsgIntrospector;
import one.inve.localfullnode2.sync.msg.MsgIntrospectorDependency;
import one.inve.localfullnode2.sync.msg.MsgIntrospectorDependent;

/**
 * 
 * 
 * Copyright © INVE FOUNDATION. All rights reserved.
 * 
 * @Description: build indexes for old,rusty messages and system messages in
 *               mysql
 * @author: Francis.Deng [francis_xiiiv@163.com]
 * @date: Sep 4, 2019 1:39:18 AM
 * @version: V1.0
 */
public abstract class Indexer extends Configurator {

	@Override
	protected void buildMessagesAndSysMessagesIndexOnce() {
		MsgIntrospectorDependent dep = DepItemsManager.getInstance().getItemConcerned(MsgIntrospectorDependency.class);

		MsgIntrospector introspector = new MsgIntrospector(dep);
		if (!introspector.isMsgIndexExisted()) {
			introspector.buildMsgIndex();
		}

		if (!introspector.isSysMsgIndexExisted()) {
			introspector.buildSysMsgIndex();
		}

	}

}

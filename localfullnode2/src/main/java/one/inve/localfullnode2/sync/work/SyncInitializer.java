package one.inve.localfullnode2.sync.work;

import one.inve.localfullnode2.conf.Config;
import one.inve.localfullnode2.store.mysql.MysqlHelper;
import one.inve.localfullnode2.store.mysql.NewTableCreate;
import one.inve.localfullnode2.sync.IContext;
import one.inve.localfullnode2.sync.SynchronizationWork;
import one.inve.localfullnode2.sync.SynchronizationWork.Whole;
import one.inve.localfullnode2.sync.source.ISyncSourceProfile;

/**
 * 
 * Copyright © INVE FOUNDATION. All rights reserved.
 * 
 * @ClassName: SyncInitializer
 * @Description: initialize mysql tables for messages.
 * @author Francis.Deng [francis_xiiiv@163.com]
 * @date Aug 24, 2019
 *
 */
public class SyncInitializer implements Whole {

	@Override
	public boolean run(IContext context) {
		ISyncSourceProfile srcProfile = context.getSyncSource().getSyncSourceProfile();
		MysqlHelper mysqlHelper = new MysqlHelper(srcProfile.getDBId(), 1 == 1);

		NewTableCreate.createMessagesTable(mysqlHelper, Config.MESSAGES + "_0");// messages table
		NewTableCreate.createTransactionsMsgTable(mysqlHelper, Config.SYSTEMAUTOTX + Config.SPLIT + "0");// system
																											// messages
																											// table

		return true;
	}

}

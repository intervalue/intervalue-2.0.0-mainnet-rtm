package one.inve.localfullnode2.sync.msg;

import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import one.inve.localfullnode2.store.mysql.MysqlHelper;
import one.inve.localfullnode2.store.mysql.QueryTableSplit;

/**
 * 
 * 
 * Copyright © INVE FOUNDATION. All rights reserved.
 * 
 * @Description: build indexes for rusty old messages and system messages.
 * @author: Francis.Deng [francis_xiiiv@163.com]
 * @date: Sep 4, 2019 12:14:27 AM
 * @version: V1.0
 */
public class RottenMessagesAndSystemMessages {
	private static final Logger logger = LoggerFactory.getLogger(QueryTableSplit.class);

	private String dbId;

	public RottenMessagesAndSystemMessages(String dbId) {
		super();
		this.dbId = dbId;
	}

	public static interface IndexMessagesStore {
		void messageHashIndex(String hash);
	}

	public static interface IndexSystemMessagesStore {
		void sysMessageTypeIdIndex(String typeId);
	}

	public void buildMessageHashIndex(IndexMessagesStore store) {
		MysqlHelper h = new MysqlHelper(dbId, false);

		StringBuilder sql = new StringBuilder("select hash from messages_0");

		try {
			h.executeQuery(sql.toString(), (rs, index) -> {
				String hash = rs.getString("hash");
				store.messageHashIndex(hash);

				return null;
			});
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void buildSysMessageTypeIdIndex(IndexSystemMessagesStore store) {
		MysqlHelper h = new MysqlHelper(dbId, false);

		StringBuilder sql = new StringBuilder("select id,type from system_auto_tx_0");

		try {
			h.executeQuery(sql.toString(), (rs, index) -> {
				String type = rs.getString("type");
				long id = rs.getBigDecimal("id").longValue();
				store.sysMessageTypeIdIndex(type + id);

				return null;
			});
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}

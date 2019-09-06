package one.inve.localfullnode2.sync.msg;

import one.inve.localfullnode2.store.rocks.key.MessageIndexes;

/**
 * 
 * 
 * Copyright © INVE FOUNDATION. All rights reserved.
 * 
 * @Description: Msg introspection is capable of building indexes for system
 *               message and message in order to abandon the usage of "indexes"
 *               in mysql.
 * @author: Francis.Deng [francis_xiiiv@163.com]
 * @date: Sep 3, 2019 11:31:56 PM
 * @version: V1.0
 */
public class MsgIntrospector {

	private final MsgIntrospectorDependent dep;
	private final RottenMessagesAndSystemMessages rottenMessages;

	public MsgIntrospector(MsgIntrospectorDependent dep) {
		this.dep = dep;
		rottenMessages = new RottenMessagesAndSystemMessages(dep.getDbId());
	}

	public boolean isMsgIndexExisted() {
		return dep.getNosql().isPrefixKeyExisted(MessageIndexes.getMessageHashPrefix().getBytes());
	}

	public boolean isSysMsgIndexExisted() {
		return dep.getNosql().isPrefixKeyExisted(MessageIndexes.getSysMessageTypeIdPrefix().getBytes());
	}

	public void buildMsgIndex() {
		// rocksJavaUtil.put(MessageIndexes.getMessageHashKey(msg.getHash()).getBytes(),
		// new byte[0]);
		rottenMessages
				.buildMessageHashIndex(hash -> dep.getNosql().put(MessageIndexes.getMessageHashKey(hash), new byte[0]));
	}

	public void buildSysMsgIndex() {
		// rocksJavaUtil.put(MessageIndexes.getSysMessageTypeIdKey(type + id), new
		// byte[0]);
		rottenMessages.buildSysMessageTypeIdIndex(
				typeId -> dep.getNosql().put(MessageIndexes.getSysMessageTypeIdKey(typeId), new byte[0]));
	}
}

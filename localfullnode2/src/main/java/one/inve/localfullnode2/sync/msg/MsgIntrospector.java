package one.inve.localfullnode2.sync.msg;

import java.util.Iterator;
import java.util.Map;

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
 * @version: V1.1 get all messages hash and system message type id via
 *           {@code MsgIntrospector}
 * @version: V1.2 get message body by key
 */
public class MsgIntrospector {

	private final MsgIntrospectorDependent dep;
	private final RottenMessagesAndSystemMessages rottenMessages;

	public MsgIntrospector(MsgIntrospectorDependent dep) {
		this.dep = dep;
		rottenMessages = new RottenMessagesAndSystemMessages(dep.getDbId());
	}

	public boolean isMsgIndexExisted() {
		// return
		// dep.getNosql().isPrefixKeyExisted(MessageIndexes.getMessageHashPrefix().getBytes());
		return dep.getNosql().isPrefixKeyMoreThan(MessageIndexes.getMessageHashPrefix().getBytes(), 11);
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

	/**
	 * Get all message hashes [message hash]
	 */
	public String[] getMsgHashes(IHashConverter hashConverter) {
		Map<byte[], byte[]> allMessageHashesBytes = dep.getNosql()
				.startWith(MessageIndexes.getMessageHashPrefix().getBytes());
		String[] finalVal = new String[allMessageHashesBytes.size()];
		int index = 0;

		Iterator<byte[]> keys = allMessageHashesBytes.keySet().iterator();
		while (keys.hasNext()) {
			if (hashConverter != null) {
				String rawMessageHash = MessageIndexes.getMessageHash(new String(keys.next()));
				finalVal[index] = hashConverter.convert(rawMessageHash);
			} else {
				finalVal[index] = MessageIndexes.getMessageHash(new String(keys.next()));
			}

			index++;
		}

		return finalVal;
	}

	/**
	 * Get all system message hashes [type id]
	 */
	public String[] getSysMsgHashes(IHashConverter hashConverter) {
		Map<byte[], byte[]> allSysMessageHashesBytes = dep.getNosql()
				.startWith(MessageIndexes.getSysMessageTypeIdPrefix().getBytes());
		String[] finalVal = new String[allSysMessageHashesBytes.size()];
		int index = 0;

		Iterator<byte[]> keys = allSysMessageHashesBytes.keySet().iterator();
		while (keys.hasNext()) {
			if (hashConverter != null) {
				finalVal[index] = hashConverter.convert(MessageIndexes.getSysMessageTypeIdKey(new String(keys.next())));
			} else {
				finalVal[index] = MessageIndexes.getSysMessageTypeIdKey(new String(keys.next()));
			}

			index++;
		}

		return finalVal;
	}

	/**
	 * Get message body whether message or system message
	 */
	public byte[] getMessageBytes(String key) {
		return dep.getNosql().get(key);
	}

	@FunctionalInterface
	public static interface IHashConverter {
		String convert(String hash);
	}
}

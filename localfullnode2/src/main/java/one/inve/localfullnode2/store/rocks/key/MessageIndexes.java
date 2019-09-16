package one.inve.localfullnode2.store.rocks.key;

/**
 * 
 * 
 * Copyright © INVE FOUNDATION. All rights reserved.
 * 
 * @Description: encapsulate how to build and resolve the keys of message
 * @author: Francis.Deng [francis_xiiiv@163.com]
 * @date: Sep 3, 2019 8:43:14 PM
 * @version: V1.0
 * 
 * @see {@code NewTableCreate addTransactionToRocksDB}
 * @see {@code MessagePersistence saveSystemAutoTx}
 */
public class MessageIndexes {
	public static String getMessageHashKey(String messageHash) {
		String key = String.format(getMessageHashPrefix() + "%s", messageHash);

		return key;
	}

	public static String getMessageHashPrefix() {
		return "msgs$h$";
	}

	public static String getMessageHash(String messageHashKey) {
		String messageHash = null;

		if (messageHashKey != null) {
			String parts[] = messageHashKey.split("\\$");

			if (parts != null && parts.length == 3) {
				messageHash = parts[2];
			}
		}

		return messageHash;
	}

	// <code>rocksJavaUtil.put(type + id, JSONArray.toJSONString(sysAutoTx));</code>
	// {@code typeId} worked as sysMessage key
	public static String getSysMessageTypeIdKey(String typeId) {
		String key = String.format(getSysMessageTypeIdPrefix() + "%s", typeId);

		return key;
	}

	public static String getSysMessageTypeIdPrefix() {
		return "smsgs$t$";
	}

	public static String getSysMessageTypeId(String typeIdKey) {
		String typeId = null;

		if (typeIdKey != null) {
			String parts[] = typeIdKey.split("\\$");

			if (parts != null && parts.length == 3) {
				typeId = parts[2];
			}
		}

		return typeId;
	}
}

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
}

package one.inve.localfullnode2.store.rocks.key;

import one.inve.core.EventBody;
import one.inve.localfullnode2.store.EventKeyPair;

/**
 * 
 * Copyright © INVE FOUNDATION. All rights reserved.
 * 
 * @ClassName: EventIndexes
 * @Description: event index in rocksdb
 * @author Francis.Deng [francis_xiiiv@163.com]
 * @date Sep 8, 2019
 *
 */
public class EventIndexes {
	// rocksdb event sort key - "events$s${consTimestamp}${shardId}"
	public static String getConcensusEventSortKey(EventBody eb) {
		EventKeyPair pair = new EventKeyPair(eb.getShardId(), eb.getCreatorId(), eb.getCreatorSeq());
		String key = String.format(getConcensusEventSortPrefix() + "%d$%d$%s", eb.getConsTimestamp().getEpochSecond(),
				eb.getShardId(), pair.toString());

		return key;
	}

	public static String getConcensusEventSortPrefix() {
		return "events$s$";
	}

	public static String getConcensusEventPair(String messageHashKey) {
		String pair = null;

		if (messageHashKey != null) {
			String parts[] = messageHashKey.split("\\$");

			if (parts != null && parts.length == 5) {
				pair = parts[4];
			}
		}

		return pair;
	}

	// shardId_creatorId_seq
	public static String getEventPair(EventBody eb) {
		EventKeyPair pair = new EventKeyPair(eb.getShardId(), eb.getCreatorId(), eb.getCreatorSeq());

		return pair.toString();
	}

}

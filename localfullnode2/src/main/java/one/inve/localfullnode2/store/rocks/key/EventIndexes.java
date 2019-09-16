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
	// rocksdb event sort key - "events$s${consTimestamp}${shardId}${pair}"
	public static String getConcensusEventSortKey(EventBody eb) {
		EventKeyPair pair = new EventKeyPair(eb.getShardId(), eb.getCreatorId(), eb.getCreatorSeq());
		String key = String.format(getConcensusEventSortPrefix() + "%d$%d$%s", eb.getConsTimestamp().getEpochSecond(),
				eb.getShardId(), pair.toString());

		return key;
	}

	public static String getConcensusEventSortPrefix() {
		return "events$s$";
	}

	public static String getConcensusEventPair(String cesk) {
		String pair = null;

		if (cesk != null) {
			String parts[] = cesk.split("\\$");

			if (parts != null && parts.length == 5) {
				pair = parts[4];
			}
		}

		return pair;
	}

	private static long getConsensusTime(String key) {
		long consensusTime = 0l;

		if (key != null) {
			String parts[] = key.split("\\$");

			if (parts != null && parts.length == 5) {
				String consensusTimeStr = parts[2];
				consensusTime = Long.parseLong(consensusTimeStr);
			}
		}
		return consensusTime;
	}

	private static long getShardId(String key) {
		long shardId = 0l;

		if (key != null) {
			String parts[] = key.split("\\$");

			if (parts != null && parts.length == 5) {
				String consensusTimeStr = parts[3];
				shardId = Long.parseLong(consensusTimeStr);
			}
		}
		return shardId;
	}

	public static int compareConcensusEventSortKey(String concensusEventSortKey1, String concensusEventSortKey2) {
		long consensusTime1 = getConsensusTime(concensusEventSortKey1);
		long shardId1 = getShardId(concensusEventSortKey1);
		long consensusTime2 = getConsensusTime(concensusEventSortKey2);
		long shardId2 = getShardId(concensusEventSortKey2);

		return (int) (consensusTime1 != consensusTime2 ? (consensusTime1 - consensusTime2) : (shardId1 - shardId2));
	}

	// shardId_creatorId_seq
	public static String getEventPair(EventBody eb) {
		EventKeyPair pair = new EventKeyPair(eb.getShardId(), eb.getCreatorId(), eb.getCreatorSeq());

		return pair.toString();
	}

}

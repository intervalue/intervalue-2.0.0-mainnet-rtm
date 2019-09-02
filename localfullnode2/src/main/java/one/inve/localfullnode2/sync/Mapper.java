package one.inve.localfullnode2.sync;

import java.time.Instant;
import java.util.Arrays;

import one.inve.core.EventBody;
import one.inve.localfullnode2.sync.rpc.gen.SyncEvent;
import one.inve.localfullnode2.utilities.GenericArray;
import one.inve.localfullnode2.utilities.merkle.INodeContent;

/**
 * 
 * Copyright © INVE FOUNDATION. All rights reserved.
 * 
 * @ClassName: Mapper
 * @Description: a helper class to deal with object properties transformation
 * @author Francis.Deng [francis_xiiiv@163.com]
 * @date Aug 27, 2019
 *
 */
public class Mapper {
	// destination-->source when inverse is true
	public static void copyProperties(EventBody source, SyncEvent destination, boolean inverse) {
		if (!inverse) {
			destination.shardId = source.getShardId();
			destination.selfId = source.getCreatorId();
			destination.selfSeq = source.getCreatorSeq();
			destination.otherId = source.getOtherId();
			destination.otherSeq = source.getOtherSeq();
			destination.messages = source.getTrans();
			destination.timeCreatedNano = source.getTimeCreated().getNano();
			destination.timeCreatedSecond = source.getTimeCreated().getEpochSecond();
			destination.sign = source.getSignature();
			destination.isFamous = source.isFamous();
			destination.hash = source.getHash();
			destination.generation = source.getGeneration();
			destination.consensusTimestampSecond = source.getConsTimestamp().getEpochSecond();
			destination.consensusTimestampNano = source.getConsTimestamp().getNano();
			destination.otherHash = source.getOtherHash();
			destination.parentHash = source.getParentHash();
		} else {
			source.setShardId(destination.shardId);
			source.setCreatorId(destination.selfId);
			source.setCreatorSeq(destination.selfSeq);
			source.setOtherId(destination.otherId);
			source.setOtherSeq(destination.otherSeq);
			source.setTrans(destination.messages);
			source.setTimeCreated(Instant.ofEpochSecond(destination.timeCreatedNano, destination.timeCreatedSecond));
			source.setSignature(destination.sign);
			source.setFamous(destination.isFamous);
			source.setHash(destination.hash);
			source.setGeneration(destination.generation);
			source.setConsTimestamp(
					Instant.ofEpochSecond(destination.timeCreatedSecond, destination.consensusTimestampNano));
			source.setOtherHash(destination.otherHash);
			source.setParentHash(destination.parentHash);

		}
	}

	public static INodeContent transformFrom(EventBody eb) {
		return new INodeContent() {

			@Override
			public byte[] hash() {
				return eb.getHash();
			}

			@Override
			public boolean equals(INodeContent content) {
				return Arrays.equals(eb.getHash(), content.hash());
			}

		};
	}

	public static INodeContent transformFrom(SyncEvent se) {
		EventBody target = new EventBody();
		copyProperties(target, se, true);
		return transformFrom(target);
	}

	public static INodeContent[] transformFromArray(GenericArray<EventBody> eventBodyArray) {
		GenericArray<INodeContent> nodeContentArray = new GenericArray<>();

		for (EventBody eb : eventBodyArray) {
			nodeContentArray.append(Mapper.transformFrom(eb));
		}

		return nodeContentArray.toArray(new INodeContent[nodeContentArray.length()]);
	}
}

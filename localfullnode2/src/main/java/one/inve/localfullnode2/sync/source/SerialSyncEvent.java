package one.inve.localfullnode2.sync.source;

import java.io.Serializable;

import one.inve.core.EventBody;
import one.inve.localfullnode2.sync.ObjectVerifiable;
import one.inve.localfullnode2.utilities.Hash;
import one.inve.localfullnode2.utilities.merkle.INodeContent;
import one.inve.localfullnode2.utilities.merkle.MerklePath;

public class SerialSyncEvent implements INodeContent, ObjectVerifiable<EventBody>, Serializable {

	private static final long serialVersionUID = 2922777530658846374L;

	private EventBody event;
	private MerklePath mp;

	public SerialSyncEvent(EventBody event, MerklePath mp) {
		this.event = event;
		this.mp = mp;
	}

	@Override
	public EventBody getObject() {
		return event;
	}

	@Override
	public boolean verify(byte[] rootHash) {
		return mp.validate(this, rootHash);
	}

	@Override
	public byte[] hash() {
		return Hash.hash(event.getShardId(), event.getCreatorId(), event.getCreatorSeq(), event.getParentHash(),
				event.getOtherHash(), event.getConsTimestamp(), event.getTrans());
	}

	@Override
	public boolean equals(INodeContent nc) {
		return hash() == nc.hash();
	}

}

package one.inve.localfullnode2.sync.source;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.zeroc.Ice.Communicator;

import one.inve.core.EventBody;
import one.inve.localfullnode2.sync.Addr;
import one.inve.localfullnode2.sync.DistributedObjects;
import one.inve.localfullnode2.sync.Mapper;
import one.inve.localfullnode2.sync.measure.Distribution;
import one.inve.localfullnode2.sync.rpc.DataSynchronizationZerocInvoker;
import one.inve.localfullnode2.sync.rpc.gen.DistributedEventObjects;
import one.inve.localfullnode2.sync.rpc.gen.MerkleTreeizedSyncEvent;
import one.inve.localfullnode2.sync.rpc.gen.SyncEvent;
import one.inve.localfullnode2.utilities.GenericArray;
import one.inve.localfullnode2.utilities.merkle.MerklePath;

/**
 * 
 * Copyright © INVE FOUNDATION. All rights reserved.
 * 
 * @ClassName: ProxiedSyncSource
 * @Description: TODO
 * @author Francis.Deng [francis_xiiiv@163.com]
 * @date Aug 27, 2019
 *
 */
public class ProxiedSyncSource implements ISyncSource {
	private Communicator communicator;
	private Addr addresses[];

	@Override
	public ISyncSourceProfile getSyncSourceProfile() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DistributedObjects<EventBody> getNotInDistributionEvents(Distribution dist) {
		Addr preferred = addresses[0];
		Addr candidate = null;
		if (addresses.length > 1) {
			candidate = addresses[1];
		}

		CompletableFuture<DistributedEventObjects> f = DataSynchronizationZerocInvoker
				.invokeGetNotInDistributionEventsAsync(communicator, preferred.getIp(), preferred.getPort(),
						JSON.toJSONString(dist));
		DistributedEventObjects deo = null;
		try {
			deo = f.get();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		MerkleTreeizedSyncEvent[] events = deo.events;
		byte[] rootHash = deo.rootHash;
		String distJson = deo.distJson;
		GenericArray<EventBody> eventBodies = new GenericArray<>();

		boolean valid = Arrays.stream(events).parallel().anyMatch((t) -> {
			String merklePathJson = t.merklePathJson;
			SyncEvent syncEvent = t.syncEvent;

			MerklePath mp = JSON.parseObject(merklePathJson, MerklePath.class);
			return mp.validate(Mapper.transformFrom(syncEvent), rootHash);
		});

		if (!valid)
			throw new RuntimeException("failed in merkle tree validation");

		Arrays.stream(events).parallel().forEach(t -> {
			EventBody eb = new EventBody();
			Mapper.copyProperties(eb, t.syncEvent, true);

			eventBodies.append(eb);
		});

		return new DistributedObjects<EventBody>(JSON.parseObject(distJson, Distribution.class),
				eventBodies.toArray(new EventBody[eventBodies.length()]));
	}

	@Override
	public DistributedObjects<JSONObject> getNotInDistributionMessages(Distribution dist) {
		// TODO Auto-generated method stub
		return null;
	}

	public void setCommunicator(Communicator communicator) {
		this.communicator = communicator;
	}

	public void setAddresses(String[] addresses) {
		GenericArray<Addr> addrs = new GenericArray<>();
		Arrays.stream(addresses).forEach(t -> {
			addrs.append(new Addr(t));
		});

		this.addresses = addrs.toArray(new Addr[addrs.length()]);
	}

}

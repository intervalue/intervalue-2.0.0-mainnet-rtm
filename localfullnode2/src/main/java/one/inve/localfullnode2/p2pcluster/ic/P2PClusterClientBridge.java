package one.inve.localfullnode2.p2pcluster.ic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.grpc.Channel;
import io.grpc.netty.NegotiationType;
import io.grpc.netty.NettyChannelBuilder;
import one.inve.cluster.Member;
import one.inve.localfullnode2.p2pcluster.ic.P2PClusterClient.MetaData;
import one.inve.localfullnode2.p2pcluster.ic.P2PClusterClient.MetaData.Builder;
import one.inve.localfullnode2.p2pcluster.ic.P2PClusterClient.RequestFindMembers;
import one.inve.localfullnode2.p2pcluster.ic.P2PClusterClient.RequestUpdateMeta;
import one.inve.localfullnode2.p2pcluster.ic.P2PClusterClient.ResponseFindMembers;
import one.inve.transport.Address;

/**
 * 
 * 
 * Copyright © INVE FOUNDATION. All rights reserved.
 * 
 * @Description: provide ability to set node's meta(which would be broadcasted
 *               to the whole cluster) and get others's alive/suspected nodes on
 *               top of p2pcluster framework.
 * @author: Francis.Deng
 * @date: Aug 4, 2019 8:55:31 PM
 * @version: V1.0
 */
public class P2PClusterClientBridge {

	private String host = "127.0.0.1";
	private int port = -1;

	private List<Peer> aliveMembers;
	private List<Peer> suspectedMembers;

	public P2PClusterClientBridge(int port) {
		super();
		this.port = port;
	}

	public void setMeta(Map<String, String> metaMap) {
		Channel channel = NettyChannelBuilder.forAddress(host, port).negotiationType(NegotiationType.PLAINTEXT).build();
		RequestUpdateMeta requestUpdateMeta = P2PClusterClient.RequestUpdateMeta.newBuilder()
				.addAllMeta(localMetaToStream(metaMap)).build();

		ClusterGrpc.newBlockingStub(channel).updateMeta(requestUpdateMeta);
	}

	// "findMembersByShardId" and "otherMembers" be compatible with returned @{code
	// Member},which is considered to be alive
	public Collection<Member> findMembersByShardId(String shid) {
		Collection<Peer> alivePeersWithId, suspectedPeersWithId, mergedPeersWithId;
		mergedPeersWithId = Collections.EMPTY_LIST;
		Collection<Member> visibleMembers = Collections.EMPTY_LIST;

		if (aliveMembers != null) {
			alivePeersWithId = aliveMembers.stream()
					.filter(m -> m.getMeta().get("shard") != null && m.getMeta().get("shard").equals(shid))
					.collect(Collectors.toList());

			if (!alivePeersWithId.isEmpty()) {
				mergedPeersWithId = alivePeersWithId;
			}
		}

//		if (suspectedMembers != null) {
//			suspectedMembersWithId = suspectedMembers.stream().filter(m -> m.getMeta().get("shard")!=null && m.getMeta().get("shard").equals(id)).collect(Collectors.toList());
//		}

		visibleMembers = mergedPeersWithId.stream().map((x) -> {
			return new Member(x.getName(), Address.from(x.getAddr()), x.getMeta());
		}).collect(Collectors.toList());

		return visibleMembers;

	}

	public Collection<Member> otherMembers() {
		Collection<Peer> alivePeersWithId, suspectedPeersWithId, mergedPeersWithId;
		mergedPeersWithId = Collections.EMPTY_LIST;
		Collection<Member> visibleMembers = Collections.EMPTY_LIST;

		if (aliveMembers != null) {
			mergedPeersWithId = aliveMembers;
		}

		visibleMembers = mergedPeersWithId.stream().map((x) -> {
			return new Member(x.getName(), Address.from(x.getAddr()), x.getMeta());
		}).collect(Collectors.toList());

		return visibleMembers;
	}

	public P2PClusterClientBridge getMembers() {
		Channel channel = NettyChannelBuilder.forAddress(host, port).negotiationType(NegotiationType.PLAINTEXT).build();
		RequestFindMembers requestFindMembers = P2PClusterClient.RequestFindMembers.newBuilder().build();

		ResponseFindMembers responseFindAliveMembers = ClusterGrpc.newBlockingStub(channel)
				.findAliveMembers(requestFindMembers);
		ResponseFindMembers ResponseFindSuspectedMembers = ClusterGrpc.newBlockingStub(channel)
				.findSuspectedMembers(requestFindMembers);

		aliveMembers = responseFindAliveMembers.getFindMemberList().stream().map((x) -> {
			Peer p = new Peer();

			p.setName(x.getName());
			p.setAddr(x.getAddr());
			p.setMeta(streamToLocalMeta(x.getMetaList()));

			return p;
		}).collect(Collectors.toList());

		suspectedMembers = ResponseFindSuspectedMembers.getFindMemberList().stream().map((x) -> {
			Peer p = new Peer();

			p.setName(x.getName());
			p.setAddr(x.getAddr());
			p.setMeta(streamToLocalMeta(x.getMetaList()));

			return p;
		}).collect(Collectors.toList());

		return this;
	}

	// localMetaToStream - turn workable meta into streamed meta
	// streamToLocalMeta - vice versa

	private Iterable localMetaToStream(Map<String, String> localMeta) {
		List<MetaData> metaList = new ArrayList<>();
		for (Map.Entry<String, String> entry : localMeta.entrySet()) {
			Builder b = MetaData.newBuilder();
			b.setKey(entry.getKey());
			b.setValue(entry.getValue());

			metaList.add(b.buildPartial());
		}

		return metaList;
	}

	private Map<String, String> streamToLocalMeta(List<MetaData> stream) {
		Map<String, String> localMeta = new HashMap<>();
		for (MetaData md : stream) {
			localMeta.put(md.getKey(), md.getValue());
		}

		return localMeta;
	}

	public List<Peer> alive() {
		return aliveMembers;
	}

	public List<Peer> suspected() {
		return suspectedMembers;
	}

	// external-friendly class
	public static class Peer {
		private String name;
		private String addr;
		private Map<String, String> meta;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getAddr() {
			return addr;
		}

		public void setAddr(String addr) {
			this.addr = addr;
		}

		public Map<String, String> getMeta() {
			return meta;
		}

		public void setMeta(Map<String, String> meta) {
			this.meta = meta;
		}

	}
}

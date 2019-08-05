package one.inve.localfullnode2.p2pcluster.ic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.grpc.Channel;
import io.grpc.netty.NegotiationType;
import io.grpc.netty.NettyChannelBuilder;
import one.inve.localfullnode2.p2pcluster.ic.P2PClusterClient.MetaData;
import one.inve.localfullnode2.p2pcluster.ic.P2PClusterClient.MetaData.Builder;
import one.inve.localfullnode2.p2pcluster.ic.P2PClusterClient.RequestFindMembers;
import one.inve.localfullnode2.p2pcluster.ic.P2PClusterClient.RequestUpdateMeta;
import one.inve.localfullnode2.p2pcluster.ic.P2PClusterClient.ResponseFindMembers;

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

	private List<Member> aliveMembers;
	private List<Member> suspectedMembers;

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

	public P2PClusterClientBridge getMembers() {
		Channel channel = NettyChannelBuilder.forAddress(host, port).negotiationType(NegotiationType.PLAINTEXT).build();
		RequestFindMembers requestFindMembers = P2PClusterClient.RequestFindMembers.newBuilder().build();

		ResponseFindMembers responseFindAliveMembers = ClusterGrpc.newBlockingStub(channel)
				.findAliveMembers(requestFindMembers);
		ResponseFindMembers ResponseFindSuspectedMembers = ClusterGrpc.newBlockingStub(channel)
				.findSuspectedMembers(requestFindMembers);

		aliveMembers = responseFindAliveMembers.getFindMemberList().stream().map((x) -> {
			Member m = new Member();

			m.setName(x.getName());
			m.setAddr(x.getAddr());
			m.setMeta(streamToLocalMeta(x.getMetaList()));

			return m;
		}).collect(Collectors.toList());

		suspectedMembers = ResponseFindSuspectedMembers.getFindMemberList().stream().map((x) -> {
			Member m = new Member();

			m.setName(x.getName());
			m.setAddr(x.getAddr());
			m.setMeta(streamToLocalMeta(x.getMetaList()));

			return m;
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

	public List<Member> alive() {
		return aliveMembers;
	}

	public List<Member> suspected() {
		return suspectedMembers;
	}

	// external-friendly class
	public static class Member {
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

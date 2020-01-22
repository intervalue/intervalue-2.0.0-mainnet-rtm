package one.inve.localfullnode2.p2pcluster.ic;

import static io.grpc.MethodDescriptor.generateFullMethodName;
import static io.grpc.stub.ClientCalls.asyncUnaryCall;
import static io.grpc.stub.ClientCalls.blockingUnaryCall;
import static io.grpc.stub.ClientCalls.futureUnaryCall;
import static io.grpc.stub.ServerCalls.asyncUnaryCall;
import static io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall;

/**
 */
@javax.annotation.Generated(value = "by gRPC proto compiler (version 1.26.0)", comments = "Source: ic.proto")
public final class ClusterGrpc {

	private ClusterGrpc() {
	}

	public static final String SERVICE_NAME = "ic.Cluster";

	// Static method descriptors that strictly reflect the proto.
	private static volatile io.grpc.MethodDescriptor<one.inve.localfullnode2.p2pcluster.ic.P2PClusterClient.RequestUpdateMeta, one.inve.localfullnode2.p2pcluster.ic.P2PClusterClient.ResponseUpdateMeta> getUpdateMetaMethod;

	@io.grpc.stub.annotations.RpcMethod(fullMethodName = SERVICE_NAME + '/'
			+ "UpdateMeta", requestType = one.inve.localfullnode2.p2pcluster.ic.P2PClusterClient.RequestUpdateMeta.class, responseType = one.inve.localfullnode2.p2pcluster.ic.P2PClusterClient.ResponseUpdateMeta.class, methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
	public static io.grpc.MethodDescriptor<one.inve.localfullnode2.p2pcluster.ic.P2PClusterClient.RequestUpdateMeta, one.inve.localfullnode2.p2pcluster.ic.P2PClusterClient.ResponseUpdateMeta> getUpdateMetaMethod() {
		io.grpc.MethodDescriptor<one.inve.localfullnode2.p2pcluster.ic.P2PClusterClient.RequestUpdateMeta, one.inve.localfullnode2.p2pcluster.ic.P2PClusterClient.ResponseUpdateMeta> getUpdateMetaMethod;
		if ((getUpdateMetaMethod = ClusterGrpc.getUpdateMetaMethod) == null) {
			synchronized (ClusterGrpc.class) {
				if ((getUpdateMetaMethod = ClusterGrpc.getUpdateMetaMethod) == null) {
					ClusterGrpc.getUpdateMetaMethod = getUpdateMetaMethod = io.grpc.MethodDescriptor.<one.inve.localfullnode2.p2pcluster.ic.P2PClusterClient.RequestUpdateMeta, one.inve.localfullnode2.p2pcluster.ic.P2PClusterClient.ResponseUpdateMeta>newBuilder()
							.setType(io.grpc.MethodDescriptor.MethodType.UNARY)
							.setFullMethodName(generateFullMethodName(SERVICE_NAME, "UpdateMeta"))
							.setSampledToLocalTracing(true)
							.setRequestMarshaller(io.grpc.protobuf.ProtoUtils
									.marshaller(one.inve.localfullnode2.p2pcluster.ic.P2PClusterClient.RequestUpdateMeta
											.getDefaultInstance()))
							.setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
									one.inve.localfullnode2.p2pcluster.ic.P2PClusterClient.ResponseUpdateMeta
											.getDefaultInstance()))
							.setSchemaDescriptor(new ClusterMethodDescriptorSupplier("UpdateMeta")).build();
				}
			}
		}
		return getUpdateMetaMethod;
	}

	private static volatile io.grpc.MethodDescriptor<one.inve.localfullnode2.p2pcluster.ic.P2PClusterClient.RequestFindMembers, one.inve.localfullnode2.p2pcluster.ic.P2PClusterClient.ResponseFindMembers> getFindAliveMembersMethod;

	@io.grpc.stub.annotations.RpcMethod(fullMethodName = SERVICE_NAME + '/'
			+ "FindAliveMembers", requestType = one.inve.localfullnode2.p2pcluster.ic.P2PClusterClient.RequestFindMembers.class, responseType = one.inve.localfullnode2.p2pcluster.ic.P2PClusterClient.ResponseFindMembers.class, methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
	public static io.grpc.MethodDescriptor<one.inve.localfullnode2.p2pcluster.ic.P2PClusterClient.RequestFindMembers, one.inve.localfullnode2.p2pcluster.ic.P2PClusterClient.ResponseFindMembers> getFindAliveMembersMethod() {
		io.grpc.MethodDescriptor<one.inve.localfullnode2.p2pcluster.ic.P2PClusterClient.RequestFindMembers, one.inve.localfullnode2.p2pcluster.ic.P2PClusterClient.ResponseFindMembers> getFindAliveMembersMethod;
		if ((getFindAliveMembersMethod = ClusterGrpc.getFindAliveMembersMethod) == null) {
			synchronized (ClusterGrpc.class) {
				if ((getFindAliveMembersMethod = ClusterGrpc.getFindAliveMembersMethod) == null) {
					ClusterGrpc.getFindAliveMembersMethod = getFindAliveMembersMethod = io.grpc.MethodDescriptor.<one.inve.localfullnode2.p2pcluster.ic.P2PClusterClient.RequestFindMembers, one.inve.localfullnode2.p2pcluster.ic.P2PClusterClient.ResponseFindMembers>newBuilder()
							.setType(io.grpc.MethodDescriptor.MethodType.UNARY)
							.setFullMethodName(generateFullMethodName(SERVICE_NAME, "FindAliveMembers"))
							.setSampledToLocalTracing(true)
							.setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
									one.inve.localfullnode2.p2pcluster.ic.P2PClusterClient.RequestFindMembers
											.getDefaultInstance()))
							.setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
									one.inve.localfullnode2.p2pcluster.ic.P2PClusterClient.ResponseFindMembers
											.getDefaultInstance()))
							.setSchemaDescriptor(new ClusterMethodDescriptorSupplier("FindAliveMembers")).build();
				}
			}
		}
		return getFindAliveMembersMethod;
	}

	private static volatile io.grpc.MethodDescriptor<one.inve.localfullnode2.p2pcluster.ic.P2PClusterClient.RequestFindMembers, one.inve.localfullnode2.p2pcluster.ic.P2PClusterClient.ResponseFindMembers> getFindSuspectedMembersMethod;

	@io.grpc.stub.annotations.RpcMethod(fullMethodName = SERVICE_NAME + '/'
			+ "FindSuspectedMembers", requestType = one.inve.localfullnode2.p2pcluster.ic.P2PClusterClient.RequestFindMembers.class, responseType = one.inve.localfullnode2.p2pcluster.ic.P2PClusterClient.ResponseFindMembers.class, methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
	public static io.grpc.MethodDescriptor<one.inve.localfullnode2.p2pcluster.ic.P2PClusterClient.RequestFindMembers, one.inve.localfullnode2.p2pcluster.ic.P2PClusterClient.ResponseFindMembers> getFindSuspectedMembersMethod() {
		io.grpc.MethodDescriptor<one.inve.localfullnode2.p2pcluster.ic.P2PClusterClient.RequestFindMembers, one.inve.localfullnode2.p2pcluster.ic.P2PClusterClient.ResponseFindMembers> getFindSuspectedMembersMethod;
		if ((getFindSuspectedMembersMethod = ClusterGrpc.getFindSuspectedMembersMethod) == null) {
			synchronized (ClusterGrpc.class) {
				if ((getFindSuspectedMembersMethod = ClusterGrpc.getFindSuspectedMembersMethod) == null) {
					ClusterGrpc.getFindSuspectedMembersMethod = getFindSuspectedMembersMethod = io.grpc.MethodDescriptor.<one.inve.localfullnode2.p2pcluster.ic.P2PClusterClient.RequestFindMembers, one.inve.localfullnode2.p2pcluster.ic.P2PClusterClient.ResponseFindMembers>newBuilder()
							.setType(io.grpc.MethodDescriptor.MethodType.UNARY)
							.setFullMethodName(generateFullMethodName(SERVICE_NAME, "FindSuspectedMembers"))
							.setSampledToLocalTracing(true)
							.setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
									one.inve.localfullnode2.p2pcluster.ic.P2PClusterClient.RequestFindMembers
											.getDefaultInstance()))
							.setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
									one.inve.localfullnode2.p2pcluster.ic.P2PClusterClient.ResponseFindMembers
											.getDefaultInstance()))
							.setSchemaDescriptor(new ClusterMethodDescriptorSupplier("FindSuspectedMembers")).build();
				}
			}
		}
		return getFindSuspectedMembersMethod;
	}

	/**
	 * Creates a new async stub that supports all call types for the service
	 */
	public static ClusterStub newStub(io.grpc.Channel channel) {
		io.grpc.stub.AbstractStub.StubFactory<ClusterStub> factory = new io.grpc.stub.AbstractStub.StubFactory<ClusterStub>() {

			public ClusterStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
				return new ClusterStub(channel, callOptions);
			}
		};
		return ClusterStub.newStub(factory, channel);
	}

	/**
	 * Creates a new blocking-style stub that supports unary and streaming output
	 * calls on the service
	 */
	public static ClusterBlockingStub newBlockingStub(io.grpc.Channel channel) {
		io.grpc.stub.AbstractStub.StubFactory<ClusterBlockingStub> factory = new io.grpc.stub.AbstractStub.StubFactory<ClusterBlockingStub>() {

			public ClusterBlockingStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
				return new ClusterBlockingStub(channel, callOptions);
			}
		};
		return ClusterBlockingStub.newStub(factory, channel);
	}

	/**
	 * Creates a new ListenableFuture-style stub that supports unary calls on the
	 * service
	 */
	public static ClusterFutureStub newFutureStub(io.grpc.Channel channel) {
		io.grpc.stub.AbstractStub.StubFactory<ClusterFutureStub> factory = new io.grpc.stub.AbstractStub.StubFactory<ClusterFutureStub>() {

			public ClusterFutureStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
				return new ClusterFutureStub(channel, callOptions);
			}
		};
		return ClusterFutureStub.newStub(factory, channel);
	}

	/**
	 */
	public static abstract class ClusterImplBase implements io.grpc.BindableService {

		/**
		 */
		public void updateMeta(one.inve.localfullnode2.p2pcluster.ic.P2PClusterClient.RequestUpdateMeta request,
				io.grpc.stub.StreamObserver<one.inve.localfullnode2.p2pcluster.ic.P2PClusterClient.ResponseUpdateMeta> responseObserver) {
			asyncUnimplementedUnaryCall(getUpdateMetaMethod(), responseObserver);
		}

		/**
		 */
		public void findAliveMembers(one.inve.localfullnode2.p2pcluster.ic.P2PClusterClient.RequestFindMembers request,
				io.grpc.stub.StreamObserver<one.inve.localfullnode2.p2pcluster.ic.P2PClusterClient.ResponseFindMembers> responseObserver) {
			asyncUnimplementedUnaryCall(getFindAliveMembersMethod(), responseObserver);
		}

		/**
		 */
		public void findSuspectedMembers(
				one.inve.localfullnode2.p2pcluster.ic.P2PClusterClient.RequestFindMembers request,
				io.grpc.stub.StreamObserver<one.inve.localfullnode2.p2pcluster.ic.P2PClusterClient.ResponseFindMembers> responseObserver) {
			asyncUnimplementedUnaryCall(getFindSuspectedMembersMethod(), responseObserver);
		}

		public final io.grpc.ServerServiceDefinition bindService() {
			return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
					.addMethod(getUpdateMetaMethod(), asyncUnaryCall(
							new MethodHandlers<one.inve.localfullnode2.p2pcluster.ic.P2PClusterClient.RequestUpdateMeta, one.inve.localfullnode2.p2pcluster.ic.P2PClusterClient.ResponseUpdateMeta>(
									this, METHODID_UPDATE_META)))
					.addMethod(getFindAliveMembersMethod(), asyncUnaryCall(
							new MethodHandlers<one.inve.localfullnode2.p2pcluster.ic.P2PClusterClient.RequestFindMembers, one.inve.localfullnode2.p2pcluster.ic.P2PClusterClient.ResponseFindMembers>(
									this, METHODID_FIND_ALIVE_MEMBERS)))
					.addMethod(getFindSuspectedMembersMethod(), asyncUnaryCall(
							new MethodHandlers<one.inve.localfullnode2.p2pcluster.ic.P2PClusterClient.RequestFindMembers, one.inve.localfullnode2.p2pcluster.ic.P2PClusterClient.ResponseFindMembers>(
									this, METHODID_FIND_SUSPECTED_MEMBERS)))
					.build();
		}
	}

	/**
	 */
	public static final class ClusterStub extends io.grpc.stub.AbstractAsyncStub<ClusterStub> {
		private ClusterStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
			super(channel, callOptions);
		}

		protected ClusterStub build(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
			return new ClusterStub(channel, callOptions);
		}

		/**
		 */
		public void updateMeta(one.inve.localfullnode2.p2pcluster.ic.P2PClusterClient.RequestUpdateMeta request,
				io.grpc.stub.StreamObserver<one.inve.localfullnode2.p2pcluster.ic.P2PClusterClient.ResponseUpdateMeta> responseObserver) {
			asyncUnaryCall(getChannel().newCall(getUpdateMetaMethod(), getCallOptions()), request, responseObserver);
		}

		/**
		 */
		public void findAliveMembers(one.inve.localfullnode2.p2pcluster.ic.P2PClusterClient.RequestFindMembers request,
				io.grpc.stub.StreamObserver<one.inve.localfullnode2.p2pcluster.ic.P2PClusterClient.ResponseFindMembers> responseObserver) {
			asyncUnaryCall(getChannel().newCall(getFindAliveMembersMethod(), getCallOptions()), request,
					responseObserver);
		}

		/**
		 */
		public void findSuspectedMembers(
				one.inve.localfullnode2.p2pcluster.ic.P2PClusterClient.RequestFindMembers request,
				io.grpc.stub.StreamObserver<one.inve.localfullnode2.p2pcluster.ic.P2PClusterClient.ResponseFindMembers> responseObserver) {
			asyncUnaryCall(getChannel().newCall(getFindSuspectedMembersMethod(), getCallOptions()), request,
					responseObserver);
		}
	}

	/**
	 */
	public static final class ClusterBlockingStub extends io.grpc.stub.AbstractBlockingStub<ClusterBlockingStub> {
		private ClusterBlockingStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
			super(channel, callOptions);
		}

		protected ClusterBlockingStub build(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
			return new ClusterBlockingStub(channel, callOptions);
		}

		/**
		 */
		public one.inve.localfullnode2.p2pcluster.ic.P2PClusterClient.ResponseUpdateMeta updateMeta(
				one.inve.localfullnode2.p2pcluster.ic.P2PClusterClient.RequestUpdateMeta request) {
			return blockingUnaryCall(getChannel(), getUpdateMetaMethod(), getCallOptions(), request);
		}

		/**
		 */
		public one.inve.localfullnode2.p2pcluster.ic.P2PClusterClient.ResponseFindMembers findAliveMembers(
				one.inve.localfullnode2.p2pcluster.ic.P2PClusterClient.RequestFindMembers request) {
			return blockingUnaryCall(getChannel(), getFindAliveMembersMethod(), getCallOptions(), request);
		}

		/**
		 */
		public one.inve.localfullnode2.p2pcluster.ic.P2PClusterClient.ResponseFindMembers findSuspectedMembers(
				one.inve.localfullnode2.p2pcluster.ic.P2PClusterClient.RequestFindMembers request) {
			return blockingUnaryCall(getChannel(), getFindSuspectedMembersMethod(), getCallOptions(), request);
		}
	}

	/**
	 */
	public static final class ClusterFutureStub extends io.grpc.stub.AbstractFutureStub<ClusterFutureStub> {
		private ClusterFutureStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
			super(channel, callOptions);
		}

		protected ClusterFutureStub build(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
			return new ClusterFutureStub(channel, callOptions);
		}

		/**
		 */
		public com.google.common.util.concurrent.ListenableFuture<one.inve.localfullnode2.p2pcluster.ic.P2PClusterClient.ResponseUpdateMeta> updateMeta(
				one.inve.localfullnode2.p2pcluster.ic.P2PClusterClient.RequestUpdateMeta request) {
			return futureUnaryCall(getChannel().newCall(getUpdateMetaMethod(), getCallOptions()), request);
		}

		/**
		 */
		public com.google.common.util.concurrent.ListenableFuture<one.inve.localfullnode2.p2pcluster.ic.P2PClusterClient.ResponseFindMembers> findAliveMembers(
				one.inve.localfullnode2.p2pcluster.ic.P2PClusterClient.RequestFindMembers request) {
			return futureUnaryCall(getChannel().newCall(getFindAliveMembersMethod(), getCallOptions()), request);
		}

		/**
		 */
		public com.google.common.util.concurrent.ListenableFuture<one.inve.localfullnode2.p2pcluster.ic.P2PClusterClient.ResponseFindMembers> findSuspectedMembers(
				one.inve.localfullnode2.p2pcluster.ic.P2PClusterClient.RequestFindMembers request) {
			return futureUnaryCall(getChannel().newCall(getFindSuspectedMembersMethod(), getCallOptions()), request);
		}
	}

	private static final int METHODID_UPDATE_META = 0;
	private static final int METHODID_FIND_ALIVE_MEMBERS = 1;
	private static final int METHODID_FIND_SUSPECTED_MEMBERS = 2;

	private static final class MethodHandlers<Req, Resp> implements io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
			io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
			io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
			io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
		private final ClusterImplBase serviceImpl;
		private final int methodId;

		MethodHandlers(ClusterImplBase serviceImpl, int methodId) {
			this.serviceImpl = serviceImpl;
			this.methodId = methodId;
		}

		@java.lang.SuppressWarnings("unchecked")
		public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
			switch (methodId) {
			case METHODID_UPDATE_META:
				serviceImpl.updateMeta(
						(one.inve.localfullnode2.p2pcluster.ic.P2PClusterClient.RequestUpdateMeta) request,
						(io.grpc.stub.StreamObserver<one.inve.localfullnode2.p2pcluster.ic.P2PClusterClient.ResponseUpdateMeta>) responseObserver);
				break;
			case METHODID_FIND_ALIVE_MEMBERS:
				serviceImpl.findAliveMembers(
						(one.inve.localfullnode2.p2pcluster.ic.P2PClusterClient.RequestFindMembers) request,
						(io.grpc.stub.StreamObserver<one.inve.localfullnode2.p2pcluster.ic.P2PClusterClient.ResponseFindMembers>) responseObserver);
				break;
			case METHODID_FIND_SUSPECTED_MEMBERS:
				serviceImpl.findSuspectedMembers(
						(one.inve.localfullnode2.p2pcluster.ic.P2PClusterClient.RequestFindMembers) request,
						(io.grpc.stub.StreamObserver<one.inve.localfullnode2.p2pcluster.ic.P2PClusterClient.ResponseFindMembers>) responseObserver);
				break;
			default:
				throw new AssertionError();
			}
		}

		@java.lang.SuppressWarnings("unchecked")
		public io.grpc.stub.StreamObserver<Req> invoke(io.grpc.stub.StreamObserver<Resp> responseObserver) {
			switch (methodId) {
			default:
				throw new AssertionError();
			}
		}
	}

	private static abstract class ClusterBaseDescriptorSupplier
			implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
		ClusterBaseDescriptorSupplier() {
		}

		public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
			return one.inve.localfullnode2.p2pcluster.ic.P2PClusterClient.getDescriptor();
		}

		public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
			return getFileDescriptor().findServiceByName("Cluster");
		}
	}

	private static final class ClusterFileDescriptorSupplier extends ClusterBaseDescriptorSupplier {
		ClusterFileDescriptorSupplier() {
		}
	}

	private static final class ClusterMethodDescriptorSupplier extends ClusterBaseDescriptorSupplier
			implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
		private final String methodName;

		ClusterMethodDescriptorSupplier(String methodName) {
			this.methodName = methodName;
		}

		public com.google.protobuf.Descriptors.MethodDescriptor getMethodDescriptor() {
			return getServiceDescriptor().findMethodByName(methodName);
		}
	}

	private static volatile io.grpc.ServiceDescriptor serviceDescriptor;

	public static io.grpc.ServiceDescriptor getServiceDescriptor() {
		io.grpc.ServiceDescriptor result = serviceDescriptor;
		if (result == null) {
			synchronized (ClusterGrpc.class) {
				result = serviceDescriptor;
				if (result == null) {
					serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
							.setSchemaDescriptor(new ClusterFileDescriptorSupplier()).addMethod(getUpdateMetaMethod())
							.addMethod(getFindAliveMembersMethod()).addMethod(getFindSuspectedMembersMethod()).build();
				}
			}
		}
		return result;
	}
}

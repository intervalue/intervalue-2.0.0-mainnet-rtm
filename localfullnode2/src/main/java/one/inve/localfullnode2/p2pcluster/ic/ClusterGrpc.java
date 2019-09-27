package one.inve.localfullnode2.p2pcluster.ic;

import static io.grpc.MethodDescriptor.generateFullMethodName;
import static io.grpc.stub.ClientCalls.asyncBidiStreamingCall;
import static io.grpc.stub.ClientCalls.asyncClientStreamingCall;
import static io.grpc.stub.ClientCalls.asyncServerStreamingCall;
import static io.grpc.stub.ClientCalls.asyncUnaryCall;
import static io.grpc.stub.ClientCalls.blockingServerStreamingCall;
import static io.grpc.stub.ClientCalls.blockingUnaryCall;
import static io.grpc.stub.ClientCalls.futureUnaryCall;
import static io.grpc.stub.ServerCalls.asyncBidiStreamingCall;
import static io.grpc.stub.ServerCalls.asyncClientStreamingCall;
import static io.grpc.stub.ServerCalls.asyncServerStreamingCall;
import static io.grpc.stub.ServerCalls.asyncUnaryCall;
import static io.grpc.stub.ServerCalls.asyncUnimplementedStreamingCall;
import static io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall;

/**
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.9.1)",
    comments = "Source: ic.proto")
public final class ClusterGrpc {

  private ClusterGrpc() {}

  public static final String SERVICE_NAME = "ic.Cluster";

  // Static method descriptors that strictly reflect the proto.
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  @java.lang.Deprecated // Use {@link #getUpdateMetaMethod()} instead. 
  public static final io.grpc.MethodDescriptor<one.inve.localfullnode2.p2pcluster.ic.P2PClusterClient.RequestUpdateMeta,
      one.inve.localfullnode2.p2pcluster.ic.P2PClusterClient.ResponseUpdateMeta> METHOD_UPDATE_META = getUpdateMetaMethod();

  private static volatile io.grpc.MethodDescriptor<one.inve.localfullnode2.p2pcluster.ic.P2PClusterClient.RequestUpdateMeta,
      one.inve.localfullnode2.p2pcluster.ic.P2PClusterClient.ResponseUpdateMeta> getUpdateMetaMethod;

  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static io.grpc.MethodDescriptor<one.inve.localfullnode2.p2pcluster.ic.P2PClusterClient.RequestUpdateMeta,
      one.inve.localfullnode2.p2pcluster.ic.P2PClusterClient.ResponseUpdateMeta> getUpdateMetaMethod() {
    io.grpc.MethodDescriptor<one.inve.localfullnode2.p2pcluster.ic.P2PClusterClient.RequestUpdateMeta, one.inve.localfullnode2.p2pcluster.ic.P2PClusterClient.ResponseUpdateMeta> getUpdateMetaMethod;
    if ((getUpdateMetaMethod = ClusterGrpc.getUpdateMetaMethod) == null) {
      synchronized (ClusterGrpc.class) {
        if ((getUpdateMetaMethod = ClusterGrpc.getUpdateMetaMethod) == null) {
          ClusterGrpc.getUpdateMetaMethod = getUpdateMetaMethod = 
              io.grpc.MethodDescriptor.<one.inve.localfullnode2.p2pcluster.ic.P2PClusterClient.RequestUpdateMeta, one.inve.localfullnode2.p2pcluster.ic.P2PClusterClient.ResponseUpdateMeta>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(
                  "ic.Cluster", "UpdateMeta"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  one.inve.localfullnode2.p2pcluster.ic.P2PClusterClient.RequestUpdateMeta.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  one.inve.localfullnode2.p2pcluster.ic.P2PClusterClient.ResponseUpdateMeta.getDefaultInstance()))
                  .setSchemaDescriptor(new ClusterMethodDescriptorSupplier("UpdateMeta"))
                  .build();
          }
        }
     }
     return getUpdateMetaMethod;
  }
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  @java.lang.Deprecated // Use {@link #getFindAliveMembersMethod()} instead. 
  public static final io.grpc.MethodDescriptor<one.inve.localfullnode2.p2pcluster.ic.P2PClusterClient.RequestFindMembers,
      one.inve.localfullnode2.p2pcluster.ic.P2PClusterClient.ResponseFindMembers> METHOD_FIND_ALIVE_MEMBERS = getFindAliveMembersMethod();

  private static volatile io.grpc.MethodDescriptor<one.inve.localfullnode2.p2pcluster.ic.P2PClusterClient.RequestFindMembers,
      one.inve.localfullnode2.p2pcluster.ic.P2PClusterClient.ResponseFindMembers> getFindAliveMembersMethod;

  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static io.grpc.MethodDescriptor<one.inve.localfullnode2.p2pcluster.ic.P2PClusterClient.RequestFindMembers,
      one.inve.localfullnode2.p2pcluster.ic.P2PClusterClient.ResponseFindMembers> getFindAliveMembersMethod() {
    io.grpc.MethodDescriptor<one.inve.localfullnode2.p2pcluster.ic.P2PClusterClient.RequestFindMembers, one.inve.localfullnode2.p2pcluster.ic.P2PClusterClient.ResponseFindMembers> getFindAliveMembersMethod;
    if ((getFindAliveMembersMethod = ClusterGrpc.getFindAliveMembersMethod) == null) {
      synchronized (ClusterGrpc.class) {
        if ((getFindAliveMembersMethod = ClusterGrpc.getFindAliveMembersMethod) == null) {
          ClusterGrpc.getFindAliveMembersMethod = getFindAliveMembersMethod = 
              io.grpc.MethodDescriptor.<one.inve.localfullnode2.p2pcluster.ic.P2PClusterClient.RequestFindMembers, one.inve.localfullnode2.p2pcluster.ic.P2PClusterClient.ResponseFindMembers>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(
                  "ic.Cluster", "FindAliveMembers"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  one.inve.localfullnode2.p2pcluster.ic.P2PClusterClient.RequestFindMembers.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  one.inve.localfullnode2.p2pcluster.ic.P2PClusterClient.ResponseFindMembers.getDefaultInstance()))
                  .setSchemaDescriptor(new ClusterMethodDescriptorSupplier("FindAliveMembers"))
                  .build();
          }
        }
     }
     return getFindAliveMembersMethod;
  }
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  @java.lang.Deprecated // Use {@link #getFindSuspectedMembersMethod()} instead. 
  public static final io.grpc.MethodDescriptor<one.inve.localfullnode2.p2pcluster.ic.P2PClusterClient.RequestFindMembers,
      one.inve.localfullnode2.p2pcluster.ic.P2PClusterClient.ResponseFindMembers> METHOD_FIND_SUSPECTED_MEMBERS = getFindSuspectedMembersMethod();

  private static volatile io.grpc.MethodDescriptor<one.inve.localfullnode2.p2pcluster.ic.P2PClusterClient.RequestFindMembers,
      one.inve.localfullnode2.p2pcluster.ic.P2PClusterClient.ResponseFindMembers> getFindSuspectedMembersMethod;

  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static io.grpc.MethodDescriptor<one.inve.localfullnode2.p2pcluster.ic.P2PClusterClient.RequestFindMembers,
      one.inve.localfullnode2.p2pcluster.ic.P2PClusterClient.ResponseFindMembers> getFindSuspectedMembersMethod() {
    io.grpc.MethodDescriptor<one.inve.localfullnode2.p2pcluster.ic.P2PClusterClient.RequestFindMembers, one.inve.localfullnode2.p2pcluster.ic.P2PClusterClient.ResponseFindMembers> getFindSuspectedMembersMethod;
    if ((getFindSuspectedMembersMethod = ClusterGrpc.getFindSuspectedMembersMethod) == null) {
      synchronized (ClusterGrpc.class) {
        if ((getFindSuspectedMembersMethod = ClusterGrpc.getFindSuspectedMembersMethod) == null) {
          ClusterGrpc.getFindSuspectedMembersMethod = getFindSuspectedMembersMethod = 
              io.grpc.MethodDescriptor.<one.inve.localfullnode2.p2pcluster.ic.P2PClusterClient.RequestFindMembers, one.inve.localfullnode2.p2pcluster.ic.P2PClusterClient.ResponseFindMembers>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(
                  "ic.Cluster", "FindSuspectedMembers"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  one.inve.localfullnode2.p2pcluster.ic.P2PClusterClient.RequestFindMembers.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  one.inve.localfullnode2.p2pcluster.ic.P2PClusterClient.ResponseFindMembers.getDefaultInstance()))
                  .setSchemaDescriptor(new ClusterMethodDescriptorSupplier("FindSuspectedMembers"))
                  .build();
          }
        }
     }
     return getFindSuspectedMembersMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static ClusterStub newStub(io.grpc.Channel channel) {
    return new ClusterStub(channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static ClusterBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    return new ClusterBlockingStub(channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static ClusterFutureStub newFutureStub(
      io.grpc.Channel channel) {
    return new ClusterFutureStub(channel);
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
    public void findSuspectedMembers(one.inve.localfullnode2.p2pcluster.ic.P2PClusterClient.RequestFindMembers request,
        io.grpc.stub.StreamObserver<one.inve.localfullnode2.p2pcluster.ic.P2PClusterClient.ResponseFindMembers> responseObserver) {
      asyncUnimplementedUnaryCall(getFindSuspectedMembersMethod(), responseObserver);
    }

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
          .addMethod(
            getUpdateMetaMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                one.inve.localfullnode2.p2pcluster.ic.P2PClusterClient.RequestUpdateMeta,
                one.inve.localfullnode2.p2pcluster.ic.P2PClusterClient.ResponseUpdateMeta>(
                  this, METHODID_UPDATE_META)))
          .addMethod(
            getFindAliveMembersMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                one.inve.localfullnode2.p2pcluster.ic.P2PClusterClient.RequestFindMembers,
                one.inve.localfullnode2.p2pcluster.ic.P2PClusterClient.ResponseFindMembers>(
                  this, METHODID_FIND_ALIVE_MEMBERS)))
          .addMethod(
            getFindSuspectedMembersMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                one.inve.localfullnode2.p2pcluster.ic.P2PClusterClient.RequestFindMembers,
                one.inve.localfullnode2.p2pcluster.ic.P2PClusterClient.ResponseFindMembers>(
                  this, METHODID_FIND_SUSPECTED_MEMBERS)))
          .build();
    }
  }

  /**
   */
  public static final class ClusterStub extends io.grpc.stub.AbstractStub<ClusterStub> {
    private ClusterStub(io.grpc.Channel channel) {
      super(channel);
    }

    private ClusterStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected ClusterStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new ClusterStub(channel, callOptions);
    }

    /**
     */
    public void updateMeta(one.inve.localfullnode2.p2pcluster.ic.P2PClusterClient.RequestUpdateMeta request,
        io.grpc.stub.StreamObserver<one.inve.localfullnode2.p2pcluster.ic.P2PClusterClient.ResponseUpdateMeta> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getUpdateMetaMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void findAliveMembers(one.inve.localfullnode2.p2pcluster.ic.P2PClusterClient.RequestFindMembers request,
        io.grpc.stub.StreamObserver<one.inve.localfullnode2.p2pcluster.ic.P2PClusterClient.ResponseFindMembers> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getFindAliveMembersMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void findSuspectedMembers(one.inve.localfullnode2.p2pcluster.ic.P2PClusterClient.RequestFindMembers request,
        io.grpc.stub.StreamObserver<one.inve.localfullnode2.p2pcluster.ic.P2PClusterClient.ResponseFindMembers> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getFindSuspectedMembersMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   */
  public static final class ClusterBlockingStub extends io.grpc.stub.AbstractStub<ClusterBlockingStub> {
    private ClusterBlockingStub(io.grpc.Channel channel) {
      super(channel);
    }

    private ClusterBlockingStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected ClusterBlockingStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new ClusterBlockingStub(channel, callOptions);
    }

    /**
     */
    public one.inve.localfullnode2.p2pcluster.ic.P2PClusterClient.ResponseUpdateMeta updateMeta(one.inve.localfullnode2.p2pcluster.ic.P2PClusterClient.RequestUpdateMeta request) {
      return blockingUnaryCall(
          getChannel(), getUpdateMetaMethod(), getCallOptions(), request);
    }

    /**
     */
    public one.inve.localfullnode2.p2pcluster.ic.P2PClusterClient.ResponseFindMembers findAliveMembers(one.inve.localfullnode2.p2pcluster.ic.P2PClusterClient.RequestFindMembers request) {
      return blockingUnaryCall(
          getChannel(), getFindAliveMembersMethod(), getCallOptions(), request);
    }

    /**
     */
    public one.inve.localfullnode2.p2pcluster.ic.P2PClusterClient.ResponseFindMembers findSuspectedMembers(one.inve.localfullnode2.p2pcluster.ic.P2PClusterClient.RequestFindMembers request) {
      return blockingUnaryCall(
          getChannel(), getFindSuspectedMembersMethod(), getCallOptions(), request);
    }
  }

  /**
   */
  public static final class ClusterFutureStub extends io.grpc.stub.AbstractStub<ClusterFutureStub> {
    private ClusterFutureStub(io.grpc.Channel channel) {
      super(channel);
    }

    private ClusterFutureStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected ClusterFutureStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new ClusterFutureStub(channel, callOptions);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<one.inve.localfullnode2.p2pcluster.ic.P2PClusterClient.ResponseUpdateMeta> updateMeta(
        one.inve.localfullnode2.p2pcluster.ic.P2PClusterClient.RequestUpdateMeta request) {
      return futureUnaryCall(
          getChannel().newCall(getUpdateMetaMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<one.inve.localfullnode2.p2pcluster.ic.P2PClusterClient.ResponseFindMembers> findAliveMembers(
        one.inve.localfullnode2.p2pcluster.ic.P2PClusterClient.RequestFindMembers request) {
      return futureUnaryCall(
          getChannel().newCall(getFindAliveMembersMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<one.inve.localfullnode2.p2pcluster.ic.P2PClusterClient.ResponseFindMembers> findSuspectedMembers(
        one.inve.localfullnode2.p2pcluster.ic.P2PClusterClient.RequestFindMembers request) {
      return futureUnaryCall(
          getChannel().newCall(getFindSuspectedMembersMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_UPDATE_META = 0;
  private static final int METHODID_FIND_ALIVE_MEMBERS = 1;
  private static final int METHODID_FIND_SUSPECTED_MEMBERS = 2;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final ClusterImplBase serviceImpl;
    private final int methodId;

    MethodHandlers(ClusterImplBase serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_UPDATE_META:
          serviceImpl.updateMeta((one.inve.localfullnode2.p2pcluster.ic.P2PClusterClient.RequestUpdateMeta) request,
              (io.grpc.stub.StreamObserver<one.inve.localfullnode2.p2pcluster.ic.P2PClusterClient.ResponseUpdateMeta>) responseObserver);
          break;
        case METHODID_FIND_ALIVE_MEMBERS:
          serviceImpl.findAliveMembers((one.inve.localfullnode2.p2pcluster.ic.P2PClusterClient.RequestFindMembers) request,
              (io.grpc.stub.StreamObserver<one.inve.localfullnode2.p2pcluster.ic.P2PClusterClient.ResponseFindMembers>) responseObserver);
          break;
        case METHODID_FIND_SUSPECTED_MEMBERS:
          serviceImpl.findSuspectedMembers((one.inve.localfullnode2.p2pcluster.ic.P2PClusterClient.RequestFindMembers) request,
              (io.grpc.stub.StreamObserver<one.inve.localfullnode2.p2pcluster.ic.P2PClusterClient.ResponseFindMembers>) responseObserver);
          break;
        default:
          throw new AssertionError();
      }
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public io.grpc.stub.StreamObserver<Req> invoke(
        io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        default:
          throw new AssertionError();
      }
    }
  }

  private static abstract class ClusterBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    ClusterBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return one.inve.localfullnode2.p2pcluster.ic.P2PClusterClient.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("Cluster");
    }
  }

  private static final class ClusterFileDescriptorSupplier
      extends ClusterBaseDescriptorSupplier {
    ClusterFileDescriptorSupplier() {}
  }

  private static final class ClusterMethodDescriptorSupplier
      extends ClusterBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final String methodName;

    ClusterMethodDescriptorSupplier(String methodName) {
      this.methodName = methodName;
    }

    @java.lang.Override
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
              .setSchemaDescriptor(new ClusterFileDescriptorSupplier())
              .addMethod(getUpdateMetaMethod())
              .addMethod(getFindAliveMembersMethod())
              .addMethod(getFindSuspectedMembersMethod())
              .build();
        }
      }
    }
    return result;
  }
}

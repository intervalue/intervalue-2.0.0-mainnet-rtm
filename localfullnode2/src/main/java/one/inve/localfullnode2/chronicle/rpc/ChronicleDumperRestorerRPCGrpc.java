package one.inve.localfullnode2.chronicle.rpc;

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
    value = "by gRPC proto compiler (version 1.26.0)",
    comments = "Source: chronicle.proto")
public final class ChronicleDumperRestorerRPCGrpc {

  private ChronicleDumperRestorerRPCGrpc() {}

  public static final String SERVICE_NAME = "rpc.ChronicleDumperRestorerRPC";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<com.google.protobuf.Empty,
      one.inve.localfullnode2.chronicle.rpc.StringArray> getGetMessageHashesMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetMessageHashes",
      requestType = com.google.protobuf.Empty.class,
      responseType = one.inve.localfullnode2.chronicle.rpc.StringArray.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.google.protobuf.Empty,
      one.inve.localfullnode2.chronicle.rpc.StringArray> getGetMessageHashesMethod() {
    io.grpc.MethodDescriptor<com.google.protobuf.Empty, one.inve.localfullnode2.chronicle.rpc.StringArray> getGetMessageHashesMethod;
    if ((getGetMessageHashesMethod = ChronicleDumperRestorerRPCGrpc.getGetMessageHashesMethod) == null) {
      synchronized (ChronicleDumperRestorerRPCGrpc.class) {
        if ((getGetMessageHashesMethod = ChronicleDumperRestorerRPCGrpc.getGetMessageHashesMethod) == null) {
          ChronicleDumperRestorerRPCGrpc.getGetMessageHashesMethod = getGetMessageHashesMethod =
              io.grpc.MethodDescriptor.<com.google.protobuf.Empty, one.inve.localfullnode2.chronicle.rpc.StringArray>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetMessageHashes"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.google.protobuf.Empty.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  one.inve.localfullnode2.chronicle.rpc.StringArray.getDefaultInstance()))
              .setSchemaDescriptor(new ChronicleDumperRestorerRPCMethodDescriptorSupplier("GetMessageHashes"))
              .build();
        }
      }
    }
    return getGetMessageHashesMethod;
  }

  private static volatile io.grpc.MethodDescriptor<com.google.protobuf.Empty,
      one.inve.localfullnode2.chronicle.rpc.StringArray> getGetSysMessageHashesMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetSysMessageHashes",
      requestType = com.google.protobuf.Empty.class,
      responseType = one.inve.localfullnode2.chronicle.rpc.StringArray.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.google.protobuf.Empty,
      one.inve.localfullnode2.chronicle.rpc.StringArray> getGetSysMessageHashesMethod() {
    io.grpc.MethodDescriptor<com.google.protobuf.Empty, one.inve.localfullnode2.chronicle.rpc.StringArray> getGetSysMessageHashesMethod;
    if ((getGetSysMessageHashesMethod = ChronicleDumperRestorerRPCGrpc.getGetSysMessageHashesMethod) == null) {
      synchronized (ChronicleDumperRestorerRPCGrpc.class) {
        if ((getGetSysMessageHashesMethod = ChronicleDumperRestorerRPCGrpc.getGetSysMessageHashesMethod) == null) {
          ChronicleDumperRestorerRPCGrpc.getGetSysMessageHashesMethod = getGetSysMessageHashesMethod =
              io.grpc.MethodDescriptor.<com.google.protobuf.Empty, one.inve.localfullnode2.chronicle.rpc.StringArray>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetSysMessageHashes"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.google.protobuf.Empty.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  one.inve.localfullnode2.chronicle.rpc.StringArray.getDefaultInstance()))
              .setSchemaDescriptor(new ChronicleDumperRestorerRPCMethodDescriptorSupplier("GetSysMessageHashes"))
              .build();
        }
      }
    }
    return getGetSysMessageHashesMethod;
  }

  private static volatile io.grpc.MethodDescriptor<one.inve.localfullnode2.chronicle.rpc.StringArray,
      one.inve.localfullnode2.chronicle.rpc.ByteStream> getGetMessageStreamByMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetMessageStreamBy",
      requestType = one.inve.localfullnode2.chronicle.rpc.StringArray.class,
      responseType = one.inve.localfullnode2.chronicle.rpc.ByteStream.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<one.inve.localfullnode2.chronicle.rpc.StringArray,
      one.inve.localfullnode2.chronicle.rpc.ByteStream> getGetMessageStreamByMethod() {
    io.grpc.MethodDescriptor<one.inve.localfullnode2.chronicle.rpc.StringArray, one.inve.localfullnode2.chronicle.rpc.ByteStream> getGetMessageStreamByMethod;
    if ((getGetMessageStreamByMethod = ChronicleDumperRestorerRPCGrpc.getGetMessageStreamByMethod) == null) {
      synchronized (ChronicleDumperRestorerRPCGrpc.class) {
        if ((getGetMessageStreamByMethod = ChronicleDumperRestorerRPCGrpc.getGetMessageStreamByMethod) == null) {
          ChronicleDumperRestorerRPCGrpc.getGetMessageStreamByMethod = getGetMessageStreamByMethod =
              io.grpc.MethodDescriptor.<one.inve.localfullnode2.chronicle.rpc.StringArray, one.inve.localfullnode2.chronicle.rpc.ByteStream>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetMessageStreamBy"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  one.inve.localfullnode2.chronicle.rpc.StringArray.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  one.inve.localfullnode2.chronicle.rpc.ByteStream.getDefaultInstance()))
              .setSchemaDescriptor(new ChronicleDumperRestorerRPCMethodDescriptorSupplier("GetMessageStreamBy"))
              .build();
        }
      }
    }
    return getGetMessageStreamByMethod;
  }

  private static volatile io.grpc.MethodDescriptor<one.inve.localfullnode2.chronicle.rpc.ByteStream,
      com.google.protobuf.Empty> getPersistMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "Persist",
      requestType = one.inve.localfullnode2.chronicle.rpc.ByteStream.class,
      responseType = com.google.protobuf.Empty.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<one.inve.localfullnode2.chronicle.rpc.ByteStream,
      com.google.protobuf.Empty> getPersistMethod() {
    io.grpc.MethodDescriptor<one.inve.localfullnode2.chronicle.rpc.ByteStream, com.google.protobuf.Empty> getPersistMethod;
    if ((getPersistMethod = ChronicleDumperRestorerRPCGrpc.getPersistMethod) == null) {
      synchronized (ChronicleDumperRestorerRPCGrpc.class) {
        if ((getPersistMethod = ChronicleDumperRestorerRPCGrpc.getPersistMethod) == null) {
          ChronicleDumperRestorerRPCGrpc.getPersistMethod = getPersistMethod =
              io.grpc.MethodDescriptor.<one.inve.localfullnode2.chronicle.rpc.ByteStream, com.google.protobuf.Empty>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "Persist"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  one.inve.localfullnode2.chronicle.rpc.ByteStream.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.google.protobuf.Empty.getDefaultInstance()))
              .setSchemaDescriptor(new ChronicleDumperRestorerRPCMethodDescriptorSupplier("Persist"))
              .build();
        }
      }
    }
    return getPersistMethod;
  }

  private static volatile io.grpc.MethodDescriptor<one.inve.localfullnode2.chronicle.rpc.ByteStream,
      com.google.protobuf.Empty> getPersistSysMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "PersistSys",
      requestType = one.inve.localfullnode2.chronicle.rpc.ByteStream.class,
      responseType = com.google.protobuf.Empty.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<one.inve.localfullnode2.chronicle.rpc.ByteStream,
      com.google.protobuf.Empty> getPersistSysMethod() {
    io.grpc.MethodDescriptor<one.inve.localfullnode2.chronicle.rpc.ByteStream, com.google.protobuf.Empty> getPersistSysMethod;
    if ((getPersistSysMethod = ChronicleDumperRestorerRPCGrpc.getPersistSysMethod) == null) {
      synchronized (ChronicleDumperRestorerRPCGrpc.class) {
        if ((getPersistSysMethod = ChronicleDumperRestorerRPCGrpc.getPersistSysMethod) == null) {
          ChronicleDumperRestorerRPCGrpc.getPersistSysMethod = getPersistSysMethod =
              io.grpc.MethodDescriptor.<one.inve.localfullnode2.chronicle.rpc.ByteStream, com.google.protobuf.Empty>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "PersistSys"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  one.inve.localfullnode2.chronicle.rpc.ByteStream.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.google.protobuf.Empty.getDefaultInstance()))
              .setSchemaDescriptor(new ChronicleDumperRestorerRPCMethodDescriptorSupplier("PersistSys"))
              .build();
        }
      }
    }
    return getPersistSysMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static ChronicleDumperRestorerRPCStub newStub(io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<ChronicleDumperRestorerRPCStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<ChronicleDumperRestorerRPCStub>() {
        @java.lang.Override
        public ChronicleDumperRestorerRPCStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new ChronicleDumperRestorerRPCStub(channel, callOptions);
        }
      };
    return ChronicleDumperRestorerRPCStub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static ChronicleDumperRestorerRPCBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<ChronicleDumperRestorerRPCBlockingStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<ChronicleDumperRestorerRPCBlockingStub>() {
        @java.lang.Override
        public ChronicleDumperRestorerRPCBlockingStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new ChronicleDumperRestorerRPCBlockingStub(channel, callOptions);
        }
      };
    return ChronicleDumperRestorerRPCBlockingStub.newStub(factory, channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static ChronicleDumperRestorerRPCFutureStub newFutureStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<ChronicleDumperRestorerRPCFutureStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<ChronicleDumperRestorerRPCFutureStub>() {
        @java.lang.Override
        public ChronicleDumperRestorerRPCFutureStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new ChronicleDumperRestorerRPCFutureStub(channel, callOptions);
        }
      };
    return ChronicleDumperRestorerRPCFutureStub.newStub(factory, channel);
  }

  /**
   */
  public static abstract class ChronicleDumperRestorerRPCImplBase implements io.grpc.BindableService {

    /**
     */
    public void getMessageHashes(com.google.protobuf.Empty request,
        io.grpc.stub.StreamObserver<one.inve.localfullnode2.chronicle.rpc.StringArray> responseObserver) {
      asyncUnimplementedUnaryCall(getGetMessageHashesMethod(), responseObserver);
    }

    /**
     */
    public void getSysMessageHashes(com.google.protobuf.Empty request,
        io.grpc.stub.StreamObserver<one.inve.localfullnode2.chronicle.rpc.StringArray> responseObserver) {
      asyncUnimplementedUnaryCall(getGetSysMessageHashesMethod(), responseObserver);
    }

    /**
     */
    public void getMessageStreamBy(one.inve.localfullnode2.chronicle.rpc.StringArray request,
        io.grpc.stub.StreamObserver<one.inve.localfullnode2.chronicle.rpc.ByteStream> responseObserver) {
      asyncUnimplementedUnaryCall(getGetMessageStreamByMethod(), responseObserver);
    }

    /**
     */
    public void persist(one.inve.localfullnode2.chronicle.rpc.ByteStream request,
        io.grpc.stub.StreamObserver<com.google.protobuf.Empty> responseObserver) {
      asyncUnimplementedUnaryCall(getPersistMethod(), responseObserver);
    }

    /**
     */
    public void persistSys(one.inve.localfullnode2.chronicle.rpc.ByteStream request,
        io.grpc.stub.StreamObserver<com.google.protobuf.Empty> responseObserver) {
      asyncUnimplementedUnaryCall(getPersistSysMethod(), responseObserver);
    }

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
          .addMethod(
            getGetMessageHashesMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                com.google.protobuf.Empty,
                one.inve.localfullnode2.chronicle.rpc.StringArray>(
                  this, METHODID_GET_MESSAGE_HASHES)))
          .addMethod(
            getGetSysMessageHashesMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                com.google.protobuf.Empty,
                one.inve.localfullnode2.chronicle.rpc.StringArray>(
                  this, METHODID_GET_SYS_MESSAGE_HASHES)))
          .addMethod(
            getGetMessageStreamByMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                one.inve.localfullnode2.chronicle.rpc.StringArray,
                one.inve.localfullnode2.chronicle.rpc.ByteStream>(
                  this, METHODID_GET_MESSAGE_STREAM_BY)))
          .addMethod(
            getPersistMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                one.inve.localfullnode2.chronicle.rpc.ByteStream,
                com.google.protobuf.Empty>(
                  this, METHODID_PERSIST)))
          .addMethod(
            getPersistSysMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                one.inve.localfullnode2.chronicle.rpc.ByteStream,
                com.google.protobuf.Empty>(
                  this, METHODID_PERSIST_SYS)))
          .build();
    }
  }

  /**
   */
  public static final class ChronicleDumperRestorerRPCStub extends io.grpc.stub.AbstractAsyncStub<ChronicleDumperRestorerRPCStub> {
    private ChronicleDumperRestorerRPCStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected ChronicleDumperRestorerRPCStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new ChronicleDumperRestorerRPCStub(channel, callOptions);
    }

    /**
     */
    public void getMessageHashes(com.google.protobuf.Empty request,
        io.grpc.stub.StreamObserver<one.inve.localfullnode2.chronicle.rpc.StringArray> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getGetMessageHashesMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void getSysMessageHashes(com.google.protobuf.Empty request,
        io.grpc.stub.StreamObserver<one.inve.localfullnode2.chronicle.rpc.StringArray> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getGetSysMessageHashesMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void getMessageStreamBy(one.inve.localfullnode2.chronicle.rpc.StringArray request,
        io.grpc.stub.StreamObserver<one.inve.localfullnode2.chronicle.rpc.ByteStream> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getGetMessageStreamByMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void persist(one.inve.localfullnode2.chronicle.rpc.ByteStream request,
        io.grpc.stub.StreamObserver<com.google.protobuf.Empty> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getPersistMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void persistSys(one.inve.localfullnode2.chronicle.rpc.ByteStream request,
        io.grpc.stub.StreamObserver<com.google.protobuf.Empty> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getPersistSysMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   */
  public static final class ChronicleDumperRestorerRPCBlockingStub extends io.grpc.stub.AbstractBlockingStub<ChronicleDumperRestorerRPCBlockingStub> {
    private ChronicleDumperRestorerRPCBlockingStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected ChronicleDumperRestorerRPCBlockingStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new ChronicleDumperRestorerRPCBlockingStub(channel, callOptions);
    }

    /**
     */
    public one.inve.localfullnode2.chronicle.rpc.StringArray getMessageHashes(com.google.protobuf.Empty request) {
      return blockingUnaryCall(
          getChannel(), getGetMessageHashesMethod(), getCallOptions(), request);
    }

    /**
     */
    public one.inve.localfullnode2.chronicle.rpc.StringArray getSysMessageHashes(com.google.protobuf.Empty request) {
      return blockingUnaryCall(
          getChannel(), getGetSysMessageHashesMethod(), getCallOptions(), request);
    }

    /**
     */
    public one.inve.localfullnode2.chronicle.rpc.ByteStream getMessageStreamBy(one.inve.localfullnode2.chronicle.rpc.StringArray request) {
      return blockingUnaryCall(
          getChannel(), getGetMessageStreamByMethod(), getCallOptions(), request);
    }

    /**
     */
    public com.google.protobuf.Empty persist(one.inve.localfullnode2.chronicle.rpc.ByteStream request) {
      return blockingUnaryCall(
          getChannel(), getPersistMethod(), getCallOptions(), request);
    }

    /**
     */
    public com.google.protobuf.Empty persistSys(one.inve.localfullnode2.chronicle.rpc.ByteStream request) {
      return blockingUnaryCall(
          getChannel(), getPersistSysMethod(), getCallOptions(), request);
    }
  }

  /**
   */
  public static final class ChronicleDumperRestorerRPCFutureStub extends io.grpc.stub.AbstractFutureStub<ChronicleDumperRestorerRPCFutureStub> {
    private ChronicleDumperRestorerRPCFutureStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected ChronicleDumperRestorerRPCFutureStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new ChronicleDumperRestorerRPCFutureStub(channel, callOptions);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<one.inve.localfullnode2.chronicle.rpc.StringArray> getMessageHashes(
        com.google.protobuf.Empty request) {
      return futureUnaryCall(
          getChannel().newCall(getGetMessageHashesMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<one.inve.localfullnode2.chronicle.rpc.StringArray> getSysMessageHashes(
        com.google.protobuf.Empty request) {
      return futureUnaryCall(
          getChannel().newCall(getGetSysMessageHashesMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<one.inve.localfullnode2.chronicle.rpc.ByteStream> getMessageStreamBy(
        one.inve.localfullnode2.chronicle.rpc.StringArray request) {
      return futureUnaryCall(
          getChannel().newCall(getGetMessageStreamByMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.google.protobuf.Empty> persist(
        one.inve.localfullnode2.chronicle.rpc.ByteStream request) {
      return futureUnaryCall(
          getChannel().newCall(getPersistMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.google.protobuf.Empty> persistSys(
        one.inve.localfullnode2.chronicle.rpc.ByteStream request) {
      return futureUnaryCall(
          getChannel().newCall(getPersistSysMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_GET_MESSAGE_HASHES = 0;
  private static final int METHODID_GET_SYS_MESSAGE_HASHES = 1;
  private static final int METHODID_GET_MESSAGE_STREAM_BY = 2;
  private static final int METHODID_PERSIST = 3;
  private static final int METHODID_PERSIST_SYS = 4;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final ChronicleDumperRestorerRPCImplBase serviceImpl;
    private final int methodId;

    MethodHandlers(ChronicleDumperRestorerRPCImplBase serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_GET_MESSAGE_HASHES:
          serviceImpl.getMessageHashes((com.google.protobuf.Empty) request,
              (io.grpc.stub.StreamObserver<one.inve.localfullnode2.chronicle.rpc.StringArray>) responseObserver);
          break;
        case METHODID_GET_SYS_MESSAGE_HASHES:
          serviceImpl.getSysMessageHashes((com.google.protobuf.Empty) request,
              (io.grpc.stub.StreamObserver<one.inve.localfullnode2.chronicle.rpc.StringArray>) responseObserver);
          break;
        case METHODID_GET_MESSAGE_STREAM_BY:
          serviceImpl.getMessageStreamBy((one.inve.localfullnode2.chronicle.rpc.StringArray) request,
              (io.grpc.stub.StreamObserver<one.inve.localfullnode2.chronicle.rpc.ByteStream>) responseObserver);
          break;
        case METHODID_PERSIST:
          serviceImpl.persist((one.inve.localfullnode2.chronicle.rpc.ByteStream) request,
              (io.grpc.stub.StreamObserver<com.google.protobuf.Empty>) responseObserver);
          break;
        case METHODID_PERSIST_SYS:
          serviceImpl.persistSys((one.inve.localfullnode2.chronicle.rpc.ByteStream) request,
              (io.grpc.stub.StreamObserver<com.google.protobuf.Empty>) responseObserver);
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

  private static abstract class ChronicleDumperRestorerRPCBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    ChronicleDumperRestorerRPCBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return one.inve.localfullnode2.chronicle.rpc.Chronicle.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("ChronicleDumperRestorerRPC");
    }
  }

  private static final class ChronicleDumperRestorerRPCFileDescriptorSupplier
      extends ChronicleDumperRestorerRPCBaseDescriptorSupplier {
    ChronicleDumperRestorerRPCFileDescriptorSupplier() {}
  }

  private static final class ChronicleDumperRestorerRPCMethodDescriptorSupplier
      extends ChronicleDumperRestorerRPCBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final String methodName;

    ChronicleDumperRestorerRPCMethodDescriptorSupplier(String methodName) {
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
      synchronized (ChronicleDumperRestorerRPCGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new ChronicleDumperRestorerRPCFileDescriptorSupplier())
              .addMethod(getGetMessageHashesMethod())
              .addMethod(getGetSysMessageHashesMethod())
              .addMethod(getGetMessageStreamByMethod())
              .addMethod(getPersistMethod())
              .addMethod(getPersistSysMethod())
              .build();
        }
      }
    }
    return result;
  }
}

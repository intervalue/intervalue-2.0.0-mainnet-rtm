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
 * <pre>
 *support steaming communication
 * </pre>
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.26.0)",
    comments = "Source: chronicle.proto")
public final class ChronicleDumperRestorerStreamRPCGrpc {

  private ChronicleDumperRestorerStreamRPCGrpc() {}

  public static final String SERVICE_NAME = "rpc.ChronicleDumperRestorerStreamRPC";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<com.google.protobuf.Empty,
      one.inve.localfullnode2.chronicle.rpc.StringArray> getGetMessageHashesMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetMessageHashes",
      requestType = com.google.protobuf.Empty.class,
      responseType = one.inve.localfullnode2.chronicle.rpc.StringArray.class,
      methodType = io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
  public static io.grpc.MethodDescriptor<com.google.protobuf.Empty,
      one.inve.localfullnode2.chronicle.rpc.StringArray> getGetMessageHashesMethod() {
    io.grpc.MethodDescriptor<com.google.protobuf.Empty, one.inve.localfullnode2.chronicle.rpc.StringArray> getGetMessageHashesMethod;
    if ((getGetMessageHashesMethod = ChronicleDumperRestorerStreamRPCGrpc.getGetMessageHashesMethod) == null) {
      synchronized (ChronicleDumperRestorerStreamRPCGrpc.class) {
        if ((getGetMessageHashesMethod = ChronicleDumperRestorerStreamRPCGrpc.getGetMessageHashesMethod) == null) {
          ChronicleDumperRestorerStreamRPCGrpc.getGetMessageHashesMethod = getGetMessageHashesMethod =
              io.grpc.MethodDescriptor.<com.google.protobuf.Empty, one.inve.localfullnode2.chronicle.rpc.StringArray>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetMessageHashes"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.google.protobuf.Empty.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  one.inve.localfullnode2.chronicle.rpc.StringArray.getDefaultInstance()))
              .setSchemaDescriptor(new ChronicleDumperRestorerStreamRPCMethodDescriptorSupplier("GetMessageHashes"))
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
      methodType = io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
  public static io.grpc.MethodDescriptor<com.google.protobuf.Empty,
      one.inve.localfullnode2.chronicle.rpc.StringArray> getGetSysMessageHashesMethod() {
    io.grpc.MethodDescriptor<com.google.protobuf.Empty, one.inve.localfullnode2.chronicle.rpc.StringArray> getGetSysMessageHashesMethod;
    if ((getGetSysMessageHashesMethod = ChronicleDumperRestorerStreamRPCGrpc.getGetSysMessageHashesMethod) == null) {
      synchronized (ChronicleDumperRestorerStreamRPCGrpc.class) {
        if ((getGetSysMessageHashesMethod = ChronicleDumperRestorerStreamRPCGrpc.getGetSysMessageHashesMethod) == null) {
          ChronicleDumperRestorerStreamRPCGrpc.getGetSysMessageHashesMethod = getGetSysMessageHashesMethod =
              io.grpc.MethodDescriptor.<com.google.protobuf.Empty, one.inve.localfullnode2.chronicle.rpc.StringArray>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetSysMessageHashes"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.google.protobuf.Empty.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  one.inve.localfullnode2.chronicle.rpc.StringArray.getDefaultInstance()))
              .setSchemaDescriptor(new ChronicleDumperRestorerStreamRPCMethodDescriptorSupplier("GetSysMessageHashes"))
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
      methodType = io.grpc.MethodDescriptor.MethodType.BIDI_STREAMING)
  public static io.grpc.MethodDescriptor<one.inve.localfullnode2.chronicle.rpc.StringArray,
      one.inve.localfullnode2.chronicle.rpc.ByteStream> getGetMessageStreamByMethod() {
    io.grpc.MethodDescriptor<one.inve.localfullnode2.chronicle.rpc.StringArray, one.inve.localfullnode2.chronicle.rpc.ByteStream> getGetMessageStreamByMethod;
    if ((getGetMessageStreamByMethod = ChronicleDumperRestorerStreamRPCGrpc.getGetMessageStreamByMethod) == null) {
      synchronized (ChronicleDumperRestorerStreamRPCGrpc.class) {
        if ((getGetMessageStreamByMethod = ChronicleDumperRestorerStreamRPCGrpc.getGetMessageStreamByMethod) == null) {
          ChronicleDumperRestorerStreamRPCGrpc.getGetMessageStreamByMethod = getGetMessageStreamByMethod =
              io.grpc.MethodDescriptor.<one.inve.localfullnode2.chronicle.rpc.StringArray, one.inve.localfullnode2.chronicle.rpc.ByteStream>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.BIDI_STREAMING)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetMessageStreamBy"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  one.inve.localfullnode2.chronicle.rpc.StringArray.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  one.inve.localfullnode2.chronicle.rpc.ByteStream.getDefaultInstance()))
              .setSchemaDescriptor(new ChronicleDumperRestorerStreamRPCMethodDescriptorSupplier("GetMessageStreamBy"))
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
      methodType = io.grpc.MethodDescriptor.MethodType.CLIENT_STREAMING)
  public static io.grpc.MethodDescriptor<one.inve.localfullnode2.chronicle.rpc.ByteStream,
      com.google.protobuf.Empty> getPersistMethod() {
    io.grpc.MethodDescriptor<one.inve.localfullnode2.chronicle.rpc.ByteStream, com.google.protobuf.Empty> getPersistMethod;
    if ((getPersistMethod = ChronicleDumperRestorerStreamRPCGrpc.getPersistMethod) == null) {
      synchronized (ChronicleDumperRestorerStreamRPCGrpc.class) {
        if ((getPersistMethod = ChronicleDumperRestorerStreamRPCGrpc.getPersistMethod) == null) {
          ChronicleDumperRestorerStreamRPCGrpc.getPersistMethod = getPersistMethod =
              io.grpc.MethodDescriptor.<one.inve.localfullnode2.chronicle.rpc.ByteStream, com.google.protobuf.Empty>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.CLIENT_STREAMING)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "Persist"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  one.inve.localfullnode2.chronicle.rpc.ByteStream.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.google.protobuf.Empty.getDefaultInstance()))
              .setSchemaDescriptor(new ChronicleDumperRestorerStreamRPCMethodDescriptorSupplier("Persist"))
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
      methodType = io.grpc.MethodDescriptor.MethodType.CLIENT_STREAMING)
  public static io.grpc.MethodDescriptor<one.inve.localfullnode2.chronicle.rpc.ByteStream,
      com.google.protobuf.Empty> getPersistSysMethod() {
    io.grpc.MethodDescriptor<one.inve.localfullnode2.chronicle.rpc.ByteStream, com.google.protobuf.Empty> getPersistSysMethod;
    if ((getPersistSysMethod = ChronicleDumperRestorerStreamRPCGrpc.getPersistSysMethod) == null) {
      synchronized (ChronicleDumperRestorerStreamRPCGrpc.class) {
        if ((getPersistSysMethod = ChronicleDumperRestorerStreamRPCGrpc.getPersistSysMethod) == null) {
          ChronicleDumperRestorerStreamRPCGrpc.getPersistSysMethod = getPersistSysMethod =
              io.grpc.MethodDescriptor.<one.inve.localfullnode2.chronicle.rpc.ByteStream, com.google.protobuf.Empty>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.CLIENT_STREAMING)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "PersistSys"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  one.inve.localfullnode2.chronicle.rpc.ByteStream.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.google.protobuf.Empty.getDefaultInstance()))
              .setSchemaDescriptor(new ChronicleDumperRestorerStreamRPCMethodDescriptorSupplier("PersistSys"))
              .build();
        }
      }
    }
    return getPersistSysMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static ChronicleDumperRestorerStreamRPCStub newStub(io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<ChronicleDumperRestorerStreamRPCStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<ChronicleDumperRestorerStreamRPCStub>() {
        @java.lang.Override
        public ChronicleDumperRestorerStreamRPCStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new ChronicleDumperRestorerStreamRPCStub(channel, callOptions);
        }
      };
    return ChronicleDumperRestorerStreamRPCStub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static ChronicleDumperRestorerStreamRPCBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<ChronicleDumperRestorerStreamRPCBlockingStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<ChronicleDumperRestorerStreamRPCBlockingStub>() {
        @java.lang.Override
        public ChronicleDumperRestorerStreamRPCBlockingStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new ChronicleDumperRestorerStreamRPCBlockingStub(channel, callOptions);
        }
      };
    return ChronicleDumperRestorerStreamRPCBlockingStub.newStub(factory, channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static ChronicleDumperRestorerStreamRPCFutureStub newFutureStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<ChronicleDumperRestorerStreamRPCFutureStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<ChronicleDumperRestorerStreamRPCFutureStub>() {
        @java.lang.Override
        public ChronicleDumperRestorerStreamRPCFutureStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new ChronicleDumperRestorerStreamRPCFutureStub(channel, callOptions);
        }
      };
    return ChronicleDumperRestorerStreamRPCFutureStub.newStub(factory, channel);
  }

  /**
   * <pre>
   *support steaming communication
   * </pre>
   */
  public static abstract class ChronicleDumperRestorerStreamRPCImplBase implements io.grpc.BindableService {

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
    public io.grpc.stub.StreamObserver<one.inve.localfullnode2.chronicle.rpc.StringArray> getMessageStreamBy(
        io.grpc.stub.StreamObserver<one.inve.localfullnode2.chronicle.rpc.ByteStream> responseObserver) {
      return asyncUnimplementedStreamingCall(getGetMessageStreamByMethod(), responseObserver);
    }

    /**
     */
    public io.grpc.stub.StreamObserver<one.inve.localfullnode2.chronicle.rpc.ByteStream> persist(
        io.grpc.stub.StreamObserver<com.google.protobuf.Empty> responseObserver) {
      return asyncUnimplementedStreamingCall(getPersistMethod(), responseObserver);
    }

    /**
     */
    public io.grpc.stub.StreamObserver<one.inve.localfullnode2.chronicle.rpc.ByteStream> persistSys(
        io.grpc.stub.StreamObserver<com.google.protobuf.Empty> responseObserver) {
      return asyncUnimplementedStreamingCall(getPersistSysMethod(), responseObserver);
    }

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
          .addMethod(
            getGetMessageHashesMethod(),
            asyncServerStreamingCall(
              new MethodHandlers<
                com.google.protobuf.Empty,
                one.inve.localfullnode2.chronicle.rpc.StringArray>(
                  this, METHODID_GET_MESSAGE_HASHES)))
          .addMethod(
            getGetSysMessageHashesMethod(),
            asyncServerStreamingCall(
              new MethodHandlers<
                com.google.protobuf.Empty,
                one.inve.localfullnode2.chronicle.rpc.StringArray>(
                  this, METHODID_GET_SYS_MESSAGE_HASHES)))
          .addMethod(
            getGetMessageStreamByMethod(),
            asyncBidiStreamingCall(
              new MethodHandlers<
                one.inve.localfullnode2.chronicle.rpc.StringArray,
                one.inve.localfullnode2.chronicle.rpc.ByteStream>(
                  this, METHODID_GET_MESSAGE_STREAM_BY)))
          .addMethod(
            getPersistMethod(),
            asyncClientStreamingCall(
              new MethodHandlers<
                one.inve.localfullnode2.chronicle.rpc.ByteStream,
                com.google.protobuf.Empty>(
                  this, METHODID_PERSIST)))
          .addMethod(
            getPersistSysMethod(),
            asyncClientStreamingCall(
              new MethodHandlers<
                one.inve.localfullnode2.chronicle.rpc.ByteStream,
                com.google.protobuf.Empty>(
                  this, METHODID_PERSIST_SYS)))
          .build();
    }
  }

  /**
   * <pre>
   *support steaming communication
   * </pre>
   */
  public static final class ChronicleDumperRestorerStreamRPCStub extends io.grpc.stub.AbstractAsyncStub<ChronicleDumperRestorerStreamRPCStub> {
    private ChronicleDumperRestorerStreamRPCStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected ChronicleDumperRestorerStreamRPCStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new ChronicleDumperRestorerStreamRPCStub(channel, callOptions);
    }

    /**
     */
    public void getMessageHashes(com.google.protobuf.Empty request,
        io.grpc.stub.StreamObserver<one.inve.localfullnode2.chronicle.rpc.StringArray> responseObserver) {
      asyncServerStreamingCall(
          getChannel().newCall(getGetMessageHashesMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void getSysMessageHashes(com.google.protobuf.Empty request,
        io.grpc.stub.StreamObserver<one.inve.localfullnode2.chronicle.rpc.StringArray> responseObserver) {
      asyncServerStreamingCall(
          getChannel().newCall(getGetSysMessageHashesMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public io.grpc.stub.StreamObserver<one.inve.localfullnode2.chronicle.rpc.StringArray> getMessageStreamBy(
        io.grpc.stub.StreamObserver<one.inve.localfullnode2.chronicle.rpc.ByteStream> responseObserver) {
      return asyncBidiStreamingCall(
          getChannel().newCall(getGetMessageStreamByMethod(), getCallOptions()), responseObserver);
    }

    /**
     */
    public io.grpc.stub.StreamObserver<one.inve.localfullnode2.chronicle.rpc.ByteStream> persist(
        io.grpc.stub.StreamObserver<com.google.protobuf.Empty> responseObserver) {
      return asyncClientStreamingCall(
          getChannel().newCall(getPersistMethod(), getCallOptions()), responseObserver);
    }

    /**
     */
    public io.grpc.stub.StreamObserver<one.inve.localfullnode2.chronicle.rpc.ByteStream> persistSys(
        io.grpc.stub.StreamObserver<com.google.protobuf.Empty> responseObserver) {
      return asyncClientStreamingCall(
          getChannel().newCall(getPersistSysMethod(), getCallOptions()), responseObserver);
    }
  }

  /**
   * <pre>
   *support steaming communication
   * </pre>
   */
  public static final class ChronicleDumperRestorerStreamRPCBlockingStub extends io.grpc.stub.AbstractBlockingStub<ChronicleDumperRestorerStreamRPCBlockingStub> {
    private ChronicleDumperRestorerStreamRPCBlockingStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected ChronicleDumperRestorerStreamRPCBlockingStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new ChronicleDumperRestorerStreamRPCBlockingStub(channel, callOptions);
    }

    /**
     */
    public java.util.Iterator<one.inve.localfullnode2.chronicle.rpc.StringArray> getMessageHashes(
        com.google.protobuf.Empty request) {
      return blockingServerStreamingCall(
          getChannel(), getGetMessageHashesMethod(), getCallOptions(), request);
    }

    /**
     */
    public java.util.Iterator<one.inve.localfullnode2.chronicle.rpc.StringArray> getSysMessageHashes(
        com.google.protobuf.Empty request) {
      return blockingServerStreamingCall(
          getChannel(), getGetSysMessageHashesMethod(), getCallOptions(), request);
    }
  }

  /**
   * <pre>
   *support steaming communication
   * </pre>
   */
  public static final class ChronicleDumperRestorerStreamRPCFutureStub extends io.grpc.stub.AbstractFutureStub<ChronicleDumperRestorerStreamRPCFutureStub> {
    private ChronicleDumperRestorerStreamRPCFutureStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected ChronicleDumperRestorerStreamRPCFutureStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new ChronicleDumperRestorerStreamRPCFutureStub(channel, callOptions);
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
    private final ChronicleDumperRestorerStreamRPCImplBase serviceImpl;
    private final int methodId;

    MethodHandlers(ChronicleDumperRestorerStreamRPCImplBase serviceImpl, int methodId) {
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
        default:
          throw new AssertionError();
      }
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public io.grpc.stub.StreamObserver<Req> invoke(
        io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_GET_MESSAGE_STREAM_BY:
          return (io.grpc.stub.StreamObserver<Req>) serviceImpl.getMessageStreamBy(
              (io.grpc.stub.StreamObserver<one.inve.localfullnode2.chronicle.rpc.ByteStream>) responseObserver);
        case METHODID_PERSIST:
          return (io.grpc.stub.StreamObserver<Req>) serviceImpl.persist(
              (io.grpc.stub.StreamObserver<com.google.protobuf.Empty>) responseObserver);
        case METHODID_PERSIST_SYS:
          return (io.grpc.stub.StreamObserver<Req>) serviceImpl.persistSys(
              (io.grpc.stub.StreamObserver<com.google.protobuf.Empty>) responseObserver);
        default:
          throw new AssertionError();
      }
    }
  }

  private static abstract class ChronicleDumperRestorerStreamRPCBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    ChronicleDumperRestorerStreamRPCBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return one.inve.localfullnode2.chronicle.rpc.Chronicle.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("ChronicleDumperRestorerStreamRPC");
    }
  }

  private static final class ChronicleDumperRestorerStreamRPCFileDescriptorSupplier
      extends ChronicleDumperRestorerStreamRPCBaseDescriptorSupplier {
    ChronicleDumperRestorerStreamRPCFileDescriptorSupplier() {}
  }

  private static final class ChronicleDumperRestorerStreamRPCMethodDescriptorSupplier
      extends ChronicleDumperRestorerStreamRPCBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final String methodName;

    ChronicleDumperRestorerStreamRPCMethodDescriptorSupplier(String methodName) {
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
      synchronized (ChronicleDumperRestorerStreamRPCGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new ChronicleDumperRestorerStreamRPCFileDescriptorSupplier())
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

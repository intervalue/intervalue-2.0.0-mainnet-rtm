package one.inve.localfullnode2.chronicle.rpc.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.ByteString;
import com.google.protobuf.Empty;

import io.grpc.stub.StreamObserver;
import one.inve.localfullnode2.chronicle.rpc.ByteStream;
import one.inve.localfullnode2.chronicle.rpc.ChronicleDumperRestorerStreamRPCGrpc.ChronicleDumperRestorerStreamRPCImplBase;
import one.inve.localfullnode2.chronicle.rpc.StringArray;
import one.inve.localfullnode2.chronicle.rpc.StringArray.Builder;

/**
 * 
 * Copyright © INVE FOUNDATION. All rights reserved.
 * 
 * @ClassName: ChronicleDumperRestorerStreamRPCService
 * @Description: stream-style services
 * @author Francis.Deng [francis_xiiiv@163.com]
 * @date Jan 14, 2020
 *
 */
public class ChronicleDumperRestorerStreamRPCService extends ChronicleDumperRestorerStreamRPCImplBase {
	private static final Logger logger = LoggerFactory.getLogger("ChronicleDumperRestorerSteamRPCService");

	private IServicesRuntime runtime;
	private boolean closeRestorer; // determine whether restorer services are open or not

	public ChronicleDumperRestorerStreamRPCService(IServicesRuntime runtime) {
		this(runtime, true);
	}

	public ChronicleDumperRestorerStreamRPCService(IServicesRuntime runtime, boolean closeRestorer) {
		super();
		this.runtime = runtime;
		this.closeRestorer = closeRestorer;
	}

	@Override
	public void getMessageHashes(Empty request, StreamObserver<StringArray> responseObserver) {
		// super.getMessageHashes(request, responseObserver);
		Iterable<String> messageHashes = runtime.messageHashesIter();
		hashesLoop(messageHashes, responseObserver);
	}

	@Override
	public void getSysMessageHashes(Empty request, StreamObserver<StringArray> responseObserver) {
		// super.getSysMessageHashes(request, responseObserver);
		Iterable<String> sysMessageHashes = runtime.sysMessageHashesIter();
		hashesLoop(sysMessageHashes, responseObserver);
	}

	@Override
	public StreamObserver<ByteStream> persist(StreamObserver<Empty> responseObserver) {

		if (closeRestorer) {
			return super.persist(responseObserver);
		}

		return new StreamObserver<ByteStream>() {

			@Override
			public void onCompleted() {
				logger.info("message persistence process is over");
				// responseObserver.onNext(null);
				responseObserver.onCompleted();
				// responseObserver.onNext(Empty.getDefaultInstance());

			}

			@Override
			public void onError(Throwable t) {
				logger.error("message persistence error: {}", t);
				t.printStackTrace();
				responseObserver.onError(t);

			}

			@Override
			public void onNext(ByteStream value) {
				int count = value.getDataCount();
				for (int index = 0; index < count; index++) {
					ByteString byteString = value.getData(index);
					runtime.getMessagePersister().persist(byteString.toByteArray());
				}

				responseObserver.onNext(Empty.getDefaultInstance());

			}

		};
	}

	@Override
	public StreamObserver<ByteStream> persistSys(StreamObserver<Empty> responseObserver) {

		if (closeRestorer) {
			return super.persistSys(responseObserver);
		}

		return new StreamObserver<ByteStream>() {

			@Override
			public void onCompleted() {
				logger.info("sysmessage persistence process is over");
				responseObserver.onCompleted();

			}

			@Override
			public void onError(Throwable t) {
				logger.error("sysmessage persistence error: {}", t);
				// responseObserver.onError(t);

			}

			@Override
			public void onNext(ByteStream value) {
				int count = value.getDataCount();
				for (int index = 0; index < count; index++) {
					ByteString byteString = value.getData(index);
					runtime.getMessagePersister().persistSys(byteString.toByteArray());
				}

				responseObserver.onNext(Empty.getDefaultInstance());

			}

		};
	}

	protected void hashesLoop(Iterable<String> hashIter, StreamObserver<StringArray> responseObserver) {
		for (String mh : hashIter) {
			StringArray sa = null;
			try {
//				byte[] mhBytes = mh.getBytes();
//				ByteBuffer bytesBuffer = ByteBuffer.wrap(mhBytes);

				Builder b = StringArray.newBuilder();
				b.addData(mh);
				sa = b.build();

				// sa = StringArray.parseFrom(bytesBuffer);
			} catch (Exception e) {
				logger.error("[{}] parsing error: {}", mh, e.toString());
				responseObserver.onError(e);

				break;
			}
			responseObserver.onNext(sa);
		}

		responseObserver.onCompleted();
	}

}

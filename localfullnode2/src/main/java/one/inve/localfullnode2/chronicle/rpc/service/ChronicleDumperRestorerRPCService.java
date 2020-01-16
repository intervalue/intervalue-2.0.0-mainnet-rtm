package one.inve.localfullnode2.chronicle.rpc.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.ByteString;

import io.grpc.stub.StreamObserver;
import one.inve.localfullnode2.chronicle.rpc.ByteStream;
import one.inve.localfullnode2.chronicle.rpc.ByteStream.Builder;
import one.inve.localfullnode2.chronicle.rpc.ChronicleDumperRestorerRPCGrpc.ChronicleDumperRestorerRPCImplBase;
import one.inve.localfullnode2.chronicle.rpc.StringArray;
import one.inve.localfullnode2.chronicle.rpc.service.IServicesRuntime.IMessageQuery;

/**
 * 
 * Copyright © INVE FOUNDATION. All rights reserved.
 * 
 * @ClassName: ChronicleDumperRestorerRPCService
 * @Description: non stream-style services
 * @author Francis.Deng [francis_xiiiv@163.com]
 * @date Jan 14, 2020
 *
 */
public class ChronicleDumperRestorerRPCService extends ChronicleDumperRestorerRPCImplBase {
	private static final Logger logger = LoggerFactory.getLogger("ChronicleDumperRestorerRPCService");

	private IServicesRuntime runtime;
	private boolean closeRestorer; // determine whether restorer services are open or not

	public ChronicleDumperRestorerRPCService(IServicesRuntime runtime) {
		this(runtime, true);
	}

	public ChronicleDumperRestorerRPCService(IServicesRuntime runtime, boolean closeRestorer) {
		super();
		this.runtime = runtime;
		this.closeRestorer = closeRestorer;
	}

	@Override
	public void getMessageStreamBy(StringArray request, StreamObserver<ByteStream> responseObserver) {
		/**
		 * // super.getMessageStreamBy(request, responseObserver); IMessageQuery query =
		 * runtime.getMessageQuery(); // ArrayList<byte[]> messagesList = new
		 * ArrayList<byte[]>(); for (int i = 0; i < request.getDataList().size(); i++) {
		 * ByteStream byteStream = null; byte[] messageOrSystemMessageBytes =
		 * query.byHash(request.getData(i)); if (messageOrSystemMessageBytes != null &&
		 * messageOrSystemMessageBytes.length != 0) { //
		 * messagesList.add(messageOrSystemMessageBytes); try { // ByteStream
		 * bytesStream = ByteStream.parseFrom(messageOrSystemMessageBytes); Builder b =
		 * ByteStream.newBuilder();
		 * 
		 * b.addData(ByteString.copyFrom(messageOrSystemMessageBytes)); byteStream =
		 * b.build();
		 * 
		 * } catch (Exception e) { responseObserver.onError(e); break; }
		 * 
		 * responseObserver.onNext(byteStream); } }
		 * 
		 * responseObserver.onCompleted();
		 */

		IMessageQuery query = runtime.getMessageQuery();
		Builder b = ByteStream.newBuilder();
		ByteStream byteStream = null;
		for (int i = 0; i < request.getDataList().size(); i++) {

			byte[] messageOrSystemMessageBytes = query.byHash(request.getData(i));
			if (messageOrSystemMessageBytes != null && messageOrSystemMessageBytes.length != 0) {
				try {
					b.addData(ByteString.copyFrom(messageOrSystemMessageBytes));
				} catch (Exception e) {
					responseObserver.onError(e);
					break;
				}
			}
		}
		byteStream = b.build();
		responseObserver.onNext(byteStream);

		responseObserver.onCompleted();
	}

}

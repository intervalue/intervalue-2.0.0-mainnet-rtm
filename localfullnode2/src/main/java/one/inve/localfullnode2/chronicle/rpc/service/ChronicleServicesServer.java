package one.inve.localfullnode2.chronicle.rpc.service;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.grpc.Server;
import io.grpc.ServerBuilder;

/**
 * 
 * Copyright © INVE FOUNDATION. All rights reserved.
 * 
 * @ClassName: ChronicleServicesServer
 * @Description: ChronicleServices Server that serve the
 *               {@code ChronicleDumperRestorerRPCService} and
 *               {@code ChronicleDumperRestorerSteamRPCService}
 * @author Francis.Deng [francis_xiiiv@163.com]
 * @date Jan 9, 2020
 *
 */
public class ChronicleServicesServer {
	private static final Logger logger = LoggerFactory.getLogger("ChronicleServicesMgr");

	private final int port;
	private final Server server;
	private final boolean closeRestorer;

	/**
	 * Create a ChronicleServices server listening on {@code port} using
	 * {@code IServicesRuntime}.
	 */
	public ChronicleServicesServer(int port, IServicesRuntime runtime) throws IOException {
		this(ServerBuilder.forPort(port), port, runtime);
	}

	public ChronicleServicesServer(int port, IServicesRuntime runtime, boolean closeRestorer) throws IOException {
		this(ServerBuilder.forPort(port), port, runtime, closeRestorer);
	}

	/**
	 * Create a ChronicleServices server using IServicesRuntime without persistence
	 * services
	 */
	public ChronicleServicesServer(ServerBuilder<?> serverBuilder, int port, IServicesRuntime runtime) {
		this.port = port;
		this.closeRestorer = true;
		serverBuilder.addService(new ChronicleDumperRestorerRPCService(runtime, this.closeRestorer));
		serverBuilder.addService(new ChronicleDumperRestorerStreamRPCService(runtime, this.closeRestorer));
		server = serverBuilder.build();
	}

	/**
	 * Create a ChronicleServices server using IServicesRuntime
	 */
	public ChronicleServicesServer(ServerBuilder<?> serverBuilder, int port, IServicesRuntime runtime,
			boolean closeRestorer) {
		this.port = port;
		this.closeRestorer = closeRestorer;
		serverBuilder.addService(new ChronicleDumperRestorerRPCService(runtime, closeRestorer));
		serverBuilder.addService(new ChronicleDumperRestorerStreamRPCService(runtime, closeRestorer));
		server = serverBuilder.build();
	}

	/** Start serving requests. */
	public void startUntilShutdown() throws IOException {
		server.start();
		logger.info("Services started, listening on " + port);
		try {
			blockUntilShutdown();
		} catch (InterruptedException e) {
			logger.error("block shutdown err: %s", e);
			e.printStackTrace();
		}
	}

	/** Stop serving requests and shutdown resources. */
	public void shutdown() throws InterruptedException {
		if (server != null) {
			server.shutdown().awaitTermination(30, TimeUnit.SECONDS);
		}
	}

	/**
	 * Await termination on the main thread since the grpc library uses daemon
	 * threads.
	 */
	private void blockUntilShutdown() throws InterruptedException {
		if (server != null) {
			server.awaitTermination();
		}
	}
}

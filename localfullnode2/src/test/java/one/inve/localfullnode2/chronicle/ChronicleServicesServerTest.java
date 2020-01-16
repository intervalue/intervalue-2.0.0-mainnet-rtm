package one.inve.localfullnode2.chronicle;

import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import one.inve.localfullnode2.chronicle.rpc.service.ChronicleServicesServer;
import one.inve.localfullnode2.chronicle.rpc.service.IServicesRuntime;

/**
 * 
 * Copyright © INVE FOUNDATION. All rights reserved.
 * 
 * @ClassName: ChronicleServicesServerTest
 * @Description: try to prove the communication mechanism is all right,along
 *               with {@code chronicle_rpc_test.go}
 * @author Francis.Deng [francis_xiiiv@163.com]
 * @date Jan 13, 2020
 *
 */
public class ChronicleServicesServerTest {
	private static final Logger logger = LoggerFactory.getLogger("ChronicleServicesServerTest");

	@Test
	public void testHowToStartChronicleServicesServer() {
		IServicesRuntime serviceRuntime = new IServicesRuntime() {

			@Override
			public Iterable<String> messageHashesIter() {
				List<String> jdks = Arrays.asList("JDK6", "JDK8", "JDK10");
				return new Iterable<String>() {
					@Override
					public Iterator<String> iterator() {
						return jdks.iterator();
					}
				};
			}

			@Override
			public Iterable<String> sysMessageHashesIter() {
				List<String> jets = Arrays.asList("F15", "F16", "F22");
				return new Iterable<String>() {
					@Override
					public Iterator<String> iterator() {
						return jets.iterator();
					}
				};
			}

			@Override
			public IMessageQuery getMessageQuery() {
				return new IMessageQuery() {
					@Override
					public byte[] byHash(String hash) {
						return hash.getBytes();
					}
				};
			}

			@Override
			public IMessagePersister getMessagePersister() {
				// TODO Auto-generated method stub
				return new IMessagePersister() {

					@Override
					public void persist(byte[] message) {
						logger.info("check: ready to persist '{}'", new String(message));
					}

					@Override
					public void persistSys(byte[] sysMessage) {
						logger.info("check: ready to persist sys '{}'", new String(sysMessage));
					}

				};
			}

		};

		try {
			ChronicleServicesServer chronicleServicesServer = new ChronicleServicesServer(8980, serviceRuntime);
			chronicleServicesServer.startUntilShutdown();
		} catch (IOException e) {
			logger.error(e.toString());
			e.printStackTrace();
		}
	}
}

package one.inve.localfullnode2.chronicle;

import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;

import one.inve.localfullnode2.chronicle.rpc.RawWrappedMessage;
import one.inve.localfullnode2.chronicle.rpc.WrappedMessage;
import one.inve.localfullnode2.chronicle.rpc.service.ChronicleServicesRuntime.MessageResolver;
import one.inve.localfullnode2.chronicle.rpc.service.ChronicleServicesServer;
import one.inve.localfullnode2.chronicle.rpc.service.IServicesRuntime;

/**
 * 
 * Copyright © INVE FOUNDATION. All rights reserved.
 * 
 * @ClassName: ChronicleDumperRestorerEmulatorTest
 * @Description: introduce {@code WrappedMessage} to normalize message,system
 *               message or others.Similar to
 *               {@code ChronicleServicesServerTest}
 * @author Francis.Deng [francis_xiiiv@163.com]
 * @date Jan 15, 2020
 *
 */
public class ChronicleDumperRestorerEmulatorTest {
	private static final Logger logger = LoggerFactory.getLogger("ChronicleDumperRestorerEmulatorTest");

	// @Test
	public void testChronicleDumperAndRestorer0() {
		IServicesRuntime serviceRuntime = new IServicesRuntime() {

			@Override
			public Iterable<String> messageHashesIter() {
				List<String> jdks = Arrays.asList("msgJDK0", "msgJDK1", "msgJDK2", "msgJDK3", "msgJDK4", "msgJDK5",
						"msgJDK6", "msgJDK8", "msgJDK10", "msgJDK11", "msgJDK12");
				return new Iterable<String>() {
					@Override
					public Iterator<String> iterator() {
						return jdks.iterator();
					}
				};
			}

			@Override
			public Iterable<String> sysMessageHashesIter() {
				List<String> jets = Arrays.asList("smsgF15");
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
						String genuineHash;
						WrappedMessage wrappedMessage;

						if (hash.startsWith("msg")) {
							genuineHash = hash.substring("msg".length());
							wrappedMessage = WrappedMessage.newBuilder().setMessageType(1)
									.setMessageBody(ByteString.copyFrom(genuineHash.getBytes())).build();
						} else if (hash.startsWith("smsg")) {
							genuineHash = hash.substring("smsg".length());
							wrappedMessage = WrappedMessage.newBuilder().setMessageType(2)
									.setMessageBody(ByteString.copyFrom(genuineHash.getBytes())).build();
						} else {
							genuineHash = "nothing";
							wrappedMessage = WrappedMessage.newBuilder().setMessageType(0)
									.setMessageBody(ByteString.copyFrom(genuineHash.getBytes())).build();
						}

						return wrappedMessage.toByteArray();
					}
				};
			}

			@Override
			public IMessagePersister getMessagePersister() {
				// TODO Auto-generated method stub
				return new IMessagePersister() {

					@Override
					public void persist(byte[] wrappedMessageBytes) {
						WrappedMessage wrappedMessage;
						try {
							wrappedMessage = WrappedMessage.parseFrom(wrappedMessageBytes);
							logger.info("check: ready to persist wrappedMessage body '<{}>'",
									new String(wrappedMessage.getMessageBody().toByteArray()));
						} catch (InvalidProtocolBufferException e) {
							logger.error("error in WrappedMessage.parseFrom({%s})", wrappedMessageBytes);
							e.printStackTrace();
						}

					}

					@Override
					public void persistSys(byte[] wrappedMessageBytes) {
						throw new RuntimeException("Deprecated Method");
					}

				};
			}

		};

		try {
			ChronicleServicesServer chronicleServicesServer = new ChronicleServicesServer(8980, serviceRuntime, false);
			chronicleServicesServer.startUntilShutdown();
		} catch (IOException e) {
			logger.error(e.toString());
			e.printStackTrace();
		}
	}

	/**
	 * use WrappedMessage
	 */
	@Test
	public void testChronicleDumperAndRestorer1() {
		IServicesRuntime serviceRuntime = new IServicesRuntime() {

			@Override
			public Iterable<String> messageHashesIter() {
				List<String> jdks = Arrays.asList("JDK0",
						"SUN-JDK1"/*
									 * , "JDK2", "JDK3", "JDK4", "JDK5", "JDK6", "JDK8", "JDK10", "JDK11", "JDK12"
									 */);

				String[] msgHashes = new String[jdks.size()];
				for (int index = 0; index < jdks.size(); index++) {
					MessageResolver mResolver = new MessageResolver();
					mResolver.setHash(jdks.get(index)).setType(MessageResolver.MsgType);

					msgHashes[index] = mResolver.toTaggedHash();

				}

				return () -> {
					List<String> list = Arrays.asList(msgHashes);
					return list.iterator();
				};
			}

			@Override
			public Iterable<String> sysMessageHashesIter() {
				List<String> jets = Arrays.asList("F15");

				String[] sysMsgHashes = new String[jets.size()];
				for (int index = 0; index < jets.size(); index++) {
					MessageResolver mResolver = new MessageResolver();
					mResolver.setHash(jets.get(index)).setType(MessageResolver.SysMsgType);

					sysMsgHashes[index] = mResolver.toTaggedHash();

				}

				return () -> {
					List<String> list = Arrays.asList(sysMsgHashes);
					return list.iterator();
				};
			}

			@Override
			public IMessageQuery getMessageQuery() {
				return (hash) -> {
					MessageResolver mResolver = MessageResolver.parse(hash);
					String s = null;

					// simply process the message
					if (mResolver.type() == MessageResolver.MsgType) {
						s = String.format("%s,message body", mResolver.hash());
					} else if (mResolver.type() == MessageResolver.SysMsgType) {
						s = String.format("%s,system message body", mResolver.hash());
					}

					WrappedMessage wrappedMessage = WrappedMessage.newBuilder().setMessageType(mResolver.type())
							.setMessageBody(ByteString.copyFrom(s.getBytes())).build();

					return wrappedMessage.toByteArray();
				};
			}

			@Override
			public IMessagePersister getMessagePersister() {
				// TODO Auto-generated method stub
				return new IMessagePersister() {

					@Override
					public void persist(byte[] wrappedMessageBytes) {
						WrappedMessage wrappedMessage;
						try {
							wrappedMessage = WrappedMessage.parseFrom(wrappedMessageBytes);
							logger.info("check: ready to persist wrappedMessage body '<{}>'",
									new String(wrappedMessage.getMessageBody().toByteArray()));
						} catch (InvalidProtocolBufferException e) {
							logger.error("error in WrappedMessage.parseFrom({%s})", wrappedMessageBytes);
							e.printStackTrace();
						}

					}

					@Override
					public void persistSys(byte[] wrappedMessageBytes) {
						throw new RuntimeException("Deprecated Method");
					}

				};
			}

		};

		try {
			ChronicleServicesServer chronicleServicesServer = new ChronicleServicesServer(8980, serviceRuntime, false);
			chronicleServicesServer.startUntilShutdown();
		} catch (IOException e) {
			logger.error(e.toString());
			e.printStackTrace();
		}
	}

	/**
	 * use RawWrappedMessage
	 */
	// @Test
	public void testChronicleDumperAndRestorer2() {
		IServicesRuntime serviceRuntime = new IServicesRuntime() {

			@Override
			public Iterable<String> messageHashesIter() {
				List<String> jdks = Arrays.asList("JDK0", "JDK2", "JDK3", "JDK4", "JDK5", "JDK6", "JDK8", "JDK10",
						"JDK11", "JDK12");

				String[] msgHashes = new String[jdks.size()];
				for (int index = 0; index < jdks.size(); index++) {
					MessageResolver mResolver = new MessageResolver();
					mResolver.setHash(jdks.get(index)).setType(MessageResolver.MsgType);

					msgHashes[index] = mResolver.toTaggedHash();

				}

				return () -> {
					List<String> list = Arrays.asList(msgHashes);
					return list.iterator();
				};
			}

			@Override
			public Iterable<String> sysMessageHashesIter() {
				List<String> jets = Arrays.asList("F15");

				String[] sysMsgHashes = new String[jets.size()];
				for (int index = 0; index < jets.size(); index++) {
					MessageResolver mResolver = new MessageResolver();
					mResolver.setHash(jets.get(index)).setType(MessageResolver.SysMsgType);

					sysMsgHashes[index] = mResolver.toTaggedHash();

				}

				return () -> {
					List<String> list = Arrays.asList(sysMsgHashes);
					return list.iterator();
				};
			}

			@Override
			public IMessageQuery getMessageQuery() {
				return (hash) -> {
					MessageResolver mResolver = MessageResolver.parse(hash);
					String s = null;

					// simply process the message
					if (mResolver.type() == MessageResolver.MsgType) {
						s = String.format("%s,message body", mResolver.hash());
					} else if (mResolver.type() == MessageResolver.SysMsgType) {
						s = String.format("%s,system message body", mResolver.hash());
					}

					RawWrappedMessage rawWrappedMessage = RawWrappedMessage.Builder.newBuilder()
							.setMessageType(mResolver.type()).setMessageBody(s).build();

					return rawWrappedMessage.serialize();
				};
			}

			@Override
			public IMessagePersister getMessagePersister() {
				// TODO Auto-generated method stub
				return new IMessagePersister() {

					@Override
					public void persist(byte[] rawWrappedMessageBytes) {
						RawWrappedMessage rawWrappedMessage;
						rawWrappedMessage = RawWrappedMessage.Builder.newBuilder().build(rawWrappedMessageBytes);
						logger.info("check: ready to persist wrappedMessage body '<{}> <{}>'",
								rawWrappedMessage.getMessageBody(), rawWrappedMessage.getMessageType());

					}

					@Override
					public void persistSys(byte[] wrappedMessageBytes) {
						throw new RuntimeException("Deprecated Method");
					}

				};
			}

		};

		try {
			ChronicleServicesServer chronicleServicesServer = new ChronicleServicesServer(8980, serviceRuntime, false);
			chronicleServicesServer.startUntilShutdown();
		} catch (IOException e) {
			logger.error(e.toString());
			e.printStackTrace();
		}
	}
}

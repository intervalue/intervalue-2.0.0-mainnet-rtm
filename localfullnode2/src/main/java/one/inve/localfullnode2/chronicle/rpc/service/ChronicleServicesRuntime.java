package one.inve.localfullnode2.chronicle.rpc.service;

import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;

import one.inve.localfullnode2.chronicle.rpc.WrappedMessage;
import one.inve.localfullnode2.dep.DepItemsManager;
import one.inve.localfullnode2.message.MessagePersistence;
import one.inve.localfullnode2.message.MessagePersistenceDependency;
import one.inve.localfullnode2.sync.msg.MsgIntrospector;
import one.inve.localfullnode2.sync.msg.MsgIntrospectorDependency;
import one.inve.localfullnode2.sync.msg.MsgIntrospectorDependent;

/**
 * 
 * Copyright © INVE FOUNDATION. All rights reserved.
 * 
 * @ClassName: ChronicleServicesRuntime
 * @Description: overlap between chronicle and localfullnode2
 * @author Francis.Deng [francis_xiiiv@163.com]
 * @date Jan 15, 2020
 *
 */
public class ChronicleServicesRuntime implements IServicesRuntime {
	private static final Logger logger = LoggerFactory.getLogger(ChronicleServicesRuntime.class);

	private MsgIntrospector introspector;

	private void initInternalReference() {
		MsgIntrospectorDependent dep = DepItemsManager.getInstance().getItemConcerned(MsgIntrospectorDependency.class);
		introspector = new MsgIntrospector(dep);
	}

	public ChronicleServicesRuntime() {
		initInternalReference();
	}

	/**
	 * Note:hash(returned) is a processed hash with {@code MsgTag}
	 */
	@Override
	public Iterable<String> messageHashesIter() {
		String[] msgHashes = introspector.getMsgHashes((hash) -> {
			MessageResolver mResolver = new MessageResolver();
			mResolver.setHash(hash).setType(MessageResolver.MsgType);

			return mResolver.toTaggedHash();
		});

		return () -> {
			List<String> list = Arrays.asList(msgHashes);
			return list.iterator();
		};
	}

	/**
	 * Note:hash(returned) is a processed hash with {@code SysMsgType}
	 */
	@Override
	public Iterable<String> sysMessageHashesIter() {
		String[] sysMsgHashes = introspector.getSysMsgHashes((hash) -> {
			MessageResolver mResolver = new MessageResolver();
			mResolver.setHash(hash).setType(MessageResolver.SysMsgType);

			return mResolver.toTaggedHash();
		});

		return () -> {
//			ArrayList<String> al = new ArrayList<>(sysMsgHashes.length);
//			return al.iterator();

			List<String> list = Arrays.asList(sysMsgHashes);
			return list.iterator();
		};
	}

	/**
	 * Note:message(returned) is WrappedMessage bytes instead of original message
	 * body
	 */
	@Override
	public IMessageQuery getMessageQuery() {
		return (hash) -> {
			MessageResolver mResolver = MessageResolver.parse(hash);
			byte[] oBodyBytes = introspector.getMessageBytes(mResolver.hash());

			if (oBodyBytes == null || oBodyBytes.length <= 0) {
				logger.error("cannot find message body by hash[{}]", hash);
				return null;
			}
			WrappedMessage wrappedMessage = WrappedMessage.newBuilder().setMessageType(mResolver.type())
					.setMessageBody(ByteString.copyFrom(oBodyBytes)).build();

			return wrappedMessage.toByteArray();
		};
	}

	/**
	 * trace the usage of {@code MessagePersistence.persisMessages} and
	 * {@code MessagePersistence.persistSystemMessages} in a reverse way
	 */
	@Override
	public IMessagePersister getMessagePersister() {
		return new IMessagePersister() {

			@Override
			public void persist(byte[] wrappedMessageBytes) {
				WrappedMessage wrappedMessage = null;

				MessagePersistenceDependency messagePersistenceDependency = DepItemsManager.getInstance()
						.getItemConcerned(MessagePersistenceDependency.class);
				MessagePersistence messagePersistence = new MessagePersistence(messagePersistenceDependency);

				try {
					wrappedMessage = WrappedMessage.parseFrom(wrappedMessageBytes);
				} catch (InvalidProtocolBufferException e) {
					logger.error("error in WrappedMessage.parseFrom: {}", e);
					e.printStackTrace();
					return;
				}

				byte[] messageBodyBytes = wrappedMessage.getMessageBody().toByteArray();
				if (wrappedMessage.getMessageType() == MessageResolver.MsgType) {
					JSONObject jo = InternalMessageAwareness.fromMessageBytesToJSONObject(messageBodyBytes);

					messagePersistenceDependency.getConsMessageSaveQueue().add(jo);
					messagePersistence.persisMessages();
				} else if (wrappedMessage.getMessageType() == MessageResolver.SysMsgType) {
					JSONObject jo = InternalMessageAwareness.fromSysMessageBytesToJSONObject(messageBodyBytes);

					try {
						messagePersistenceDependency.getSystemAutoTxSaveQueue().put(jo);
					} catch (InterruptedException e) {
						logger.error("error in messagePersistenceDependency.getSystemAutoTxSaveQueue(): {}", e);
						e.printStackTrace();
						return;
					}
					messagePersistence.persistSystemMessages();
				}

			}

			@Override
			public void persistSys(byte[] wrappedMessageBytes) {
				throw new RuntimeException("Deprecated method");
			}
		};
	}

	/**
	 * wrap message into a tagged message or resolve tagged into original message
	 */
	public static class MessageResolver {
		public static String MsgTag = "msg";
		public static String SysMsgTag = "smsg";
		public static int MsgType = 1;
		public static int SysMsgType = 2;

		private String hash;
		private int type;

		public String toTaggedHash() {
			if (type == SysMsgType) {
				return SysMsgTag + hash;
			} else {
				return MsgTag + hash;
			}
		}

		public static MessageResolver parse(String taggedHash) {
			MessageResolver mResolver = new MessageResolver();
			String oHash;
			int type;
			if (taggedHash.startsWith(SysMsgTag)) {
				oHash = taggedHash.substring(SysMsgTag.length());
				type = SysMsgType;
			} else {
				oHash = taggedHash.substring(MsgTag.length());
				type = MsgType;
			}

			return mResolver.setHash(oHash).setType(type);
		}

		public String hash() {
			return hash;
		}

		public MessageResolver setHash(String hash) {
			this.hash = hash;
			return this;
		}

		public int type() {
			return type;
		}

		public MessageResolver setType(int type) {
			this.type = type;
			return this;
		}

	}
}

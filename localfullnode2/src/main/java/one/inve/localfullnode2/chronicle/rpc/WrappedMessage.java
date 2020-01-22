// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: misc.proto

package one.inve.localfullnode2.chronicle.rpc;

/**
 * <pre>
 *TODO: attach signature to WrappedMessage
 * </pre>
 *
 * Protobuf type {@code rpc.WrappedMessage}
 */
public final class WrappedMessage extends com.google.protobuf.GeneratedMessageV3 implements
		// @@protoc_insertion_point(message_implements:rpc.WrappedMessage)
		WrappedMessageOrBuilder {
	private static final long serialVersionUID = 0L;

	// Use WrappedMessage.newBuilder() to construct.
	private WrappedMessage(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
		super(builder);
	}

	private WrappedMessage() {
		messageBody_ = com.google.protobuf.ByteString.EMPTY;
	}

	@SuppressWarnings({ "unused" })
	protected java.lang.Object newInstance(UnusedPrivateParameter unused) {
		return new WrappedMessage();
	}

	public final com.google.protobuf.UnknownFieldSet getUnknownFields() {
		return this.unknownFields;
	}

	private WrappedMessage(com.google.protobuf.CodedInputStream input,
			com.google.protobuf.ExtensionRegistryLite extensionRegistry)
			throws com.google.protobuf.InvalidProtocolBufferException {
		this();
		if (extensionRegistry == null) {
			throw new java.lang.NullPointerException();
		}
		com.google.protobuf.UnknownFieldSet.Builder unknownFields = com.google.protobuf.UnknownFieldSet.newBuilder();
		try {
			boolean done = false;
			while (!done) {
				int tag = input.readTag();
				switch (tag) {
				case 0:
					done = true;
					break;
				case 8: {

					messageType_ = input.readInt32();
					break;
				}
				case 18: {

					messageBody_ = input.readBytes();
					break;
				}
				default: {
					if (!parseUnknownField(input, unknownFields, extensionRegistry, tag)) {
						done = true;
					}
					break;
				}
				}
			}
		} catch (com.google.protobuf.InvalidProtocolBufferException e) {
			throw e.setUnfinishedMessage(this);
		} catch (java.io.IOException e) {
			throw new com.google.protobuf.InvalidProtocolBufferException(e).setUnfinishedMessage(this);
		} finally {
			this.unknownFields = unknownFields.build();
			makeExtensionsImmutable();
		}
	}

	public static final com.google.protobuf.Descriptors.Descriptor getDescriptor() {
		return one.inve.localfullnode2.chronicle.rpc.Misc.internal_static_rpc_WrappedMessage_descriptor;
	}

	protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable internalGetFieldAccessorTable() {
		return one.inve.localfullnode2.chronicle.rpc.Misc.internal_static_rpc_WrappedMessage_fieldAccessorTable
				.ensureFieldAccessorsInitialized(one.inve.localfullnode2.chronicle.rpc.WrappedMessage.class,
						one.inve.localfullnode2.chronicle.rpc.WrappedMessage.Builder.class);
	}

	public static final int MESSAGETYPE_FIELD_NUMBER = 1;
	private int messageType_;

	/**
	 * <code>int32 messageType = 1;</code>
	 * 
	 * @return The messageType.
	 */
	public int getMessageType() {
		return messageType_;
	}

	public static final int MESSAGEBODY_FIELD_NUMBER = 2;
	private com.google.protobuf.ByteString messageBody_;

	/**
	 * <code>bytes messageBody = 2;</code>
	 * 
	 * @return The messageBody.
	 */
	public com.google.protobuf.ByteString getMessageBody() {
		return messageBody_;
	}

	private byte memoizedIsInitialized = -1;

	public final boolean isInitialized() {
		byte isInitialized = memoizedIsInitialized;
		if (isInitialized == 1)
			return true;
		if (isInitialized == 0)
			return false;

		memoizedIsInitialized = 1;
		return true;
	}

	public void writeTo(com.google.protobuf.CodedOutputStream output) throws java.io.IOException {
		if (messageType_ != 0) {
			output.writeInt32(1, messageType_);
		}
		if (!messageBody_.isEmpty()) {
			output.writeBytes(2, messageBody_);
		}
		unknownFields.writeTo(output);
	}

	public int getSerializedSize() {
		int size = memoizedSize;
		if (size != -1)
			return size;

		size = 0;
		if (messageType_ != 0) {
			size += com.google.protobuf.CodedOutputStream.computeInt32Size(1, messageType_);
		}
		if (!messageBody_.isEmpty()) {
			size += com.google.protobuf.CodedOutputStream.computeBytesSize(2, messageBody_);
		}
		size += unknownFields.getSerializedSize();
		memoizedSize = size;
		return size;
	}

	public boolean equals(final java.lang.Object obj) {
		if (obj == this) {
			return true;
		}
		if (!(obj instanceof one.inve.localfullnode2.chronicle.rpc.WrappedMessage)) {
			return super.equals(obj);
		}
		one.inve.localfullnode2.chronicle.rpc.WrappedMessage other = (one.inve.localfullnode2.chronicle.rpc.WrappedMessage) obj;

		if (getMessageType() != other.getMessageType())
			return false;
		if (!getMessageBody().equals(other.getMessageBody()))
			return false;
		if (!unknownFields.equals(other.unknownFields))
			return false;
		return true;
	}

	public int hashCode() {
		if (memoizedHashCode != 0) {
			return memoizedHashCode;
		}
		int hash = 41;
		hash = (19 * hash) + getDescriptor().hashCode();
		hash = (37 * hash) + MESSAGETYPE_FIELD_NUMBER;
		hash = (53 * hash) + getMessageType();
		hash = (37 * hash) + MESSAGEBODY_FIELD_NUMBER;
		hash = (53 * hash) + getMessageBody().hashCode();
		hash = (29 * hash) + unknownFields.hashCode();
		memoizedHashCode = hash;
		return hash;
	}

	public static one.inve.localfullnode2.chronicle.rpc.WrappedMessage parseFrom(java.nio.ByteBuffer data)
			throws com.google.protobuf.InvalidProtocolBufferException {
		return PARSER.parseFrom(data);
	}

	public static one.inve.localfullnode2.chronicle.rpc.WrappedMessage parseFrom(java.nio.ByteBuffer data,
			com.google.protobuf.ExtensionRegistryLite extensionRegistry)
			throws com.google.protobuf.InvalidProtocolBufferException {
		return PARSER.parseFrom(data, extensionRegistry);
	}

	public static one.inve.localfullnode2.chronicle.rpc.WrappedMessage parseFrom(com.google.protobuf.ByteString data)
			throws com.google.protobuf.InvalidProtocolBufferException {
		return PARSER.parseFrom(data);
	}

	public static one.inve.localfullnode2.chronicle.rpc.WrappedMessage parseFrom(com.google.protobuf.ByteString data,
			com.google.protobuf.ExtensionRegistryLite extensionRegistry)
			throws com.google.protobuf.InvalidProtocolBufferException {
		return PARSER.parseFrom(data, extensionRegistry);
	}

	public static one.inve.localfullnode2.chronicle.rpc.WrappedMessage parseFrom(byte[] data)
			throws com.google.protobuf.InvalidProtocolBufferException {
		return PARSER.parseFrom(data);
	}

	public static one.inve.localfullnode2.chronicle.rpc.WrappedMessage parseFrom(byte[] data,
			com.google.protobuf.ExtensionRegistryLite extensionRegistry)
			throws com.google.protobuf.InvalidProtocolBufferException {
		return PARSER.parseFrom(data, extensionRegistry);
	}

	public static one.inve.localfullnode2.chronicle.rpc.WrappedMessage parseFrom(java.io.InputStream input)
			throws java.io.IOException {
		return com.google.protobuf.GeneratedMessageV3.parseWithIOException(PARSER, input);
	}

	public static one.inve.localfullnode2.chronicle.rpc.WrappedMessage parseFrom(java.io.InputStream input,
			com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
		return com.google.protobuf.GeneratedMessageV3.parseWithIOException(PARSER, input, extensionRegistry);
	}

	public static one.inve.localfullnode2.chronicle.rpc.WrappedMessage parseDelimitedFrom(java.io.InputStream input)
			throws java.io.IOException {
		return com.google.protobuf.GeneratedMessageV3.parseDelimitedWithIOException(PARSER, input);
	}

	public static one.inve.localfullnode2.chronicle.rpc.WrappedMessage parseDelimitedFrom(java.io.InputStream input,
			com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
		return com.google.protobuf.GeneratedMessageV3.parseDelimitedWithIOException(PARSER, input, extensionRegistry);
	}

	public static one.inve.localfullnode2.chronicle.rpc.WrappedMessage parseFrom(
			com.google.protobuf.CodedInputStream input) throws java.io.IOException {
		return com.google.protobuf.GeneratedMessageV3.parseWithIOException(PARSER, input);
	}

	public static one.inve.localfullnode2.chronicle.rpc.WrappedMessage parseFrom(
			com.google.protobuf.CodedInputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry)
			throws java.io.IOException {
		return com.google.protobuf.GeneratedMessageV3.parseWithIOException(PARSER, input, extensionRegistry);
	}

	public Builder newBuilderForType() {
		return newBuilder();
	}

	public static Builder newBuilder() {
		return DEFAULT_INSTANCE.toBuilder();
	}

	public static Builder newBuilder(one.inve.localfullnode2.chronicle.rpc.WrappedMessage prototype) {
		return DEFAULT_INSTANCE.toBuilder().mergeFrom(prototype);
	}

	public Builder toBuilder() {
		return this == DEFAULT_INSTANCE ? new Builder() : new Builder().mergeFrom(this);
	}

	protected Builder newBuilderForType(com.google.protobuf.GeneratedMessageV3.BuilderParent parent) {
		Builder builder = new Builder(parent);
		return builder;
	}

	/**
	 * <pre>
	 *TODO: attach signature to WrappedMessage
	 * </pre>
	 *
	 * Protobuf type {@code rpc.WrappedMessage}
	 */
	public static final class Builder extends com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
			// @@protoc_insertion_point(builder_implements:rpc.WrappedMessage)
			one.inve.localfullnode2.chronicle.rpc.WrappedMessageOrBuilder {
		public static final com.google.protobuf.Descriptors.Descriptor getDescriptor() {
			return one.inve.localfullnode2.chronicle.rpc.Misc.internal_static_rpc_WrappedMessage_descriptor;
		}

		protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable internalGetFieldAccessorTable() {
			return one.inve.localfullnode2.chronicle.rpc.Misc.internal_static_rpc_WrappedMessage_fieldAccessorTable
					.ensureFieldAccessorsInitialized(one.inve.localfullnode2.chronicle.rpc.WrappedMessage.class,
							one.inve.localfullnode2.chronicle.rpc.WrappedMessage.Builder.class);
		}

		// Construct using
		// one.inve.localfullnode2.chronicle.rpc.WrappedMessage.newBuilder()
		private Builder() {
			maybeForceBuilderInitialization();
		}

		private Builder(com.google.protobuf.GeneratedMessageV3.BuilderParent parent) {
			super(parent);
			maybeForceBuilderInitialization();
		}

		private void maybeForceBuilderInitialization() {
			if (com.google.protobuf.GeneratedMessageV3.alwaysUseFieldBuilders) {
			}
		}

		public Builder clear() {
			super.clear();
			messageType_ = 0;

			messageBody_ = com.google.protobuf.ByteString.EMPTY;

			return this;
		}

		public com.google.protobuf.Descriptors.Descriptor getDescriptorForType() {
			return one.inve.localfullnode2.chronicle.rpc.Misc.internal_static_rpc_WrappedMessage_descriptor;
		}

		public one.inve.localfullnode2.chronicle.rpc.WrappedMessage getDefaultInstanceForType() {
			return one.inve.localfullnode2.chronicle.rpc.WrappedMessage.getDefaultInstance();
		}

		public one.inve.localfullnode2.chronicle.rpc.WrappedMessage build() {
			one.inve.localfullnode2.chronicle.rpc.WrappedMessage result = buildPartial();
			if (!result.isInitialized()) {
				throw newUninitializedMessageException(result);
			}
			return result;
		}

		public one.inve.localfullnode2.chronicle.rpc.WrappedMessage buildPartial() {
			one.inve.localfullnode2.chronicle.rpc.WrappedMessage result = new one.inve.localfullnode2.chronicle.rpc.WrappedMessage(
					this);
			result.messageType_ = messageType_;
			result.messageBody_ = messageBody_;
			onBuilt();
			return result;
		}

		public Builder clone() {
			return super.clone();
		}

		public Builder setField(com.google.protobuf.Descriptors.FieldDescriptor field, java.lang.Object value) {
			return super.setField(field, value);
		}

		public Builder clearField(com.google.protobuf.Descriptors.FieldDescriptor field) {
			return super.clearField(field);
		}

		public Builder clearOneof(com.google.protobuf.Descriptors.OneofDescriptor oneof) {
			return super.clearOneof(oneof);
		}

		public Builder setRepeatedField(com.google.protobuf.Descriptors.FieldDescriptor field, int index,
				java.lang.Object value) {
			return super.setRepeatedField(field, index, value);
		}

		public Builder addRepeatedField(com.google.protobuf.Descriptors.FieldDescriptor field, java.lang.Object value) {
			return super.addRepeatedField(field, value);
		}

		public Builder mergeFrom(com.google.protobuf.Message other) {
			if (other instanceof one.inve.localfullnode2.chronicle.rpc.WrappedMessage) {
				return mergeFrom((one.inve.localfullnode2.chronicle.rpc.WrappedMessage) other);
			} else {
				super.mergeFrom(other);
				return this;
			}
		}

		public Builder mergeFrom(one.inve.localfullnode2.chronicle.rpc.WrappedMessage other) {
			if (other == one.inve.localfullnode2.chronicle.rpc.WrappedMessage.getDefaultInstance())
				return this;
			if (other.getMessageType() != 0) {
				setMessageType(other.getMessageType());
			}
			if (other.getMessageBody() != com.google.protobuf.ByteString.EMPTY) {
				setMessageBody(other.getMessageBody());
			}
			this.mergeUnknownFields(other.unknownFields);
			onChanged();
			return this;
		}

		public final boolean isInitialized() {
			return true;
		}

		public Builder mergeFrom(com.google.protobuf.CodedInputStream input,
				com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
			one.inve.localfullnode2.chronicle.rpc.WrappedMessage parsedMessage = null;
			try {
				parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
			} catch (com.google.protobuf.InvalidProtocolBufferException e) {
				parsedMessage = (one.inve.localfullnode2.chronicle.rpc.WrappedMessage) e.getUnfinishedMessage();
				throw e.unwrapIOException();
			} finally {
				if (parsedMessage != null) {
					mergeFrom(parsedMessage);
				}
			}
			return this;
		}

		private int messageType_;

		/**
		 * <code>int32 messageType = 1;</code>
		 * 
		 * @return The messageType.
		 */
		public int getMessageType() {
			return messageType_;
		}

		/**
		 * <code>int32 messageType = 1;</code>
		 * 
		 * @param value The messageType to set.
		 * @return This builder for chaining.
		 */
		public Builder setMessageType(int value) {

			messageType_ = value;
			onChanged();
			return this;
		}

		/**
		 * <code>int32 messageType = 1;</code>
		 * 
		 * @return This builder for chaining.
		 */
		public Builder clearMessageType() {

			messageType_ = 0;
			onChanged();
			return this;
		}

		private com.google.protobuf.ByteString messageBody_ = com.google.protobuf.ByteString.EMPTY;

		/**
		 * <code>bytes messageBody = 2;</code>
		 * 
		 * @return The messageBody.
		 */
		public com.google.protobuf.ByteString getMessageBody() {
			return messageBody_;
		}

		/**
		 * <code>bytes messageBody = 2;</code>
		 * 
		 * @param value The messageBody to set.
		 * @return This builder for chaining.
		 */
		public Builder setMessageBody(com.google.protobuf.ByteString value) {
			if (value == null) {
				throw new NullPointerException();
			}

			messageBody_ = value;
			onChanged();
			return this;
		}

		/**
		 * <code>bytes messageBody = 2;</code>
		 * 
		 * @return This builder for chaining.
		 */
		public Builder clearMessageBody() {

			messageBody_ = getDefaultInstance().getMessageBody();
			onChanged();
			return this;
		}

		public final Builder setUnknownFields(final com.google.protobuf.UnknownFieldSet unknownFields) {
			return super.setUnknownFields(unknownFields);
		}

		public final Builder mergeUnknownFields(final com.google.protobuf.UnknownFieldSet unknownFields) {
			return super.mergeUnknownFields(unknownFields);
		}

		// @@protoc_insertion_point(builder_scope:rpc.WrappedMessage)
	}

	// @@protoc_insertion_point(class_scope:rpc.WrappedMessage)
	private static final one.inve.localfullnode2.chronicle.rpc.WrappedMessage DEFAULT_INSTANCE;
	static {
		DEFAULT_INSTANCE = new one.inve.localfullnode2.chronicle.rpc.WrappedMessage();
	}

	public static one.inve.localfullnode2.chronicle.rpc.WrappedMessage getDefaultInstance() {
		return DEFAULT_INSTANCE;
	}

	private static final com.google.protobuf.Parser<WrappedMessage> PARSER = new com.google.protobuf.AbstractParser<WrappedMessage>() {

		public WrappedMessage parsePartialFrom(com.google.protobuf.CodedInputStream input,
				com.google.protobuf.ExtensionRegistryLite extensionRegistry)
				throws com.google.protobuf.InvalidProtocolBufferException {
			return new WrappedMessage(input, extensionRegistry);
		}
	};

	public static com.google.protobuf.Parser<WrappedMessage> parser() {
		return PARSER;
	}

	public com.google.protobuf.Parser<WrappedMessage> getParserForType() {
		return PARSER;
	}

	public one.inve.localfullnode2.chronicle.rpc.WrappedMessage getDefaultInstanceForType() {
		return DEFAULT_INSTANCE;
	}

}

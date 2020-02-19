package one.inve.localfullnode2.chronicle.rpc;

import com.google.gson.Gson;

/**
 * 
 * Copyright © INVE FOUNDATION. All rights reserved.
 * 
 * @ClassName: RawWrappedMessage
 * @Description: is capable of serializing/deserializing self.
 * @author Francis.Deng [francis_xiiiv@163.com]
 * @date Feb 17, 2020
 *
 */
public class RawWrappedMessage {
	private int mt;
	private String mb;

	private RawWrappedMessage() {
	}

	public int getMessageType() {
		return mt;
	}

	public String getMessageBody() {
		return mb;
	}

	public byte[] serialize() {
		Gson gson = new Gson();
		String sWm = gson.toJson(this);
		return sWm.getBytes();
	}

	public static class Builder {
		private RawWrappedMessage wm;

		private Builder() {
			wm = new RawWrappedMessage();
		}

		public static Builder newBuilder() {
			return new Builder();
		}

		public Builder setMessageType(int value) {
			wm.mt = value;
			return this;
		}

		public Builder setMessageBody(String value) {
			wm.mb = value;
			return this;
		}

		public RawWrappedMessage build(byte[] bytes) {
			Gson gson = new Gson();
			String sWm = new String(bytes);

			wm = gson.fromJson(sWm, RawWrappedMessage.class);
			return wm;
		}

		public RawWrappedMessage build() {
			return wm;
		}
	}
}

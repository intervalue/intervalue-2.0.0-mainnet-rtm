package one.inve.contract.shell.edge;

public class TxMixin {
	public interface Interceptable {
		InterceptResult intercept(StringRef str);
	}

	public static class InterceptResult {
		public static final InterceptResult OK = new InterceptResult(true, null);

		public final boolean isOk;
		public final String reasonIfNotOk;

		public InterceptResult(boolean isOk, String reasonIfNotOk) {
			this.isOk = isOk;
			this.reasonIfNotOk = reasonIfNotOk;
		}
	}

	// turn pass-value variable to be a pass-reference variable in order for
	// interceptors
	public static class StringRef {
		private byte[] bytes;

		public StringRef(String s) {
			if (s != null) {
				this.bytes = s.getBytes();
			} else {
				bytes = null;
			}
		}

		public byte[] getBytes() {
			return bytes;
		}
	}

	public interface TxSubmittedMixin {
		default String submitTX(String json, Interceptable... interceptors) {
			String signed;
			StringRef string = new StringRef(json);

			for (Interceptable interceptor : interceptors) {
				InterceptResult ir = interceptor.intercept(string);

				if (!ir.isOk) {
					return ir.reasonIfNotOk;
				}
			}

			signed = new String(string.getBytes());
			// String s = CommonLocalImpl.sendMessage(signed);
			String s = doSendMessage(signed);

			return s;
		}

		String doSendMessage(String signed);
	}
}

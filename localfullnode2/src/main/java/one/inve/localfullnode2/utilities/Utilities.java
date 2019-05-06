package one.inve.localfullnode2.utilities;

import java.util.Arrays;

import one.inve.localfullnode2.conf.Config;

public class Utilities {

	public static long[][] deepClone(long[][] original) {
		if (original == null) {
			return null;
		} else {
			long[][] result = original.clone();

			for (int i = 0; i < original.length; ++i) {
				if (original[i] != null) {
					result[i] = original[i].clone();
				}
			}

			return result;
		}
	}

	public static byte[][] deepClone(byte[][] original) {
		if (original == null) {
			return null;
		} else {
			byte[][] result = original.clone();

			for (int i = 0; i < original.length; ++i) {
				if (original[i] != null) {
					result[i] = original[i].clone();
				}
			}

			return result;
		}
	}

	static int arrayCompare(byte[] sig1, byte[] sig2) {
		if (sig1 == null && sig2 != null) {
			return -1;
		} else if (sig1 != null && sig2 == null) {
			return 1;
		} else {
			for (int i = 0; i < Math.min(sig1.length, sig2.length); ++i) {
				if (sig1[i] < sig2[i]) {
					return -1;
				}

				if (sig1[i] > sig2[i]) {
					return 1;
				}
			}

			return compareArrayLength(sig1, sig2);
		}
	}

	public static int arrayCompare(byte[] sig1, byte[] sig2, byte[] whitening) {
		if (sig1 == null && sig2 != null) {
			return -1;
		} else if (sig1 != null && sig2 == null) {
			return 1;
		} else {
			int len = Math.max(sig1.length, sig2.length);
			if (whitening.length < len) {
				whitening = Arrays.copyOf(whitening, len);
			}

			for (int i = 0; i < Math.min(sig1.length, sig2.length); ++i) {
				if ((sig1[i] ^ whitening[i]) < (sig2[i] ^ whitening[i])) {
					return -1;
				}

				if ((sig1[i] ^ whitening[i]) > (sig2[i] ^ whitening[i])) {
					return 1;
				}
			}

			return compareArrayLength(sig1, sig2);
		}
	}

	private static int compareArrayLength(byte[] sig1, byte[] sig2) {
		if (sig1.length < sig2.length) {
			return -1;
		} else if (sig1.length > sig2.length) {
			return 1;
		} else {
			return 0;
		}
	}

	static Object err(final Throwable e) {
		Object obj = new Object() {
			@Override
			public String toString() {
				Throwable cause = e.getCause();
				String localizedMessage = e.getLocalizedMessage();
				String message = e.getMessage();
				StackTraceElement[] stackTrace = e.getStackTrace();
				Throwable[] suppressed = e.getSuppressed();
				String className = e.getClass().getName();
				StringBuilder result = new StringBuilder();
				result.append(className).append(":").append(message);
				if (message != null && !message.equals(localizedMessage)) {
					result.append("(").append(localizedMessage).append(") ");
				}

				if (cause != null) {
					result.append("cause: [\n").append(Utilities.err(cause)).append("\n]");
				}

				int len;
				int i;
				if (suppressed != null && suppressed.length > 0) {
					result.append("suppressed: ");
					Throwable[] throwables = suppressed;
					len = suppressed.length;

					for (i = 0; i < len; ++i) {
						Throwable supp = throwables[i];
						result.append("[\n").append(Utilities.err(supp)).append("\n]");
					}
				}

				if (stackTrace != null && Config.logStack) {
					StackTraceElement[] elements = stackTrace;
					len = stackTrace.length;

					for (i = 0; i < len; ++i) {
						StackTraceElement elm = elements[i];
						result.append("\n|   ").append(elm);
					}
				}

				return result.toString();
			}
		};
		return obj;
	}
}

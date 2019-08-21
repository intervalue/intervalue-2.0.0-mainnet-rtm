package one.inve.localfullnode2.utilities;

import java.util.Arrays;

/**
 * 
 * Copyright © INVE FOUNDATION. All rights reserved.
 * 
 * @ClassName: ByteUtil
 * @Description: Byte and byte array helper function
 * @author Francis.Deng
 * @date Aug 20, 2019
 *
 */
public class ByteUtil {
	/**
	 * Creates a copy of bytes and appends b to the end of it
	 */
	public static byte[] appendByte(byte[] bytes, byte b) {
		byte[] result = Arrays.copyOf(bytes, bytes.length + 1);
		result[result.length - 1] = b;
		return result;
	}

	/**
	 * Creates a copy of bytes and appends bs to the end of it
	 */
	public static byte[] appendByte(byte[] bytes, byte[] bs) {
		byte[] result = Arrays.copyOf(bytes, bytes.length + bs.length);
		for (int i = 0; i < bs.length; i++) {
			int offset = bytes.length + i;
			result[offset] = bs[i];
		}
		return result;
	}
}

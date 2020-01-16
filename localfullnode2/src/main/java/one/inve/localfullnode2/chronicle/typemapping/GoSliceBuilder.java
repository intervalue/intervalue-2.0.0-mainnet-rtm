package one.inve.localfullnode2.chronicle.typemapping;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.google.protobuf.CodedOutputStream;
import com.sun.jna.Memory;
import com.sun.jna.Pointer;

import one.inve.localfullnode2.chronicle.typemapping.GoSlice.ByValue;

/**
 * 
 * Copyright © INVE FOUNDATION. All rights reserved.
 * 
 * @ClassName: GoSliceBuilder
 * @Description: build a {@ GoSlice} instance which is fitted into JNA
 *               convention
 * @author Francis.Deng [francis_xiiiv@163.com]
 * @date Jan 7, 2020
 *
 */
public class GoSliceBuilder {
	public static ByValue newValue(byte[][] twoDimArray) {
		GoSlice.ByValue stream = new GoSlice.ByValue();
		ByteBuffer bytesBuffer = ByteBuffer.allocate(sizeof(twoDimArray));
		CodedOutputStream cos = CodedOutputStream.newInstance(bytesBuffer);

		try {
			for (int i = 0; i < twoDimArray.length; i++) {
				cos.writeByteArrayNoTag(twoDimArray[i]);
			}

			cos.flush();
			bytesBuffer.flip();
			byte[] dst = new byte[bytesBuffer.limit()];
			bytesBuffer.get(dst);

			Pointer pointer = Pointer.NULL;
			long len = dst.length;
			pointer = new Memory(len);
			for (int i = 0; i < len; i++) {
				pointer.setByte(i, dst[i]);
			}

			stream.data = pointer;
			stream.len = len;
			stream.cap = len;
		} catch (IOException e) {
			e.printStackTrace();
			throw new TypeBuildingException(e);
		}

		return stream;
	}

	protected static int sizeof(byte[][] bytes) {
		int size = 0;

		for (int i = 0; i < bytes.length; i++) {
			size += bytes[0].length;
		}

		return size;
	}
}

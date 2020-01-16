package one.inve.localfullnode2.chronicle;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.CodedOutputStream;
import com.ochafik.lang.jnaerator.runtime.NativeSize;
import com.sun.jna.IntegerType;
import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Pointer;

import one.inve.localfullnode2.chronicle.typemapping.GoSlice;
import one.inve.localfullnode2.chronicle.typemapping.GoStringBuilder;
import one.inve.localfullnode2.chronicle.typemapping.RetrieveTransactionsByBlockNumber_return;
import one.inve.localfullnode2.chronicle.typemapping._GoString_;
import one.inve.localfullnode2.utilities.nativelib.JNANativeLibraryLoader;

public class ChronicleTest {
	private static final Logger logger = LoggerFactory.getLogger("ChronicleTest");

	// @Test
	public void testChronicleInterface() {
		JNANativeLibraryLoader libLoader = new JNANativeLibraryLoader("chronicle");
		try {
			IChronicleLib chronicleLib = libLoader.load(IChronicleLib.class);

//			GoString.ByValue dir = new GoString.ByValue();
//			dir.p = "my path";
//			dir.n = dir.p.length();
//
//			byte[][] bytesArray = new byte[2][];
//			bytesArray[0] = "I guess that".getBytes();
//			bytesArray[1] = "I think that".getBytes();
//
//			chronicle.Init(dir);
//
//			// boolean bln = chronicle.AddBlock(gen(bytesArray[0]));
//			// boolean bln = chronicle.AddBlock(gen(bytesArray, 2));
//
//			RetrieveTransactionsByBlockNumberReturn r = chronicle.RetrieveTransactionsByBlockNumber(11);
//
//			chronicle.Close();

			String myString = "francis";
			Pointer m = new Memory(myString.length() + 1); // WARNING: assumes ascii-only string
			m.setString(0, myString);

			_GoString_.ByValue dir = new _GoString_.ByValue();
			dir.p = m;
			dir.n = new NativeSize((long) (myString.length()));

			chronicleLib.Init(dir);

			GoSlice.ByValue txes = new GoSlice.ByValue();

			byte[][] bytesArray = new byte[2][];
			bytesArray[0] = "I guess that".getBytes();
			bytesArray[1] = "I think thats".getBytes();

//			int size = Native.getNativeSize(byte.class);
//			Pointer p0 = new Memory(bytesArray[0].length * size);
//			p0.setPointer(0, genPointer(bytesArray[0]));
//			// p0.setPointer(size, genPointer(bytesArray[1]));
//			txes.cap = bytesArray[0].length;
//			txes.len = bytesArray[0].length;
//			txes.data = p0;
//			Memory arr = new Memory(bytesArray[0].length * Native.getNativeSize(byte.class));
//			arr.write(0, bytesArray[0], 0, bytesArray[0].length);
//			// fill in the GoSlice class for type mapping

//			txes.data = genPointer(bytesArray[0]);
//			txes.len = bytesArray[0].length;
//			txes.cap = bytesArray[0].length;
			txes.data = genPointer(bytesArray);
			txes.len = sizeof(bytesArray);
			txes.cap = sizeof(bytesArray);

			// byte bRet = chronicle.AddBlock(txes);
			// chronicle.AddTx(genGoSlice(bytesArray), 0);

			// RetrieveTransactionsByBlockNumber_return.ByValue r =
			// chronicle.RetrieveTransactionsByBlockNumber(1);

			// logger.info(String.valueOf(bRet));

		} catch (IOException e) {
			logger.error(e.toString());
			e.printStackTrace();
		}
	}

	@Test
	public void testStdChronicleInterface() {
		JNANativeLibraryLoader libLoader = new JNANativeLibraryLoader("chronicle");
		try {
			IChronicleLib chronicleLib = libLoader.load(IChronicleLib.class);

			_GoString_.ByValue dir = GoStringBuilder.newValue("/home/francis/chronicleEnv");

			chronicleLib.Init(dir);

//			byte[][] twoDimArray = new byte[2][];
//			twoDimArray[0] = "I guess that".getBytes();
//			twoDimArray[1] = "I think thats".getBytes();
//
//			boolean b = chronicle.AddBlock(GoSliceBuilder.newValue(twoDimArray));
//			_GoString_.ByValue blockNum = GoStringBuilder.newValue("1");
//			UnsignedInt blockNum = new UnsignedInt();
//			blockNum.setValue(1);
			chronicleLib.RetrieveTransactionsByBlockNumber1((long) 10);
			System.out.println(chronicleLib.Add(2, 8));
			RetrieveTransactionsByBlockNumber_return.ByValue r = chronicleLib.GetTransactionsByBlockNumber(119);
			chronicleLib.Close();

			// logger.info(String.valueOf(bRet));

		} catch (IOException e) {
			logger.error(e.toString());
			e.printStackTrace();
		}
	}

//	protected GoSlice.ByValue[] gen(byte[][] bytesArray, int sz) {
//		GoSlice.ByValue[] slices = new GoSlice.ByValue[sz];
//		int index = 0;
//
//		for (byte[] bytes : bytesArray) {
//			Memory m = new Memory(bytes.length * Native.getNativeSize(Byte.TYPE));
//			m.write(0, bytes, 0, bytes.length);
//
//			GoSlice.ByValue ref = new GoSlice.ByValue();
//			ref.data = m;
//			ref.len = bytes.length;
//			ref.cap = bytes.length;
//
//			slices[index] = ref;
//			index++;
//		}
//
//		return slices;
//	}
//
//	protected GoSlice.ByValue gen(byte[] bytes) {
//		Memory m = new Memory(bytes.length * Native.getNativeSize(Byte.TYPE));
//		m.write(0, bytes, 0, bytes.length);
//
//		GoSlice.ByValue ref = new GoSlice.ByValue();
//		ref.data = m;
//		ref.len = bytes.length;
//		ref.cap = bytes.length;
//
//		return ref;
//	}

	protected Pointer genPointer(byte[] bytes) {
//		Memory m = new Memory(bytes.length * Native.getNativeSize(Byte.TYPE));
//		m.write(0, bytes, 0, bytes.length);
//
//		return m;
		Pointer pointer = Pointer.NULL;
		int len = bytes.length;
		pointer = new Memory(len);
		for (int i = 0; i < len; i++) {
			pointer.setByte(i, bytes[i]);
		}

		return pointer;
	}

	protected int sizeof(byte[][] bytes) {
		int size = 0;

		for (int i = 0; i < bytes.length; i++) {
			size += bytes[0].length;
		}

		return size;
	}

	protected GoSlice.ByValue genGoSlice(byte[][] bytes) throws IOException {
		GoSlice.ByValue txes = new GoSlice.ByValue();
		ByteBuffer bytesBuffer = ByteBuffer.allocate(bytes[0].length * bytes.length + 100);
		CodedOutputStream cos = CodedOutputStream.newInstance(bytesBuffer);

		for (int i = 0; i < bytes.length; i++) {
			// cos.writeRawBytes(bytes[i]);
			cos.writeByteArrayNoTag(bytes[i]);
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

		txes.data = pointer;
		txes.len = len;
		txes.cap = len;

		return txes;
	}

	protected Pointer genPointer(byte[][] bytes) throws IOException {
		ByteBuffer bytesBuffer = ByteBuffer.allocate(bytes[0].length * bytes.length + 100);
		CodedOutputStream cos = CodedOutputStream.newInstance(bytesBuffer);

		for (int i = 0; i < bytes.length; i++) {
			// cos.writeRawBytes(bytes[i]);
			cos.writeByteArrayNoTag(bytes[i]);
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

		return pointer;
	}

	protected Pointer[] genPointers(byte[][] bytesbytes) {
		Memory m[] = new Memory[2];
		byte[] bytes;
		for (int i = 0; i < bytesbytes.length; i++) {
			bytes = bytesbytes[i];
			Memory m0 = new Memory(bytes.length * Native.getNativeSize(Byte.TYPE));
			m0.write(0, bytes, 0, bytes.length);

			m[i] = m0;
		}

		return m;
	}

	public static class UnsignedInt extends IntegerType {
		public UnsignedInt() {
			super(4, true);
		}
	}
}

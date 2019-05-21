package one.inve.contract.ethplugin.invocation;

import java.io.*;

/**
 * 
 * 
 * Copyright © CHXX Co.,Ltd. All rights reserved.
 * 
 * @Description: Decision had to been made in the rush circumstances that there
 *               is last record(root value) left,which is less competitive than
 *               all history root value kept.Next improvement is hopefully.
 * @author: Francis.Deng
 * @date: 2018年12月6日 上午11:39:18
 * @version: V1.0
 */
public class OnlyOneRetainmentBinFile extends DiskFile implements Appendable, Closeable {

	public OnlyOneRetainmentBinFile(String path) {
		super(path);

		try {
			createEmptyFileIfNotExist(path);
			this.raf = new RandomAccessFile(path, "rw");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// private String filePath;

	// private boolean initialized;

	// private MappedByteBuffer mbb;
	// private FileChannel fc;
	private RandomAccessFile raf;

	@Override
	public void write(byte[] bytes) {
		try {
//			if (raf.length() != 0) {
//				raf.seek(0);
//			}
			raf.seek(0);

			raf.write(tinyIntToByte(bytes.length));
			raf.write(bytes);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Override
	public byte[] readLast() {
		throw new RuntimeException("not support");
	}

	@Override
	public byte[] readFirst() {
		try {
			if (raf.length() != 0) {
				raf.seek(0);

				// int length = raf.readInt();
				int length = byteToTinyInt(raf.readByte());
				byte[] bytes = new byte[length];
				raf.read(bytes);

				return bytes;
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	@Override
	public void close() throws IOException {
		raf.close();

	}

	protected void createEmptyFileIfNotExist(String path) throws IOException {
		File file = new File(path);
		if (!file.exists()) {
			// file.mkdirs();
			file.createNewFile();
		}

	}

	// make an assumption that byte array does not exceed 127 bits.
	private byte tinyIntToByte(int i) {
		return (byte) (i & 0xFF);
	}

	private int byteToTinyInt(byte b) {
		return b & 0x000000FF;
	}

}

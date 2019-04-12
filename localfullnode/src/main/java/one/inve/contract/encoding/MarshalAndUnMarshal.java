package one.inve.contract.encoding;

import com.alibaba.fastjson.JSON;

import java.io.IOException;

/**
 * 
 * 
 * Copyright © CHXX Co.,Ltd. All rights reserved.
 * 
 * @Description: Simple is better(fast forward key),but what has left is
 *               performance issue to figure it out in the future.
 * @author: Francis.Deng
 * @date: 2018年11月2日 下午3:23:24
 * @version: V1.0
 * @version: V1.1 the content was replaced with object jsonified representation
 */
public class MarshalAndUnMarshal {
//	public static byte[] marshal(Object object) throws IOException {
//		ObjectOutputStream oos = null;
//		ByteArrayOutputStream baos = null;
//
//		baos = new ByteArrayOutputStream();
//		oos = new ObjectOutputStream(baos);
//		oos.writeObject(object);
//		byte[] bytes = baos.toByteArray();
//		return bytes;
//
//	}
//
//	public static Object unmarshal(byte[] bytes) throws IOException, ClassNotFoundException {
//		ByteArrayInputStream bais = null;
//		bais = new ByteArrayInputStream(bytes);
//		ObjectInputStream ois = new ObjectInputStream(bais);
//		return ois.readObject();
//
//	}

	public static byte[] marshal(Object object) throws IOException {

		return JSON.toJSONBytes(object);

	}

	public static <T> T unmarshal(byte[] bytes, Class<T> clazz) throws IOException, ClassNotFoundException {
		return JSON.parseObject(bytes, clazz);

	}

//	
//	
//	default byte[] marshal(ContractTransaction ct) throws IOException {
//		ByteArrayOutputStream baos = new ByteArrayOutputStream();
//
//		baos.write(6);
//		baos.write(ct.getHash().length);
//		baos.write(ct.getNonce().length);
//		baos.write(ct.getBytecode().length);
//		baos.write(ct.getAbi().length);
//		baos.write(ct.getFrom().length);
//		baos.write(ct.getTo().length);
//
//		baos.write(ct.getHash());
//		baos.write(ct.getNonce());
//		baos.write(ct.getBytecode());
//		baos.write(ct.getAbi());
//		baos.write(ct.getFrom());
//		baos.write(ct.getTo());
//
//		baos.flush();
//		byte[] finalBytes = baos.toByteArray();
//		baos.close();
//
//		return finalBytes;
//	}
//
//	default ContractTransaction unmarshal(byte[] bs) {
//		ContractTransaction ct = new ContractTransaction();
//
//		ByteArrayInputStream bais = new ByteArrayInputStream(bs);
//
//		int sizeOfArray = bais.read();
//		int lengthOfArrays[] = new int[sizeOfArray];
//		byte[][] arrays = new byte[sizeOfArray][];
//		
//		for (int i = 0; i < sizeOfArray; i++) {
//			lengthOfArrays[i] = bais.read();
//		}
//		
//		for (int i = 0; i < sizeOfArray; i++) {
//			
//		}
//	}

}

package one.inve.localfullnode2.sync;

/**
 * 
 * Copyright © INVE FOUNDATION. All rights reserved.
 * 
 * @ClassName: ObjectVerifiable
 * @Description: TODO
 * @author Francis.Deng
 * @mailbox francis_xiiiv@163.com
 * @date Aug 21, 2019
 *
 */
public interface ObjectVerifiable<E> {
	// extract EventBody
	E getObject();

	// verify its rootHash
	boolean verify(byte[] rootHash);
}

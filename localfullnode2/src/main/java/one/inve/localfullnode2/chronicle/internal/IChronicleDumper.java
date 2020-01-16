package one.inve.localfullnode2.chronicle.internal;

/**
 * 
 * Copyright © INVE FOUNDATION. All rights reserved.
 * 
 * @ClassName: IChronicleDumper
 * @Description: export messages to chronicle
 * @author Francis.Deng [francis_xiiiv@163.com]
 * @date Jan 8, 2020
 *
 */
public interface IChronicleDumper {
	// pay attention to performance in the future
	String[] getMessageHashes();

	String[] getSysMessageHashes();

	byte[][] getMessagStreamBy(String[] messageHashes);// by a list of message hash
}

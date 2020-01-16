package one.inve.localfullnode2.chronicle.internal;

/**
 * 
 * Copyright © INVE FOUNDATION. All rights reserved.
 * 
 * @ClassName: IChronicle
 * @Description: import messages from chronicle along with
 *               {@code IChronicleExport}
 * @author Francis.Deng [francis_xiiiv@163.com]
 * @date Jan 8, 2020
 *
 */
public interface IChronicleDumperRestorer extends IChronicleDumper {
	void persist(byte[][] messagesByes);// for message

	void persistSys(byte[][] messagesByes);// support system message
}

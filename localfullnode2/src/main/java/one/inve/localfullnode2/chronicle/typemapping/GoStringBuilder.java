package one.inve.localfullnode2.chronicle.typemapping;

import com.ochafik.lang.jnaerator.runtime.NativeSize;
import com.sun.jna.Memory;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;

import one.inve.localfullnode2.chronicle.typemapping._GoString_.ByValue;

/**
 * 
 * Copyright © INVE FOUNDATION. All rights reserved.
 * 
 * @ClassName: GoStringBuilder
 * @Description: build a {@ _GoString_} instance which is fitted into JNA
 *               convention
 * @author Francis.Deng [francis_xiiiv@163.com]
 * @date Jan 7, 2020
 *
 */
public class GoStringBuilder {
	public static ByValue newValue(String str) {
		Pointer m = new Memory(str.length() + 1); // WARNING: assumes ascii-only string
		m.setString(0, str);

		_GoString_.ByValue val = new _GoString_.ByValue();
		val.p = m;
		val.n = new NativeSize((long) (str.length()));

		return val;
	}

	public static ByValue newValue(long l) {
		Pointer m = new Memory(NativeLong.SIZE);
		m.setNativeLong(0, new NativeLong(l));

		_GoString_.ByValue val = new _GoString_.ByValue();
		val.p = m;
		val.n = new NativeSize(NativeLong.SIZE);

		return val;
	}
}

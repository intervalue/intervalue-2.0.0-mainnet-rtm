package one.inve.localfullnode2.chronicle.typemapping;

import java.util.Arrays;
import java.util.List;

import com.ochafik.lang.jnaerator.runtime.NativeSize;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;

public class _GoString_ extends Structure {
	/** C type : const char* */
	public Pointer p;
	public NativeSize n;

	public _GoString_() {
		super();
	}

	protected List<?> getFieldOrder() {
		return Arrays.asList("p", "n");
	}

	/** @param p C type : const char* */
	public _GoString_(Pointer p, NativeSize n) {
		super();
		this.p = p;
		this.n = n;
	}

	public _GoString_(Pointer peer) {
		super(peer);
	}

	public static class ByReference extends _GoString_ implements Structure.ByReference {

	};

	public static class ByValue extends _GoString_ implements Structure.ByValue {

	};
}

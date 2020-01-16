package one.inve.localfullnode2.chronicle.typemapping;

import java.util.Arrays;
import java.util.List;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;

public class GoSlice extends Structure {
	/** C type : void* */
	public Pointer data;
	/** C type : GoInt */
	public long len;
	/** C type : GoInt */
	public long cap;

	public GoSlice() {
		super();
	}

	protected List<?> getFieldOrder() {
		return Arrays.asList("data", "len", "cap");
	}

	/**
	 * @param data C type : void*<br>
	 * @param len  C type : GoInt<br>
	 * @param cap  C type : GoInt
	 */
	public GoSlice(Pointer data, long len, long cap) {
		super();
		this.data = data;
		this.len = len;
		this.cap = cap;
	}

//	public GoSlice(Pointer peer) {
//		super(peer);
//	}

	public static class ByReference extends GoSlice implements Structure.ByReference {

	};

	public static class ByValue extends GoSlice implements Structure.ByValue {

	};
}

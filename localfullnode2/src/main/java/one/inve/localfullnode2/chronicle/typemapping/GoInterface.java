package one.inve.localfullnode2.chronicle.typemapping;

import java.util.Arrays;
import java.util.List;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;

public class GoInterface extends Structure {
	/** C type : void* */
	public Pointer t;
	/** C type : void* */
	public Pointer v;

	public GoInterface() {
		super();
	}

	protected List<?> getFieldOrder() {
		return Arrays.asList("t", "v");
	}

	/**
	 * @param t C type : void*<br>
	 * @param v C type : void*
	 */
	public GoInterface(Pointer t, Pointer v) {
		super();
		this.t = t;
		this.v = v;
	}

	public GoInterface(Pointer peer) {
		super(peer);
	}

	public static class ByReference extends GoInterface implements Structure.ByReference {

	};

	public static class ByValue extends GoInterface implements Structure.ByValue {

	};
}

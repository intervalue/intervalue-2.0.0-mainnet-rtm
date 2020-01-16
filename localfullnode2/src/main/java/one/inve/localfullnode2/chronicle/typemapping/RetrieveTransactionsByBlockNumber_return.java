package one.inve.localfullnode2.chronicle.typemapping;

import java.util.Arrays;
import java.util.List;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;

public class RetrieveTransactionsByBlockNumber_return extends Structure {
	/** C type : GoSlice */
	public GoSlice r0;
	/** C type : GoUint8 */
	public byte r1;

	public RetrieveTransactionsByBlockNumber_return() {
		super();
	}

	protected List<?> getFieldOrder() {
		return Arrays.asList("r0", "r1");
	}

	/**
	 * @param r0 C type : GoSlice<br>
	 * @param r1 C type : GoUint8
	 */
	public RetrieveTransactionsByBlockNumber_return(GoSlice r0, byte r1) {
		super();
		this.r0 = r0;
		this.r1 = r1;
	}

	public RetrieveTransactionsByBlockNumber_return(Pointer peer) {
		super(peer);
	}

	public static class ByReference extends RetrieveTransactionsByBlockNumber_return implements Structure.ByReference {

	};

	public static class ByValue extends RetrieveTransactionsByBlockNumber_return implements Structure.ByValue {

	};
}

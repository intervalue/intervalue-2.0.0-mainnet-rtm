package one.inve.localfullnode2.chronicle;

import com.sun.jna.Library;

import one.inve.localfullnode2.chronicle.typemapping.GoSlice;
import one.inve.localfullnode2.chronicle.typemapping.RetrieveTransactionsByBlockNumber_return;
import one.inve.localfullnode2.chronicle.typemapping._GoString_;

public interface IChronicleLib extends Library {
//	public void Init(GoString.ByValue dir);
//
//	public boolean AddBlock(GoSlice.ByValue[] txes);
//	// public boolean AddBlock(GoSlice.ByReference txes);
//
//	public RetrieveTransactionsByBlockNumberReturn RetrieveTransactionsByBlockNumber(long p0);
//
//	public void Close();

	public void Init(_GoString_.ByValue p0);

	public boolean AddBlock(GoSlice.ByValue p0);

	public RetrieveTransactionsByBlockNumber_return.ByValue RetrieveTransactionsByBlockNumber1(long p0);

	public long Add(long a, long b);

	public RetrieveTransactionsByBlockNumber_return.ByValue GetTransactionsByBlockNumber(int bn);

	public void Close();
}

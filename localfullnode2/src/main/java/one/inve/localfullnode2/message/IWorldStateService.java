package one.inve.localfullnode2.message;

import java.math.BigInteger;

public interface IWorldStateService {
	boolean decreaseBalance(String dbId, String address, BigInteger value);

	boolean transfer(String dbId, String fromAddr, String toAddr, BigInteger value);

//	List<InternalTransferData> executeContractMessage(String dbId, ContractMessage contractMsg)
//			throws NullPointerException, RuntimeException;

	BigInteger getBalanceByAddr(String dbId, String address) throws NullPointerException;

	byte[] getRoothash(String dbId);

	void setBalance(String dbId, String address, BigInteger value);

	byte[] executeViewTransaction(String dbId, String address, String callData);
}

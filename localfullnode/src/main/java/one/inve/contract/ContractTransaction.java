package one.inve.contract;

import one.inve.contract.ethplugin.crypto.ECKey;
import one.inve.contract.ethplugin.crypto.ECKey.ECDSASignature;
import one.inve.contract.ethplugin.crypto.ECKey.MissingPrivateKeyException;
import one.inve.contract.ethplugin.crypto.HashUtil;
import one.inve.contract.ethplugin.util.RLP;

/**
 * 
 * 
 * Copyright © CHXX Co.,Ltd. All rights reserved.
 * 
 * @Description: bring in elliptical curve signature.
 * @author: Francis.Deng
 * @date: 2018年11月27日 下午3:15:17
 * @version: V1.0
 */
public class ContractTransaction implements java.io.Serializable {
	private ECDSASignature signature;
	private byte[] rawHash;

	// transaction hash
	private byte[] hash;
	private byte[] nonce;

	// low-level representation bytecode
	private byte[] bytecode;
	private byte[] abi;

	private byte[] gasPrice;
	private byte[] gasLimit;

	private byte[] endowment;

	private byte[] recieveAddress;
	// append new property "sendAddress" derived by public key
	private byte[] sendAddress;

	// available when calling a contract function.
	private byte[] functionName;

	// which indicate that this is a calling function transaction if not empty.
	// private Function callFunction;

	public byte[] getHash() {
		return hash;
	}

	public void setHash(byte[] hash) {
		this.hash = hash;
	}

	public byte[] getNonce() {
		return nonce;
	}

	public void setNonce(byte[] nonce) {
		this.nonce = nonce;
	}

	public byte[] getBytecode() {
		return bytecode;
	}

	public void setBytecode(byte[] bytecode) {
		this.bytecode = bytecode;
	}

	public byte[] getAbi() {
		return abi;
	}

	public void setAbi(byte[] abi) {
		this.abi = abi;
	}

	public byte[] getGasPrice() {
		return gasPrice;
	}

	public void setGasPrice(byte[] gasPrice) {
		this.gasPrice = gasPrice;
	}

	public byte[] getGasLimit() {
		return gasLimit;
	}

	public void setGasLimit(byte[] gasLimit) {
		this.gasLimit = gasLimit;
	}

	public byte[] getEndowment() {
		return endowment;
	}

	public void setEndowment(byte[] endowment) {
		this.endowment = endowment;
	}

	public byte[] getRecieveAddress() {
		return recieveAddress;
	}

	public void setRecieveAddress(byte[] recieveAddress) {
		this.recieveAddress = recieveAddress;
	}

	public byte[] getEncodedRaw() {
		byte[] rlpRaw;
		byte[] bytecode = (this.bytecode == null ? new byte[0] : this.bytecode);

		rlpRaw = RLP.encodeList(bytecode);

		return rlpRaw;
	}

	public void sign(ECKey key) throws MissingPrivateKeyException {
		this.signature = key.sign(this.getRawHash());
	}

	public byte[] getRawHash() {
		byte[] plainMsg = this.getEncodedRaw();
		return rawHash = HashUtil.sha3(plainMsg);
	}

	public ECDSASignature getSignature() {
		return signature;
	}

	public byte[] getSendAddress() {
		return sendAddress;
	}

	public void setSendAddress(byte[] sendAddress) {
		this.sendAddress = sendAddress;
	}

	public byte[] getFunctionName() {
		return functionName;
	}

	public void setFunctionName(byte[] functionName) {
		this.functionName = functionName;
	}

//	public Function getCallFunction() {
//		return callFunction;
//	}
//
//	public void setCallFunction(Function callFunction) {
//		this.callFunction = callFunction;
//	}

}

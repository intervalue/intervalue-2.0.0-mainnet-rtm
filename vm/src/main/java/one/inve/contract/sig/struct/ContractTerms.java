package one.inve.contract.sig.struct;

/**
 * 
 * 
 * Copyright © CHXX Co.,Ltd. All rights reserved.
 * 
 * @Description: mnemonicCode and saddress play a important role in signing
 *               message.
 * @author: Francis.Deng
 * @date: Dec 16, 2018 10:20:52 PM
 * @version: V1.0
 */
public class ContractTerms {
	private String calldata;
	private String toAddress;
	private String saddress;
	private String nonce;
	private String gasPrice;
	private String value;
	private String gasLimit;

	private String mnemonicCode;

	public String getCalldata() {
		return calldata;
	}

	public void setCalldata(String _calldata) {
		this.calldata = _calldata;
	}

	public String getToAddress() {
		return toAddress;
	}

	public void setToAddress(String _toAddress) {
		this.toAddress = _toAddress;
	}

	public String getSender() {
		return saddress;
	}

	public void setSender(String _sender) {
		this.saddress = _sender;
	}

	public String getNonce() {
		return nonce;
	}

	public void setNonce(String nonce) {
		this.nonce = nonce;
	}

	public String getGasPrice() {
		return gasPrice;
	}

	public void setGasPrice(String gasPrice) {
		this.gasPrice = gasPrice;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String _value) {
		this.value = _value;
	}

	public String getGasLimit() {
		return gasLimit;
	}

	public void setGasLimit(String gasLimit) {
		this.gasLimit = gasLimit;
	}

	public String getMnemonicCode() {
		return mnemonicCode;
	}

	public void setMnemonicCode(String mnemonicCode) {
		this.mnemonicCode = mnemonicCode;
	}

}

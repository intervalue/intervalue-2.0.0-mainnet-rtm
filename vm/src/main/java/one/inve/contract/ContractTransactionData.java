package one.inve.contract;

public class ContractTransactionData {
    private byte[] nonce;
    private byte[] gasPrice;
    private byte[] gasLimit;
    private byte[] toAddress;
    private byte[] value;
    private byte[] calldata;

    public byte[] getNonce() {
        return nonce;
    }

    public void setNonce(byte[] nonce) {
        this.nonce = nonce;
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

    public byte[] getToAddress() {
        return toAddress;
    }

    public void setToAddress(byte[] toAddress) {
        this.toAddress = toAddress;
    }

    public byte[] getValue() {
        return value;
    }

    public void setValue(byte[] value) {
        this.value = value;
    }

    public byte[] getCalldata() {
        return calldata;
    }

    public void setCalldata(byte[] calldata) {
        this.calldata = calldata;
    }
}
package one.inve.contract;

public class ContractTransactionData {
    private String nonce;
    private String gasPrice;
    private String gasLimit;
    private String toAddress;
    private String value;
    private String calldata;

    public void setNonce(String nonce) {
        this.nonce = nonce;
    }

    public void setGasPrice(String gasPrice) {
        this.gasPrice = gasPrice;
    }

    public void setGasLimit(String gasLimit) {
        this.gasLimit = gasLimit;
    }

    public void setToAddress(String toAddress) {
        this.toAddress = toAddress;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public void setCalldata(String calldata) {
        this.calldata = calldata;
    }

    public String getNonce() {
        return nonce;
    }

    public String getGasPrice() {
        return gasPrice;
    }

    public String getGasLimit() {
        return gasLimit;
    }

    public String getToAddress() {
        return toAddress;
    }

    public String getValue() {
        return value;
    }

    public String getCalldata() {
        return calldata;
    }
}
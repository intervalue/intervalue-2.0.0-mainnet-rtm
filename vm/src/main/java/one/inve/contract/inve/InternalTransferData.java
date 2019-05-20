package one.inve.contract.inve;

import java.math.BigInteger;

public class InternalTransferData {
    private String fromAddress;
    private String toAddress;
    private BigInteger fee;
    private BigInteger value;

    public String getFromAddress() {
        return fromAddress;
    }

    public void setFromAddress(String fromAddress) {
        this.fromAddress = fromAddress;
    }

    public String getToAddress() {
        return toAddress;
    }

    public void setToAddress(String toAddress) {
        this.toAddress = toAddress;
    }

    public BigInteger getFee() {
        return fee;
    }

    public void setFee(BigInteger fee) {
        this.fee = fee;
    }

    public BigInteger getValue() {
        return value;
    }

    public void setValue(BigInteger value) {
        this.value = value;
    }

    public InternalTransferData(String fromAddress, String toAddress, BigInteger fee, BigInteger value) {
        this.fromAddress = fromAddress;
        this.toAddress = toAddress;
        this.fee = fee;
        this.value = value;
    }

    public String toString() {
        return new StringBuffer()
            .append(" fromAddress: ").append(fromAddress != null ? fromAddress : "")
            .append(" toAddress: ").append(toAddress != null ? toAddress : "")
            .append(" fee: ").append(fee != null ? fee.toString() : "")
            .append(" value: ").append(value != null ? value.toString() : "").toString();
    }
}
package one.inve.localfullnode2.store.rocks;

import java.io.Serializable;
import java.math.BigInteger;

/**
 * TransactionMessage表对应数据结构
 * @author
 * @date
 */
public class TransactionMsg implements Serializable {
    /**
     * 交易ID
     */
	private String id;

    /**
     * 类型 快照，奖励等
     */
	private String type;
    /**
     *
     */
    private String mHash;
    /**
     * 发送者地址
     */
    private String fromAddress;
    /**
     * 接受者地址
     */
    private String toAddress;
    /**
     * 交易金额
     */
    private BigInteger amount;

    /**
     * 入库时间戳
     */
    private long updateTime;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getmHash() {
        return mHash;
    }

    public void setmHash(String mHash) {
        this.mHash = mHash;
    }

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

    public BigInteger getAmount() {
        return amount;
    }

    public void setAmount(BigInteger amount) {
        this.amount = amount;
    }

    public long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(long updateTime) {
        this.updateTime = updateTime;
    }

    @Override
    public String toString() {
        return "TransactionMsg{" +
                "id=" + id +
                ", type='" + type + '\'' +
                ", mHash='" + mHash + '\'' +
                ", fromAddress='" + fromAddress + '\'' +
                ", toAddress='" + toAddress + '\'' +
                ", amount=" + amount +
                ", updateTime=" + updateTime +
                '}';
    }
}



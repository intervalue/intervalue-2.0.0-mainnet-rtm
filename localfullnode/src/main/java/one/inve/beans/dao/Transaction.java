package one.inve.beans.dao;

import com.alibaba.fastjson.annotation.JSONField;

import java.io.Serializable;
import java.math.BigInteger;

/**
 * transaction表对应数据结构（只对应TransactionMessage）
 * @author Clare
 * @date   2018/7/30 0030.
 */
public class Transaction implements Serializable {
    /**
     * 交易ID（赋值：在共识之后的第id个交易）
     */
	private BigInteger id;
    /**
     * 发送者公钥
     */
	private String pubkey;
    /**
     * 类型 快照，奖励等
     */
	private String type;
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
     * 交易手续费
     */
    private BigInteger fee;
    /**
     * 整个交易消息内容的哈希值
     */
    private String hash;
    /**
     * 整个交易消息内容的签名
     */
    private String signature;
    /**
     * 交易生成时的时间戳
     */
    private long time;
    /**
     * 入库时间戳
     */
    private long updateTime;
    /**
     * 是否共识确认
     */
    private boolean isStable;
    /**
     * 是否有效交易
     */
    private boolean isValid;
    /**
     * 所属打包event的哈希值
     */
    private String eHash;
    /**
     * 消息
     */
    private String msg;
    /**
     * 交易附带的留言(限制50字节)
     */
    private String remark;

    public Transaction() {
    }
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
    private Transaction(Builder builder) {
        setId(builder.id);
        setPubkey(builder.pubkey);
        setFromAddress(builder.fromAddress);
        setToAddress(builder.toAddress);
        setAmount(builder.amount);
        setFee(builder.fee);
        setHash(builder.hash);
        setSignature(builder.signature);
        setTime(builder.time);
        setUpdateTime(builder.updateTime);
        setStable(builder.isStable);
        setValid(builder.isValid);
        seteHash(builder.eHash);
        setRemark(builder.remark);
        setType(builder.type);
        setMsg(builder.msg);
    }

    public BigInteger getId() {
        return id;
    }

    public void setId(BigInteger id) {
        this.id = id;
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

    public BigInteger getFee() {
        return fee;
    }

    public void setFee(BigInteger fee) {
        this.fee = fee;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(long updateTime) {
        this.updateTime = updateTime;
    }

    @JSONField(name="isStable")
    public boolean isStable() {
        return isStable;
    }

    @JSONField(name="isStable")
    public void setStable(boolean stable) {
        isStable = stable;
    }

    @JSONField(name="isValid")
    public boolean isValid() {
        return isValid;
    }

    @JSONField(name="isValid")
    public void setValid(boolean valid) {
        isValid = valid;
    }

    public String geteHash() {
        return eHash;
    }

    public void seteHash(String eHash) {
        this.eHash = eHash;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getPubkey() {
        return pubkey;
    }

    public void setPubkey(String pubkey) {
        this.pubkey = pubkey;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public static final class Builder {
        private BigInteger id;
        private String pubkey;
        private String fromAddress;
        private String toAddress;
        private BigInteger amount;
        private BigInteger fee;
        private String hash;
        private String signature;
        private long time;
        private long updateTime;
        private String type;
        private String msg;
        /**
         * 是否共识
         */
        private boolean isStable;
        /**
         * 是否有效
         */
        private boolean isValid;
        private String eHash;
        private String remark;

        public Builder() {
        }

        public Builder id(BigInteger val) {
            id = val;
            return this;
        }

        public Builder pubkey(String val) {
            pubkey = val;
            return this;
        }

        public Builder fromAddress(String val) {
            fromAddress = val;
            return this;
        }

        public Builder toAddress(String val) {
            toAddress = val;
            return this;
        }

        public Builder amount(BigInteger val) {
            amount = val;
            return this;
        }
        public Builder msg(String val) {
            msg = val;
            return this;
        }
        public Builder fee(BigInteger val) {
            fee = val;
            return this;
        }

        public Builder hash(String val) {
            hash = val;
            return this;
        }

        public Builder signature(String val) {
            signature = val;
            return this;
        }

        public Builder time(long val) {
            time = val;
            return this;
        }

        public Builder updateTime(long val) {
            updateTime = val;
            return this;
        }

        public Builder isStable(boolean val) {
            isStable = val;
            return this;
        }

        public Builder isValid(boolean val) {
            isValid = val;
            return this;
        }

        public Builder eHash(String val) {
            eHash = val;
            return this;
        }

        public Builder remark(String val) {
            remark = val;
            return this;
        }

        public Builder type(String type) {
            this.type = type;
            return this;
        }

        public Transaction build() {
            return new Transaction(this);
        }
    }
}



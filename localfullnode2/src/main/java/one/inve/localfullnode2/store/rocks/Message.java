package one.inve.localfullnode2.store.rocks;

import com.alibaba.fastjson.annotation.JSONField;
import one.inve.bean.message.TransactionMessage;

import java.io.Serializable;
import java.math.BigInteger;

public class Message  implements Serializable {
    private String eHash;
    private String hash;
    private String id;
    private boolean lastIdx;
    private boolean isValid;
    private boolean isStable;
    private long updateTime;
    private String snapVersion;
    private String message;

    public String geteHash() {
        return eHash;
    }

    public void seteHash(String eHash) {
        this.eHash = eHash;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Message(){

    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    private Message(Builder builder) {

        setId(builder.id+"");
        setLastIdx(builder.lastIdx);
        setValid(builder.isValid);
        setUpdateTime(builder.updateTime);
        setStable(builder.isStable);
        setSnapVersion(builder.snapVersion);
        setMessage(builder.message);
        seteHash(builder.eHash);
        setHash(builder.hash);
    }

    public boolean isLastIdx() {
        return lastIdx;
    }

    public void setLastIdx(boolean lastIdx) {
        this.lastIdx = lastIdx;
    }

    @JSONField(name="isValid")
    public boolean isValid() {
        return isValid;
    }

    @JSONField(name="isValid")
    public void setValid(boolean isValid) {
        this.isValid = isValid;
    }

    @JSONField(name="isStable")
    public boolean isStable() {
        return isStable;
    }

    @JSONField(name="isStable")
    public void setStable(boolean isStable) {
        this.isStable = isStable;
    }

    public long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(long updateTime) {
        this.updateTime = updateTime;
    }

    public String getSnapVersion() {
        return snapVersion;
    }

    public void setSnapVersion(String snapVersion) {
        this.snapVersion = snapVersion;
    }


    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }


    public static final class Builder {

        private String id;
        private boolean lastIdx;
        private boolean isValid;
        private long updateTime;
        private boolean isStable;
        private String snapVersion;
        private String message;
        private String eHash;
        private String hash;

        public Builder() {
        }



        public Builder id(String val) {
            id = val;
            return this;
        }

        public Builder lastIdx(boolean val) {
            lastIdx = val;
            return this;
        }

        public Builder isValid(boolean val) {
            isValid = val;
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

        public Builder snapVersion(String val) {
            snapVersion = val;
            return this;
        }
        public Builder hash(String val) {
            hash = val;
            return this;
        }
        public Builder message(String val) {
            message = val;
            return this;
        }

        public Builder eHash(String val) {
            eHash = val;
            return this;
        }

        public Message build() {
            return new Message(this);
        }
    }
}

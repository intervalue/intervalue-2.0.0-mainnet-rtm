package one.inve.localfullnode2.store;

import com.alibaba.fastjson.annotation.JSONField;
import one.inve.utils.DSA;

import java.math.BigInteger;
import java.time.Instant;
import java.util.Objects;

public class EventBody {
    private int shardId;
    private long creatorId;
    private long creatorSeq;
    private long otherId;
    private long otherSeq;
    private Instant timeCreated;
    private boolean isFamous;

    private byte[][] trans;
    private byte[] signature;

    /**
     * 这个可以在网络上传输也可以不传输，如果不传输则需要按照generation排序以后发送
     */
    private long generation;
    /**
     * 这个值不用在网络上传输，由获得者根据两个父亲的hash自行计算
     * 这样一方面节约了传输带宽
     * 另一方面即使传输了，接受者也要重新计算以进行验证，确保内容没有被篡改
     */
    private byte[] hash;

    private Instant consTimestamp;
    byte[] otherHash;
    byte[] parentHash;

    private BigInteger transCount;
    private BigInteger consEventCount;

    public EventBody() {
    }

    private EventBody(Builder builder) {
        setShardId(builder.shardId);
        setCreatorId(builder.creatorId);
        setCreatorSeq(builder.creatorSeq);
        setOtherId(builder.otherId);
        setOtherSeq(builder.otherSeq);
        setTimeCreated(builder.timeCreated);
        setFamous(builder.isFamous);
        setTrans(builder.trans);
        setSignature(builder.signature);
        setGeneration(builder.generation);
        setHash(builder.hash);
        setConsTimestamp(builder.consTimestamp);
        setOtherHash(builder.otherHash);
        setParentHash(builder.parentHash);
        setTransCount(builder.transCount);
        setConsEventCount(builder.consEventCount);
    }

    public int getShardId() {
        return shardId;
    }

    public void setShardId(int shardId) {
        this.shardId = shardId;
    }

    public long getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(long creatorId) {
        this.creatorId = creatorId;
    }

    public long getCreatorSeq() {
        return creatorSeq;
    }

    public void setCreatorSeq(long creatorSeq) {
        this.creatorSeq = creatorSeq;
    }

    public long getOtherId() {
        return otherId;
    }

    public void setOtherId(long otherId) {
        this.otherId = otherId;
    }

    public long getOtherSeq() {
        return otherSeq;
    }

    public void setOtherSeq(long otherSeq) {
        this.otherSeq = otherSeq;
    }

    public Instant getTimeCreated() {
        return timeCreated;
    }

    public void setTimeCreated(Instant timeCreated) {
        this.timeCreated = timeCreated;
    }

    @JSONField(name="isFamous")
    public boolean isFamous() {
        return isFamous;
    }

    @JSONField(name="isFamous")
    public void setFamous(boolean famous) {
        isFamous = famous;
    }

    public byte[][] getTrans() {
        return trans;
    }

    public void setTrans(byte[][] trans) {
        this.trans = trans;
    }

    public byte[] getSignature() {
        return signature;
    }

    public void setSignature(byte[] signature) {
        this.signature = signature;
    }

    public long getGeneration() {
        return generation;
    }

    public void setGeneration(long generation) {
        this.generation = generation;
    }

    public byte[] getHash() {
        return hash;
    }

    public void setHash(byte[] hash) {
        this.hash = hash;
    }

    public Instant getConsTimestamp() {
        return consTimestamp;
    }

    public void setConsTimestamp(Instant consTimestamp) {
        this.consTimestamp = consTimestamp;
    }

    public byte[] getOtherHash() {
        return otherHash;
    }

    public void setOtherHash(byte[] otherHash) {
        this.otherHash = otherHash;
    }

    public byte[] getParentHash() {
        return parentHash;
    }

    public void setParentHash(byte[] parentHash) {
        this.parentHash = parentHash;
    }

    public BigInteger getTransCount() {
        return transCount;
    }

    public void setTransCount(BigInteger transCount) {
        this.transCount = transCount;
    }

    public BigInteger getConsEventCount() {
        return consEventCount;
    }

    public void setConsEventCount(BigInteger consEventCount) {
        this.consEventCount = consEventCount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        EventBody that = (EventBody) o;
        return hashCode() == that.hashCode() &&
                creatorId == that.creatorId &&
                shardId == that.shardId &&
                creatorSeq == that.creatorSeq &&
                otherId == that.otherId &&
                otherSeq == that.otherSeq &&
                DSA.encryptBASE64(hash).equals(DSA.encryptBASE64(that.hash)) &&
                DSA.encryptBASE64(signature).equals(DSA.encryptBASE64(that.signature)) &&
                generation == that.generation&&
                otherHash==null?(that.otherHash==null?true:false):(that.otherHash==null?false: DSA.encryptBASE64(otherHash).equals(DSA.encryptBASE64(that.otherHash)))&&
                parentHash==null?(that.parentHash==null?true:false):(that.parentHash==null?false: DSA.encryptBASE64(parentHash).equals(DSA.encryptBASE64(that.parentHash)));
    }

    @Override
    public int hashCode() {
        return Objects.hash(shardId, creatorId, creatorSeq, otherId, otherSeq,
                DSA.encryptBASE64(hash), DSA.encryptBASE64(signature), generation,
                otherHash==null?null: DSA.encryptBASE64(otherHash), parentHash==null?null: DSA.encryptBASE64(parentHash));
    }


    public static final class Builder {
        private int shardId;
        private long creatorId;
        private long creatorSeq;
        private long otherId;
        private long otherSeq;
        private Instant timeCreated;
        private boolean isFamous;
        private byte[][] trans;
        private byte[] signature;
        private long generation;
        private byte[] hash;
        private Instant consTimestamp;
        private byte[] otherHash;
        private byte[] parentHash;
        private BigInteger transCount;
        private BigInteger consEventCount;

        public Builder() {
        }

        public Builder shardId(int val) {
            shardId = val;
            return this;
        }

        public Builder creatorId(long val) {
            creatorId = val;
            return this;
        }

        public Builder creatorSeq(long val) {
            creatorSeq = val;
            return this;
        }

        public Builder otherId(long val) {
            otherId = val;
            return this;
        }

        public Builder otherSeq(long val) {
            otherSeq = val;
            return this;
        }

        public Builder timeCreated(Instant val) {
            timeCreated = val;
            return this;
        }

        public Builder isFamous(boolean val) {
            isFamous = val;
            return this;
        }

        public Builder trans(byte[][] val) {
            trans = val;
            return this;
        }

        public Builder signature(byte[] val) {
            signature = val;
            return this;
        }

        public Builder generation(long val) {
            generation = val;
            return this;
        }

        public Builder hash(byte[] val) {
            hash = val;
            return this;
        }

        public Builder consTimestamp(Instant val) {
            consTimestamp = val;
            return this;
        }

        public Builder otherHash(byte[] val) {
            otherHash = val;
            return this;
        }

        public Builder parentHash(byte[] val) {
            parentHash = val;
            return this;
        }

        public Builder transCount(BigInteger val) {
            transCount = val;
            return this;
        }

        public Builder consEventCount(BigInteger val) {
            consEventCount = val;
            return this;
        }

        public EventBody build() {
            return new EventBody(this);
        }
    }
}

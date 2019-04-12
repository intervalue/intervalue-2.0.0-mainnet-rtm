package one.inve.core;

import java.math.BigInteger;
import java.util.Objects;

/**
 * event key pair
 *
 * @author Clare
 * @date 2018/9/4 0004.
 */
public class EventKeyPair {
    public final int shardId;
    public final long creatorId;
    public final long seq;
//    public final BigInteger seq;

    public EventKeyPair(int shardId, long creatorId, long seq) {
//    public EventKeyPair(int shardId, long creatorId, BigInteger seq) {
        this.shardId = shardId;
        this.creatorId = creatorId;
        this.seq = seq;
    }

    private EventKeyPair(Builder builder) {
        shardId = builder.shardId;
        creatorId = builder.creatorId;
        seq = builder.seq;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof EventKeyPair)) {
            return false;
        } else {
            EventKeyPair x = (EventKeyPair) other;
            return this.hashCode() == x.hashCode()
                    && this.shardId == x.shardId
                    && this.creatorId == x.creatorId
                    && this.seq == x.seq;
//                    && this.seq.equals(x.seq);
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(shardId, creatorId, seq);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        return sb.append(shardId).append("_").append(creatorId).append("_").append(seq).toString();
    }

    public static final class Builder {
        private final int shardId;
        private final long creatorId;
        private final long seq;
//        private final BigInteger seq;

        public Builder(int shardId, long creatorId, long seq) {
//        public Builder(int shardId, long creatorId, BigInteger seq) {
            this.shardId = shardId;
            this.creatorId = creatorId;
            this.seq = seq;
        }

        public EventKeyPair build() {
            return new EventKeyPair(this);
        }
    }
}

package one.inve.core;

/**
 * Created by Clare  on 2018/6/21 0021.
 */
public class ShardInfo {
    private String pubkey;
    private String shard;
    private String index;
    private String rpcPort;

    public ShardInfo() {
    }

    private ShardInfo(Builder builder) {
        setPubkey(builder.pubkey);
        setShard(builder.shard);
        setIndex(builder.index);
        setRpcPort(builder.rpcPort);
    }

    public String getPubkey() {
        return pubkey;
    }

    public void setPubkey(String pubkey) {
        this.pubkey = pubkey;
    }

    public String getShard() {
        return shard;
    }

    public void setShard(String shard) {
        this.shard = shard;
    }

    public String getIndex() {
        return index;
    }

    public void setIndex(String index) {
        this.index = index;
    }

    public String getRpcPort() {
        return rpcPort;
    }

    public void setRpcPort(String rpcPort) {
        this.rpcPort = rpcPort;
    }

    public static final class Builder {
        private String pubkey;
        private String shard;
        private String index;
        private String rpcPort;

        public Builder() {
        }

        public Builder pubkey(String val) {
            pubkey = val;
            return this;
        }

        public Builder shard(String val) {
            shard = val;
            return this;
        }

        public Builder index(String val) {
            index = val;
            return this;
        }

        public Builder rpcPort(String val) {
            rpcPort = val;
            return this;
        }

        public ShardInfo build() {
            return new ShardInfo(this);
        }
    }
}

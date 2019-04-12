package one.inve.beans.dao;

import java.io.Serializable;

/**
 * 分片成功的局部全节点的信息
 * @author Clare
 * @date   2018/6/21 0021.
 */
public class ShardInfo implements Serializable {
    /**
     * 公钥（base64）
     */
    private String pubkey;
    /**
     * 分片ID
     */
    private String shard;
    /**
     * 在分片内的索引
     */
    private String index;
    /**
     * rpc端口
     */
    private String rpcPort;
    /**
     * 分片内的节点数量
     */
    private int nValue;
    /**
     * 总分片数
     */
    private int shardCount;

    public ShardInfo() {
    }

    private ShardInfo(Builder builder) {
        setPubkey(builder.pubkey);
        setShard(builder.shard);
        setIndex(builder.index);
        setRpcPort(builder.rpcPort);
        setnValue(builder.nValue);
        setShardCount(builder.shardCount);
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

    public int getnValue() {
        return nValue;
    }

    public void setnValue(int nValue) {
        this.nValue = nValue;
    }

    public int getShardCount() {
        return shardCount;
    }

    public void setShardCount(int shardCount) {
        this.shardCount = shardCount;
    }


    public static final class Builder {
        private String pubkey;
        private String shard;
        private String index;
        private String rpcPort;
        private int nValue;
        private int shardCount;

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

        public Builder nValue(int val) {
            nValue = val;
            return this;
        }

        public Builder shardCount(int val) {
            shardCount = val;
            return this;
        }

        public ShardInfo build() {
            return new ShardInfo(this);
        }
    }
}

package one.inve.core;

import com.alibaba.fastjson.JSON;
import one.inve.bean.wallet.KeyPair;
import one.inve.exception.InveException;

import java.time.Instant;
import java.util.Arrays;

/**
 * @Description pbft共识区块结构
 * @Author Clarelau61803@gmail.com
 * @Date 2018/10/17 0017 上午 11:40
 **/
public class Block {
    /**
     * 上一区块hash值
     */
    private Hash preHash;
    /**
     * 本区块hash值
     */
    private Hash hash;
    /**
     * 本区块hash值的签名
     */
    private byte[] signature;
    /**
     * 高度索引
     */
    private long index;
    /**
     * 时间戳
     */
    private long timestamp;
    /**
     * 区块内容数据
     */
    private byte[] data;

    public Block() {
    }

    private Block(Builder builder) {
        setPreHash(builder.preHash);
        setHash(builder.hash);
        setSignature(builder.signature);
        setIndex(builder.index);
        setTimestamp(builder.timestamp);
        setData(builder.data);
    }

    public Hash getPreHash() {
        return preHash;
    }

    public void setPreHash(Hash preHash) {
        this.preHash = preHash;
    }

    public Hash getHash() {
        return hash;
    }

    public void setHash(Hash hash) {
        this.hash = hash;
    }

    public byte[] getSignature() {
        return signature;
    }

    public void setSignature(byte[] signature) {
        this.signature = signature;
    }

    public long getIndex() {
        return index;
    }

    public void setIndex(long index) {
        this.index = index;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "Block{" +
                "preHash=" + preHash +
                ", hash=" + hash +
                ", signature=" + Arrays.toString(signature) +
                ", index=" + index +
                ", timestamp=" + timestamp +
                ", data=" + Arrays.toString(data) +
                '}';
    }

    public static final class Builder {
        private Hash preHash;
        private Hash hash;
        private byte[] signature;
        private long index;
        private long timestamp;
        private byte[] data;

        public Builder() {
        }

        public Builder preHash(Hash val) {
            preHash = val;
            return this;
        }

        public Builder hash(Hash val) {
            hash = val;
            return this;
        }

        public Builder signature(byte[] val) {
            signature = val;
            return this;
        }

        public Builder index(long val) {
            index = val;
            return this;
        }

        public Builder timestamp(long val) {
            timestamp = val;
            return this;
        }

        public Builder data(byte[] val) {
            data = val;
            return this;
        }

        public Block build() {
            return new Block(this);
        }
    }

    public static void main(String[] args) throws InveException {
        Crypto crypto = new Crypto();
        KeyPair keyPair = crypto.getKeyPair();
        byte[] publicKey = keyPair.getPublicKey();
        byte[] privateKey = keyPair.getPrivateKey();

        byte[] hash = "Hash".getBytes();
        Block block = new Builder().preHash(new Hash("preHash".getBytes())).hash(new Hash(hash))
                .signature(Crypto.sign(hash, privateKey))
                .timestamp(Instant.now().toEpochMilli()).index(0).data("sdfsdfasdfsdfasdf".getBytes()).build();
        System.out.println("block: " + block.toString());
        String blockStr = JSON.toJSONString(block);
        System.out.println("block json: " + blockStr);
        Block block1 = JSON.parseObject(blockStr, Block.class);
        System.out.println("preHash: " + new String(block1.getPreHash().getHash()));
        System.out.println("hash: " + new String(block1.getHash().getHash()));
        System.out.println("signature: " + new String(block1.getSignature()));
        System.out.println("timestamp: " + block1.getTimestamp());
        System.out.println("index: " + block1.getIndex());
        System.out.println("data: " + new String(block1.getData()));

        System.out.println("verify sign: " + Crypto.verifySignature(hash, block1.getSignature(), publicKey));
    }
}

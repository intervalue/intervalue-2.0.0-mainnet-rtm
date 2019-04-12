package one.inve.core;

import com.github.aelstad.keccakj.provider.Constants;
import com.github.aelstad.keccakj.provider.KeccakjProvider;
import org.apache.log4j.Logger;

import java.security.MessageDigest;
import java.security.Security;

/**
 * @Description pbft共识算法采用的hash算法
 * @Author Clarelau61803@gmail.com
 * @Date 2018/10/17 0017 下午 2:29
 **/
public class Hash implements Comparable<Hash> {
    public static Logger logger = Logger.getLogger(Hash.class);
    static {
        Security.addProvider(new KeccakjProvider());
    }

    private byte[] hash;

    public Hash() {
    }

    public byte[] getHash() {
        return this.hash;
    }

    public void setHash(byte[] hash) {
        this.hash = hash;
    }

    public Hash(byte[] hash) {
        this.hash = hash;
    }

    public Hash(byte[] preHash, long index, long timestamp, byte[] data) {
        this.hash = hash(preHash, index, timestamp, data);
    }

    public Hash(Block block) {
        this.hash = hash(block.getPreHash().hash, block.getIndex(), block.getTimestamp(), block.getData());
    }

    public static byte[] hash(byte[] preHash, long index, long timestamp, byte[] data) {
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance(Constants.SHA3_512, Constants.PROVIDER);
        } catch (Exception e) {
            logger.error("error", e);
        }

        if (preHash != null) {
            md.update(preHash);
        }
        update(md, index);
        update(md, timestamp);
        update(md, data);

        return md.digest();
    }

    static void update(MessageDigest digest, long n) {
        for (int i = 0; i < 8; i++) {
            digest.update((byte) ((int) (255 & n)));
            n >>= 8;
        }
    }

    static void update(MessageDigest digest, byte[] t) {
        if (t == null) {
            update(digest, 0);
            return;
        }
        update(digest, (long) t.length);
        digest.update(t);
    }

    public boolean equals(Object object) {
        if ((object instanceof Hash) && compareTo((Hash) object) == 0) {
            return true;
        }
        return false;
    }

    public int compareTo(Hash other) {
        if (other == null) {
            return 1;
        }
        for (int i = 0; i < this.hash.length; i++) {
            if (this.hash[i] > other.hash[i]) {
                return 1;
            }
            if (this.hash[i] < other.hash[i]) {
                return -1;
            }
        }
        return 0;
    }
}

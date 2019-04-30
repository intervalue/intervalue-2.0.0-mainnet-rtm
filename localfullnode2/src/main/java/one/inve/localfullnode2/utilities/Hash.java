package one.inve.localfullnode2.utilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.MessageDigest;
import java.time.Instant;

public class Hash implements Comparable<Hash> {
    private static final Logger logger = LoggerFactory.getLogger(Hash.class);

    final byte[] hash;

    static void update(MessageDigest digest, long n) {
        for (int i = 0; i < 8; i++) {
            digest.update((byte) ((int) (255 & n)));
            n >>= 8;
        }
    }

    static void update(MessageDigest digest, byte[][] t) {
        if (t == null) {
            update(digest, 0);
            return;
        }
        update(digest, (long) t.length);
        for (byte[] a : t) {
            if (a == null) {
                update(digest, 0);
            } else {
                update(digest, (long) a.length);
                digest.update(a);
            }
        }
    }

    public Hash(byte[] hash) {
        this.hash = hash;
    }

    public Hash(int shardId, long creatorId, long seq, Hash selfHash, Hash otherHash, Instant time,
                byte[][] transactions) {
        long j = 0;
        MessageDigest md = null;

        try {
            md = MessageDigest.getInstance("SHA-384");
        } catch (Exception e) {
            logger.error("error", e);
        }

        hashUniqueness(shardId, creatorId, seq, selfHash, md);
        hashUniqueness(shardId, creatorId, seq, otherHash, md);
        update(md, time == null ? 0 : time.getEpochSecond());
        if (time != null) {
            j = (long) time.getNano();
        }
        update(md, j);
        update(md, transactions);
        this.hash = md.digest();
    }

    static public byte[] hash(int shardId, long creatorId, long seq, byte[] selfHash, byte[] otherHash,
                              Instant time, byte[][] transactions) {
        long j = 0;
        MessageDigest md = null;

        try {
            md = MessageDigest.getInstance("SHA-384");
        } catch (Exception e) {
            logger.error("error", e);
        }

        hashUniqueness(shardId, creatorId, seq, selfHash, md);
        hashUniqueness(shardId, creatorId, seq, otherHash, md);
        update(md, time == null ? 0 : time.getEpochSecond());
        if (time != null) {
            j = (long) time.getNano();
        }
        update(md, j);
        update(md, transactions);
        return md.digest();
    }

    private static void hashUniqueness(int shardId, long creatorId, long seq, Hash hash, MessageDigest md) {
        if (hash != null) {
            md.update(hash.hash);
        } else {
            update(md, shardId);
            update(md, creatorId);
            update(md, seq);
        }
    }

    private static void hashUniqueness(int shardId, long creatorId, long seq, byte[] hash, MessageDigest md) {
        if (hash != null) {
            md.update(hash);
        } else {
            update(md, shardId);
            update(md, creatorId);
            update(md, seq);
        }
    }

    static public byte[] hash(byte[] firstHash, byte[] secondHash) {
        MessageDigest md = null;

        try {
//            md = MessageDigest.getInstance(Constants.SHA3_512, Constants.PROVIDER);
            md = MessageDigest.getInstance("SHA-384");
        } catch (Exception e) {
            logger.error("error: {}", e);
        }

        if (firstHash != null) {
            md.update(firstHash);
        }
        if (secondHash != null) {
            md.update(secondHash);
        }
        return md.digest();
    }

    static public byte[] hash(String firstHash) {
        MessageDigest md = null;

        try {
//            md = MessageDigest.getInstance(Constants.SHA3_512, Constants.PROVIDER);
            md = MessageDigest.getInstance("SHA-384");
        } catch (Exception e) {
            logger.error("error: {}", e);
        }

        if (firstHash != null) {
            md.update(firstHash.getBytes());
        }
        return md.digest();
    }
    static public byte[] hash(String firstHash, String secondHash) {
        MessageDigest md = null;

        try {
//            md = MessageDigest.getInstance(Constants.SHA3_512, Constants.PROVIDER);
            md = MessageDigest.getInstance("SHA-384");
        } catch (Exception e) {
            logger.error("error: {}", e);
        }

        if (firstHash != null) {
            md.update(firstHash.getBytes());
        }
        if (secondHash != null) {
            md.update(secondHash.getBytes());
        }
        return md.digest();
    }


    @Override
    public boolean equals(Object object) {
        if ((object instanceof Hash) && compareTo((Hash) object) == 0) {
            return true;
        }
        return false;
    }

    @Override
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

package one.inve.contract.inve;

import one.inve.contract.ethplugin.core.AccountState;
import one.inve.contract.ethplugin.core.Repository;
import one.inve.contract.ethplugin.datasource.*;
import one.inve.contract.ethplugin.db.RepositoryImpl;
import one.inve.contract.ethplugin.trie.SecureTrie;
import one.inve.contract.ethplugin.trie.Trie;
import one.inve.contract.ethplugin.trie.TrieImpl;
import one.inve.contract.ethplugin.vm.DataWord;
import one.inve.contract.util.PathUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Properties;

import static one.inve.contract.ethplugin.util.ByteUtil.toHexString;

public class INVERepositoryRoot extends RepositoryImpl {
    private static final Logger logger = LoggerFactory.getLogger("contract");
    
    private static class StorageCache extends ReadWriteCache<DataWord, DataWord> {
        Trie<byte[]> trie;

        public StorageCache(Trie<byte[]> trie) {
            super(new SourceCodec<>(trie, Serializers.StorageKeySerializer, Serializers.StorageValueSerializer), WriteCache.CacheType.SIMPLE);
            this.trie = trie;
        }
    }
    private class MultiStorageCache extends MultiCache<StorageCache> {
        public MultiStorageCache() {
            super(null);
        }
        @Override
        protected synchronized StorageCache create(byte[] key, StorageCache srcCache) {
            AccountState accountState = accountStateCache.get(key);
            Serializer<byte[], byte[]> keyCompositor = new NodeKeyCompositor(key);
            Source<byte[], byte[]> composingSrc = new SourceCodec.KeyOnly<>(trieCache, keyCompositor);
            TrieImpl storageTrie = createTrie(composingSrc, accountState == null ? null : accountState.getStateRoot());
            return new StorageCache(storageTrie);
        }

        @Override
        protected synchronized boolean flushChild(byte[] key, StorageCache childCache) {
            if (super.flushChild(key, childCache)) {
                if (childCache != null) {
                    AccountState storageOwnerAcct = accountStateCache.get(key);
                    // need to update account storage root
                    childCache.trie.flush();
                    byte[] rootHash = childCache.trie.getRootHash();
                    accountStateCache.put(key, storageOwnerAcct.withStateRoot(rootHash));
                    return true;
                } else {
                    // account was deleted
                    return true;
                }
            } else {
                // no storage changes
                return false;
            }
        }
    }
    private Source<byte[], byte[]> stateDS;
    private Source<byte[], byte[]> receiptDS;
    private CachedSource.BytesKey<byte[]> trieCache;
    private Trie<byte[]> stateTrie;
    private Source<byte[], byte[]> receiptCache;

    /**
     * Building the following structure for snapshot Repository:
     *
     * stateDS --> trieCache --> stateTrie --> accountStateCodec --> accountStateCache
     *  \                 \
     *   \                 \-->>> storageKeyCompositor --> contractStorageTrie --> storageCodec --> storageCache
     *    \--> codeCache
     *
     *
     * @param stateDS
     * @param root
     */
    public INVERepositoryRoot(final Source<byte[], byte[]> stateDS, final Source<byte[], byte[]> receiptDS, byte[] root) {
        this.stateDS = stateDS;
        this.receiptDS = receiptDS;
        trieCache = new WriteCache.BytesKey<>(stateDS, WriteCache.CacheType.COUNTING);
        stateTrie = new SecureTrie(trieCache, root);

        SourceCodec.BytesKey<AccountState, byte[]> accountStateCodec = new SourceCodec.BytesKey<>(stateTrie, Serializers.AccountStateSerializer);
        final ReadWriteCache.BytesKey<AccountState> accountStateCache = new ReadWriteCache.BytesKey<>(accountStateCodec, WriteCache.CacheType.SIMPLE);
        final MultiCache<StorageCache> storageCache = new MultiStorageCache();

        // counting as there can be 2 contracts with the same code, 1 can suicide
        Source<byte[], byte[]> codeCache = new WriteCache.BytesKey<>(stateDS, WriteCache.CacheType.COUNTING);
        init(accountStateCache, codeCache, storageCache);

        this.receiptCache = new WriteCache.BytesKey<>(receiptDS, WriteCache.CacheType.COUNTING);
    }

    public synchronized void commit(String dbId) {
        super.commit();
        receiptCache.flush();
        try {
            String roothash = toHexString(stateTrie.getRootHash());

            String path = PathUtils.getDataFileDir();
            File file = new File(path + "configdata/" + dbId + "root.cfg");
            if (!file.exists()) file.createNewFile();
            Properties properties = new Properties();
            FileOutputStream out = new FileOutputStream(file);
            properties.setProperty("roothash", roothash);
            properties.store(out, "Root Configuration");
            out.close();
            trieCache.flush();
            logger.debug("State root updated: {}", roothash);

        } catch (Exception e) {
            logger.error("error occurs when trying to write root.cfg.", e);
        }
    }

    @Override
    public synchronized byte[] getRoot() {
        storageCache.flush();
        accountStateCache.flush();
        return stateTrie.getRootHash();
    }

    @Override
    public synchronized void flush() {
        commit();
    }

    @Override
    public Repository getSnapshotTo(byte[] root) {
        return new INVERepositoryRoot(stateDS, receiptDS, root);
    }

    @Override
    public Repository clone() {
        return getSnapshotTo(getRoot());
    }

    @Override
    public synchronized String dumpStateTrie() {
        return ((TrieImpl) stateTrie).dumpTrie();
    }

    @Override
    public synchronized void syncToRoot(byte[] root) {
        stateTrie.setRoot(root);
    }

    protected TrieImpl createTrie(Source<byte[], byte[]> trieCache, byte[] root) {
        return new SecureTrie(trieCache, root);
    }

    public synchronized void setReceipt(byte[] key, byte[] value) {
        receiptCache.put(key, value);
    }

    // public byte[] getReceipt(byte[] address, byte[] nonce) {
    //     byte[] value = new byte[32];
    //     System.arraycopy(nonce, 0, value, 32 - nonce.length, nonce.length);
    //     byte[] key = sha3(ByteUtil.merge(address, value));
    //     return receiptCache.get(key);
    // }

    public byte[] getReceipt(byte[] txHash) {
        return receiptCache.get(txHash);
    }

    // public ReceiptInfo parseReceipt(byte[] receipt) throws Exception {
    //     String type = "";
    //     String status = "";
    //     String result = "";
    //     if (receipt != null) {
    //         switch (receipt[0]) {
    //             case 0x00:
    //                 type = "普通转账";
    //                 break;
    //             case 0x01:
    //                 type = "合约创建";
    //                 break;
    //             case 0x02:
    //                 type = "合约调用";
    //                 break;
    //         }
    //         switch (receipt[1]) {
    //             case 0x00:
    //                 status = "失败";
    //                 break;
    //             case 0x01:
    //                 status = "成功";
    //                 if (receipt[0] == 0x01) {
    //                     //result = "合约地址：";
    //                     byte[] output = new byte[32];
    //                     System.arraycopy(receipt, 2, output, 0, 32);
    //                     result = new String(output, StandardCharsets.UTF_8);
    //                 }
    //                 if (receipt[0] == 0x02) {
    //                     //result = "输出结果：0x";
    //                     byte[] output = new byte[receipt.length - 2];
    //                     System.arraycopy(receipt, 2, output, 0, receipt.length - 2);
    //                     result = toHexString(output);
    //                 }
    //                 break;
    //         }
    //         return new ReceiptInfo(type, status, result);
    //     }
    //     return null;
    // }

    // public AccountInfo getAccountInfo(byte[] address) throws Exception {
    //     String strAddress = new String(address, StandardCharsets.UTF_8);
    //     String strNonce = this.getNonce(address).toString();
    //     String strBalance = this.getBalance(address).toString();
    //     byte[] nonce = ByteUtil.bigIntegerToBytes(this.getNonce(address).subtract(BigInteger.ONE));
    //     byte[] receipt = this.getReceipt(address, nonce);
    //     ReceiptInfo newReceipt = this.parseReceipt(receipt);
    //     AccountInfo ret = new AccountInfo(strAddress, strNonce, strBalance, newReceipt);
    //     return ret;
    // }
}

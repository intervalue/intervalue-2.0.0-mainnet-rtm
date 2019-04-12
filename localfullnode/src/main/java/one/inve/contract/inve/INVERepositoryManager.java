package one.inve.contract.inve;

import one.inve.contract.ethplugin.datasource.*;
import one.inve.contract.ethplugin.datasource.inmem.HashMapDB;
import one.inve.contract.ethplugin.datasource.leveldb.LevelDbDataSource;
import one.inve.util.PathUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;
import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Properties;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class INVERepositoryManager {
    private static final Logger logger = LoggerFactory.getLogger("contract");
    private static HashMap<String, DbSource<byte[]>> dsMap = new HashMap<>();
    public static INVERepositoryRoot repoRoot;

    public static INVERepositoryRoot getRepoRoot(String dbId) {
        
        DbSource<byte[]> stateDS = dsMap.get(dbId + "state");
        if(stateDS == null) {
            stateDS = blockchainDB("rocksdb", dbId + "state");
            dsMap.put(dbId + "state", stateDS);
        }

        DbSource<byte[]> receiptDS = dsMap.get(dbId + "receipt");
        if(receiptDS == null) {
            receiptDS = blockchainDB("rocksdb", dbId + "receipt");
            dsMap.put(dbId + "receipt", stateDS);
        }
        byte[] root = null;
        try {
            //创建目录
            String path = PathUtils.getDataFileDir();
            String dirStr = path + "configdata/";
            File dir = new File(dirStr);
            if(!dir.exists()) {
                dir.mkdirs();
            }
            
            File file = new File(dirStr + dbId + "root.cfg");

            logger.debug("file path of root is: {}", file.getAbsolutePath());

            if (file.exists()) {
                Properties properties = new Properties();
                FileInputStream in = new FileInputStream(file);
                properties.load(in);
                in.close();
                String rootHash = properties.getProperty("roothash");
                if(rootHash != null && rootHash.length() > 0) {
                    root = Hex.decode(rootHash);
                    logger.debug("root hash found, ready to set root: {}", root);
                }
            }
        } catch (Exception e) {
            logger.error("Failed to getRepoRoot!", e);
        }
        return new INVERepositoryRoot(stateDS, receiptDS, root);
    }

    public static void backupDB(String dbId) {
        repoRoot = getRepoRoot(dbId);
        synchronized (repoRoot) {
            INVERepositoryManager repoManager = new INVERepositoryManager();
            repoManager.backupDB(dbId, "state");
            repoManager.backupDB(dbId, "receipt");
        }
    }

    public static void restoreDB(String dbId) {

        INVERepositoryManager cfg = new INVERepositoryManager();
        cfg.restoreDB(dbId, "state");
        cfg.restoreDB(dbId, "receipt");
    }

    private void backupDB(String dbId, String name) {
        try {
            if (name.equals("state")) {
                File from = new File("configdata/" + dbId + "root.cfg");
                File to = new File("configdata/" + dbId + "root_bak.cfg");
                if (from.exists()) {
                    Files.copy(from.toPath(), to.toPath(), REPLACE_EXISTING);
                }
                DbSource<byte[]> stateDS = dsMap.get(dbId + "state");
                if(stateDS != null){
                    INVERocksDbDataSource rd = (INVERocksDbDataSource)stateDS;
                    rd.backup();
                }
            }else if(name.equals("receipt")) {
                DbSource<byte[]> receiptDS = dsMap.get(dbId + "receipt");
                if(receiptDS != null) {
                    INVERocksDbDataSource rd = (INVERocksDbDataSource)receiptDS;
                    rd.backup();
                }
            }
        } catch (Exception e) {
            logger.error("backup database of {} failed.", name, e);
        }
    }

    private void restoreDB(String dbId, String name) {

        DbSettings settings = DbSettings.newInstance()
                .withMaxThreads(Math.max(1, Runtime.getRuntime().availableProcessors() / 2));
        INVERocksDbDataSource rds = new INVERocksDbDataSource();
        try {
            if(name.equals("state")) {  //只有state的数据库牵涉到了 root.cfg
                File from = new File("configdata/" + dbId + "root_bak.cfg");
                File to = new File("configdata/" + dbId + "root.cfg");
                if (from.exists()) {
                    Files.copy(from.toPath(), to.toPath(), REPLACE_EXISTING);
                }
            }
            rds.setName(dbId + name);
            rds.allowRestore();
            rds.init(settings);
        } catch (Exception e) {
            logger.error("restore database of {} failed.", name, e);
        };
    }

    private static DbSource<byte[]> keyValueDataSource(String dbType, String name, DbSettings settings) {
        try {
            DbSource<byte[]> dbSource;
            if ("inmem".equals(dbType)) {
                dbSource = new HashMapDB<>();
            } else if ("leveldb".equals(dbType)){
                dbSource = levelDbDataSource();
            } else {
                dbSource = rocksDbDataSource();
            }
            dbSource.setName(name);
            logger.debug("DbSource set name: {}", dbSource.getName());
            dbSource.init(settings);
            logger.debug("DbSource name {} set", dbSource.getName());
            return dbSource;
        } finally {
            logger.info(dbType + " key-value data source created: " + name);
        }
    }

    private static LevelDbDataSource levelDbDataSource() {
        return new LevelDbDataSource();
    }

    private static INVERocksDbDataSource rocksDbDataSource() {
        return new INVERocksDbDataSource();
    }

    // private void resetDataSource(Source source) {
    //     if (source instanceof DbSource) {
    //         System.out.println("DATABASE TRY: resetDataSource");
    //         ((DbSource) source).reset();
    //     } else {
    //         throw new Error("Cannot cleanup non-db Source");
    //     }
    // }

    private static DbSource<byte[]> blockchainDB(String dbType, String name) {
        DbSettings settings = DbSettings.newInstance()
                .withMaxThreads(Math.max(1, Runtime.getRuntime().availableProcessors() / 2));
        return keyValueDataSource(dbType, name, settings);
    }
}

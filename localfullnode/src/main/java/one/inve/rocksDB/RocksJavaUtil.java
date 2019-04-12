package one.inve.rocksDB;

/**
 * rocksDB工具类
 */

import com.alibaba.fastjson.JSONArray;
import one.inve.beans.dao.TransactionSplit;
import one.inve.core.Config;
import one.inve.util.PathUtils;
import org.rocksdb.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RocksJavaUtil {
    private static final Logger logger = LoggerFactory.getLogger(RocksJavaUtil.class);
    private static String dbPath = PathUtils.getDataFileDir();
    private RocksDB rocksDB;
    public static Map<String, RocksDB> rockSDBMap = new HashMap<>();

    public RocksJavaUtil(String dbId) {

        try {
            rocksDB = rockSDBMap.get(dbId);
            if (rocksDB == null) {

                String rocksDBPath = dbPath + dbId;
                RocksDB.loadLibrary();


                List<ColumnFamilyDescriptor> columnFamilyDescriptors = new ArrayList<>();
                Options options = new Options();
                options.setCreateIfMissing(true);

                List<byte[]> cfs = RocksDB.listColumnFamilies(options, rocksDBPath);
                if (cfs.size() > 0) {
                    for (byte[] cf : cfs) {
                        columnFamilyDescriptors.add(new ColumnFamilyDescriptor(cf, new ColumnFamilyOptions()));
                    }
                } else {
                    columnFamilyDescriptors.add(new ColumnFamilyDescriptor(RocksDB.DEFAULT_COLUMN_FAMILY, new ColumnFamilyOptions()));
                }

                List<ColumnFamilyHandle> columnFamilyHandles = new ArrayList<>();
                DBOptions dbOptions = new DBOptions();
                dbOptions.setCreateIfMissing(true);

                rocksDB = RocksDB.open(dbOptions, rocksDBPath, columnFamilyDescriptors, columnFamilyHandles);
                rockSDBMap.put(dbId, rocksDB);
            }
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        }
    }

    public void set(String key) {
        try {
            rocksDB.put(key.getBytes(), key.getBytes());

        } catch (Exception ex) {
            logger.error("rocksDB.put异常", ex);
        }
    }

    public void put(String key, String value) {
        try {
            rocksDB.put(key.getBytes(), value.getBytes());

        } catch (Exception ex) {
            logger.error("rocksDB.put异常", ex);
        }
    }
    public void put(String key, byte[] value) {
        try {
            rocksDB.put(key.getBytes(), value);

        } catch (Exception ex) {
            logger.error("rocksDB.put异常", ex);
        }
    }
    public byte[] get(String key) {
        try {

            return rocksDB.get(key.getBytes());
        } catch (Exception ex) {
            logger.error("rocksDB.get异常", ex);
        }
        return null;
    }

    public void delete(String key) {
        try {
            rocksDB.delete(key.getBytes());
        } catch (Exception ex) {
            logger.error("rocksDB.delete异常", ex);
        }
    }

    public static void main(String[] args) {
        RocksJavaUtil test = new RocksJavaUtil("50");
        MyThread thread = test.new MyThread();
        thread.start();
        test = new RocksJavaUtil("50");
        thread = test.new MyThread();
        thread.start();
        test = new RocksJavaUtil("50");
        thread = test.new MyThread();
        thread.start();
        test = new RocksJavaUtil("50");
        thread = test.new MyThread();
        thread.start();

    }

    class MyThread extends Thread {
        @Override
        public void run() {
            int i = 0;
            RocksJavaUtil t = null;
            while (i < Integer.MAX_VALUE) {
                System.out.println(i + "========== while循环============" + this.getName());
                TransactionSplit split = new TransactionSplit();
                split.setTableName(Config.MESSAGES + "_0");
                split.setTableIndex(BigInteger.ZERO);
                split.setTableNamePrefix(Config.MESSAGES);
                split.setTotal(0);
                //test.testDefaultColumnFamily();
                //test.testCertainColumnFamily("test", JSONArray.toJSONString(s));
                t = new RocksJavaUtil("50");
                for (int j = 0; j < 100; j++) {
                    t.put("1test" + j, JSONArray.toJSONString(split));
                    t.put("2test" + j, JSONArray.toJSONString(split));
                    t.put("3test" + j, JSONArray.toJSONString(split));
                    t.put("4test" + j, JSONArray.toJSONString(split));
                    t.put("5test" + j, JSONArray.toJSONString(split));
                    t.put("6test" + j, JSONArray.toJSONString(split));
                    t.put("7test" + j, JSONArray.toJSONString(split));
                    t.put("8test" + j, JSONArray.toJSONString(split));
                    // byte[] value = t.get("test");
                   /* System.out.println(new String(value));
                    if (value != null) {
                        System.out.println(new String(t.get("test")));
                    }*/
                }
                i++;
            }
            System.out.println(i + "========== 循坏结束============" + this.getName());
        }
    }
}
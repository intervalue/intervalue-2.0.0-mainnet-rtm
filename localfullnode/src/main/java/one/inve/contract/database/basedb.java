package one.inve.contract.database;

import one.inve.contract.ethplugin.datasource.rocksdb.RocksDbDataSource;
import one.inve.core.Cryptos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 RocksDb的操作类
 */
public class basedb {
    private static final Logger logger = LoggerFactory.getLogger(basedb.class);

    public static String accountDbName="account";
    public static String stateDbName="worldstate";
    public static RocksDbDataSource accountRoscksDataSource=null;//操作AccountDB的RocksDB的句柄
    //public static RocksDbDataSource stateRoscksDataSource=null;//操作State的RocksDB的句柄
    /**
     *  @Function:Open an exisiting database
     *  @param :
     *  @return RocksDbDataSource dataSource
     */
    public static void openDataSourceAccount() {
        try {
            if (accountRoscksDataSource==null)
            {
                accountRoscksDataSource = new RocksDbDataSource(accountDbName);
                accountRoscksDataSource.addition();
            }
        } catch (Exception e) {
            logger.error("error", e);
        }
    }

    /**
     *  @Function:Open create a new database
     *  @param dbname: name of database
     *  @return RocksDbDataSource dataSource
     */
    public static void createDataSourceAccount() {
        try {
            if (accountRoscksDataSource==null)
            {
                accountRoscksDataSource = new RocksDbDataSource(accountDbName);
                accountRoscksDataSource.reset();
            }
        } catch (Exception e) {
            logger.error("error", e);
        }
    }

    /**
     *  @Function:Open close datasource of accountdb
     *  @param
     *  @return
     */
    public static void closeAccountDataSource() {
        try {
            if (accountRoscksDataSource==null)
            {
                accountRoscksDataSource.close();
            }
        } catch (Exception e) {
            logger.error("error", e);
        }
    }

}//end class basedb

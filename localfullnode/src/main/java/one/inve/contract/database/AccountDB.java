package one.inve.contract.database;
import one.inve.contract.struct.AccountInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static one.inve.contract.ethplugin.util.ByteUtil.bytesToAscii;


/**
 有关账户信息存储，查询的相关操作
 */
public  class AccountDB {
    private static final Logger logger = LoggerFactory.getLogger(AccountDB.class);

    /**
     *  @Function:Add a new account,save it to database
     *  @param account:Account Information
     *  @return if add account success return 1 else return 0
     */
    public static long addAccount(AccountInfo account) {
        try {
            if (basedb.accountRoscksDataSource==null)
            {
                basedb.openDataSourceAccount();
            }
            String key=account.accountAddress;
            String value=AccountJson.AccountInfo2JsonStr(account);
            basedb.accountRoscksDataSource.put(key.getBytes(), value.getBytes());
            System.out.println(basedb.accountRoscksDataSource.keys().size());
        } catch (Exception ex) {
            String strex=ex.toString();
            logger.info("addAccount():"+strex);
        }
        return 0;
    }


    /**
     *  @Function:Query
     *  @param
     *  @return
     */
    public static AccountInfo getAccount(String accountAddress) {
        AccountInfo result=new AccountInfo("",0,0,0,"");
        try {
            if (basedb.accountRoscksDataSource==null)
            {
                basedb.openDataSourceAccount();
                System.out.println(basedb.accountRoscksDataSource.keys().size());
            }
            String value=bytesToAscii(basedb.accountRoscksDataSource.get(accountAddress.getBytes()));
            result=AccountJson.JsonStr2AccountInfo(value);
        } catch (Exception ex) {
            String strex=ex.toString();
            logger.info("getAccount():"+strex);
        }
        return result;
    }
}//end class AccountDB

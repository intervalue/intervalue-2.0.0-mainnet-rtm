package one.inve.contract.database;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import one.inve.contract.struct.AccountInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AccountJson {
    private static final Logger logger = LoggerFactory.getLogger(AccountJson.class);

    // String  JSON_OBJ_STR8 = "{\"accountAddress\":\"hs4\",\"accountType\":1,,\"balance\":10,\"tranactionHash\":\"0x00000000006\"}";
    //将上述字符串转换为AccountInfo结构
    public static AccountInfo JsonStr2AccountInfo(String JsonStr)
    {
        AccountInfo info=new AccountInfo("",0,0,0,"");
        try {
            JSONObject jsonObject = JSON.parseObject(JsonStr);
            info.accountAddress=jsonObject.getString("accountAddress");
            info.accountType=jsonObject.getInteger("accountType");
            info.balance=jsonObject.getInteger("balance");
            //info.tranactionHash=jsonObject.getString("tranactionHash");
        } catch (Exception e) {
            logger.error("error", e);
        }
        return info;
    }

    //将AccountInfo结构，转换为String  JSON_OBJ_STR8 = "{\"accountAddress\":\"hs4\",\"accountType\":1,,\"balance\":10,\"tranactionHash\":\"0x00000000006\"}";
    //这种字符串
    public static String AccountInfo2JsonStr(AccountInfo info)
    {
       String JsonStr="";
        try {
            JsonStr = JSON.toJSONString(info);
        } catch (Exception e) {
            logger.error("error", e);
        }
        return JsonStr;
    }

}//End class AccountJson

package one.inve.util;

import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * 上线倒计时工具类
 * Created by Clare  on 2018/6/28 0028.
 */
public class OnlineUtils {
    static SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssz");
    static String _timeZome = "GMT+8:00";

    /**
     * GMT+8时区获取设置的上线时间戳
     * @param onlineTime 设置的上线时间串
     * @return
     */
    public static long getOnlineTimestampGMT8(String onlineTime) {
        SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone(_timeZome));
        sdf1.setTimeZone(TimeZone.getTimeZone(_timeZome));
        long onlineTimestampGMT9 = Integer.MAX_VALUE;
        try {
            Date date = sdf.parse(onlineTime);

            System.out.println("======================= online time: " + sdf1.format(date) + " (" + _timeZome + ") ================");
            onlineTimestampGMT9 = date.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return onlineTimestampGMT9;
    }

    public static long getNowTimestampGMT8() {
        long nowTimestampGMT9 = -1L;
        try {
//            URL url = new URL("http://www.ntsc.ac.cn");// 取得资源对象
            URL url = new URL("http://time.windows.com");// 取得资源对象
            URLConnection uc = url.openConnection();// 生成连接对象
            uc.connect(); // 发出连接
            long ld = uc.getDate(); // 取得网站日期时间
            Date newDate = new Date(ld); // 转换为标准时间对象
            SimpleDateFormat sdfw = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            sdfw.setTimeZone(TimeZone.getTimeZone(_timeZome));
            System.out.println("now time: " +  sdfw.format(newDate) + " (" + _timeZome + ")");

            nowTimestampGMT9 = newDate.getTime();
        } catch (Exception e) {
            e.printStackTrace();
            return getNowTimestampGMT8();
        }
        return nowTimestampGMT9;
    }
}

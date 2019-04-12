package one.inve.util;

import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import org.slf4j.Logger; import org.slf4j.LoggerFactory;

/**
 * 上线倒计时工具类
 * @author Clare
 * @date   2018/6/28 0028.
 */
public class OnlineUtils {
    private static final Logger logger = LoggerFactory.getLogger(OnlineUtils.class);
    static SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssz");
    static String timeZome = "GMT+8:00";

    /**
     * GMT+8时区获取设置的上线时间戳
     * @param onlineTime 设置的上线时间串
     * @return
     */
    public static long getOnlineTimestampGMT8(String onlineTime) {
        SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone(timeZome));
        sdf1.setTimeZone(TimeZone.getTimeZone(timeZome));
        long onlineTimestampGMT9 = Integer.MAX_VALUE;
        try {
            Date date = sdf.parse(onlineTime);

            logger.info("======================= online time: {} ({}) ================", sdf1.format(date),  timeZome);
            onlineTimestampGMT9 = date.getTime();
        } catch (ParseException e) {
            logger.error("error: {}", e);
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
            sdfw.setTimeZone(TimeZone.getTimeZone(timeZome));
            logger.info("now time: {} ({})", sdfw.format(newDate), timeZome);

            nowTimestampGMT9 = newDate.getTime();
        } catch (Exception e) {
            logger.error("error: {}", e);
            return getNowTimestampGMT8();
        }
        return nowTimestampGMT9;
    }
}

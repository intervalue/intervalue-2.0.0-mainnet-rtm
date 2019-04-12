package one.inve.contract.util;

/**
 * inve地址（String类型）在16进制之间的转换
 * @author 肖毅
 * @since 2018-11-15
 */
public class AddressUtil {

    /**
     * 将字符串转换为 16 进制字符串
     * @param s：被转换的字符串
     * @return HEX串
     */
    public static String strTo16(String s) {
        String str = "";

        if (s == null) {
            return str;
        }

        for (int i = 0; i < s.length(); i++) {
            int ch = (int) s.charAt(i);
            String s4 = Integer.toHexString(ch);
            str = str + s4;
        }
        return str;
    }

    /**
     * 16进制转换成为string类型字符串
     * @param s：被转换的字符串
     * @return String字符串
     */
    public static String hex2String(String s) {
        if (s == null || s.equals("")) {
            return null;
        }
        s = s.replace(" ", "");
        byte[] baKeyword = new byte[s.length() / 2];
        for (int i = 0; i < baKeyword.length; i++) {
            try {
                baKeyword[i] = (byte) (0xff & Integer.parseInt(s.substring(i * 2, i * 2 + 2), 16));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        try {
            s = new String(baKeyword, "UTF-8");
            new String();
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        return s;
    }
}
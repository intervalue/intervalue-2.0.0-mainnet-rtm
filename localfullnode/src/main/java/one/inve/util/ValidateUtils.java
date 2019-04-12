package one.inve.util;

import org.slf4j.Logger; import org.slf4j.LoggerFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 校验工具类
 * @author Clare
 * @date
 */
public class ValidateUtils {
	private static final Logger logger = LoggerFactory.getLogger(ValidateUtils.class);

	private static final Pattern NUM_PATTERN = Pattern.compile("[0-9]*");

	/**
	 * check length
	 * 
	 * @param value
	 * @param length
	 * @param desc
	 * @return String
	 */
	public static String checkLength(String value, int length, String desc) {
		if (!"".equals(value) && getLength(value) != length) {
			return desc + "长度必须为" + length + ";";
		}
		return "";
	}

	public static int getLength(String value) {
		int length = 0;

		char[] chars = value.toCharArray();
		for (int i = 0; i < chars.length; i++) {
			if ((chars[i]) > 0x80) {
				length += 2;
			} else {
				length += 1;
			}
		}

		return length;
	}

	/**
	 * check min length
	 * 
	 * @param value
	 * @param length
	 * @param desc
	 * @return String
	 */
	public static String checkMinLength(String value, int length, String desc) {
		if (!"".equals(value) && getLength(value) < length) {
			return desc + "最小长度不能低于" + length + ";";
		}
		return "";
	}

	/**
	 * check max length
	 * 
	 * @param value
	 * @param length
	 * @param desc
	 * @return String
	 */
	public static String checkMaxLength(String value, int length, String desc) {
		if (!"".equals(value) && getLength(value) > length) {
			return desc + "最大长度不能超过" + length + ";";
		}
		return "";
	}

	/**
	 * check text
	 * 
	 * @param value
	 * @param desc
	 * @return String
	 */
	public static String checkText(String value, String desc) {
		if ("".equals(value)) {
			return desc + "不能为空;";
		}
		return "";
	}

	/**
	 * check numeric
	 * 
	 * @param value
	 * @param format
	 * @param desc
	 * @return String
	 */
	public static String checkNumeric(String value, String format, String desc) {
		String expression = "[+-]?\\d+";
		String checkdesc = "必须为整数";
		if (format != null && format.indexOf(".") != -1) {
			expression = "[+-]?\\d+(\\.\\d{1,"
					+ (format.length() - format.indexOf(".") - 1) + "})?";
			checkdesc = "必须为数字格式(" + format + ")";
		}
		if (!"".equals(value) && !isMatches(value, expression)) {
			return desc + checkdesc + ";";
		}
		return "";
	}

	/**
	 * is matches
	 * 
	 * @param str
	 * @param regex
	 * @return boolean
	 */
	public static boolean isMatches(String str, String regex) {
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(str);
		return matcher.matches();
	}

	/**
	 * check date
	 * 
	 * @param value
	 * @param format
	 * @param desc
	 * @return String
	 */
	public static String checkDate(String value, String format, String desc) {
		String expression = "(\\d{1,4})(-|\\/)(\\d{1,2})\\2(\\d{1,2})";

		if ("yyyy-MM-dd".equals(format)) {
			expression = "(\\d{1,4})(-|\\/)(\\d{1,2})\\2(\\d{1,2})";
		} else if ("yyyy-MM-dd HH:mm".equals(format)) {
			expression = "(\\d{1,4})(-|\\/)(\\d{1,2})\\2(\\d{1,2}) (\\d{1,2}):(\\d{1,2})";
		} else if ("yyyy-MM-dd HH:mm:ss".equals(format)) {
			expression = "(\\d{1,4})(-|\\/)(\\d{1,2})\\2(\\d{1,2}) (\\d{1,2}):(\\d{1,2}):(\\d{1,2})";
		} else if ("HH:mm:ss".equals(format)) {
			expression = "(\\d{1,2})(:)?(\\d{1,2})\\2(\\d{1,2})";
		} else if ("yyyy".equals(format)) {
			expression = "(\\d{1,4})";
		} else if ("yyyy-MM".equals(format)) {
			expression = "(\\d{1,4})(-|\\/)(\\d{1,2})";
		} else if ("HH".equals(format)) {
			expression = "(\\d{1,2})";
		} else if ("HH:mm".equals(format)) {
			expression = "(\\d{1,2})(:)?(\\d{1,2})";
		} else if ("yyyy-MM-dd HH".equals(format)) {
			expression = "(\\d{1,4})(-|\\/)(\\d{1,2})\\2(\\d{1,2}) (\\d{1,2})";
		}

		if (!"".equals(value) && !isMatches(value, expression)) {
			return desc + "必须为时间格式(" + format + ");";
		}

		return "";
	}

	/**
	 * check date
	 *
	 * @param value
	 * @param format
	 * @return String
	 */
	public static boolean checkDate(String value, String format) {
		String expression = "(\\d{1,4})(-|\\/)(\\d{1,2})\\2(\\d{1,2})";

		if ("yyyy-MM-dd".equals(format)) {
			expression = "(\\d{1,4})(-|\\/)(\\d{1,2})\\2(\\d{1,2})";
		} else if ("yyyy-MM-dd HH:mm".equals(format)) {
			expression = "(\\d{1,4})(-|\\/)(\\d{1,2})\\2(\\d{1,2}) (\\d{1,2}):(\\d{1,2})";
		} else if ("yyyy-MM-dd HH:mm:ss".equals(format)) {
			expression = "(\\d{1,4})(-|\\/)(\\d{1,2})\\2(\\d{1,2}) (\\d{1,2}):(\\d{1,2}):(\\d{1,2})";
		} else if ("HH:mm:ss".equals(format)) {
			expression = "(\\d{1,2})(:)?(\\d{1,2})\\2(\\d{1,2})";
		} else if ("yyyy".equals(format)) {
			expression = "(\\d{1,4})";
		} else if ("yyyy-MM".equals(format)) {
			expression = "(\\d{1,4})(-|\\/)(\\d{1,2})";
		} else if ("HH".equals(format)) {
			expression = "(\\d{1,2})";
		} else if ("HH:mm".equals(format)) {
			expression = "(\\d{1,2})(:)?(\\d{1,2})";
		} else if ("yyyy-MM-dd HH".equals(format)) {
			expression = "(\\d{1,4})(-|\\/)(\\d{1,2})\\2(\\d{1,2}) (\\d{1,2})";
		} else if ("yyyyMMddHHmmss".equals(format)) {
			expression = "(\\d{1,4})(\\d{1,2})(\\d{1,2})(\\d{1,2})(\\d{1,2})(\\d{1,2})";
		}

		return !"".equals(value) && isMatches(value, expression);
	}

	/**
	 * 是否合法邮箱
	 * @param email
	 * @return
	 * @throws Exception
	 */
	public static boolean isEmail(String email) throws Exception {
		if (null == email || "".equals(email)) {
			return false;
		}
		String str = "^([a-zA-Z0-9_\\-\\.]+)@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.)|(([a-zA-Z0-9\\-]+\\.)+))([a-zA-Z]{2,4}|[0-9]{1,3})(\\]?)$";
		Pattern p = Pattern.compile(str);
		Matcher m = p.matcher(email);
		return m.matches();
	}

	/**
	 * 是否合法ip
	 * @param ip
	 * @return
	 * @throws Exception
	 */
	public static boolean isIp(String ip) throws Exception {
		if (null == ip || "".equals(ip)) {
			return false;
		}
		String str = "^([1-9]|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])(\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])){3}$";
		Pattern p = Pattern.compile(str);
		Matcher m = p.matcher(ip);
		return m.matches();
	}
	/**
	 * 是否合法端口
	 * @param port
	 * @return
	 * @throws Exception
	 */
	public static boolean isPort(String port) throws Exception {
		if (null == port || "".equals(port)) {
			return false;
		}
		String str = "^([0-9]|[1-9]\\d|[1-9]\\d{2}|[1-9]\\d{3}|[1-5]\\d{4}|6[0-4]\\d{3}|65[0-4]\\d{2}|655[0-2]\\d|6553[0-5])$";
		Pattern p = Pattern.compile(str);
		Matcher m = p.matcher(port);
		return m.matches();
	}

	/**
	 * 是否合法手机号
	 * @param phoneNumber
	 * @return
	 * @throws Exception
	 */
	public static boolean isMobilePhone(String phoneNumber) throws Exception {
		if (null == phoneNumber || "".equals(phoneNumber)) {
			return false;
		}
		boolean isValid = false;

		String expression = "((^(13|15|18)[0-9]{9}$)|(^0[1,2]{1}\\d{1}-?\\d{8}$)|(^0[3-9] {1}\\d{2}-?\\d{7,8}$)|(^0[1,2]{1}\\d{1}-?\\d{8}-(\\d{1,4})$)|(^0[3-9]{1}\\d{2}-? \\d{7,8}-(\\d{1,4})$))";
		CharSequence inputStr = phoneNumber;

		Pattern pattern = Pattern.compile(expression);

		Matcher matcher = pattern.matcher(inputStr);

		if (matcher.matches()) {
			isValid = true;
		}

		return isValid;
	}

	/**
	 * 校验字符串是否为数字
	 * 
	 * @param str
	 * @return
	 * @throws Exception
	 */
	public static boolean isNum(String str) throws Exception {
		Matcher isNum = NUM_PATTERN.matcher(str);

		if (isNum.matches()) {
			return true;
		}

		return false;
	}
	/**
	 * 校验unit是否本片轻节点发送的
	 * @param unit 轻节点发送的unit
	 * @param shardId 分片Id
	 * @param shardCount 总分片数
	 * @return unit是否本片轻节点发送的
	 */
	public static boolean isValidUnit(String unit, String shardId, int shardCount) {
		int idx = unit.indexOf("pubkey");
		String pubkey = unit.substring(idx+9, idx+9+44);
		return shardId.equals(generateShardIdByPubKey(shardCount, pubkey));
	}
	/**
	 * 通过pubkey计算片ID
	 * @param shardCount 片数
	 * @param pubkey 公钥
	 * @return 片ID
	 */
	public static String generateShardIdByPubKey(int shardCount, String pubkey) {
		if (StringUtils.isEmpty(pubkey)) {
			String msg = "Parameter pubkey is empty！";
			logger.error(msg);
			throw new Error(msg);
		}
		if (StringUtils.isEmpty(shardCount+"")) {
			String msg = "Parameter shardCount is empty！";
			logger.error(msg);
			throw new Error(msg);
		}
		return ""+(pubkey.charAt(pubkey.length()-1) % shardCount);
	}
}

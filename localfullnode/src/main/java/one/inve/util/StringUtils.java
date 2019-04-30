package one.inve.util;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.ArrayUtils;

/**
 * 字符串处理工具类
 * 
 * @date
 */
public class StringUtils extends org.apache.commons.lang3.StringUtils {
	public static final byte[] BOM = { -17, -69, -65 };

	public static final char[] HexDigits = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e',
			'f' };

	public static Pattern PHtml = Pattern.compile("<[^>]+>", 32);

	public static Pattern PComment = Pattern.compile("<\\!\\-\\-.*?\\-\\->", 34);

	public static Pattern PScript = Pattern.compile("<script[^>]*?>[\\s\\S]*?<\\/script>", 34);

	public static Pattern PStyle = Pattern.compile("<style[^>]*?>[\\s\\S]*?<\\/style>", 34);

	public static final Pattern PTitle = Pattern.compile("<title>(.+?)</title>", 34);

	public static final Pattern PLetterOrDigit = Pattern.compile("^\\w*$", 34);

	public static final Pattern PLetter = Pattern.compile("^[A-Za-z]*$", 34);

	public static final Pattern PDigit = Pattern.compile("^\\d*$", 34);

	private static Pattern PChinese = Pattern.compile("[^一-龥]+", 34);

	private static Pattern PID = Pattern.compile("[\\w\\s\\_\\.\\,]*", 34);

	public static String md5Hex(String src) {
		try {
			MessageDigest md5 = MessageDigest.getInstance("MD5");
			byte[] md = md5.digest(src.getBytes());
			return hexEncode(md);
		} catch (Exception e) {
		}
		return null;
	}

	public static String sha1Hex(String src) {
		try {
			MessageDigest md5 = MessageDigest.getInstance("SHA1");
			byte[] md = md5.digest(src.getBytes());
			return hexEncode(md);
		} catch (Exception e) {
		}
		return null;
	}

	public static String md5Base64(String str) {
		try {
			MessageDigest md5 = MessageDigest.getInstance("MD5");
			return base64Encode(md5.digest(str.getBytes()));
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String md5Base64FromHex(String md5str) {
		char[] cs = md5str.toCharArray();
		byte[] bs = new byte[16];
		for (int i = 0; i < bs.length; i++) {
			char c1 = cs[(i * 2)];
			char c2 = cs[(i * 2 + 1)];
			byte m1 = 0;
			byte m2 = 0;
			for (byte k = 0; k < 16; k = (byte) (k + 1)) {
				if (HexDigits[k] == c1) {
					m1 = k;
				}
				if (HexDigits[k] == c2) {
					m2 = k;
				}
			}
			bs[i] = (byte) (m1 << 4 | 0 + m2);
		}

		String newstr = base64Encode(bs);
		return newstr;
	}

	public static String md5HexFromBase64(String base64str) {
		return hexEncode(base64Decode(base64str));
	}

	public static String hexEncode(byte[] data) {
		int l = data.length;
		char[] out = new char[l << 1];
		int i = 0;
		for (int j = 0; i < l; i++) {
			out[(j++)] = HexDigits[((0xF0 & data[i]) >>> 4)];
			out[(j++)] = HexDigits[(0xF & data[i])];
		}
		return new String(out);
	}

	public static byte[] hexDecode(String str) throws Exception {
		char[] data = str.toCharArray();
		int len = data.length;
		if ((len & 0x1) != 0) {
			throw new Exception("StringUtil.hexEncode:Odd number of characters.");
		}

		byte[] out = new byte[len >> 1];
		int i = 0;
		for (int j = 0; j < len; i++) {
			int f = toDigit(data[j], j) << 4;
			j++;
			f |= toDigit(data[j], j);
			j++;
			out[i] = (byte) (f & 0xFF);
		}

		return out;
	}

	private static int toDigit(char ch, int index) throws Exception {
		int digit = Character.digit(ch, 16);
		if (digit == -1) {
			throw new Exception("StringUtil.hexDecode:Illegal hexadecimal character " + ch + " at index " + index);
		}
		return digit;
	}

	public static String byteToBin(byte[] bs) {
		char[] cs = new char[bs.length * 9];
		for (int i = 0; i < bs.length; i++) {
			byte b = bs[i];
			int j = i * 9;
			cs[j] = ((b >>> 7 & 0x1) == 1 ? 49 : '0');
			cs[(j + 1)] = ((b >>> 6 & 0x1) == 1 ? 49 : '0');
			cs[(j + 2)] = ((b >>> 5 & 0x1) == 1 ? 49 : '0');
			cs[(j + 3)] = ((b >>> 4 & 0x1) == 1 ? 49 : '0');
			cs[(j + 4)] = ((b >>> 3 & 0x1) == 1 ? 49 : '0');
			cs[(j + 5)] = ((b >>> 2 & 0x1) == 1 ? 49 : '0');
			cs[(j + 6)] = ((b >>> 1 & 0x1) == 1 ? 49 : '0');
			cs[(j + 7)] = ((b & 0x1) == 1 ? 49 : '0');
			cs[(j + 8)] = ',';
		}
		return new String(cs);
	}

	public static String byteArrayToHexString(byte[] b) {
		StringBuilder resultSb = new StringBuilder();
		byte[] arrayOfByte = b;
		int j = b.length;
		for (int i = 0; i < j; i++) {
			byte element = arrayOfByte[i];
			resultSb.append(byteToHexString(element));
			resultSb.append(" ");
		}
		return resultSb.toString();
	}

	private static String byteToHexString(byte b) {
		int n = b;
		if (n < 0) {
			n += 256;
		}
		int d1 = n / 16;
		int d2 = n % 16;
		return String.valueOf(HexDigits[d1] + HexDigits[d2]);
	}

	public static boolean isUTF8(byte[] bs) {
		if ((bs.length > 3) && (bs[0] == BOM[0]) && (bs[1] == BOM[1]) && (bs[2] == BOM[2])) {
			return true;
		}
		int encodingBytesCount = 0;
		byte[] arrayOfByte = bs;
		int j = bs.length;
		for (int i = 0; i < j; i++) {
			byte element = arrayOfByte[i];
			byte c = element;
			if (encodingBytesCount == 0) {
				if ((c & 0x80) == 0) {
					continue;
				}
				if ((c & 0xC0) == 192) {
					encodingBytesCount = 1;
					c = (byte) (c << 2);

					while ((c & 0x80) == 128) {
						c = (byte) (c << 1);
						encodingBytesCount++;
					}
				} else {
					return false;
				}

			} else if ((c & 0xC0) == 128) {
				encodingBytesCount--;
			} else {
				return false;
			}

		}

		return encodingBytesCount == 0;
	}

	public static String base64Encode(byte[] b) {
		if (b == null) {
			return null;
		}
		return new String(Base64.encodeBase64(b));
	}

	public static byte[] base64Decode(String s) {
		if (s != null) {
			try {
				return Base64.decodeBase64(s.getBytes());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	public static String[] splitEx(String str, String spliter) {
		char escapeChar = '\\';
		if ("\\".equals(spliter)) {
			escapeChar = '&';
		}
		return splitEx(str, spliter, escapeChar);
	}

	public static String[] splitEx(String str, String spliter, char escapeChar) {
		if (str == null) {
			return new String[0];
		}
		ArrayList<String> list = new ArrayList<String>();
		if ((spliter == null) || ("".equals(spliter)) || (str.length() < spliter.length())) {
			return new String[] { str };
		}
		int length = spliter.length();
		int lastIndex = 0;
		int lastStart = 0;
		while (true) {
			int i = str.indexOf(spliter, lastIndex);
			if (i < 0) {
				break;
			}
			if ((i > 0) && (str.charAt(i - 1) == escapeChar)) {
				lastIndex = i + 1;
				continue;
			}
			list.add(str.substring(lastStart, i));
			lastStart = lastIndex = i + length;
		}
		if (lastStart <= str.length()) {
			list.add(str.substring(lastStart));
		}

		String[] arr = new String[list.size()];
		for (int i = 0; i < arr.length; i++) {
			arr[i] = ((String) list.get(i));
		}
		return arr;
	}

	public static String replaceEx(String str, String subStr, String reStr) {
		if ((str == null) || (str.length() == 0) || (reStr == null)) {
			return str;
		}
		if ((subStr == null) || (subStr.length() == 0) || (subStr.length() > str.length())) {
			return str;
		}
		StringBuilder sb = null;
		int lastIndex = 0;
		while (true) {
			int index = str.indexOf(subStr, lastIndex);
			if (index < 0) {
				break;
			}
			if (sb == null) {
				sb = new StringBuilder();
			}
			sb.append(str.substring(lastIndex, index));
			sb.append(reStr);

			lastIndex = index + subStr.length();
		}
		if (lastIndex == 0) {
			return str;
		}
		sb.append(str.substring(lastIndex));
		return sb.toString();
	}

	public static String replaceAllIgnoreCase(String source, String oldstring, String newstring) {
		Pattern p = Pattern.compile(oldstring, 34);
		Matcher m = p.matcher(source);
		return m.replaceAll(newstring);
	}

	public static String urlEncode(String str) {
		return urlEncode(str, "UTF-8");
	}

	public static String urlDecode(String str) {
		return urlDecode(str, "UTF-8");
	}

	public static String urlEncode(String str, String charset) {
		try {
			return URLEncoder.encode(str, charset);
		} catch (Exception e) {
		}
		return str;
	}

	public static String urlDecode(String str, String charset) {
		try {
			return URLDecoder.decode(str, charset);
		} catch (Exception e) {
		}
		return str;
	}

	public static String escape(String src) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < src.length(); i++) {
			char j = src.charAt(i);
			if ((Character.isDigit(j)) || (Character.isLowerCase(j)) || (Character.isUpperCase(j))) {
				sb.append(j);
			} else if (j < 'Ā') {
				sb.append("%");
				if (j < '\020') {
					sb.append("0");
				}
				sb.append(Integer.toString(j, 16));
			} else {
				sb.append("%u");
				sb.append(Integer.toString(j, 16));
			}
		}
		return sb.toString();
	}

	public static String leftPad(String srcString, char c, int length) {
		if (srcString == null) {
			srcString = "";
		}
		int tLen = srcString.length();

		if (tLen >= length) {
			return srcString;
		}
		int iMax = length - tLen;
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < iMax; i++) {
			sb.append(c);
		}
		sb.append(srcString);
		return sb.toString();
	}

	public static String subString(String src, int length) {
		if (src == null) {
			return null;
		}
		int i = src.length();
		if (i > length) {
			return src.substring(0, length);
		}
		return src;
	}

	public static String subStringEx(String src, int length) {
		if (src == null) {
			return null;
		}
		int m = 0;
		try {
			byte[] b = src.getBytes("Unicode");
			for (int i = 2; i < b.length; i += 2) {
				if (b[(i + 1)] == 0) {
					m++;
				} else {
					m += 2;
				}
				if (m > length) {
					return src.substring(0, (i - 2) / 2);
				}
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			throw new RuntimeException("String.getBytes(\"Unicode\") failed");
		}
		return src;
	}

	/**
	 * 生成随机字符串
	 * 
	 * @return 字符串
	 */
	public static String generateRandomString() {
		char[] chr = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I',
				'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z' };
		int length = 20;
		Random random = new Random();
		StringBuffer buffer = new StringBuffer();
		for (int i = 0; i < length; i++) {
			buffer.append(chr[random.nextInt(chr.length)]);
		}
		return buffer.toString();
	}

	public static String rightPad(String srcString, char c, int length) {
		if (srcString == null) {
			srcString = "";
		}
		int tLen = srcString.length();

		if (tLen >= length) {
			return srcString;
		}
		int iMax = length - tLen;
		StringBuilder sb = new StringBuilder();
		sb.append(srcString);
		for (int i = 0; i < iMax; i++) {
			sb.append(c);
		}
		return sb.toString();
	}

	public static String toSBC(String input) {
		char[] c = input.toCharArray();
		for (int i = 0; i < c.length; i++) {
			if (c[i] == ' ') {
				c[i] = '　';
			} else {
				boolean flag = ((c[i] > '@') && (c[i] < '[')) || ((c[i] > '`') && (c[i] < '{'));
				if (flag) {
					continue;
				}
				if (c[i] < '') {
					c[i] = (char) (c[i] + 65248);
				}
			}
		}
		return new String(c);
	}

	public static String toNSBC(String input) {
		char[] c = input.toCharArray();
		for (int i = 0; i < c.length; i++) {
			if (c[i] == ' ') {
				c[i] = '　';
			} else if (c[i] < '') {
				c[i] = (char) (c[i] + 65248);
			}
		}
		return new String(c);
	}

	public static String toDBC(String input) {
		char[] c = input.toCharArray();
		for (int i = 0; i < c.length; i++) {
			if (c[i] == '　') {
				c[i] = ' ';
			} else if ((c[i] > 65280) && (c[i] < 65375)) {
				c[i] = (char) (c[i] - 65248);
			}
		}
		return new String(c);
	}

	public static String getHtmlTitle(String html) {
		Matcher m = PTitle.matcher(html);
		if (m.find()) {
			return m.group(1).trim();
		}
		return null;
	}

	public static String capitalize(String str) {
		if ((str == null) || (str.length() == 0)) {
			return str;
		}
		return Character.toTitleCase(str.charAt(0)) + str.substring(1);
	}

	public static boolean isEmpty(String str) {
		int strLen;
		if (str == null || (strLen = str.length()) == 0) {
			return true;
		}
		for (int i = 0; i < strLen; i++) {
			if ((Character.isWhitespace(str.charAt(i)) == false)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * 检查对象是否为数字型字符串。
	 */
	public static boolean isNumeric(Object obj) {
		if (obj == null) {
			return false;
		}
		String str = obj.toString();
		int sz = str.length();
		for (int i = 0; i < sz; i++) {
			if (!Character.isDigit(str.charAt(i))) {
				return false;
			}
		}
		return true;
	}

	public static boolean isNotEmpty(String str) {
		return !isEmpty(str);
	}

	public static boolean isNull(String str) {
		return (isEmpty(str)) || ("null".equals(str));
	}

	public static boolean isNotNull(String str) {
		return (isNotEmpty(str)) && (!"null".equals(str));
	}

	public static final String noNull(String string, String defaultString) {
		return isEmpty(string) ? defaultString : string;
	}

	public static final String noNull(String string) {
		return noNull(string, "");
	}

	/**
	 * get length
	 *
	 * @param value
	 * @return int
	 */
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

	public static int count(String str, String findStr) {
		int lastIndex = 0;
		int length = findStr.length();
		int count = 0;
		int start = 0;
		while ((start = str.indexOf(findStr, lastIndex)) >= 0) {
			lastIndex = start + length;
			count++;
		}
		return count;
	}

	public static boolean isLetterOrDigit(String str) {
		return PLetterOrDigit.matcher(str).find();
	}

	public static boolean isLetter(String str) {
		return PLetter.matcher(str).find();
	}

	public static boolean isDigit(String str) {
		if (isEmpty(str)) {
			return false;
		}
		return PDigit.matcher(str).find();
	}

	public static boolean containsChinese(String str) {
		return !PChinese.matcher(str).matches();
	}

	public static boolean checkID(String str) {
		if (isEmpty(str)) {
			return true;
		}

		return PID.matcher(str).matches();
	}

	public static String getURLExtName(String url) {
		if (isEmpty(url)) {
			return null;
		}
		int index1 = url.indexOf('?');
		if (index1 == -1) {
			index1 = url.length();
		}
		int index2 = url.lastIndexOf('.', index1);
		if (index2 == -1) {
			return null;
		}
		int index3 = url.indexOf('/', 8);
		if (index3 == -1) {
			return null;
		}
		String ext = url.substring(index2 + 1, index1);
		if (ext.matches("[^\\/\\\\]*")) {
			return ext;
		}
		return null;
	}

	public static String getURLFileName(String url) {
		if (isEmpty(url)) {
			return null;
		}
		int index1 = url.indexOf('?');
		if (index1 == -1) {
			index1 = url.length();
		}
		int index2 = url.lastIndexOf('/', index1);
		if ((index2 == -1) || (index2 < 8)) {
			return null;
		}
		String ext = url.substring(index2 + 1, index1);
		return ext;
	}

	/**
	 * 过滤不可见字符
	 */
	public static String stripNonValidXMLCharacters(String input) {
		if (input == null || ("".equals(input))) {
			return "";
		}
		StringBuilder out = new StringBuilder();
		char current;
		for (int i = 0; i < input.length(); i++) {
			current = input.charAt(i);
			boolean isValidXmlChar = (current == 0x9) || (current == 0xA) || (current == 0xD)
					|| ((current >= 0x20) && (current <= 0xD7FF)) || ((current >= 0xE000) && (current <= 0xFFFD))
					|| ((current >= 0x10000) && (current <= 0x10FFFF));
			if (isValidXmlChar) {
				out.append(current);
			}
		}
		return out.toString();
	}

	public static String clearForXML(String str) {
		char[] cs = str.toCharArray();
		char[] ncs = new char[cs.length];
		int j = 0;
		for (char element : cs) {
			if (element > 65533) {
				continue;
			}
			if (element < ' ') {
				if (((element != '\t' ? 1 : 0) & (element != '\n' ? 1 : 0) & (element != '\r' ? 1 : 0)) != 0) {
					continue;
				}
			}
			ncs[(j++)] = element;
		}
		ncs = ArrayUtils.subarray(ncs, 0, j);
		return new String(ncs);
	}

	public static String quickHtmlEncode(String html) {
		boolean flag = false;
		for (int j = 0; j < html.length(); j++) {
			char c = html.charAt(j);
			if ((c == ' ') || (c == '"') || (c == '\'') || (c == '<') || (c == '>') || (c == '&')) {
				flag = true;
				break;
			}
		}
		if (!flag) {
			return html;
		}
		StringBuilder sb = new StringBuilder();
		int last = 0;
		for (int j = 0; j < html.length(); j++) {
			char c = html.charAt(j);
			if ((c == ' ') || (c == '"') || (c == '\'') || (c == '<') || (c == '>') || (c == '&')) {
				if (last != j) {
					sb.append(html.substring(last, j));
				}
				if (c == '"') {
					sb.append("&quot;");
				} else if (c == '\'') {
					sb.append("&apos;");
				} else if (c == ' ') {
					sb.append("&#32;");
				} else if (c == '<') {
					sb.append("&lt;");
				} else if (c == '>') {
					sb.append("&gt;");
				} else if (c == '&') {
					sb.append("&amp;");
				}
				last = j + 1;
			}
		}
		if (last != html.length()) {
			sb.append(html.substring(last));
		}
		return sb.toString();
	}

	public static String quickHtmlDecode(String html) {
		boolean flag = false;
		for (int j = 0; j < html.length(); j++) {
			if (html.charAt(j) == '&') {
				flag = true;
				break;
			}
		}
		if (!flag) {
			return html;
		}
		char[] buf = new char[html.length()];
		int j = 0;
		for (int i = 0; i < html.length(); i++) {
			char c = html.charAt(i);
			if (c == '&') {
				if (html.startsWith("&quot;", j)) {
					buf[(j++)] = '"';
					i += 5;
				} else if (html.startsWith("&amp;", i)) {
					buf[(j++)] = '&';
					i += 4;
				} else if (html.startsWith("&lt;", i)) {
					buf[(j++)] = '<';
					i += 3;
				} else if (html.startsWith("&gt;", i)) {
					buf[(j++)] = '>';
					i += 3;
				} else if (html.startsWith("&apos;", i)) {
					buf[(j++)] = '\'';
					i += 5;
				} else if (html.startsWith("&nbsp;", i)) {
					buf[(j++)] = ' ';
					i += 5;
				} else if (html.startsWith("&#32;", i)) {
					buf[(j++)] = ' ';
					i += 4;
				} else if (html.charAt(i + 1) == '#') {
					int k = i + 2;
					flag = false;
					int radix = 10;
					if ((html.charAt(k) == 'x') || (html.charAt(k) == 'X')) {
						radix = 16;
						k++;
					}
					for (; (k < i + 9) && (k < html.length()); k++) {
						if (html.charAt(k) == ';') {
							flag = true;
							break;
						}
					}
					if (flag) {
						char ch = (char) Integer.parseInt(html.substring(radix == 10 ? i + 2 : i + 3, k), radix);
						buf[(j++)] = ch;
						i += k;
					}
				}
			} else {
				buf[(j++)] = c;
			}
		}

		return new String(buf, 0, j);
	}

	/**
	 * lpad
	 *
	 * @param s
	 * @param n
	 * @param replace
	 * @return
	 */
	public static String lpad(String s, int n, String replace) {
		while (s.length() < n) {
			s = replace + s;
		}
		return s;
	}

	/**
	 * rpad
	 *
	 * @param s
	 * @param n
	 * @param replace
	 * @return
	 */
	public static String rpad(String s, int n, String replace) {
		while (s.length() < n) {
			s = s + replace;
		}
		return s;
	}

	/**
	 * get match str
	 *
	 * @param str
	 * @param regex
	 * @return String
	 */
	public static String getMatchStr(String str, String regex) {
		List<String> result = getMatchArray(str, regex);
		return result.size() == 0 ? null : (String) result.get(0);
	}

	/**
	 * get match array
	 *
	 * @param str
	 * @param regex
	 * @return List
	 */
	public static List<String> getMatchArray(String str, String regex) {
		List<String> result = new ArrayList<String>();

		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(str);
		while (matcher.find()) {
			result.add(matcher.group());
		}

		return result;
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
	 * trim prefix
	 *
	 * @param str
	 * @return String
	 */
	public static String trimPrefix(String str, String prefix) {
		return str.startsWith(prefix) ? str.substring(prefix.length()) : str;
	}

	/**
	 * trim suffix
	 *
	 * @param str
	 * @param suffix
	 * @return String
	 */
	public static String trimSuffix(String str, String suffix) {
		return str.endsWith(suffix) ? str.substring(0, str.length() - 1) : str;
	}

	/**
	 * get str by array
	 *
	 * @param array
	 * @return String
	 */
	public static String getStrByArray(String[] array) {
		return getStrByArray(array, ",");
	}

	/**
	 * get str by array
	 *
	 * @param array
	 * @param split
	 * @return String
	 */
	public static String getStrByArray(String[] array, String split) {
		StringBuffer str = new StringBuffer();
		for (int i = 0; i < array.length; i++) {
			str.append(array[i] + (i != array.length - 1 ? split : ""));
		}
		return str.toString();
	}

	/**
	 * encode charset
	 *
	 * @param charSet
	 * @param charSetBytsLang
	 * @param targetLang
	 * @return
	 * @throws Exception
	 */
	public static String encodeCharset(String charSet, String charSetBytsLang, String targetLang) throws Exception {
		return new String(charSet.getBytes(charSetBytsLang), targetLang);
	}

	/**
	 * get char length
	 *
	 * @param value
	 * @return String
	 */
	public static int getCharLength(String value) {
		char[] chars = value.toCharArray();

		int charlen = 0;
		for (int i = 0; i < chars.length; i++) {
			if (chars[i] > 0x80) {
				charlen += 2;
			} else {
				charlen += 1;
			}
		}

		return charlen;
	}

	/**
	 * get char length
	 *
	 * @param value
	 * @param length
	 * @return String
	 */
	public static int getCharLength(String value, int length) {
		char[] chars = value.toCharArray();
		int charidx = 0, charlen = 0;
		while (charlen < length && charidx < chars.length) {
			if (chars[charidx] > 0x80) {
				charlen += 2;
			} else {
				charlen += 1;
			}
			charidx++;
		}

		return charidx;
	}

	/**
	 * get values
	 *
	 * @param value
	 * @return String[]
	 * @throws Exception
	 */
	public static String[] getValues(Object value) throws Exception {
		if (value == null) {
			return new String[] {};
		}
		if (value instanceof String[]) {
			return (String[]) value;
		} else {
			return new String[] { String.valueOf(value) };
		}
	}

	/**
	 * equalsNVL
	 *
	 * @param obj1
	 * @param obj2
	 * @return boolean
	 */
	public static boolean equalsNVL(Object obj1, Object obj2) {
		if ((obj1 == null) && (obj2 == null)) {
			return true;
		}
		if ((obj1 != null) && (obj2 != null) && obj1.equals(obj2)) {
			return true;
		}
		return false;
	}

	/**
	 * hashCodeNVL
	 *
	 * @param o
	 * @return int
	 */
	public static int hashCodeNVL(Object o) {
		if (o == null) {
			return 0;
		}
		return o.hashCode();
	}

	/**
	 * @param value
	 * @return
	 */
	public static String stringToAscii(String value) {
		StringBuffer sbu = new StringBuffer();
		char[] chars = value.toCharArray();
		for (int i = 0; i < chars.length; i++) {
			if (i != chars.length - 1) {
				sbu.append((int) chars[i]).append(",");
			} else {
				sbu.append((int) chars[i]);
			}
		}
		return sbu.toString();
	}

	/**
	 *
	 * @param value
	 * @return
	 */
	public static String asciiToString(String value) {
		StringBuffer sbu = new StringBuffer();
		String[] chars = value.split(",");
		for (int i = 0; i < chars.length; i++) {
			sbu.append((char) Integer.parseInt(chars[i]));
		}
		return sbu.toString();
	}

	public static byte[] objectToByte(java.lang.Object obj) {
		byte[] bytes = null;
		try {
			ByteArrayOutputStream bo = new ByteArrayOutputStream();
			ObjectOutputStream oo = new ObjectOutputStream(bo);
			oo.writeObject(obj);

			bytes = bo.toByteArray();
			bo.close();
			oo.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return bytes;
	}

	/**
	 * 把中文转成Unicode码
	 *
	 * @param str
	 * @return
	 */
	public static String chineseToUnicode(String str) {
		if (str == null) {
			str = "";
		}
		String result = "";
		for (int i = 0; i < str.length(); i++) {
			int chr1 = str.charAt(i);
			if (chr1 >= 19968 && chr1 <= 171941) {// 汉字范围 \u4e00-\u9fa5 (中文)
				result += "\\u" + Integer.toHexString(chr1);
			} else {
				result += str.charAt(i);
			}
		}
		return result;
	}

	/**
	 * 判断是否为中文字符
	 *
	 * @param c
	 * @return
	 */
	public static boolean isChinese(char c) {
		Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
		if (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
				|| ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
				|| ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
				|| ub == Character.UnicodeBlock.GENERAL_PUNCTUATION
				|| ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
				|| ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS) {
			return true;
		}
		return false;
	}

	/**
	 * 检查指定的字符串列表是否不为空。
	 */
	public static boolean areNotEmpty(String... values) {
		boolean result = true;
		if (values == null || values.length == 0) {
			result = false;
		} else {
			for (String value : values) {
				result &= !isEmpty(value);
			}
		}
		return result;
	}
}

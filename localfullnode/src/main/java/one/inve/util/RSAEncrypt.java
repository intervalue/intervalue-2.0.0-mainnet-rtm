package one.inve.util;

import org.apache.commons.codec.binary.Base64;
import javax.crypto.Cipher;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashMap;
import java.util.Map;

public class RSAEncrypt {
	//用于封装随机产生的公钥与私钥
	private static Map<Integer, String> keyMap = new HashMap<Integer, String>();

	private static String publicKey="MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDQa5li4Dq1SMfYHh4nkzXJQKtgxTFKuV2rVaSIRd50anD9EE0udMoeTFqbVJ04cYtNQWJw6mkwLqylH4l7LDiMsK9gRMml4r822kcH46rHMru72Bh9fcZatQBplV/969LvV05W97+/WmR2EU+I7oU0hoX15wZs4owz9XXHcvEBPwIDAQAB";
	private static String privateKey="MIICdQIBADANBgkqhkiG9w0BAQEFAASCAl8wggJbAgEAAoGBANBrmWLgOrVIx9geHieTNclAq2DFMUq5XatVpIhF3nRqcP0QTS50yh5MWptUnThxi01BYnDqaTAurKUfiXssOIywr2BEyaXivzbaRwfjqscyu7vYGH19xlq1AGmVX/3r0u9XTlb3v79aZHYRT4juhTSGhfXnBmzijDP1dcdy8QE/AgMBAAECgYAU29/zDFvzzr/pekIsVchZRvaf7bxcLScZwa2A0fVMk2aRfTMsRhYAaEXdK9+8SWjWp4eRo3Q5lUJEGF7rC+bwx244uPkbepL1Rgg/KOpfVS1dGzGNrjAVj6uS3gcOfLD5Mx2bIO5j/tKcudxB6o89hqlCpChMYObJ92eoTPVQyQJBAO9ec4FLA650xFWaUZ6CNo8xU71ArCq1qYqHNj0Y5cezSnVwUOLpASLAkL7/PclpmwDrdTCl5O+nwXT+wDOaps0CQQDe5q2k3MvSsqoPDlHCvMDl3XPA0DYwYFzetdUuIL8VbiNqSTEIaCxGSrqklxpxNfsaAVX/NcWb4yAZ6GxF3tA7AkATRStPUVauiL/1lELV2+3AgVwYdEhn98/6UwBO0t5MwWZRJgWc/t0UiyQb/DhhKptL66i3jeNsV7j/TjLy9JRRAkBb4qviMkKzcIM6AaUiqby7BTcaXLp55r9h74MZqNYcd3KR9eoIlSjrMRMPllqIIMCKT9KrifcT8+TfyTgY9WjRAkAHRDWxogYMDrDLTlSqg3DobW3Jl9VzB6eMMmLuY+wVTIhvFFIZHOmQ1BXxDTqLG9cwnmIld3Ydin+fVuSOZekG";
	public static void main(String[] args) throws Exception {
		//生成公钥和私钥
		//genKeyPair();
		//加密字符串
		String message = "123456";
		System.out.println("随机生成的公钥为:" + publicKey);
		System.out.println("随机生成的私钥为:" + privateKey);
		String messageEn = encrypt(message,publicKey);
		System.out.println(message + "\t加密后的字符串为:" + messageEn);
		messageEn = encrypt(message);
		System.out.println(message + "\t加密后的字符串为:" + messageEn);
		String messageDe = decrypt(messageEn,privateKey);
		System.out.println("还原后的字符串为:" + messageDe);
		messageDe = decrypt(messageEn);
		System.out.println("还原后的字符串为:" + messageDe);
	}

	/** 
	 * 随机生成密钥对 
	 * @throws NoSuchAlgorithmException 
	 */  
	public static void genKeyPair() throws NoSuchAlgorithmException {  
		// KeyPairGenerator类用于生成公钥和私钥对，基于RSA算法生成对象  
		KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("RSA");  
		// 初始化密钥对生成器，密钥大小为96-1024位  
		keyPairGen.initialize(1024,new SecureRandom());  
		// 生成一个密钥对，保存在keyPair中  
		KeyPair keyPair = keyPairGen.generateKeyPair();  
		RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();   // 得到私钥  
		RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();  // 得到公钥  
		String publicKeyString = new String(Base64.encodeBase64(publicKey.getEncoded()));  
		// 得到私钥字符串  
		String privateKeyString = new String(Base64.encodeBase64((privateKey.getEncoded())));  
		// 将公钥和私钥保存到Map
		keyMap.put(0,publicKeyString);  //0表示公钥
		keyMap.put(1,privateKeyString);  //1表示私钥
	}
	/**
	 * RSA公钥加密
	 *
	 * @param str
	 *            加密字符串
	 *            公钥
	 * @return 密文
	 * @throws Exception
	 *             加密过程中的异常信息
	 */
	public static String encrypt( String str ) throws Exception{
		return encrypt(  str,  publicKey );
	}
	/** 
	 * RSA公钥加密 
	 *  
	 * @param str 
	 *            加密字符串
	 * @param publicKey 
	 *            公钥 
	 * @return 密文 
	 * @throws Exception 
	 *             加密过程中的异常信息 
	 */  
	public static String encrypt( String str, String publicKey ) throws Exception{
		//base64编码的公钥
		byte[] decoded = Base64.decodeBase64(publicKey);
		RSAPublicKey pubKey = (RSAPublicKey) KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(decoded));
		//RSA加密
		Cipher cipher = Cipher.getInstance("RSA");
		cipher.init(Cipher.ENCRYPT_MODE, pubKey);
		String outStr = Base64.encodeBase64String(cipher.doFinal(str.getBytes("UTF-8")));
		return outStr;
	}
	/**
	 * RSA私钥解密
	 *
	 * @param str
	 *            加密字符
	 *            私钥
	 * @return 铭文
	 * @throws Exception
	 *             解密过程中的异常信息
	 */
	public static String decrypt(String str) throws Exception{
		return decrypt( str,  privateKey);
	}
	/** 
	 * RSA私钥解密
	 *  
	 * @param str 
	 *            加密字符串
	 * @param privateKey 
	 *            私钥 
	 * @return 铭文
	 * @throws Exception 
	 *             解密过程中的异常信息 
	 */  
	public static String decrypt(String str, String privateKey) throws Exception{
		//64位解码加密后的字符串
		byte[] inputByte = Base64.decodeBase64(str.getBytes("UTF-8"));
		//base64编码的私钥
		byte[] decoded = Base64.decodeBase64(privateKey);  
        RSAPrivateKey priKey = (RSAPrivateKey) KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(decoded));  
		//RSA解密
		Cipher cipher = Cipher.getInstance("RSA");
		cipher.init(Cipher.DECRYPT_MODE, priKey);
		String outStr = new String(cipher.doFinal(inputByte));
		return outStr;
	}

}

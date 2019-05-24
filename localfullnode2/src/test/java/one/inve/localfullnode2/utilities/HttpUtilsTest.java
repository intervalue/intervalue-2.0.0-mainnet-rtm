package one.inve.localfullnode2.utilities;

import java.math.BigInteger;
import java.util.HashMap;

import org.junit.Test;

public class HttpUtilsTest {
	@Test
	public void testHttpsPost() {
		Signer signer = new Signer("A5EzYgpcef+fO3s/HsppAfM4JZXN1VC9aVka1YALCZc/",
				"/cJdT9jrlkTDjynRtv8bKoojbqZjGlDdx7H0bM+XAw8=");

		String tx = signer.signTx("ASWD2MRMIEYR27PMUXGOOZYCBOXLOXPK", "VQ3KIIGB4ULKUM2UFGGQ7YVRLP4AJEDR",
				BigInteger.valueOf(1000000), BigInteger.valueOf(500000), BigInteger.valueOf(1000000000));

		final String sendMessageURL = "http://localhost:35893/v1/sendmsg";
		HashMap<String, String> params = new HashMap<String, String>() {
			{
				put("message", tx);
			}
		};

		try {
			String result = HttpUtils.httpPost(sendMessageURL, params);
			System.out.println("result=" + result);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}

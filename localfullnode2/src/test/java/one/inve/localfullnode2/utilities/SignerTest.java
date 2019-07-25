package one.inve.localfullnode2.utilities;

import java.math.BigInteger;

import org.junit.Test;

public class SignerTest {
	@Test
	public void testSignTx() {
		Signer signer = new Signer("A5EzYgpcef+fO3s/HsppAfM4JZXN1VC9aVka1YALCZc/",
				"/cJdT9jrlkTDjynRtv8bKoojbqZjGlDdx7H0bM+XAw8=");

		String tx = signer.signTx("ASWD2MRMIEYR27PMUXGOOZYCBOXLOXPK", "VQ3KIIGB4ULKUM2UFGGQ7YVRLP4AJEDR",
				BigInteger.valueOf(1000000), BigInteger.valueOf(500000), BigInteger.valueOf(1000000000));

		System.out.println(tx);

	}
}

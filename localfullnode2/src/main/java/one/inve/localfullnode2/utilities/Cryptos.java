package one.inve.localfullnode2.utilities;

import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import one.inve.localfullnode2.conf.Config;

public class Cryptos {
	private static final Logger logger = LoggerFactory.getLogger(Cryptos.class);

	static final String SIG_PROVIDER = Config.useRSA ? "SunRsaSign" : "SunEC";
	static final String SIG_TYPE2 = Config.useRSA ? "SHA384withRSA" : "SHA384withECDSA";

	static final String HASH_TYPE = "SHA-384";
	static final int numCryptoThreads = 32;
	private static final ExecutorService cryptoThreadPool = Executors.newFixedThreadPool(32);

	static KeyPair[] generateKeyPairs(int n) {
		KeyPair[] ret = new KeyPair[n];
		try {
			KeyPairGenerator keyGen = KeyPairGenerator.getInstance(SIG_TYPE2, SIG_PROVIDER);
			keyGen.initialize(1024, null);
			for (int i = 0; i < n; ++i) {
				ret[i] = keyGen.generateKeyPair();
			}
		} catch (Exception e) {
			logger.error("error", e);
		}
		return ret;
	}

	public static byte[] sign(byte[] data, PrivateKey privKey) {
		Signature signature = null;
		try {
			signature = Signature.getInstance(SIG_TYPE2, SIG_PROVIDER);
			signature.initSign(privKey);
			signature.update(data);
			byte[] result = signature.sign();
			if (result == null) {
				logger.error(">>>>>> ERROR:  signature is null.");
			}
			return result;
		} catch (NoSuchAlgorithmException | NoSuchProviderException | InvalidKeyException | SignatureException e) {
			logger.error("error", e);
		}
		return null;
	}

	static Future<Boolean> verifySignatureParallel(byte[] data, final byte[] signature, final PublicKey publicKey,
			final Consumer<Boolean> doLast) {
		return cryptoThreadPool.submit(new Callable<Boolean>() {
			@Override
			public Boolean call() {
				boolean result = Cryptos.verifySignature(data, signature, publicKey);
				doLast.accept(Boolean.valueOf(result));
				return Boolean.valueOf(result);
			}
		});
	}

	public static boolean verifySignature(byte[] data, byte[] signature, PublicKey publicKey) {
		try {
			Signature sig = Signature.getInstance(SIG_TYPE2, SIG_PROVIDER);

			sig.initVerify(publicKey);
			sig.update(data);
			return sig.verify(signature);
		} catch (NoSuchAlgorithmException | NoSuchProviderException | InvalidKeyException | SignatureException e) {
			logger.error("error", e);
		}
		return false;
	}

	public KeyPair getKeyPair() throws NoSuchAlgorithmException {
		KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
		keyPairGenerator.initialize(1024);
		return keyPairGenerator.generateKeyPair();
	}
}

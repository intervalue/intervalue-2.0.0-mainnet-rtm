package one.inve.contract.sig;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import one.inve.mnemonic.Mnemonic;
import one.inve.utils.DSA;
import one.inve.utils.SignUtil;
import org.bitcoinj.core.ECKey;

/**
 * The purpose of the demo is to showcase how to leverage the mnemonic function.
 * 
 * Note:The mnemonic demo has a dependency with fastjson(from alibaba) and
 * mnemonic*.jar,consult with your salesman about how to get jar.
 * 
 * @author Francis.Deng
 *
 */
public class WalletEdge {

	/**
	 * build new PublicKeyAndAddressPair instance(passed into your words) which
	 * provides a tool to populate the json template with new "pubkey","fromAddress"
	 * && "signature" properties to which Mnemonic assign the computed value.
	 * 
	 * @param words
	 * @return
	 * @throws Exception
	 */
	/**
	 * @param words
	 * @return
	 * @throws Exception
	 */
	public static PublicKeyAndAddressPair NewPPKPair(String words) throws Exception {
		Mnemonic mnemonic = new Mnemonic(words, "");

		byte[] bytes = mnemonic.getPubKey();
		String pkey = DSA.encryptBASE64(bytes);
		// JSONArray def = JSONObject.parseArray(String.format("[\"sig\",
		// {\"pubkey\":\"%s\"}]", pkey));
		JSONArray def = JSONObject.parseArray("[\"sig\", {\"pubkey\":\"" + pkey + "\"}]");

		// return new PublicKeyAndAddressPair(mnemonic.getPubKey(),
		// InveWallet.getInveAddressByDefinidion(def), mnemonic);
		return new PublicKeyAndAddressPair(bytes, /* InveWallet.getInveAddressByDefinidion(def) */mnemonic.getAddress(),
				mnemonic);

	}

	public interface Signable {
		String resign(String message);
	}

	public static class PublicKeyAndAddressPair {
		protected byte[] publicKey;
		protected String address;

		private Mnemonic mnemonicRef;

		public PublicKeyAndAddressPair(byte[] publicKey, String address, Mnemonic mnemonic) {
			super();
			this.publicKey = publicKey;
			this.address = address;
			this.mnemonicRef = mnemonic;
		}

		/**
		 * Note:unit template(variable name:unit) should not include empty "signature"
		 * value or even show off "signature" because it might cause incorrect result.
		 * 
		 * @return
		 */
		public Signable getSigner() {
			return new Signable() {

				public String resign(String unit) {
					JSONObject unitObj = JSONObject.parseObject(unit);
					JSONObject unitObject = (JSONObject) unitObj.get("unit");

					unitObject.put("pubkey", DSA.encryptBASE64(publicKey));
					unitObject.put("fromAddress", address);

					// String sig = SignUtil.Sign(unitObj.toJSONString(),
					// ECKey.fromPrivate(mnemonicRef.getxPrivKey()));

					// generate unitObject sig
					String source = SignUtil.getSourceString(unitObject);
//					String sig = SignUtil.signStr(SignUtil.getSourceString(unitObject),
//							ECKey.fromPrivate(mnemonicRef.getxPrivKey()));
//					String sig = SignUtil.sign(SignUtil.getSourceString(unitObject),
//							ECKey.fromPrivate(mnemonicRef.getxPrivKey()));
					String sig = SignUtil.sign(unitObject.toJSONString(), ECKey.fromPrivate(mnemonicRef.getxPrivKey()));
					unitObject.put("signature", sig);

					if (!SignUtil.verify(unitObject)) {
						throw new RuntimeException("To avoid verification failure in the future,early verify it");
					}

					return unitObj.toJSONString();
				}

			};
		}

	}
}

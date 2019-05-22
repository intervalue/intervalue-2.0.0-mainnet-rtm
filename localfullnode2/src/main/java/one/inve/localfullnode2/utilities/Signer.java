package one.inve.localfullnode2.utilities;

import java.math.BigInteger;
import java.time.Instant;

import org.bitcoinj.core.ECKey;

import com.alibaba.fastjson.JSONObject;

import one.inve.bean.message.MessageType;
import one.inve.bean.message.MessageVersion;
import one.inve.bean.message.TransactionMessage;
import one.inve.core.Constant;
import one.inve.utils.DSA;
import one.inve.utils.SignUtil;

/**
 * 
 * Copyright © CHXX Co.,Ltd. All rights reserved.
 * 
 * @Description: signature tools which are able to sign all kinds of messages
 *               using existed public-private key pair.
 * @author: Francis.Deng
 * @see TransactionMessage
 * @date: May 22, 2019 12:39:34 AM
 * @version: V1.0
 */
public class Signer {
	private final String sPublicKey;
	private final String sPrivateKey;

	public Signer(String sPublicKey, String sPrivateKey) {
		super();
		this.sPublicKey = sPublicKey;
		this.sPrivateKey = sPrivateKey;
	}

	/**
	 * signing a "2.0" transaction message
	 */
	public String signMessage(String fromAddress, String toAddress, BigInteger amount, BigInteger fee,
			BigInteger nrgPrice) {
		JSONObject json = getMessage("2.0", fromAddress, toAddress, amount, fee, nrgPrice);

		String msgSignature = SignUtil.sign(json.toJSONString(), ECKey.fromPrivate(DSA.decryptBASE64(sPrivateKey)));

		json.put("signature", msgSignature);

		return json.toJSONString();
	}

	private JSONObject getMessage(String ver, String fromAddress, String toAddress, BigInteger amount, BigInteger fee,
			BigInteger nrgPrice) {
		JSONObject json = new JSONObject();
		json.put("fromAddress", fromAddress);
		json.put("toAddress", toAddress);
		json.put("amount", amount.toString());
		json.put("timestamp", Instant.now().toEpochMilli());
		json.put("pubkey", sPublicKey);
		if (MessageVersion.DEV_1_0.equals(ver)) {
			json.put("fee", fee.toString());
		} else if (MessageVersion.PRO_2_0.equals(ver)) {
			if (Constant.NEED_FEE) {
				json.put("fee", fee.toString());
				json.put("nrgPrice", nrgPrice.toString());
			}
		}

		json.put("type", MessageType.TRANSACTIONS.getIndex());
		// json.put("remark", this.getRemark());
//		if (StringUtils.isNotEmpty(msgSignature)) {
//			json.put("signature", msgSignature);
//		}
		json.put("vers", ver);

		return json;
	}

}

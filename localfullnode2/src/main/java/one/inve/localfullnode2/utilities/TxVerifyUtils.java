package one.inve.localfullnode2.utilities;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

import org.bitcoinj.core.ECKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import one.inve.bean.message.MessageType;
import one.inve.bean.message.MessageVersion;
import one.inve.bean.message.TransactionMessage;
import one.inve.cfg.localfullnode.Config;
import one.inve.contract.MVM.WorldStateService;
import one.inve.core.Constant;
import one.inve.localfullnode2.store.rocks.INosql;
import one.inve.localfullnode2.store.rocks.RocksJavaUtil;
import one.inve.utils.DSA;
import one.inve.utils.SignUtil;
import one.inve.wallet.walletImp.InveWallet;

public class TxVerifyUtils {
	private static final Logger logger = LoggerFactory.getLogger(TxVerifyUtils.class);

	public static boolean hasAccountSufficientFunds(String message, String dbId) {
		TransactionMessage m0 = JSONObject.parseObject(message, TransactionMessage.class);
		String fromAddress = m0.getFromAddress();
		// String toAddress = m0.getToAddress();
		BigInteger fee = m0.getNrgPrice().multiply(m0.getFee());
		BigInteger amount = m0.getAmount();
//		JSONObject m0 = JSONObject.parseObject(message);
//		// JSONObject m1 = m0.getJSONObject("message");
//		String fromAddress = m0.getString("fromAddress");
//		BigDecimal amount = m0.getBigDecimal("amount");

		BigInteger fromAddressAvailAtoms = WorldStateService.getBalanceByAddr(dbId, fromAddress);

		return fee.equals(BigInteger.ZERO) ? fromAddressAvailAtoms.compareTo(amount) >= 0
				: fromAddressAvailAtoms.compareTo(fee.add(amount)) >= 0;
	}

//	private boolean verifyDoubleCost(String fromAddress, String toAddress, BigInteger fee, BigInteger amount) {
//		BigInteger fromAddressAvailAtoms = dep.getWorldStateService().getBalanceByAddr(dep.getDbId(), fromAddress);
//		return fee.equals(BigInteger.ZERO) ? fromAddressAvailAtoms.compareTo(amount) >= 0
//				: fromAddressAvailAtoms.compareTo(fee.add(amount)) >= 0;
//	}

	/**
	 * 消息验证
	 * 
	 * @param message 消息
	 * @param node    主节点
	 * @return 有效无效
	 */
	public static boolean verifyMessage(String message, ConcurrentHashMap<String, Long> messageHashCache, INosql nosql,
			int shardId, int shardCount) {
		JSONObject o = JSONObject.parseObject(message);

		// 消息重复性验证
		String hash = o.getString("signature");
		boolean isValid = false;
		// 判断在缓存中是否不存在
		// if (node.getMessageHashCache().get(hash) == null ||
		// node.getMessageHashCache().get(hash) <= 0) {
		if (messageHashCache.get(hash) == null || messageHashCache.get(hash) <= 0) {
			// 放入缓存
			// node.getMessageHashCache().put(hash, Instant.now().toEpochMilli());
			messageHashCache.put(hash, Instant.now().toEpochMilli());
			// 判断在数据库中是否不存在
			// byte[] existHashBytes = new
			// RocksJavaUtil(node.nodeParameters.dbId).get(hash);
			byte[] existHashBytes = nosql.get(hash);
			if (null == existHashBytes || existHashBytes.length <= 0) {
				// 写入数据库
				JSONObject msgObj = new JSONObject();
				msgObj.put("isStable", false);
				msgObj.put("msg", message);
				nosql.put(hash, msgObj.toJSONString());
				isValid = true;
			} else {
//                logger.error("node-({}, {}): msg already exists in db: {}", node.getShardId(), node.getCreatorId(), hash);
			}
		} else {
//            logger.error("node-({}, {}): msg already exists in MessageHashCache: {}", node.getShardId(), node.getCreatorId(), hash);
		}

		if (!isValid) {
//            logger.error("node-({}, {}): message-{} exist.", node.getShardId(), node.getCreatorId(), hash);
			throw new RuntimeException("message-" + hash + " exist");
		}

		// 参数合法性验证
		isValid = verifyParameters(o);
		if (!isValid) {
			return isValid;
		}
		// 发送方合法性验证
		String pubkey = o.getString("pubkey");
		isValid = verifyLegalSender(pubkey, shardId, shardCount);
		if (!isValid) {
			return isValid;
		}
		// 签名验证
		isValid = verifySignature((JSONObject) o.clone());
		if (!isValid) {
//            logger.error("node-({}, {}): verify signature faild.", node.getShardId(), node.getCreatorId());
			return isValid;
		}
		// 计算并验证手续费是否足够
		calculateRealFeeAndVerify(o);
		// 消息附带留言验证
		isValid = verifyRemark(o);
		if (!isValid) {
			return isValid;
		}
		return isValid;
	}

	public static Pair<Boolean, String> verifyMessageWithLog(String message,
			ConcurrentHashMap<String, Long> messageHashCache, INosql nosql, int shardId, int shardCount) {
		JSONObject o = JSONObject.parseObject(message);

		// 消息重复性验证
		String hash = o.getString("signature");
		boolean isValid = false;
		// 判断在缓存中是否不存在
		// if (node.getMessageHashCache().get(hash) == null ||
		// node.getMessageHashCache().get(hash) <= 0) {
		if (messageHashCache.get(hash) == null || messageHashCache.get(hash) <= 0) {
			// 放入缓存
			// node.getMessageHashCache().put(hash, Instant.now().toEpochMilli());
			messageHashCache.put(hash, Instant.now().toEpochMilli());
			// 判断在数据库中是否不存在
			// byte[] existHashBytes = new
			// RocksJavaUtil(node.nodeParameters.dbId).get(hash);
			byte[] existHashBytes = nosql.get(hash);
			if (null == existHashBytes || existHashBytes.length <= 0) {
				// 写入数据库
				JSONObject msgObj = new JSONObject();
				msgObj.put("isStable", false);
				msgObj.put("msg", message);
				nosql.put(hash, msgObj.toJSONString());
				isValid = true;
			} else {
//                logger.error("node-({}, {}): msg already exists in db: {}", node.getShardId(), node.getCreatorId(), hash);
			}
		} else {
//            logger.error("node-({}, {}): msg already exists in MessageHashCache: {}", node.getShardId(), node.getCreatorId(), hash);
		}

		if (!isValid) {
//            logger.error("node-({}, {}): message-{} exist.", node.getShardId(), node.getCreatorId(), hash);
			throw new RuntimeException("message-" + hash + " exist");
		}

		// 参数合法性验证
		isValid = verifyParameters(o);
		if (!isValid) {
			return new Pair(Boolean.FALSE, "[The verification of parameters failed]");
		}
		// 发送方合法性验证
		String pubkey = o.getString("pubkey");
		isValid = verifyLegalSender(pubkey, shardId, shardCount);
		if (!isValid) {
			return new Pair(Boolean.FALSE, "[The sender is illegal]");
		}
		// 签名验证
		// isValid = verifySignature((JSONObject) o.clone());
		isValid = verifyExoSignature((JSONObject) o.clone());
		if (!isValid) {
//            logger.error("node-({}, {}): verify signature faild.", node.getShardId(), node.getCreatorId());
			return new Pair(Boolean.FALSE, "[The validation of signature failed]");
		}
		// 计算并验证手续费是否足够
		calculateRealFeeAndVerify(o);
		// 消息附带留言验证
		isValid = verifyRemark(o);
		if (!isValid) {
			return new Pair(Boolean.FALSE, "[The validation of message attched failed]");
		}
		return new Pair(Boolean.TRUE, "");
	}

	/**
	 * 消息验证（不做签名验证）
	 * 
	 * @param o       消息体
	 * @param shardId 打包消息的Event的片号
	 * @param node    主节点
	 * @return 是否有效
	 */
	public static int verifyMessageWithoutSign(JSONObject o, int shardId, INosql nosql, int multiple, int shardCount) {
		// 消息重复性验证
		String hash = o.getString("signature");
		// if (node.nodeParameters.multiple == 1) {
		if (multiple == 1) {
			boolean isValid = false;
			// 判断在数据库中是否不存在(未共识入库前要么是null要么存的是hash)
			// byte[] existHashBytes = new
			// RocksJavaUtil(node.nodeParameters.dbId).get(hash);
			byte[] existHashBytes = nosql.get(hash);
			if (null == existHashBytes || existHashBytes.length <= 0) {
				isValid = true;
			} else if (new String(existHashBytes).equals(hash)
					|| !JSONObject.parseObject(new String(existHashBytes)).getBoolean("isStable")) {
				isValid = true;
//				logger.warn("node-({}, {}): msg receive and init in db: {}", node.getShardId(), node.getCreatorId(),
//						hash);
			} else {
//				logger.error("node-({}, {}): msg already exists in db: {}", node.getShardId(), node.getCreatorId(),
//						hash);
			}
			if (!isValid) {
				return 2;
			}
		}

		// 参数合法性验证
		boolean isValid = verifyParameters(o);
		if (!isValid) {
			return 0;
		}
		// 发送方合法性验证
		int type = (null == o.getInteger("type")) ? -1 : o.getInteger("type");
		if (type != MessageType.SNAPSHOT.getIndex()) {
			String pubkey = o.getString("pubkey");
			isValid = verifyLegalSender(pubkey, shardId, shardCount);
			if (!isValid) {
				return 0;
			}
		}

		// 计算并验证手续费是否足够
		calculateRealFeeAndVerify(o);
		// 消息附带留言验证
		isValid = verifyRemark(o);
		if (!isValid) {
			return 0;
		}
		return isValid ? 1 : 0;
	}

	/**
	 * 创世交易验证
	 * 
	 * @param o    创世交易
	 * @param node 主节点
	 * @return 验证结果
	 */
	public static boolean verifyCreationMessage(JSONObject o, INosql nosql, int multiple) {
		// 消息重复性验证
		String hash = o.getString("signature");
		// if (node.nodeParameters.multiple == 1) {
		if (multiple == 1) {
			byte[] existHashBytes = nosql.get(hash);
			if (null == existHashBytes || existHashBytes.length <= 0) {
				return true;
			} else {
				logger.error("Creation tx exist: {}", hash);
				return false;
			}
		}

		// 参数合法性验证
		boolean isValid = verifyParameters(o);
		if (!isValid) {
			return isValid;
		}
		// 创世交易参数合法性验证
		isValid = verifyCreationParameters(o);
		if (!isValid) {
			return isValid;
		}
		// 签名验证
		isValid = verifySignature((JSONObject) o.clone());
		if (!isValid) {
			logger.error("Creation tx verify signature faild: {}", hash);
			return isValid;
		}
		// 计算并验证手续费是否足够
		calculateRealFeeAndVerify(o);
		// 消息附带留言验证
		isValid = verifyRemark(o);
		if (!isValid) {
			return isValid;
		}
		return isValid;
	}

	/**
	 * 消息参数验证
	 * 
	 * @param o 消息json对象
	 * @return true-成功，异常-失败
	 */
	private static boolean verifyParameters(JSONObject o) {
		// type
		int type = (null == o.getInteger("type")) ? -1 : o.getInteger("type");
		if (type <= 0 || type > MessageType.values().length) {
			throw new RuntimeException("type is illegal.");
		}
		// vers
		String vers = o.getString("vers");
		if (StringUtils.isNotEmpty(vers) && !MessageVersion.NAMES.containsKey(vers)) {
			throw new RuntimeException("vers is illegal.");
		}
		// pubkey
		String pubkey = o.getString("pubkey");
		if (StringUtils.isEmpty(pubkey)) {
			logger.warn(o.toJSONString());
			throw new RuntimeException("pubkey is illegal.");
		}
		// signature
		String signature = o.getString("signature");
		if (StringUtils.isEmpty(signature)) {
			throw new RuntimeException("signature is illegal.");
		}
		// fromAddress
		String fromAddress = stripExoFeature(o.getString("fromAddress"));
		String toAddress = null;
		if (StringUtils.isEmpty(fromAddress)) {
			throw new RuntimeException("fromAddress is illegal." + verboseHandledAddress(fromAddress, toAddress));
		}

		// ensure toAddress and fromAddress are qualified format.
		toAddress = stripExoFeature(o.getString("toAddress"));
		if (!InveWallet.isValidAddress(fromAddress)
				|| (!StringUtils.isEmpty(toAddress) && !InveWallet.isValidAddress(toAddress))) {
			throw new RuntimeException(
					"fromAddress or toAddress is unqualified format." + verboseHandledAddress(fromAddress, toAddress));
		}

		// amount
		BigInteger amount = o.getBigInteger("amount");
		if (null != amount && amount.compareTo(BigInteger.ZERO) < 0) {
			throw new RuntimeException("amount is illegal.");
		}
		// fee
		BigInteger fee = o.getBigInteger("fee");
		if (null != fee && fee.compareTo(BigInteger.ZERO) < 0) {
			throw new RuntimeException("fee is illegal.");
		}
		if (type == MessageType.TRANSACTIONS.getIndex()) {
			// toAddress
			// String toAdress = stripExoFeature(o.getString("toAddress"));
			if (StringUtils.isEmpty(toAddress)) {
				throw new RuntimeException("toAdress is illegal." + verboseHandledAddress(fromAddress, toAddress));
			}
			// conform with the convention of "32 characters for address" in wallet by
			// Francis.Deng
			if (toAddress.length() != 32) {
				throw new IllegalArgumentException(
						"toAddress length is limited to 32." + verboseHandledAddress(fromAddress, toAddress));
			}
			if (null == amount || amount.equals(BigInteger.ZERO)) {
				throw new RuntimeException("amount is illegal.");
			}
			if (null == fee || fee.equals(BigInteger.ZERO)) {
				throw new RuntimeException("fee is illegal.");
			}
		} else if (type == MessageType.TEXT.getIndex()) {
			// context
			String context = o.getString("context");
			if (StringUtils.isEmpty(context)) {
				throw new RuntimeException("context is illegal.");
			}
		} else if (type == MessageType.CONTRACT.getIndex()) {
			// data
			String data = o.getString("data");
			if (StringUtils.isEmpty(data)) {
				throw new RuntimeException("data is illegal.");
			}
		}
		// timestamp
		Long timestamp = o.getLong("timestamp");
		if (null == timestamp || timestamp <= 0L) {
			throw new RuntimeException("timestamp is illegal.");
		}
		return true;
	}

	/**
	 * 消息参数验证
	 * 
	 * @param o 消息json对象
	 * @return true-成功，异常-失败
	 */
	private static boolean verifyCreationParameters(JSONObject o) {
		// pubkey
		String pubkey = o.getString("pubkey");
		if (!Config.GOD_PUBKEY.equals(pubkey)) {
			logger.warn(o.toJSONString());
			throw new RuntimeException("Creation tx's pubkey is illegal.");
		}
		// fromAddress
		String fromAddress = o.getString("fromAddress");
		if (!Config.GOD_ADDRESS.equals(fromAddress)) {
			throw new RuntimeException("Creation tx's fromAddress is illegal.");
		}
		// toAddress
		String toAddress = o.getString("toAddress");
		if (!Config.CREATION_ADDRESSES.contains(toAddress)) {
			throw new RuntimeException("Creation tx's toAddress is illegal.");
		}
		return true;
	}

	/**
	 * 消息手续费验证
	 * 
	 * @param o 消息json对象
	 * @return true-成功，异常-失败
	 */
	private static boolean calculateRealFeeAndVerify(JSONObject o) {
		int type = o.getInteger("type");
		String vers = o.getString("vers");
		BigInteger fee = o.getBigInteger("fee");
		boolean needVerifyFee = false;
		if (type == MessageType.TRANSACTIONS.getIndex()) {
			needVerifyFee = true;
		} else if (type == MessageType.TEXT.getIndex()) {
			if (null != fee && fee.compareTo(BigInteger.ZERO) > 0) {
				needVerifyFee = true;
			}
		}
		if (needVerifyFee) {
			if (StringUtils.isEmpty(vers) || MessageVersion.DEV_1_0.equals(vers)) {
				if (fee.compareTo(Constant.BASE_NRG) < 0) {
					throw new RuntimeException("not enough fee.");
				}
			} else if (MessageVersion.PRO_2_0.equals(vers)) {
				// 2.0: verify fee
				BigInteger nrgPrice = o.getBigInteger("nrgPrice");
				if (null == nrgPrice || nrgPrice.compareTo(BigInteger.ZERO) <= 0) {
					throw new RuntimeException("nrgPrice is illegal.");
				}
				long len = 0L;
				if (type == MessageType.TRANSACTIONS.getIndex()) {
					if (StringUtils.isNotEmpty(o.getString("remark"))) {
						len += o.getString("remark").length();
					}
				} else if (type == MessageType.TEXT.getIndex()) {
					if (StringUtils.isNotEmpty(o.getString("context"))) {
						len += DSA.decryptBASE64(o.getString("context")).length;
					}
				}
				BigInteger needFee = new BigDecimal(len * 1.0 / 1024).multiply(new BigDecimal(Constant.NRG_PEER_KBYTE))
						.toBigInteger();
				needFee = needFee.add(Constant.BASE_NRG);
				if (fee.compareTo(needFee) < 0) {
					throw new RuntimeException("not enough fee. need: " + needFee);
				}
			} else {
				throw new RuntimeException("vers is illegal.");
			}
		}

		return true;
	}

	/**
	 * 发送者（即接口调用者）合法性验证
	 * 
	 * @param pubkey     消息发送方公钥
	 * @param shardId    当前局部全节点的片号
	 * @param shardCount 总分片数
	 * @return true-成功，异常-失败
	 */
	private static boolean verifyLegalSender(String pubkey, int shardId, int shardCount) {
		if (1 == shardCount && 0 == shardId) {
			return true;
		}
		char c = pubkey.charAt(pubkey.length() - 1);
		if (shardId != (c & (shardCount - 1))) {
			logger.error("pubkey: {}, shardId: {}, shardCount: {}, need shardId: {}", pubkey, shardId, shardCount,
					c & (shardCount - 1));
			throw new RuntimeException("sender is illegal. ");
		}
		return true;
	}

	/**
	 * 消息签名验证
	 * 
	 * @param o 消息json对象
	 * @return true-成功，false-失败
	 */
	private static boolean verifySignature(JSONObject o) {
		return SignUtil.verify(o);
	}

	// <code>verifySignature</code>
	protected static boolean verifyExoSignature(JSONObject o) {
		// 对地址进行验证
		String pubkey = o.getString("pubkey");
		JSONArray definition = JSONObject.parseArray("[\"sig\", {\"pubkey\":\"" + pubkey + "\"}]");
		String address = InveWallet.getInveAddressByDefinidion(definition);

		String addressWithoutStartWithEXO = o.getString("fromAddress").startsWith("EXO")
				? o.getString("fromAddress").substring("EXO".length())
				: o.getString("fromAddress");

		if (!addressWithoutStartWithEXO.equals(address)) {
			return false;
		}
		String signature = o.getString("signature");
		o.remove("eHash");
		o.remove("hash");
		o.remove("id");
		o.remove("isStable");
		o.remove("isValid");
		o.remove("signature");
		o.remove("updateTime");
		String str = SignUtil.getSourceString(o);
		return SignUtil.verifyStr(str, signature, ECKey.fromPublicOnly(DSA.decryptBASE64(pubkey)));
	}

	public static boolean verifyFromSignUtil(String message) {
		JSONObject o = JSONObject.parseObject(message);
		String pubkey = o.getString("pubkey");
		// 对地址进行验证
		JSONArray definition = JSONObject.parseArray("[\"sig\", {\"pubkey\":\"" + pubkey + "\"}]");
		String address = InveWallet.getInveAddressByDefinidion(definition);

		String addressWithoutStartWithEXO = o.getString("fromAddress").startsWith("EXO")
				? o.getString("fromAddress").substring("EXO".length())
				: o.getString("fromAddress");

		if (!addressWithoutStartWithEXO.equals(address)) {
			return false;
		}
		String signature = o.getString("signature");
		o.remove("signature");
		o.remove("hash");
		String str = SignUtil.getSourceString(o);
		return SignUtil.verifyStr(str, signature, ECKey.fromPublicOnly(DSA.decryptBASE64(pubkey)));
	}

	/**
	 * 消息附带留言验证
	 * 
	 * @param o 消息附带留言
	 * @return true-成功，异常-失败
	 */
	private static boolean verifyRemark(JSONObject o) {
		int type = o.getInteger("type");
		if (type == MessageType.TRANSACTIONS.getIndex()) {
			String remark = o.getString("remark");
			if (StringUtils.isNotEmpty(remark) && remark.length() > Config.DEFAULT_MESSAGE_REMARK_MAX_SIZE) {
				String err = String.format("message remark field value exceeds limit %d bytes.",
						Config.DEFAULT_MESSAGE_REMARK_MAX_SIZE);
				logger.error(err);
				throw new RuntimeException(err);
			}
		}
		return true;
	}

	public static void main(String[] args) {
		boolean valid = verifyLegalSender("A78IhF6zjQIGzuzKwrjG9HEISz7/oAoEhyr7AnBr3RWn", 0, 2);
		System.out.println(valid);

		String message = "{\"fromAddress\":\"4PS6MZX6T7ELDSD2RUOZRSYGCC5RHOS7\",\"toAddress\":\"G5RPQVHDFV3ASSWRDRQZSIN3IAUXRL4H\",\"amount\":\"1000000\",\"timestamp\":\"1546068716681\",\"pubkey\":\"A78IhF6zjQIGzuzKwrjG9HEISz7/oAoEhyr7AnBr3RWn\",\"fee\":\"0\",\"type\":\"1\",\"remark\":\"\",\"signature\":\"33AIKbX5d+3kvh6PSIfumeS/LTP7kDle6VuSI8TtT5WAYeOpFi54QB1YEm6z5QWL6+RDLL7ocAE0bi+a1p6xbkEwY=\"}";
		System.out.println(verifySignature(JSONObject.parseObject(message)));

		String message0 = "{\"fromAddress\":\"4PS6MZX6T7ELDSD2RUOZRSYGCC5RHOS7\",\"toAddress\":\"G5RPQVHDFV3ASSWRDRQZSIN3IAUXRL4H\",\"amount\":\"1000000\",\"timestamp\":\"1546068716681\",\"pubkey\":\"A78IhF6zjQIGzuzKwrjG9HEISz7/oAoEhyr7AnBr3RWn\",\"fee\":\"15\",\"type\":\"1\",\"vers\":\"2.0\",\"nrgPrice\":\"100\",\"remark\":\"\",\"signature\":\"33AIKbX5d+3kvh6PSIfumeS/LTP7kDle6VuSI8TtT5WAYeOpFi54QB1YEm6z5QWL6+RDLL7ocAE0bi+a1p6xbkEwY=\"}";
		calculateRealFeeAndVerify(JSONObject.parseObject(message0));

		String message1 = "{\"amount\":1000000,\"signature\":\"32JuIWypuxq7RBbyuFBKYHQqtj/OfutXhczx3KZg2im5pLNZ+woCs49qn5g5V+rnDXz/ShLpcMb/ySQVAgFcHJLg==\",\"fee\":0,\"vers\":\"1.0dev\",\"fromAddress\":\"4PS6MZX6T7ELDSD2RUOZRSYGCC5RHOS7\",\"remark\":\"\",\"type\":1,\"toAddress\":\"G5RPQVHDFV3ASSWRDRQZSIN3IAUXRL4H\",\"timestamp\":1546072642915,\"pubkey\":\"A78IhF6zjQIGzuzKwrjG9HEISz7/oAoEhyr7AnBr3RWn\"}";
		System.out.println(verifySignature(JSONObject.parseObject(message1)));

		String message2 = "{\"fromAddress\":\"4PS6MZX6T7ELDSD2RUOZRSYGCC5RHOS7\",\"context\":\"AQIDBAUGBw==\",\"timestamp\":\"1546068398702\",\"pubkey\":\"A78IhF6zjQIGzuzKwrjG9HEISz7/oAoEhyr7AnBr3RWn\",\"type\":\"4\",\"signature\":\"32CVeOcw4Z8whxiXKrn0bobZxtaChHbmegcGSF1d1pwm0IkLaym/Y94vyZFl9j6KWTwrdyq9XuWaOopaDJ902s7g==\"}";
		;
		System.out.println(verifySignature(JSONObject.parseObject(message2)));

		String message3 = "{\"signature\":\"33AOWGrb1PpGz9fPsgVa+Sq4CVQwhX/rL4WW1sI+Gq5FqELh5x7mX2Mti8X+xuce8oxitw1gSfYOb1Uzl1LX3fS5Q=\",\"vers\":\"1.0dev\",\"context\":\"AQIDBAUGBw==\",\"fromAddress\":\"4PS6MZX6T7ELDSD2RUOZRSYGCC5RHOS7\",\"type\":4,\"timestamp\":1546073489903,\"pubkey\":\"A78IhF6zjQIGzuzKwrjG9HEISz7/oAoEhyr7AnBr3RWn\"}";
		System.out.println(verifySignature(JSONObject.parseObject(message3)));

		String message4 = "{\"fromAddress\":\"4PS6MZX6T7ELDSD2RUOZRSYGCC5RHOS7\",\"timestamp\":\"1546068595345\",\"pubkey\":\"A78IhF6zjQIGzuzKwrjG9HEISz7/oAoEhyr7AnBr3RWn\",\"type\":\"2\",\"data\":\"AQIDBAUGBw==\",\"signature\":\"32ErcPLbrTG/l0KWnopl1b4jdAbGYiimVGt2OLIG45ZCxaAZZ0r6LOvm7REbOl6cPSIv3617ffz6KMysSJ38oKwg==\"}";
		System.out.println(verifySignature(JSONObject.parseObject(message4)));

		String message5 = "{\"data\":\"AQIDBAUGBw==\",\"signature\":\"32eFQw2KLgtHohzpjnpc97/dRc4wZoAH6mov6oxXEDqdQ9jA1+02ZILtIRYQhQ+8WNNpFjpoaWTDBoCWFXfzkKOg==\",\"vers\":\"1.0dev\",\"fromAddress\":\"4PS6MZX6T7ELDSD2RUOZRSYGCC5RHOS7\",\"type\":2,\"timestamp\":1546073539253,\"pubkey\":\"A78IhF6zjQIGzuzKwrjG9HEISz7/oAoEhyr7AnBr3RWn\"}";
		System.out.println(verifySignature(JSONObject.parseObject(message5)));

		String message6 = "{\"amount\":1000000000000000000000000,\"signature\":\"33AIuqujgpPWjWDIjbSMtf8kE+RI2aKIcZQ71+tnOb8UChDdiLF99KiJkFR1KlCJGlhzSJeQXWKhZnaqejjswGXiU=\",\"fee\":20000,\"vers\":\"1.0dev\",\"fromAddress\":\"4PS6MZX6T7ELDSD2RUOZRSYGCC5RHOS7\",\"remark\":\"\",\"type\":1,\"toAddress\":\"QWXZ436UBRR4LBDLUYQTK7RQPD74R6X5\",\"timestamp\":1546076934467,\"pubkey\":\"A78IhF6zjQIGzuzKwrjG9HEISz7/oAoEhyr7AnBr3RWn\"}";
		System.out.println(verifySignature(JSONObject.parseObject(message6)));

		String remark = "5Zyw5pa55rC055S16LS55rC055S16LS55pS25Yiw5Y+R55Sf55qE5LuYMTLor7fmsYI=";
		System.out.println(DSA.decryptBASE64(remark).length);
		System.out.println(new String(DSA.decryptBASE64(remark)).length());

//        System.out.println(verifyLegalSender("1", "1", 2));
		new RocksJavaUtil("0_0")
				.put("33APDAlMvdnvWybQolUDeaRtns1EfXqdgIKKFTZc+oTOsPcbdXvXZahnnW9ssFoMuffDUnZ/l9Fxv1NqjfE7Ce6g0=", "");

//        String pubkey = "A78IhF6zjQIGzuzKwrjG9HEISz7/oAoEhyr7AnBr3RWn";
//        Instant t = Instant.now();
//        char c = pubkey.charAt(pubkey.length()-1);
//        int r = (c & 1) % 1;
//        System.out.println(Duration.between(t, Instant.now()).toMillis());

		String msg = "{\"fromAddress\":\"4PS6MZX6T7ELDSD2RUOZRSYGCC5RHOS7\",\"toAddress\":\"CVTQZYB2224MK5XXZOXRK2YVLVEJVZ2F\",\"amount\":\"1000000000000000000000000\",\"timestamp\":1544508773665,\"pubkey\":\"A78IhF6zjQIGzuzKwrjG9HEISz7/oAoEhyr7AnBr3RWn\",\"fee\":\"200000000\",\"type\":1,\"remark\":\"\",\"signature\":\"33APDAlMvdnvWybQolUDeaRtns1EfXqdgIKKFTZc+oTOsPcbdXvXZahnnW9ssFoMuffDUnZ/l9Fxv1NqjfE7Ce6g0=\"}";
//        System.out.println(verifyTransactionMessage(msg, "0", 1, 0));
	}

	// exoa feature:"EXO" + normal address
	private static String stripExoFeature(String fromAddressOrToAddress) {
		String finalAddress = fromAddressOrToAddress;
		if (!StringUtils.isEmpty(fromAddressOrToAddress) && fromAddressOrToAddress.startsWith("EXO")) {
			finalAddress = fromAddressOrToAddress.substring("EXO".length());
		}

		return finalAddress;
	}

	// for debugging purpose
	private static String verboseHandledAddress(String fromAddress, String toAddress) {
		// return "detailed information: fromAddress[" + fromAddress + "] or toAddress["
		// + toAddress + "]";
		return "";
	}

	public static class Pair<K, V> implements Serializable {

		private static final long serialVersionUID = -76702881811738599L;

		private K key;

		private V value;

		public Pair(K key, V value) {
			this.key = key;
			this.value = value;
		}

		public K getKey() {
			return key;
		}

		public V getValue() {
			return value;
		}

		@Override
		public String toString() {
			return key + "=" + value;
		}

		@Override
		public int hashCode() {

			return key.hashCode() * 13 + (value == null ? 0 : value.hashCode());
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (o instanceof Pair) {
				Pair pair = (Pair) o;
				return !((key != null ? !key.equals(pair.key) : pair.key != null)
						|| (value != null ? !value.equals(pair.value) : pair.value != null));
			}
			return false;
		}
	}
}

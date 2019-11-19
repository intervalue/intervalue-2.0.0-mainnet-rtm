package one.inve.localfullnode2.message;

import java.math.BigInteger;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import one.inve.bean.message.ContractMessage;
import one.inve.bean.message.MessageType;
import one.inve.bean.message.TextMessage;
import one.inve.bean.message.TransactionMessage;
import one.inve.cfg.localfullnode.Config;
import one.inve.contract.ContractTransactionData;
import one.inve.contract.MVM.WorldStateService;
import one.inve.contract.ethplugin.core.Repository;
import one.inve.contract.inve.INVERepositoryRoot;
import one.inve.contract.inve.INVETransactionReceipt;
import one.inve.contract.inve.InternalTransferData;
import one.inve.contract.provider.RepositoryProvider;
import one.inve.localfullnode2.dep.DepItemsManager;
import one.inve.localfullnode2.snapshot.HandleConsensusSnapshotMessage;
import one.inve.localfullnode2.snapshot.HandleConsensusSnapshotMessageDependency;
import one.inve.localfullnode2.snapshot.HandleConsensusSnapshotMessageDependent;
import one.inve.localfullnode2.snapshot.HandleSnapshotPoint;
import one.inve.localfullnode2.snapshot.HandleSnapshotPointDependency;
import one.inve.localfullnode2.snapshot.HandleSnapshotPointDependent;
import one.inve.localfullnode2.store.SnapshotDbService;
import one.inve.localfullnode2.store.SnapshotDbServiceImpl;
import one.inve.localfullnode2.utilities.StringUtils;
import one.inve.localfullnode2.utilities.TxVerifyUtils;

/**
 *
 * Copyright © INVE FOUNDATION. All rights reserved.
 *
 * @Description: execute messages that has own type-handler
 * @author: Francis.Deng
 * @see
 * @see WorldStateService
 * @date: Oct 7, 2018 2:47:59 AM
 * @version: V1.0
 */
public class MessagesExe {
	private static final Logger logger = LoggerFactory.getLogger(MessagesExe.class);

	private final MessagesExeDependent dep;

	public MessagesExe(MessagesExeDependent dep) {
		super();
		this.dep = dep;
	}

	public void exe() {
//        logger.info(">>> start ConsensusMessageHandleThread...");
//        logger.info("node-({}, {}): latest snap vers = {}, latest msgHashTreeRoot = {}, " +
//                        "totalConsEventCount = {}, consMessageMaxId = {}, contributions.size = {}",
//                node.getShardId(), node.getCreatorId(), this.vers.subtract(BigInteger.ONE),
//                node.getTreeRootMap().get(this.vers.subtract(BigInteger.ONE)),
//                node.getTotalConsEventCount(), node.getConsMessageMaxId(), node.getContributions().size());
		Instant t0 = Instant.now();
		Instant t1;
		long handleCount = 0L;
		int loopCount = 0;

		HandleConsensusSnapshotMessageDependent handleConsensusSnapshotMessageDep = null;
		HandleConsensusSnapshotMessage handleConsensusSnapshotMessage = new HandleConsensusSnapshotMessage();
		HandleSnapshotPointDependent handleSnapshotPointDep = null;
		HandleSnapshotPoint handleSnapshotPoint = new HandleSnapshotPoint();

		// while (true) {
		try {
			if (!dep.getConsMessageHandleQueue().isEmpty()) {
				logger.info("be ready for executing all({}) messages as much as possible",
						dep.getConsMessageHandleQueue().size());
			}

			while (!dep.getConsMessageHandleQueue().isEmpty()) {
				// avoid queue size expanding massively
				if (++loopCount > Config.TXS_EXECUTION_BATCH_SIZE) {
					break;
				}

				// 取共识message
				JSONObject msgObject = dep.getConsMessageHandleQueue().poll();

				assert msgObject != null;
				String eHash = msgObject.getString("eHash");
				Boolean lastIdx = msgObject.getBoolean("lastIdx");
				if (StringUtils.isNotEmpty(msgObject.getString("msg"))) {
					JSONObject tm = JSONObject.parseObject(msgObject.getString("msg"));
					msgObject.put("type", tm.getInteger("type"));
					if (tm.getInteger("type") == MessageType.TRANSACTIONS.getIndex()) {
						// 普通交易消息处理
						handleConsensusTransactionMessage(msgObject);
					} else if (tm.getInteger("type") == MessageType.CONTRACT.getIndex()) {
						// 合约消息处理
						handleConsensusContractMessage(msgObject);
					} else if (tm.getInteger("type") == MessageType.SNAPSHOT.getIndex()) {
						// key condition
						// 快照消息处理
						if (Config.ENABLE_SNAPSHOT) {
							handleConsensusSnapshotMessageDep = DepItemsManager.getInstance()
									.getItemConcerned(HandleConsensusSnapshotMessageDependency.class);
							SnapshotDbService store = new SnapshotDbServiceImpl();
							handleConsensusSnapshotMessage.handleConsensusSnapshotMessage(
									handleConsensusSnapshotMessageDep, store, msgObject);
						}
						// handleConsensusSnapshotMessage(msgObject);
					} else if (tm.getInteger("type") == MessageType.TEXT.getIndex()) {
						// 文本数据消息处理
						handleConsensusTextMessage(msgObject);
					} else {
						logger.error("not supposed message type.");
					}
				} else {
					// key condition - if empty,the following is executing snapshot message
					if (Config.ENABLE_SNAPSHOT) {
						handleSnapshotPointDep = DepItemsManager.getInstance()
								.getItemConcerned(HandleSnapshotPointDependency.class);
						handleSnapshotPoint.handleSnapshotPointMessage(handleSnapshotPointDep, msgObject);
					}
//                        SnapshotPoint sp = node.getSnapshotPointMap().get(vers);
//                        logger.warn("\nspecif msg: {}, \nvers-{} sp: {}",
//                                msgObject.toJSONString(), vers, JSON.toJSONString(sp));
//                        if (sp == null) {
//                            logger.error("node-({}, {}): snapshotPoint-{} missing\nexit...",
//                                    node.getShardId(), node.getCreatorId(), vers);
//                            System.exit(-1);
//                        } else {
//                            if (null!=lastIdx && lastIdx
//                                    && eHash.equals(DSA.encryptBASE64(sp.getEventBody().getHash()))) {
//                                // 快照点event时，判断是否基金会节点，然后生成快照
//                                handleSnapshotPoint(msgObject.getBigInteger("id"));
//                            } else {
//                                logger.warn("node-({}, {}): unknown message: {}",
//                                        node.getShardId(), node.getCreatorId(), msgObject.toJSONString());
//                            }
//                        }
				}

				handleCount++;
				t1 = Instant.now();
				long interval = Duration.between(t0, t1).toMillis();
				if (interval >= 5000) {
//					logger.info(statisticInfo.toString(), node.getShardId(), node.getCreatorId(), interval, handleCount,
//							new BigDecimal(interval).divide(BigDecimal.valueOf(handleCount), 2,
//									BigDecimal.ROUND_HALF_UP),
//							node.getConsMessageVerifyQueue().size(), node.getConsMessageHandleQueue().size(),
//							node.getConsMessageSaveQueue().size());

					t0 = t1;
					handleCount = 0L;
				}
			} // else {
//				Thread.sleep(50);
//			}
		} catch (Exception e) {
			e.printStackTrace();
//                logger.error("node-({}, {}): error: {}", node.getShardId(), node.getCreatorId(), e);
		}
		// }
	}

	/**
	 * 处理共识后的普通交易消息
	 *
	 * @param msgObject 共识后的普通交易消息
	 */
	private void handleConsensusTransactionMessage(JSONObject msgObject) throws InterruptedException {
		Instant t1 = Instant.now();
		String message = msgObject.getString("msg");
//		if (logger.isDebugEnabled()) {
//			logger.debug("node-({}, {}): TransactionMessage: {}", node.getShardId(), node.getCreatorId(), message);
//		}
		// 保存交易消息
		boolean valid = msgObject.getBoolean("isValid");
		TransactionMessage tm = JSONObject.parseObject(message, TransactionMessage.class);
		String fromAddress = tm.getFromAddress();
		String toAddress = tm.getToAddress();
		BigInteger fee = tm.getNrgPrice().multiply(tm.getFee());
		if (StringUtils.isNotEmpty(fromAddress)) {
			msgObject.put("fromAddress", fromAddress);
		}
		if (StringUtils.isNotEmpty(fromAddress)) {
			msgObject.put("toAddress", toAddress);
		}
		if (valid) {
			int validState = 0;
			try {
				validState = TxVerifyUtils.verifyMessageWithoutSign(JSONObject.parseObject(message),
						msgObject.getInteger("eShardId"), dep.getNosql(), dep.getMultiple(), dep.getShardCount());
			} catch (Exception e) {
//				logger.error("node-({}, {}): TextMessage verify error. msgObj: {}, exception: {}", node.getShardId(),
//						node.getCreatorId(), msgObject.toJSONString(), e);
			}
			if (validState == 1) {
				valid = true;
			} else if (validState == 2) {
//				logger.error("node-({}, {}): Transaction message exist, throw away!!! msgObj: {}", node.getShardId(),
//						node.getCreatorId(), msgObject.toJSONString());
				msgObject.put("error", "tx existed");
				valid = false;
				return;
			} else {
				valid = false;
				msgObject.put("error", "unidentified error");
			}
			if (!valid) {
//				logger.error("node-({}, {}): Transaction message verify failed. msgObj: {}", node.getShardId(),
//						node.getCreatorId(), msgObject.toJSONString());
			} else if (verifyIllegalCreationMessage(fromAddress, toAddress)) {
				valid = false;
				msgObject.put("error", "illegal tx");
//				logger.error("node-({}, {}): Transaction message illegal. msgObj: {}", node.getShardId(),
//						node.getCreatorId(), msgObject.toJSONString());
			} else {
				valid = verifyDoubleCost(fromAddress, toAddress, fee, tm.getAmount());
				if (!valid) {
					// Francis.Deng with the result of "not enough balance in the address(??)"
					msgObject.put("error", "not enough balance in the address(" + fromAddress + ")");

//					logger.error("node-({}, {}): Transaction message double cost. msgObj: {}", node.getShardId(),
//							node.getCreatorId(), msgObject.toJSONString());
				}
			}
		}
		Instant t2 = Instant.now();
		msgObject.put("hash", tm.getSignature());
		msgObject.put("isValid", valid);
		dep.getConsMessageSaveQueue().add(msgObject);

		// 变更世界状态
		if (valid) {
			// 变更世界状态-金额
			dep.getWorldStateService().transfer(dep.getDbId(), fromAddress, toAddress, tm.getAmount());
			// 变更世界状态-收取手续费
			if (fee.compareTo(BigInteger.ZERO) > 0) {
				dep.setTotalFeeBetween2Snapshots(dep.getTotalFeeBetween2Snapshots().add(fee));
				addTransactionFeeTx2SaveQueue(tm.getSignature(), fromAddress, fee);
				dep.getWorldStateService().transfer(dep.getDbId(), fromAddress, Config.FOUNDATION_ADDRESS, fee);
			}
		}
		long interval = Duration.between(t1, t2).toMillis();
		long interval1 = Duration.between(t2, Instant.now()).toMillis();
		long interval3 = Duration.between(t1, Instant.now()).toMillis();
//		logger.warn(
//				"node-({}, {}): handleConsensusTransactionMessage() cost: {} ms, verify: {} ms, update world state: {} ms",
//				node.getShardId(), node.getCreatorId(), interval3, interval, interval1);
	}

//	/**
//	 * 处理共识后的快照消息
//	 * 
//	 * @param msgObject 共识后的快照消息
//	 */
//	private void handleConsensusSnapshotMessage(JSONObject msgObject) throws InterruptedException {
////		logger.info("node-({}, {}): Handle Consensus Snapshot Message...msg: {}", node.getShardId(),
////				node.getCreatorId(), msgObject.toJSONString());
//		String message = msgObject.getString("msg");
//		// 保存快照消息
//		SnapshotMessage snapMsg = JSON.parseObject(message, SnapshotMessage.class);
//		boolean valid = msgObject.getBoolean("isValid");
//		if (valid) {
//			int validState = 0;
//			try {
//				validState = TxVerifyUtils.verifyMessageWithoutSign(JSONObject.parseObject(message),
//						msgObject.getInteger("eShardId"), node);
//			} catch (Exception e) {
//				logger.error("node-({}, {}): Snapshot Message verify error. msgObj:{}, exception: {}",
//						node.getShardId(), node.getCreatorId(), msgObject.toJSONString(), e);
//			}
//			if (validState == 1) {
//				valid = true;
//			} else if (validState == 2) {
//				logger.error("node-({}, {}): Snapshot message exist, throw away!!! msgObj: {}", node.getShardId(),
//						node.getCreatorId(), msgObject.toJSONString());
//				return;
//			} else {
//				valid = false;
//			}
//			if (!valid) {
//				logger.error("node-({}, {}): Snapshot Message verify failed. msgObj: {}", node.getShardId(),
//						node.getCreatorId(), msgObject.toJSONString());
//			} else {
//				valid = verifySnapshotMessage(snapMsg);
//			}
//		}
//		String fromAddress = snapMsg.getFromAddress();
//		if (StringUtils.isNotEmpty(fromAddress)) {
//			msgObject.put("fromAddress", fromAddress);
//		}
//		msgObject.put("isValid", valid);
//		msgObject.put("hash", snapMsg.getSignature());
//		msgObject.put("snapVersion", snapMsg.getSnapVersion().toString());
//		node.getConsMessageSaveQueue().add(msgObject);
//
//		if (valid) {
//			logger.warn("node-({}, {}): preHash: {}, hash: {}", node.getShardId(), node.getCreatorId(),
//					snapMsg.getPreHash(), snapMsg.getHash());
//			// 处理本快照阶段的所有节点奖励
//			handleRewardOfSnapshot(snapMsg, msgObject.getString("eHash"));
//
//			// 清除当前快照之前第 Config.DEFAULT_SNAPSHOT_CLEAR_GENERATION) 个快照的快照点之前的所有Event
//			DbUtils.clearHistoryEventsBySnapshot(snapMsg.getSnapVersion(), snapMsg.getPreHash(), node);
//
//			// 补充更新本地快照消息的部分参数(snapHash, signature, pubkey, timestamp),
//			// 避免本地生成下一个快照时getPreHash()为null导致验证快照getPreHash()失败问题问题
//			node.setSnapshotMessage(snapMsg);
//		} else {
//			logger.error("node-({}, {}): Snapshot message invalid!!!\nexit...", node.getShardId(), node.getCreatorId());
//			System.exit(-1);
//		}
//
//		if (logger.isDebugEnabled()) {
//			logger.debug("========= node-({}, {}): snapshot version-{} success.", node.getShardId(),
//					node.getCreatorId(), snapMsg.getSnapVersion());
//		}
//	}

	/**
	 * 处理共识后的文本数据消息
	 *
	 * @param msgObject 共识后的文本数据消息
	 */
	private void handleConsensusTextMessage(JSONObject msgObject) throws InterruptedException {
		String message = msgObject.getString("msg");
//		if (logger.isDebugEnabled()) {
//			logger.debug("node-({}, {}): TextMessage: {}", node.getShardId(), node.getCreatorId(), message);
//		}
		// 保存文本消息
		boolean valid = msgObject.getBoolean("isValid");
		TextMessage tm = JSONObject.parseObject(message, TextMessage.class);
		String fromAddress = tm.getFromAddress();
		String toAddress = tm.getToAddress();
		BigInteger fee = tm.getNrgPrice().multiply(tm.getFee());
		if (StringUtils.isNotEmpty(fromAddress)) {
			msgObject.put("fromAddress", fromAddress);
		}
		if (StringUtils.isNotEmpty(fromAddress)) {
			msgObject.put("toAddress", toAddress);
		}
		if (valid) {
			int validState = 0;
			try {
				validState = TxVerifyUtils.verifyMessageWithoutSign(JSONObject.parseObject(message),
						msgObject.getInteger("eShardId"), dep.getNosql(), dep.getMultiple(), dep.getShardCount());
			} catch (Exception e) {
//				logger.error("node-({}, {}): Text Message verify error. msgObj: {}, exception: {}", node.getShardId(),
//						node.getCreatorId(), msgObject.toJSONString(), e);
			}
			if (validState == 1) {
				valid = true;
			} else if (validState == 2) {
//				logger.error("node-({}, {}): Text message exist, throw away!!! msgObj: {}", node.getShardId(),
//						node.getCreatorId(), msgObject.toJSONString());
				return;
			} else {
				valid = false;
			}
			if (!valid) {
//				logger.error("node-({}, {}): Text Message verify failed. msgObj: {}", node.getShardId(),
//						node.getCreatorId(), msgObject.toJSONString());
			} else if (verifylegalCreationMessage(fromAddress, toAddress)) {
				valid = false;
//				logger.error("node-({}, {}): Text Message illegal. msgObj: {}", node.getShardId(), node.getCreatorId(),
//						msgObject.toJSONString());
			} else {
				if (fee.compareTo(BigInteger.ZERO) > 0) {
					valid = verifyDoubleCost(fromAddress, toAddress, fee);
//					if (!valid) {
//						logger.error("node-({}, {}): Text Message double cost. msgObj: {}", node.getShardId(),
//								node.getCreatorId(), msgObject.toJSONString());
//					}
				}
			}
		}
		msgObject.put("hash", tm.getSignature());
		msgObject.put("isValid", valid);
		dep.getConsMessageSaveQueue().add(msgObject);

		// 变更世界状态
		if (valid) {
			if (fee.compareTo(BigInteger.ZERO) > 0) {
				// 变更世界状态-收取手续费
				dep.setTotalFeeBetween2Snapshots(dep.getTotalFeeBetween2Snapshots().add(fee));
				addTextFeeTx2SaveQueue(tm.getSignature(), fromAddress, fee);
				dep.getWorldStateService().transfer(dep.getDbId(), fromAddress, Config.FOUNDATION_ADDRESS, fee);
			}
		}
	}

	private boolean verifyIllegalCreationMessage(String fromAddress, String toAddress) {
		return Config.GOD_ADDRESS.equals(fromAddress) && !Config.CREATION_ADDRESSES.contains(toAddress);
	}

	private boolean verifylegalCreationMessage(String fromAddress, String toAddress) {
		return Config.GOD_ADDRESS.equals(fromAddress) && Config.CREATION_ADDRESSES.contains(toAddress);
	}

	/**
	 * 发送地址账户是否金额足够
	 *
	 * @param fromAddress 发送者地址
	 * @param fee         手续费
	 * @param amount      金额
	 * @return 是否双花
	 */
	private boolean verifyDoubleCost(String fromAddress, String toAddress, BigInteger fee, BigInteger amount) {
		if (verifylegalCreationMessage(fromAddress, toAddress)) {
			return true;
		} else if (verifyIllegalCreationMessage(fromAddress, toAddress)) {
			return false;
		} else {
			// 发送地址可用余额
			BigInteger fromAddressAvailAtoms = dep.getWorldStateService().getBalanceByAddr(dep.getDbId(), fromAddress);
//			if (logger.isDebugEnabled()) {
//				logger.info("node-({}, {}): from address {} avail atoms: {}, need cost atoms: {}", node.getShardId(),
//						node.getCreatorId(), fromAddress, fromAddressAvailAtoms, amount);
//			}
			// 双花验证
			return fee.equals(BigInteger.ZERO) ? fromAddressAvailAtoms.compareTo(amount) >= 0
					: fromAddressAvailAtoms.compareTo(fee.add(amount)) >= 0;
		}
	}

	private void addTransactionFeeTx2SaveQueue(String mHash, String fromAddress, BigInteger fee)
			throws InterruptedException {
		// dep.setSystemAutoTxMaxId(dep.getSystemAutoTxMaxId().add(BigInteger.ONE));
		dep.addSystemAutoTxMaxId(1);
		JSONObject o = new JSONObject();
		o.put("id", dep.getSystemAutoTxMaxId());
		o.put("type", "transaction_fee_tx");
		o.put("mHash", mHash);
		o.put("fromAddress", fromAddress);
		o.put("toAddress", Config.FOUNDATION_ADDRESS);
		o.put("amount", fee);
		o.put("updateTime", Instant.now().toEpochMilli());
		dep.getSystemAutoTxSaveQueue().put(o);
	}

	private void addTextFeeTx2SaveQueue(String mHash, String fromAddress, BigInteger fee) throws InterruptedException {
		// dep.setSystemAutoTxMaxId(dep.getSystemAutoTxMaxId().add(BigInteger.ONE));
		dep.addSystemAutoTxMaxId(1);
		JSONObject o = new JSONObject();
		o.put("id", dep.getSystemAutoTxMaxId());
		o.put("type", "transaction_fee_tx");
		o.put("mHash", mHash);
		o.put("fromAddress", fromAddress);
		o.put("toAddress", Config.FOUNDATION_ADDRESS);
		o.put("amount", fee);
		o.put("updateTime", Instant.now().toEpochMilli());
		dep.getSystemAutoTxSaveQueue().put(o);
	}

	/**
	 * 发送地址账户是否金额足够
	 *
	 * @param fromAddress 发送者地址
	 * @param fee         手续费
	 * @return 是否双花
	 */
	private boolean verifyDoubleCost(String fromAddress, BigInteger fee) {
		return verifyDoubleCost(fromAddress, fee, BigInteger.ZERO);
	}

	/**
	 * 发送地址账户是否金额足够
	 *
	 * @param fromAddress 发送者地址
	 * @param fee         手续费
	 * @param amount      金额
	 * @return 是否双花
	 */
	private boolean verifyDoubleCost(String fromAddress, BigInteger fee, BigInteger amount) {
		if (StringUtils.isEmpty(fromAddress) && fromAddress.equals(Config.FOUNDATION_ADDRESS)) {
			return true;
		} else {
			// 发送地址可用余额
			BigInteger fromAddressAvailAtoms = dep.getWorldStateService().getBalanceByAddr(dep.getDbId(), fromAddress);
//			logger.info("node-({}, {}): from address {} avail atoms: {}, need cost atoms: {}", node.getShardId(),
//					node.getCreatorId(), fromAddress, fromAddressAvailAtoms, amount);
			// 双花验证
			return fee.equals(BigInteger.ZERO) ? fromAddressAvailAtoms.compareTo(amount) >= 0
					: fromAddressAvailAtoms.compareTo(fee.add(amount)) >= 0;
		}
	}

	/**
	 * 发送地址账户是否金额足够
	 *
	 * @param fromAddress 发送者地址
	 * @param fee         手续费
	 * @return 是否双花
	 */
	private boolean verifyDoubleCost(String fromAddress, String toAddress, BigInteger fee) {
		return verifyDoubleCost(fromAddress, toAddress, fee, BigInteger.ZERO);
	}

	/**
	 * 处理共识后的智能合约消息
	 *
	 * @param msgObject 共识后的智能合约消息
	 */
	private void handleConsensusContractMessage(JSONObject msgObject) throws InterruptedException {
//		logger.info("node-({}, {}): Handle Consensus Contract Message...", node.getShardId(), node.getCreatorId());
		String message = msgObject.getString("msg");
//		if (logger.isDebugEnabled()) {
//			logger.debug("node-({}, {}): ContractMessage: {}", node.getShardId(), node.getCreatorId(), message);
//		}
		ContractMessage cm = JSON.parseObject(message, ContractMessage.class);
		String fromAddress = cm.getFromAddress();

		ContractTransactionData ct = JSON.parseObject(cm.getData(), ContractTransactionData.class);
		BigInteger value = new BigInteger(ct.getValue());
		BigInteger gasPrice = new BigInteger(ct.getGasPrice());
		BigInteger gasLimit = new BigInteger(ct.getGasLimit());
		BigInteger fee = gasPrice.multiply(gasLimit);

		boolean valid = msgObject.getBoolean("isValid");
		List<InternalTransferData> list = null;
		if (valid) {
			int validState = 0;
			try {
				validState = TxVerifyUtils.verifyMessageWithoutSign(JSONObject.parseObject(message),
						msgObject.getInteger("eShardId"), dep.getNosql(), dep.getMultiple(), dep.getShardCount());
			} catch (Exception e) {
//				logger.error("node-({}, {}): Contract Message verify error. msgObj: {}, exception: {}",
//						node.getShardId(), node.getCreatorId(), msgObject.toJSONString(), e);
			}
			if (validState == 1) {
				valid = true;
			} else if (validState == 2) {
//				logger.error("node-({}, {}): Contract message exist, throw away!!! msgObj: {}", node.getShardId(),
//						node.getCreatorId(), msgObject.toJSONString());
				return;
			} else {
				valid = false;
			}
			if (!valid) {
//				logger.error("node-({}, {}): Contract Message verify failed. msgObj: {}", node.getShardId(),
//						node.getCreatorId(), msgObject.toJSONString());
			} else if (verifyIllegalCreationMessage(fromAddress, null)) {
				valid = false;
//				logger.error("node-({}, {}): Transaction message illegal. msgObj: {}",
//						node.getShardId(), node.getCreatorId(), msgObject.toJSONString());
			} else {
				valid = verifyDoubleCost(fromAddress, null, fee, value);
				if (!valid) {
//					logger.error("node-({}, {}): Transaction message double cost. msgObj: {}",
//							node.getShardId(), node.getCreatorId(), msgObject.toJSONString());
				}
			}
			if (valid) {
				// 执行智能合约
				list = WorldStateService.executeContractMessage(dep.getDbId(), cm);
				valid = null != list && list.size() > 0;
			}
		}

		// 保存智能合约消息
		if (StringUtils.isNotEmpty(fromAddress)) {
			msgObject.put("fromAddress", fromAddress);
		}
		msgObject.put("hash", cm.getSignature());
		msgObject.put("isValid", valid);
		dep.getConsMessageSaveQueue().add(msgObject);

		if (valid) {
			// 合约执行产生的交易入库
			boolean needRecordFee = true;
			for (int i = 0; i < list.size(); i++) {
				InternalTransferData data = list.get(i);
				if (needRecordFee) {
					if (data.getFee().compareTo(BigInteger.ZERO) > 0) {
						// 扣除和收集手续费
						// key condition
						dep.setTotalFeeBetween2Snapshots(dep.getTotalFeeBetween2Snapshots().add(data.getFee()));
						addContractFeeTx2SaveQueue(cm.getSignature() + "_fee" + i, data);
					}
					needRecordFee = false;
				}
				// 合约执行产生的交易入库
				addContractTx2SaveQueue(cm.getSignature() + "_" + i, data);

				Repository track = RepositoryProvider.getTrack(dep.getDbId());
				byte[] receiptB = ((INVERepositoryRoot) track).getReceipt(cm.getSignature().getBytes());
				INVETransactionReceipt receipt = new INVETransactionReceipt(receiptB);
				if (!receipt.isTxStatusOK()) {
					try {
						InternalTransferData backData = new InternalTransferData(data.getToAddress(),
								data.getFromAddress(), BigInteger.ZERO, value);
						addContractTx2SaveQueue(cm.getSignature() + "_" + (i + 1), backData);
					} catch (Exception e) {
						logger.error("handle error: {}", e);
					}
				}
			}
		}
	}

	private void addContractFeeTx2SaveQueue(String mHash, InternalTransferData data) throws InterruptedException {
		// node.setSystemAutoTxMaxId(node.getSystemAutoTxMaxId().add(BigInteger.ONE));
		dep.addSystemAutoTxMaxId(1);
		JSONObject o = new JSONObject();
		// o.put("id", node.getSystemAutoTxMaxId());
		o.put("id", dep.getSystemAutoTxMaxId());
		o.put("type", "contract_fee_tx");
		o.put("mHash", mHash);
		o.put("fromAddress", data.getFromAddress());
		o.put("toAddress", Config.FOUNDATION_ADDRESS);
		o.put("amount", data.getFee());
		o.put("updateTime", Instant.now().toEpochMilli());
		// node.getSystemAutoTxSaveQueue().put(o);
		dep.getSystemAutoTxSaveQueue().put(o);
	}

	private void addContractTx2SaveQueue(String mHash, InternalTransferData data) throws InterruptedException {
		// node.setSystemAutoTxMaxId(node.getSystemAutoTxMaxId().add(BigInteger.ONE));
		dep.addSystemAutoTxMaxId(1);
		JSONObject o = new JSONObject();
		// o.put("id", node.getSystemAutoTxMaxId());
		o.put("id", dep.getSystemAutoTxMaxId());
		o.put("type", "contract_tx");
		o.put("mHash", mHash);
		o.put("fromAddress", data.getFromAddress());
		o.put("toAddress", data.getToAddress());
		o.put("amount", data.getValue());
		o.put("updateTime", Instant.now().toEpochMilli());
		// node.getSystemAutoTxSaveQueue().put(o);
		dep.getSystemAutoTxSaveQueue().put(o);
	}
}

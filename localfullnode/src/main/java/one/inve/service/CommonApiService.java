package one.inve.service;

import java.math.BigInteger;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import one.inve.backups.ValuableDataOutputters;
import one.inve.beans.dao.Message;
import one.inve.beans.dao.SystemAutoArray;
import one.inve.beans.dao.TransactionArray;
import one.inve.db.transaction.QueryTableSplit;
import one.inve.node.GeneralNode;
import one.inve.util.TxVerifyUtils;

/**
 * @author Clare
 * @date 2018/6/29 0029.
 */
public class CommonApiService {
	private static final Logger logger = LoggerFactory.getLogger(CommonApiService.class);

	/**
	 * 发送交易消息
	 * 
	 * @param message 交易消息
	 * @return 成功则返回空串，否则返回错误信息
	 */
	public synchronized static String sendMessage(String message, GeneralNode node) {
		try {
			if (node.getShardId() == -1L || node.getShardCount() < 1) {
				return "service is not available.";
			}
			if (TxVerifyUtils.verifyMessage(message, node)) {
//            if(TxVerifyUtils.verifyMessage(message, node.nodeParameters.dbId, node.getShardId(), node.getShardCount())) {
				logger.debug("receive a transaction, and verify success.");
				int multiple = node.nodeParameters.multiple;
				for (int i = 0; i < multiple; i++) {
					node.getMessageQueue().add(message.getBytes());

					// Francis.Deng 04/26/2019
					// back up messages from wallet client intentionally
					ValuableDataOutputters.getInstance().outputVerifiedMessage(message);
				}
				return "";
			} else {
				logger.error("--- sendMessage() verify failed. \nmsg: {}", message);
				return "verify failed.";
			}
		} catch (Exception e) {
			logger.error("??? sendMessage() verify exception: {} \nmsg: {}", e.getMessage(), message);
			return String.format("verify failed: %s", e.getMessage());
		}
	}

	/**
	 * 用于交易地址查询接口
	 * 
	 * @param tableIndex 表ID
	 * @param offset     表中偏移量
	 * @param address    地址
	 * @param type       类型：1交易 2合约 3.快照 4文本
	 * @param node       主对象
	 * @return 交易列表
	 */
	public synchronized static TransactionArray queryTransaction(BigInteger tableIndex, long offset, String address,
			String type, GeneralNode node) {
		QueryTableSplit queryTableSplit = new QueryTableSplit();
		return queryTableSplit.queryTransaction(tableIndex, offset, address, type, node.nodeParameters.dbId);
	}

	/**
	 * 用于区块链浏览器
	 * 
	 * @param tableIndex 表ID
	 * @param offset     表中偏移量
	 * @param type       类型：1交易 2合约 3.快照 4文本
	 * @param node       主对象
	 * @return 交易列表
	 */
	public synchronized static TransactionArray queryTransaction(BigInteger tableIndex, long offset, Integer type,
			GeneralNode node) {
		QueryTableSplit queryTableSplit = new QueryTableSplit();
		return queryTableSplit.queryTransaction(tableIndex, offset, type, node.nodeParameters.dbId);
	}

	/**
	 * 用于区块链浏览器 获取SystemAuto表信息
	 * 
	 * @param tableIndex 表ID
	 * @param offset     表中偏移量
	 * @param node       主对象
	 * @return 交易列表
	 */
	public synchronized static SystemAutoArray querySystemAuto(BigInteger tableIndex, long offset, GeneralNode node) {
		QueryTableSplit queryTableSplit = new QueryTableSplit();
		return queryTableSplit.querySystemAuto(tableIndex, offset, node.nodeParameters.dbId);
	}

//    /**
//     * get all transactions for light node (wallet).
//     * @param address 收款地址
//     * @return 交易历史记录
//     */
	public static List<Message> getTransactionHistory(String address, GeneralNode node) {
		return TransactionDbService.queryTransactionHistory(address, node.nodeParameters.dbId);
	}
//    /**
//     * 根据event的hash获取transaction属于第几张表
//     * @param eHash event的hash值
//     * @return Event所有交易
//     */
//    public String queryTransactionEvent(String eHash) {
//        QueryTableSplit queryTableSplit=new QueryTableSplit();
//        return  queryTableSplit.queryTransactionEvent(eHash, node.getTransDbName());
//    }
//    /**
//     * 获取event交易的数据库文件地址路径
//     * @return event数据库文件路径
//     */
//    public String getDataFileUrl(){
//        return node.eventDbName;
//    }
}

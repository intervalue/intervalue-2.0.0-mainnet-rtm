package one.inve.contract.MVM;

import com.alibaba.fastjson.JSON;
import one.inve.bean.message.ContractMessage;
import one.inve.contract.ContractTransactionData;
import one.inve.contract.ethplugin.config.SystemProperties;
import one.inve.contract.ethplugin.core.*;
import one.inve.contract.ethplugin.db.BlockStoreDummy;
import one.inve.contract.ethplugin.vm.program.ProgramResult;
import one.inve.contract.inve.INVERepositoryRoot;
import one.inve.contract.inve.INVETransactionExecutor;
import one.inve.contract.inve.INVETransactionReceipt;
import one.inve.contract.inve.InternalTransferData;
import one.inve.contract.inve.vm.program.invoke.INVEProgramInvokeFactoryImpl;
import one.inve.contract.provider.RepositoryProvider;
import one.inve.contract.conf.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.List;

/**
 * 提供世界状态更新服务
 * 
 * @author 肖毅
 * @since 2019-01-12
 */
public class WorldStateService {
	private static final Logger logger = LoggerFactory.getLogger("contract");

	/**
	 * 世界状态更新：增加账户余额
	 * 
	 * @param address
	 * @param value
	 * @return true | false
	 */
	protected static boolean increaseBalance(String dbId, String address, BigInteger value) {
		Repository track = getTrack(dbId);
		if (!track.isExist(address.getBytes())) {
			track.createAccount(address.getBytes());
		}
		if (track.getBalance(address.getBytes()).compareTo(new BigInteger("0")) >= 0) {
			track.addBalance(address.getBytes(), value);

			((INVERepositoryRoot) track).commit(dbId);
			return true;
		}

		logger.error("Invalid address[{}], the balance of address is {}", address,
				track.getBalance(address.getBytes()));
		return false;
	}

	/**
	 * 世界状态更新：减少账户余额
	 * 
	 * @param address
	 * @param value
	 * @return true | false
	 */
	protected static boolean decreaseBalance(String dbId, String address, BigInteger value) {
		Repository track = getTrack(dbId);
		if (track.isExist(address.getBytes()) && track.getBalance(address.getBytes()).compareTo(value) >= 0) {
			track.addBalance(address.getBytes(), value.negate());
			return true;
		}

		logger.error("Not enough balance, the balance of address[{}] is less than {}", address, value);
		return false;
	}

	/**
	 * 转账
	 * 
	 * @param fromAddr
	 * @param toAddr
	 * @param value
	 * @return true | false
	 * @throws NullPointerException
	 */
	public static boolean transfer(String dbId, String fromAddr, String toAddr, BigInteger value)
			throws NullPointerException {
		boolean specialPermission = false; // 创世地址 --> 基金会，转账放行
		boolean result = false;

		if (fromAddr == null || fromAddr.isEmpty()) {
			logger.error("Transfer failed, from address is null or empty.");
			throw new NullPointerException("Transfer failed, from address is null or empty.");
		} else if (toAddr == null || toAddr.isEmpty()) {
			logger.error("Transfer failed, to address is null or empty.");
			throw new NullPointerException("Transfer failed, to address is null or empty.");
		} else if (value == null) {
			logger.error("Transfer failed, value is null.");
			throw new NullPointerException("Transfer failed, value is null.");
		}

		if (Config.GOD_ADDRESS.equals(fromAddr)
				&& (Config.CREATION_ADDRESSES.contains(toAddr) || Config.FOUNDATION_ADDRESS.equals(toAddr))) {
			specialPermission = true;
		}

		if (!specialPermission) { // 不存在特许转账：GOD_ADDRESS --> foundation_address
			result = decreaseBalance(dbId, fromAddr, value);
		}

		if (result || specialPermission) {
			result = increaseBalance(dbId, toAddr, value);
		}

		return result;
	}

	/**
	 * 执行合约消息，返回余额变动列表
	 * 
	 * @param contractMsg
	 * @return List<InternalTransferData>
	 * @throws NullPointerException
	 */
	public static List<InternalTransferData> executeContractMessage(String dbId, ContractMessage contractMsg)
			throws NullPointerException, RuntimeException {
		if (contractMsg == null) {
			logger.error("Execute contract tx failed, ContractMessage is null.");
			throw new NullPointerException("Execute contract tx failed, ContractMessage is null.");
		}

		long start = System.currentTimeMillis();

		ContractTransactionData ct = null;
		try {
		    logger.debug("message is: {}", new String(contractMsg.getData()));
			ct = JSON.parseObject(contractMsg.getData(), ContractTransactionData.class);
            logger.debug("====== Unmarshaled contract transaction info ======");
            logger.debug("nonce: {}", new BigInteger(ct.getNonce()));
            logger.debug("gas price: {}", new BigInteger(ct.getGasPrice()));
            logger.debug("gas limit: {}", new BigInteger(ct.getGasLimit()));
            logger.debug("value: {}", new BigInteger(ct.getValue()));

            byte[] calldata = Hex.decode(ct.getCalldata());
            logger.debug("call data: {}", new String(calldata));
//            logger.debug("打印正常 byte 数组:");
//            for(byte b:calldata) {
//                logger.debug("\t {}", String.valueOf(b));
//            }
            logger.debug("====== end ======");
        } catch (Exception e) {
            logger.error("Unmarshal contract message failed.", e);
            logger.error("Contract message to be unmarshalled is: {}", contractMsg.getData());
            throw new RuntimeException("Unmarshal contract message failed.", e);
        }

		List<InternalTransferData> internalTransferDataList = executeTransaction(
		        dbId,
                ct,
				contractMsg.getFromAddress().getBytes(),
                contractMsg.getSignature().getBytes(),
                contractMsg.getTimestamp()/1000
        );

		long end = System.currentTimeMillis();
		logger.debug("Smart contract transaction execution time: {} ms.", end - start);
		return internalTransferDataList;
	}

	/**
	 * 根据地址获取余额
	 * 
	 * @param address
	 * @return BigInteger
	 * @throws NullPointerException
	 */
	public static BigInteger getBalanceByAddr(String dbId, String address) throws NullPointerException {
		if (address == null || address.isEmpty()) {
			logger.error("Get balance failed, address is null or empty.");
			throw new NullPointerException("Get balance failed, address is null or empty.");
		}

		return getTrack(dbId).getBalance(address.getBytes());
	}

	private static Repository getTrack(String dbId) {
		return RepositoryProvider.getTrack(dbId);
	}

	/**
	 * 根据传入的 ContractTransaction 构造交易
	 */
	protected static List<InternalTransferData> executeTransaction(String dbId, ContractTransactionData ct,
			byte[] fromAddr, byte[] signatrue, long timestamp) {
		logger.debug("============= Starting executeTransaction");
        logger.debug("nonce: {}", new BigInteger(ct.getNonce()));
        logger.debug("gas price: {}", new BigInteger(ct.getGasPrice()));
        logger.debug("gas limit: {}", new BigInteger(ct.getGasLimit()));
        logger.debug("value: {}", new BigInteger(ct.getValue()));

        byte[] calldata = ct.getCalldata().getBytes();
        logger.debug("call data before decode: {}", new String(calldata));
        calldata = Hex.decode(calldata);
        logger.debug("call data after decode: {}", new String(calldata));

		Transaction tx = new Transaction(
                new BigInteger(ct.getNonce()).toByteArray(),
                new BigInteger(ct.getGasPrice()).toByteArray(),
                new BigInteger(ct.getGasLimit()).toByteArray(),
                ct.getToAddress().getBytes(),
                new BigInteger(ct.getValue()).toByteArray(),
                calldata
        );
		tx.setSender(fromAddr);

		logger.debug("Transaction initialized.");

		Repository track = getTrack(dbId);

        Block block = new Block();
        block.setTimestamp(timestamp);

        logger.debug("timestamp is: {}", timestamp);
        
		INVETransactionExecutor executor = new INVETransactionExecutor(tx, track, new BlockStoreDummy(),
				new INVEProgramInvokeFactoryImpl(), block);

		logger.debug("*** New TX arrived:");
		logger.debug("\\==== Sender Balance before exec is: {}", track.getBalance(fromAddr));
		logger.debug("Sender nonce before exec is: {}", track.getNonce(fromAddr).longValue());

		executor.init();
		executor.execute();
		executor.go();
		TransactionExecutionSummary summary = executor.finalization();
		List<InternalTransferData> internalTransferDataList = summary.getBalanceChanges();
		if (logger.isDebugEnabled()) {
			logger.debug("==> Ready to print balance changes:");
			for (InternalTransferData bd : internalTransferDataList) {
				logger.debug(bd.toString());
			}
		}

		// 交易执行的收据信息
		INVETransactionReceipt receipt = executor.getReceipt();
		logger.info("TX {} execution result: {}", tx.hashCode(), receipt.isSuccessful());
		logger.debug("============== Receipt of TX ==============");
		logger.debug(receipt.toString());
		logger.debug("Sender nonce after exec is: {}", track.getNonce(fromAddr).longValue());

		// 程序执行结果
		ProgramResult result = executor.getResult();
		logger.info("Gas used {}, TX reverted: {}", result.getGasUsed(), result.isRevert());

		BigInteger senderBalance = track.getBalance(tx.getSender());
		logger.debug("\\==== Sender Balance after exec is: {}", senderBalance);

		// 存储交易收据
		if (track instanceof INVERepositoryRoot) { // 只有蒿师兄的存储实现才有对交易 receipt 的存储功能
			((INVERepositoryRoot) track).setReceipt(signatrue, receipt.getEncoded());
			// 入库
			((INVERepositoryRoot) track).commit(dbId);
		}
		logger.debug("Sender nonce after commit is: {}", track.getNonce(fromAddr).longValue());
		logger.debug("\n\n");

		return internalTransferDataList;
	}

	/**
	 * 獲取當前世界狀態的 roothash
	 * 
	 * @param dbId
	 * @return byte[]
	 */
	public static byte[] getRoothash(String dbId) {
		Repository track = getTrack(dbId);

		// TODO 備份數據庫以及考慮恢復

		return ((INVERepositoryRoot) track).getRoot();
	}

	// temporal methanism which is not rigirous
	/**
	 * 直接設置世界狀態餘額
	 * 
	 * @param dbId
	 * @param address
	 * @param value
	 * @return
	 */
	public static void setBalance(String dbId, String address, BigInteger value) {
		// Repository track = getTrack(dbId);

		// // 讀取餘額
		// BigInteger balance = track.getBalance(address.getBytes());
		// // 餘額清零
		// track.addBalance(address.getBytes(), balance.negate());
		// // 直接設置餘額
		// track.addBalance(address.getBytes(), value);
		// // force it to commit root
		// ((INVERepositoryRoot) track).commit(dbId);
	}

	/**
     * 執行無手續費查詢世界狀態
     * @param dbId 指定的节点 ID
     * @param address 合約地址
     * @param callData 要执行的合约函数以及参数
     * @return 查詢結果
     */
    public static byte[] executeViewTransaction(String dbId, String address, String callData) {
        Transaction tx = new Transaction(
            ByteBuffer.allocate(4).putInt(0).array(),   // nonce
            ByteBuffer.allocate(4).putInt(10).array(),   // gas price
            ByteBuffer.allocate(4).putInt(2000000).array(),   // gas limit
            address.getBytes(),                         // to address
            ByteBuffer.allocate(4).putInt(0).array(),   // value
            Hex.decode(callData)
        );

        tx.setSender("TGAX77OVU3AGOYGKUF5IFGZVRQJ23ZFB".getBytes());

        Repository track = getTrack(dbId);
        INVETransactionExecutor executor = new INVETransactionExecutor(
            tx, track,
            new BlockStoreDummy(),
            new INVEProgramInvokeFactoryImpl(),
            SystemProperties.getDefault().getGenesis()
        ).setLocalCall(true);

        executor.init();
        executor.execute();
        executor.go();

        return executor.getResult().getHReturn();
    }
}
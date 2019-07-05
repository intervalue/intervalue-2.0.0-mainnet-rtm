package one.inve.localfullnode2.http;

import java.math.BigInteger;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.BigIntegers;
import org.spongycastle.util.encoders.Hex;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import one.inve.contract.MVM.WorldStateService;
import one.inve.contract.ethplugin.core.Repository;
import one.inve.contract.inve.INVERepositoryRoot;
import one.inve.contract.inve.INVETransactionReceipt;
import one.inve.contract.provider.RepositoryProvider;
import one.inve.localfullnode2.conf.Config;
import one.inve.localfullnode2.nodes.LocalFullNode1GeneralNode;
import one.inve.localfullnode2.store.rocks.BlockBrowserInfo;
import one.inve.localfullnode2.store.rocks.Message;
import one.inve.localfullnode2.store.rocks.RocksJavaUtil;
import one.inve.localfullnode2.store.rocks.SystemAutoArray;
import one.inve.localfullnode2.store.rocks.TransactionArray;
import one.inve.localfullnode2.utilities.HttpUtils;
import one.inve.localfullnode2.utilities.ResponseUtils;
import one.inve.localfullnode2.utilities.StringUtils;
import one.inve.localfullnode2.utilities.http.DataMap;
import one.inve.localfullnode2.utilities.http.HttpServiceImplsDependent;
import one.inve.localfullnode2.utilities.http.annotation.MethodEnum;
import one.inve.localfullnode2.utilities.http.annotation.RequestMapper;

/**
 * 
 * Copyright © CHXX Co.,Ltd. All rights reserved.
 * 
 * @Description: {@link HttpApiService} is highly coupled with
 *               {@link LocalFullNode1GeneralNode},making sure that it's pass by
 *               the caller.
 * @author: Francis.Deng
 * @date: May 13, 2019 3:04:43 AM
 * @version: V1.0
 */
public class HttpApiService {
	private static final Logger logger = LoggerFactory.getLogger(HttpApiService.class);

	private LocalFullNode1GeneralNode node;

	public HttpApiService(HttpServiceImplsDependent dep) {
		if (dep instanceof HttpServiceDependency) {
			this.node = ((HttpServiceDependency) dep).getNode();
		}
	}

	/**
	 * 发送消息上链
	 * 
	 * @param data 消息
	 * @return 成功返回“”，否则返回错误信息
	 */
	@RequestMapper(value = "/v1/sendmsg", method = MethodEnum.POST)
	public String sendMessage(DataMap<String, Object> data) {
		if (null == data || data.isEmpty()) {
			logger.error("parameter is empty.");
			return ResponseUtils.paramIllegalResponse();
		}
		String message = data.getString("message");
		if (StringUtils.isEmpty(message)) {
			logger.error("parameter is empty.");
			return ResponseUtils.paramIllegalResponse();
		}
		try {
			String result = CommonApiService.sendMessage(message, node);
			if (StringUtils.isEmpty(result)) {
				return ResponseUtils.normalResponse();
			} else {
				return ResponseUtils.response(203, result);
			}
		} catch (Exception e) {
			logger.error("sendmsg handle error: {}", e);
			return ResponseUtils.response(203, "send msg error");
		}
	}

	/**
	 * 查询交易列表
	 * 
	 * @param data 查询参数 例： { "address": "4PS6MZX6T7ELDSD2RUOZRSYGCC5RHOS7",
	 *             "tableIndex":0, "offset":0} - address 地址 - tableIndex 表索引 -
	 *             offset 表中偏移量
	 * @return 交易记录列表
	 */
	@RequestMapper(value = "/v1/gettransactionlist", method = MethodEnum.POST)
	public String getTransactionList(DataMap<String, Object> data) {
		if (null == data || data.isEmpty()) {
			logger.error("parameter is empty.");
			return ResponseUtils.paramIllegalResponse();
		}
		String address = data.getString("address");
		if (StringUtils.isEmpty(address)) {
			logger.error("parameter is empty.");
			return ResponseUtils.paramIllegalResponse();
		}
		StringBuilder type = new StringBuilder();
		if (data.containsKey("type[0]")) {
			type.append(data.getInteger("type[0]"));
		}
		if (data.containsKey("type[1]")) {
			type.append(",").append(data.getInteger("type[1]"));
		}
		if (data.containsKey("type[2]")) {
			type.append(",").append(data.getInteger("type[2]"));
		}
		if (data.containsKey("type[3]")) {
			type.append(",").append(data.getInteger("type[3]"));
		}
		BigInteger tableIndex = (null == data.getString("tableIndex")) ? BigInteger.ZERO
				: new BigInteger(data.getString("tableIndex"));
		long offset = (null == data.getLong("offset")) ? 0 : data.getLong("offset");

		try {
			TransactionArray trans = CommonApiService.queryTransaction(tableIndex, offset, address, type.toString(),
					node);

			return ResponseUtils.normalResponse(null == trans ? "" : JSON.toJSONString(trans));
		} catch (Exception e) {
			logger.error("gettransactionlist handle error: {}", e);
			return ResponseUtils.handleExceptionResponse();
		}
	}

	/**
	 * 根据hash值查询对应交易记录
	 * 
	 * @param data 交易的Hash值 例：
	 *             {"hash":"33AM+B7ZRErCw9wkR9XXHKIdR0crmEUr6M4LIoQOREI4faaTw6qmC7Og1WP65hKJPKwSyKFOnKN0yny29kAmXAQVM="}
	 * @return 交易记录
	 */
	@Deprecated
	@RequestMapper(value = "/v1/gettransaction", method = MethodEnum.POST)
	public String getTransaction(DataMap<String, Object> data) {
		if (null == data || data.isEmpty()) {
			logger.error("parameter is empty.");
			return ResponseUtils.paramIllegalResponse();
		}

		String hash = data.getString("hash");
		if (StringUtils.isEmpty(hash)) {
			logger.error("parameter is empty.");
			return ResponseUtils.paramIllegalResponse();
		}
		try {
			Message message = null;
			byte[] transationByte = new RocksJavaUtil(node.nodeParameters().dbId).get(hash);
			if (transationByte != null) {
				if (new String(transationByte).contains("isStable")) {
					message = JSON.parseObject(transationByte, Message.class);
				}
			}else {
				message = CommonApiService.querySystemAutoToMessage(node,hash);
			}
			return ResponseUtils.normalResponse((null == message) ? "" : JSON.toJSONString(message));
		} catch (Exception e) {
			logger.error("gettransaction handle error: {}", e);
			return ResponseUtils.handleExceptionResponse();
		}
	}

	/**
	 * 查询message列表
	 * 
	 * @param data 查询参数 例： { "address": "4PS6MZX6T7ELDSD2RUOZRSYGCC5RHOS7",
	 *             "tableIndex":0, "offset":0} - address 地址 - tableIndex 表索引 -
	 *             offset 表中偏移量
	 * @return message记录列表
	 */
	@RequestMapper(value = "/v1/message/list", method = MethodEnum.POST)
	public String getMessageList(DataMap<String, Object> data) {
		if (null == data || data.isEmpty()) {
			logger.error("parameter is empty.");
			return ResponseUtils.paramIllegalResponse();
		}
		String address = data.getString("address");
		if (StringUtils.isEmpty(address)) {
			logger.error("parameter is empty.");
			return ResponseUtils.paramIllegalResponse();
		}
		StringBuilder type = new StringBuilder();
		if (data.containsKey("type[0]")) {
			type.append(data.getInteger("type[0]"));
		}
		if (data.containsKey("type[1]")) {
			type.append(",").append(data.getInteger("type[1]"));
		}
		if (data.containsKey("type[2]")) {
			type.append(",").append(data.getInteger("type[2]"));
		}
		if (data.containsKey("type[3]")) {
			type.append(",").append(data.getInteger("type[3]"));
		}
		BigInteger tableIndex = (null == data.getString("tableIndex")) ? BigInteger.ZERO
				: new BigInteger(data.getString("tableIndex"));
		long offset = (null == data.getLong("offset")) ? 0 : data.getLong("offset");

		try {
			TransactionArray trans = CommonApiService.queryTransaction(tableIndex, offset, address, type.toString(),
					node);

			return ResponseUtils.normalResponse(null == trans ? "" : JSON.toJSONString(trans));
		} catch (Exception e) {
			logger.error("getMessageList handle error: {}", e);
			return ResponseUtils.handleExceptionResponse();
		}
	}

	/**
	 * 根据hash值查询对应message记录
	 * 
	 * @param data message的Hash值 例：
	 *             {"hash":"33AM+B7ZRErCw9wkR9XXHKIdR0crmEUr6M4LIoQOREI4faaTw6qmC7Og1WP65hKJPKwSyKFOnKN0yny29kAmXAQVM="}
	 * @return message记录
	 */
	@RequestMapper(value = "/v1/message/info", method = MethodEnum.POST)
	public String getMessage(DataMap<String, Object> data) {
		if (null == data || data.isEmpty()) {
			logger.error("parameter is empty.");
			return ResponseUtils.paramIllegalResponse();
		}

		String hash = data.getString("hash");
		if (StringUtils.isEmpty(hash)) {
			logger.error("parameter is empty.");
			return ResponseUtils.paramIllegalResponse();
		}
		try {
			Message message = null;
			byte[] transationByte = new RocksJavaUtil(node.nodeParameters().dbId).get(hash);
			if (transationByte != null) {
				message = JSON.parseObject(transationByte, Message.class);
			}
			return ResponseUtils.normalResponse((null == message) ? "" : JSON.toJSONString(message));
		} catch (Exception e) {
			logger.error("getMessage handle error: {}", e);
			return ResponseUtils.handleExceptionResponse();
		}
	}

	/**
	 * 根据key获取value
	 * 
	 * @param data 键 例：
	 *             {"key":"328FELEWS/Z4emYTnY9XYf5GDoBJ2iOcwcg1lNWA9uEcRQ9ZqPA31YBzANa2WMd999W7GPELYt6s6D3AThwrxAWw=="}
	 * @return 值
	 */
	@RequestMapper(value = "/v1/getvalue", method = MethodEnum.POST)
	public String getValue(DataMap<String, Object> data) {
		if (null == data || data.isEmpty()) {
			logger.error("parameter is empty.");
			return ResponseUtils.paramIllegalResponse();
		}

		String key = data.getString("key");
		if (StringUtils.isEmpty(key)) {
			logger.error("parameter is empty.");
			return ResponseUtils.paramIllegalResponse();
		}
		try {
			byte[] value = new RocksJavaUtil(node.nodeParameters().dbId).get(key);
			if (value != null) {
				return ResponseUtils.normalResponse(new String(value));
			} else {
				return ResponseUtils.normalResponse();
			}
		} catch (Exception e) {
			logger.error("getvalue handle error: {}", e);
			return ResponseUtils.handleExceptionResponse();
		}
	}

	/**
	 * 无地址查询所有交易列表 用于区块链浏览器获取数据
	 * 
	 * @param data 查询参数 例： { "tableIndex":0, "offset":0} - tableIndex 表索引 - offset
	 *             表中偏移量
	 * @return 交易记录列表
	 */
	@RequestMapper(value = "/v1/getmessagelist", method = MethodEnum.POST)
	public String messageHistorySplit(DataMap<String, Object> data) {
		if (null == data || data.isEmpty()) {
			logger.error("parameter is empty.");
			return ResponseUtils.paramIllegalResponse();
		}

		BigInteger tableIndex = (null == data.getString("tableIndex")) ? BigInteger.ZERO
				: new BigInteger(data.getString("tableIndex"));
		long offset = (null == data.getLong("offset")) ? 0 : data.getLong("offset");
		Integer type = null;
		if (data.containsKey("type")) {
			type = data.getInteger("type");
		}
		try {
			TransactionArray trans = CommonApiService.queryTransaction(tableIndex, offset, type, node);

			return ResponseUtils.normalResponse(null == trans ? "" : JSON.toJSONString(trans));
		} catch (Exception e) {
			logger.error("getmessagelist handle error: {}", e);
			return ResponseUtils.handleExceptionResponse();
		}
	}

	/**
	 * 无地址查询所有交易列表 用于区块链浏览器获取数据
	 * 
	 * @param data 查询参数 例： { "tableIndex":0, "offset":0} - tableIndex 表索引 - offset
	 *             表中偏移量
	 * @return 交易记录列表
	 */
	@RequestMapper(value = "/v1/getSystemAutolist", method = MethodEnum.POST)
	public String getSystemAutolist(DataMap<String, Object> data) {
		if (null == data || data.isEmpty()) {
			logger.error("parameter is empty.");
			return ResponseUtils.paramIllegalResponse();
		}

		BigInteger tableIndex = (null == data.getString("tableIndex")) ? BigInteger.ZERO
				: new BigInteger(data.getString("tableIndex"));
		long offset = (null == data.getLong("offset")) ? 0 : data.getLong("offset");

		try {
			SystemAutoArray trans = CommonApiService.querySystemAuto(tableIndex, offset, node);

			return ResponseUtils.normalResponse(null == trans ? "" : JSON.toJSONString(trans));
		} catch (Exception e) {
			logger.error("getSystemAutolist handle error: {}", e);
			return ResponseUtils.handleExceptionResponse();
		}
	}

	/**
	 * 获取用户数量、tps、运行时间等inve区块链系统信息 用于区块链浏览器获取数据
	 * 
	 * @param data {}
	 * @return 用户数量、tps、运行时间等inve区块链系统信息
	 */
	@RequestMapper(value = "/v1/getmessageInfo", method = MethodEnum.POST)
	public String getExplorerInfo(DataMap<String, Object> data) {
		try {
			BlockBrowserInfo info = new BlockBrowserInfo();
			// 用户数量 提供默认数量，由区块链浏览器去计算
			info.setUserCount(0L);

			// intervalue区块链系统运行时间
			byte[] value = new RocksJavaUtil(node.nodeParameters().dbId).get(Config.CREATION_TIME_KEY);
			Message message = null;
			if (value != null) {
				String json = new String(value);
				if (StringUtils.isNumeric(json)) {
					info.setRunTime(new Long(json));
				}

			}
			// TPS： 准备从rocksDB中获取，rocksDB保存最大的TPS数量
			value = new RocksJavaUtil(node.nodeParameters().dbId).get(Config.MESSAGE_TPS_KEY);
			if (value != null) {
				info.setTps("" + new String(value));
			}

			// 节点数量: 调用全节点接口，查询局部全节点总数量
			HashMap<String, String> pam = new HashMap<>();
			pam.put("pubkey", "asdfads");
			String seedIP = node.nodeParameters().seedGossipAddress.pubIP;
			int seedHttpPort = node.nodeParameters().seedGossipAddress.httpPort;
			String url = "http://" + seedIP + ":" + seedHttpPort + "/v1/getlocalfullnodesLength";

			String result = HttpUtils.httpPost(url, pam);
			String num = JSON.parseObject(result).getString("data");
			info.setShardNumber(num);

			return ResponseUtils.normalResponse(JSON.toJSONString(info));
		} catch (Exception e) {
			logger.error("getExplorerInfo handle error: {}", e);
			return ResponseUtils.handleExceptionResponse();
		}
	}

	/**
	 * 根据地址，查询对应nonce和账户余额
	 * 
	 * @param data 交易的Hash值 { "address":"4PS6MZX6T7ELDSD2RUOZRSYGCC5RHOS7"}
	 * @return {"nonce":"0" , "balance":"100000"}
	 */
	@RequestMapper(value = "/v1/account/info", method = MethodEnum.POST)
	public String getAccountInfo(DataMap<String, Object> data) {
		if (null == data || data.isEmpty()) {
			logger.error("parameter is empty.");
			return ResponseUtils.paramIllegalResponse();
		} else {
			logger.debug("getAccountInfo value is: {}", JSON.toJSONString(data));
		}

		String address = data.getString("address");
		if (StringUtils.isEmpty(address)) {
			logger.error("parameter is empty.");
			return ResponseUtils.paramIllegalResponse();
		}
		try {
			Repository track = RepositoryProvider.getTrack(node.nodeParameters().dbId);
			BigInteger nonce = track.getNonce(address.getBytes());
			BigInteger balance = track.getBalance(address.getBytes());

			JSONObject jo = new JSONObject();
			jo.put("nonce", nonce);
			jo.put("balance", balance);
			return ResponseUtils.normalResponse((null == nonce) ? "" : jo.toJSONString());
		} catch (Exception e) {
			logger.error("getAccountInfo handle error: {}", e);
			return ResponseUtils.handleExceptionResponse();
		}
	}

	/**
	 * 根据交易哈希，查询对应交易收據
	 * 
	 * @param data 交易的Hash值 { "hash":"HEX String"}
	 * @return {"txStatus":true, "gasUsed":"10000", "executionResult":"HEX String",
	 *         "error":"error String", "logs":"list of logs"}
	 */
	@RequestMapper(value = "/v1/getReceipt", method = MethodEnum.POST)
	public String getReceipt(DataMap<String, Object> data) {
		if (null == data || data.isEmpty()) {
			logger.error("parameter is empty.");
			return ResponseUtils.paramIllegalResponse();
		} else {
			logger.error("getReceipt value is: {}", JSON.toJSONString(data));
		}

		String hashHex = data.getString("hash");
		if (StringUtils.isEmpty(hashHex)) {
			logger.error("parameter is empty.");
			return ResponseUtils.paramIllegalResponse();
		}
		try {
			Repository track = RepositoryProvider.getTrack(node.nodeParameters().dbId);
			byte[] receiptB = ((INVERepositoryRoot) track).getReceipt(hashHex.getBytes());

			INVETransactionReceipt receipt = new INVETransactionReceipt(receiptB);

			JSONObject jo = new JSONObject();
			jo.put("txStatus", receipt.isTxStatusOK() ? 1 : 0);
			jo.put("gasUsed", BigIntegers.fromUnsignedByteArray(receipt.getGasUsed()));
			jo.put("executionResult", Hex.toHexString(receipt.getExecutionResult()));
			jo.put("error", new String(receipt.getError()));
//            jo.put("logs", receipt.getLogInfoList());

			return ResponseUtils.normalResponse(jo.toJSONString());
		} catch (Exception e) {
			logger.error("getReceipt handle error: {}", e);
			return ResponseUtils.handleExceptionResponse();
		}
	}

	/**
	 * 查询合约属性值（无手续费）
	 * 
	 * @param data
	 * @return
	 */
	@RequestMapper(value = "/v1/sendViewMessage", method = MethodEnum.POST)
	public String sendViewMessage(DataMap<String, Object> data) {
		if (null == data || data.isEmpty()) {
			logger.error("parameter is empty.");
			return ResponseUtils.paramIllegalResponse();
		} else {
			logger.error("getReceipt value is: {}", JSON.toJSONString(data));
		}

		// 读取查询参数(函数选择器+调用值)
		String calldata = data.getString("calldata");
		if (StringUtils.isEmpty(calldata)) {
			logger.error("parameter is empty.");
			return ResponseUtils.paramIllegalResponse();
		}
		// 读取目标合约地址
		String address = data.getString("address");
		if (StringUtils.isEmpty(calldata)) {
			logger.error("parameter is empty.");
			return ResponseUtils.paramIllegalResponse();
		}

		try {
			byte[] result = WorldStateService.executeViewTransaction(node.nodeParameters().dbId, address, calldata);

			JSONObject jo = new JSONObject();
			jo.put("result", Hex.toHexString(result));

			return ResponseUtils.normalResponse(jo.toJSONString());
		} catch (Exception e) {
			logger.error("sendViewMessage handle error: {}", e);
			return ResponseUtils.handleExceptionResponse();
		}
	}
}

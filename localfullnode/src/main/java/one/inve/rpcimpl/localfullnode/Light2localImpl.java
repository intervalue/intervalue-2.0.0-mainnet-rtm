package one.inve.rpcimpl.localfullnode;

import com.alibaba.fastjson.JSONArray;
import com.zeroc.Ice.Current;
import one.inve.beans.dao.Message;
import one.inve.beans.dao.TransactionArray;
import one.inve.db.transaction.QueryTableSplit;
import one.inve.node.GeneralNode;
import one.inve.rpc.localfullnode.Light2local;
import one.inve.service.CommonApiService;
import one.inve.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.List;
import java.util.concurrent.CompletionStage;

/**
 * light node to local full node
 * @author Clare
 * @date   2018/6/5.
 */
public class Light2localImpl implements Light2local {
    private static final Logger logger = LoggerFactory.getLogger(Light2localImpl.class);
    GeneralNode node;

    public Light2localImpl(GeneralNode node) {
        this.node = node;
    }


    /**
     * The rpc server receives the message from a light node.
     * 1. validate message signature
     * 2. for the transaction message, the rpc server put the message into the message queue.
     {
         "fromAddress": "OXV2C5ZQ7REPHM2SA3G4JS3T73ZNQOBD",
         "toAddress": "U2VRUTMT4ZFMQTZTFTHOSUHIZJNFHFTE",
         "amount": 1,
         "timestamp": 1536300281,
         "pubkey": "A5RA5jwayqjubpbtYFd87zaphu8erm5iNDBqiNOB6TB1",
         "fee": 124,
         "type": 1,
         "signature": "D9JG5+/aAgYRdPcn+y1eMompfvJjKUGW5zgfZo1M9vZ7MJ97yQN1R3mpM1TwKtQnPmywCIs4Xs13qFG7q9qqFQ=="
     }
     * @param message .
     * @param current
     * @return callback the result to the requester.
     */
    @Override
    synchronized public CompletionStage<String> sendMessageAsync(String message, Current current) {
        if (StringUtils.isEmpty(message)) {
            logger.error("param unit is empty.");
        } else {
            CommonApiService.sendMessage(message, node);
        }
        return new java.util.concurrent.CompletableFuture<>();
    }

    /**
     * get all transactions for light node (wallet).
     * @param current
     * @return
     */
    @Override
    public String getTransactionHistory(String address, Current current) {
        if (StringUtils.isEmpty(address)) {
            logger.error("param gossipAddress is empty.");
            return "";
        } else {
            List<Message> trans = CommonApiService.getTransactionHistory(address, node);
            return (null==trans || trans.size()<=0) ? "" : JSONArray.toJSONString(trans);
        }
    }


    /**
     * 交易分库分表查询
     * 查询一次无数据，再次查询下一张表(最多查询两次)
     * @param tableIndex 表索引 第一次查询请输入0
     * @param offset  跳过多少条记录  第一次查询请输入0
     * @param address 查询地址
     * @param type      类型：1交易 2合约  3.快照 4文本
     * @return 交易记录
     */
	public String getTransactionHistorySplit(String tableIndex, Long offset, String address, String type,
			Current current) {
		 QueryTableSplit split=new QueryTableSplit();
    	if(null==tableIndex || new BigInteger(tableIndex).compareTo(BigInteger.ZERO)<=0 ) {
    		tableIndex="0";
    	}
    	if(offset<0) {
    		offset=0L;
    	}
    	TransactionArray array= split.queryTransaction(new BigInteger(tableIndex), offset, address, type, node.nodeParameters.dbId);
       return JSONArray.toJSONString(array);
	}
}

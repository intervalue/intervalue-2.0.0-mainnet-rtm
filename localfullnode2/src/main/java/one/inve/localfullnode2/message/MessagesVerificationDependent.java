package one.inve.localfullnode2.message;

import java.util.concurrent.BlockingQueue;

import com.alibaba.fastjson.JSONObject;

/**
 * 
 * Copyright © CHXX Co.,Ltd. All rights reserved.
 * 
 * @Description: TODO
 * @author: Francis.Deng
 * @date: May 7, 2019 2:42:58 AM
 * @version: V1.0
 */
public interface MessagesVerificationDependent {
	// the source from which messages are retrieved
	BlockingQueue<JSONObject> getConsMessageVerifyQueue();

	// the destination to which messages are sent
	BlockingQueue<JSONObject> getConsMessageHandleQueue();
}

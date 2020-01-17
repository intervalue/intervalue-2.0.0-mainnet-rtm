package one.inve.localfullnode2.chronicle.rpc.service;

import java.math.BigInteger;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import one.inve.bean.message.TransactionMessage;
import one.inve.localfullnode2.store.rocks.Message;

/**
 * 
 * Copyright © INVE FOUNDATION. All rights reserved.
 * 
 * @ClassName: InternalMessageAwareness
 * @Description: be aware of internal message and system message
 * @author Francis.Deng [francis_xiiiv@163.com]
 * @date Jan 16, 2020
 *
 */
public class InternalMessageAwareness {
	public static JSONObject fromMessageBytesToJSONObject(byte[] messageBytes) {
		JSONObject target = new JSONObject();
		JSONObject messageJSONObject = JSONObject.parseObject(new String(messageBytes));
		Message message = JSON.toJavaObject(messageJSONObject, Message.class);
		TransactionMessage msgInMessage = JSONObject.parseObject(message.getMessage(), TransactionMessage.class);

		target.put("msg", message.getMessage());
		target.put("lastIdx", message.isLastIdx());
		target.put("isStable", message.isStable());
		target.put("id", new BigInteger(message.getId()));
		target.put("hash", message.getHash());
		target.put("fromAddress", msgInMessage.getFromAddress());
		target.put("toAddress", msgInMessage.getToAddress());
		target.put("isValid", message.isValid());

		if (message.getError() != null) {
			target.put("error", message.getError());
		}
		target.put("updateTime", message.getUpdateTime());
		target.put("type", msgInMessage.getType());
		target.put("eHash", message.geteHash());
		if (message.getSnapVersion() != null) {
			target.put("snapVersion", message.getSnapVersion());
		}
		target.put("validState", 2);

		return target;
	}

	public static JSONObject fromSysMessageBytesToJSONObject(byte[] sysMessageBytes) {
		return JSONObject.parseObject(new String(sysMessageBytes));
	}
}

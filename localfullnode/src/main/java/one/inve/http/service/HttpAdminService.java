package one.inve.http.service;

import java.math.BigInteger;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;

import one.inve.bean.message.SnapshotMessage;
import one.inve.bean.message.SnapshotPoint;
import one.inve.http.DataMap;
import one.inve.http.annotation.MethodEnum;
import one.inve.http.annotation.RequestMapper;
import one.inve.node.GeneralNode;
import one.inve.service.SnapshotDbService;

/**
 * 
 * Copyright © CHXX Co.,Ltd. All rights reserved.
 * 
 * @Description: help administrator identify some defects.
 * @author: Francis.Deng
 * @date: May 9, 2019 1:29:19 AM
 * @version: V1.0
 */
public class HttpAdminService {
	private static final Logger logger = LoggerFactory.getLogger(HttpAdminService.class);

	GeneralNode node;

	public HttpAdminService(GeneralNode node) {
		this.node = node;
	}

	@RequestMapper(value = "/v1/admin/snapshot/latest", method = MethodEnum.POST)
	public String getLatestSnapshot(DataMap<String, Object> data) {
		SnapshotMessage snapshotMessage = SnapshotDbService.queryLatestSnapshotMessage(node.nodeParameters.dbId);
		HashMap<BigInteger, SnapshotPoint> snapshotPointMap = node.getSnapshotPointMap();
		HashMap<BigInteger, String> treeRootMap = node.getTreeRootMap();
		BigInteger totalConsEventCount = node.getTotalConsEventCount();

		String strSnapshotMessage = String.format("SnapshotMessage= %s/r/n", JSONObject.toJSONString(snapshotMessage));
		String strSnapshotPointMap = String.format("SnapshotPointMap= %s/r/n",
				JSONObject.toJSONString(snapshotPointMap));
		String strTreeRootMap = String.format("TreeRootMap= %s/r/n", JSONObject.toJSONString(treeRootMap));
		String strTotalConsEventCount = String.format("TotalConsEventCount= %s/r/n",
				JSONObject.toJSONString(totalConsEventCount));

		return strSnapshotMessage + strSnapshotPointMap + strTreeRootMap + strTotalConsEventCount;

	}
}

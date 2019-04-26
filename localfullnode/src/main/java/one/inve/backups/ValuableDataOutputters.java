package one.inve.backups;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;

import one.inve.core.EventBody;

/**
 * 
 * 
 * Copyright © CHXX Co.,Ltd. All rights reserved.
 * 
 * @Description: In order to avoid data loss,we intentionally append some data
 *               to file which acts as a backups,which is based on log4j
 *               technology.
 * @author: Francis.Deng
 * @date: Apr 26, 2019 1:05:28 AM
 * @version: V1.0
 */
public class ValuableDataOutputters {
	private static final Logger logger = LoggerFactory.getLogger("Backups");

	private static class ValuableDataOutputtersIns {
		private static final ValuableDataOutputters me = new ValuableDataOutputters();
	}

	private ValuableDataOutputters() {
	}

	public static ValuableDataOutputters getInstance() {
		return ValuableDataOutputtersIns.me;
	}

	public void outputVerifiedMessage(String verifiedMessage) {
		logger.info("@AA" + verifiedMessage);
	}

	public void outputGossipyEventBody(EventBody eb) {
		logger.info("@AB" + JSONObject.toJSONString(eb));
	}
}

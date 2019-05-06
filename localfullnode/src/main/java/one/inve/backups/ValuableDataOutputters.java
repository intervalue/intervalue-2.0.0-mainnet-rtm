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
 *               
 *               
				@formatter:off
				
				## data backups setting
				log4j.logger.one.inve.backups.ValuableDataOutputters=INFO,Backups
				log4j.additivity.one.inve.backups.ValuableDataOutputters = false
				
				log4j.appender.Backups = org.apache.log4j.DailyRollingFileAppender
				log4j.appender.Backups.File = backups/replay.log
				log4j.appender.Backups.Append = true
				log4j.appender.Backups.Threshold = INFO
				log4j.appender.Backups.layout = org.apache.log4j.PatternLayout
				log4j.appender.Backups.layout.ConversionPattern = %m%n

				
				@formatter:on


 * @author: Francis.Deng
 * @date: Apr 26, 2019 1:05:28 AM
 * @version: V1.0
 */
public class ValuableDataOutputters {
	private static final Logger logger = LoggerFactory.getLogger("Backups-datalost");

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

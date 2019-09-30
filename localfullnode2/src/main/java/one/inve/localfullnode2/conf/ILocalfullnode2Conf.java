package one.inve.localfullnode2.conf;

import java.util.List;

/**
 * 
 * 
 * Copyright © INVE FOUNDATION. All rights reserved.
 * 
 * @Description: see "intervalue.yaml.template"
 * @author: Francis.Deng [francis_xiiiv@163.com]
 * @date: Sep 12, 2019 2:32:37 AM
 * @version: V1.0
 */
public interface ILocalfullnode2Conf {
	String getPubIP();

	String getGossipPort();

	String getRpcPort();

	String getHttpPort();

	List<String> getWhitelist();

	String getPrefix();

	// seed configuration
	String getSeedPubIP();

	String getSeedGossipPort();

	String getSeedRpcPort();

	String getSeedHttpPort();

	DBConnectionDescriptorsConf getDbConnection();
}

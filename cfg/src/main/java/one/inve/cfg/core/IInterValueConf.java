package one.inve.cfg.core;

import java.util.Map;

/**
 * 
 * 
 * Copyright © INVE FOUNDATION. All rights reserved.
 * 
 * @Description: describe the things retrieving from configuration file.
 * @author: Francis.Deng [francis_xiiiv@163.com]
 * @date: Sep 12, 2019 2:31:30 AM
 * @version: V1.0
 */
public interface IInterValueConf {
	String getZerocContent();

	ILocalfullnode2Conf getLocalfullnode2Conf();

	IP2PClusterConf getP2PClusterConf();

	Map<String, String> getEnv();
}

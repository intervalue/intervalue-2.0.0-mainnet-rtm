package one.inve.cfg.core;

import one.inve.cfg.fullnode.Parameters;
import one.inve.cfg.localfullnode.NodeParameters;

/**
 * 
 * 
 * Copyright © INVE FOUNDATION. All rights reserved.
 * 
 * @Description: have a connection with {@code NodeParameters} and
 *               {@code Config}
 * @author: Francis.Deng [francis_xiiiv@163.com]
 * @date: Sep 12, 2019 2:30:23 AM
 * @version: V1.0
 */
public interface IConfImplant {
	void init(String[] args);

	String[] implantZerocConf();

	Parameters implantParameters(boolean isSeed);// full node only

	NodeParameters implantNodeParameters();

	void implantStaticConfig(boolean isSeed);// full node only

	void implantStaticConfig();

	void implantEnv();

	DBConnectionDescriptorsConf getDbConnection();

}

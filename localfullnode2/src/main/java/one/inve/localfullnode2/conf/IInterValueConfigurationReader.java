package one.inve.localfullnode2.conf;

import one.inve.localfullnode2.utilities.ReflectionUtils;

/**
 * 
 * 
 * Copyright © INVE FOUNDATION. All rights reserved.
 * 
 * @Description: TODO
 * @author: Francis.Deng [francis_xiiiv@163.com]
 * @date: Sep 11, 2019 11:37:45 PM
 * @version: V1.0
 * @see InterValueYamlReader
 */
public interface IInterValueConfigurationReader {
	public static String INTERVALUE_CONF_FILE_VARIABLE_NAME = "intervalue_conf_file";

	IInterValueConf read(String[] args);

	public static IInterValueConfigurationReader getDefaultImpl() {
		return (IInterValueConfigurationReader) ReflectionUtils
				.getInstanceByClassName("one.inve.localfullnode2.conf.InterValueYamlReader");
	}
}

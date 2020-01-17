package one.inve.localfullnode2.utilities;

import java.io.File;

import org.apache.log4j.PropertyConfigurator;

/**
 * 
 * Copyright © INVE FOUNDATION. All rights reserved.
 * 
 * @ClassName: Log4jSystem
 * @Description: divide the default log4j output by some command line arguments
 *               for troubleshooting,ensuring there is a CONF variable reference
 *               inside log4j.properties.
 * @author Francis.Deng [francis_xiiiv@163.com]
 * @date Jan 17, 2020
 *
 */
public class Log4jSystem {
	public void overDefault(String[] args) {
		String confValue = "nodefault";
		final String customLog4jConfFileName = "./log4j.properties";

		for (String arg : args) {
			if (arg.startsWith("--Ice.Config=")) {
				String iceConfigValue = arg.substring("--Ice.Config=".length());

				String splitted[] = iceConfigValue.split("\\.");

				confValue = splitted[0];
				break;
			}
		}

		System.setProperty("CONF", confValue);
		// URL url = this.getClass().getResource("/log4j.properties");
		// String path = url.toString();
		// path = path.substring(path.indexOf(":") + 1, path.length());

		File file = new File(customLog4jConfFileName);
		if (file.exists() && !file.isDirectory()) {
			PropertyConfigurator.configure("./log4j.properties");
		}

	}
}

package one.inve.localfullnode2.nodes;

import java.io.File;

import org.apache.log4j.PropertyConfigurator;

/**
 * 
 * 
 * Copyright © CHXX Co.,Ltd. All rights reserved.
 * 
 * @Description: startup class with a reference of {@link WithSeed}
 * @author: Francis.Deng
 * @date: May 31, 2019 3:11:53 AM
 * @version: V1.0
 */
public class Localfullnode2 {

	public static void main(String[] args) {
		Log4jSystem log4jSystem = new Log4jSystem();
		log4jSystem.overDefault(args);

		WithSeed withSeed = new WithSeed();
		withSeed.start(args);

	}

	// Francis 4/11/2019
	// divide the default log4j output by some command line arguments for
	// troubleshooting
	// ensure there is a CONF variable reference inside log4j.properties.
	public static class Log4jSystem {
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

}

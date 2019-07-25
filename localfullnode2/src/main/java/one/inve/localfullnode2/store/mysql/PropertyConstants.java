package one.inve.localfullnode2.store.mysql;

import java.io.IOException;
import java.util.Properties;

public class PropertyConstants {
	private static Properties properties;

	static{
		if (properties == null) {
			properties = new Properties();
			ClassLoader loader = Thread.currentThread().getContextClassLoader();
			try {
				properties.load(loader.getResourceAsStream("db.properties"));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	private static void setProperty() {
		if (properties == null) {
			properties = new Properties();
			ClassLoader loader = Thread.currentThread().getContextClassLoader();
			try {
				properties.load(loader.getResourceAsStream("db.properties"));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static String getPropertiesKey(String key) {
		if (properties == null) {
			setProperty();
		}
		return properties.getProperty(key, "default");
	}
}
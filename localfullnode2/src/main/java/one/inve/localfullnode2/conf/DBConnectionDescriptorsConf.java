package one.inve.localfullnode2.conf;

import java.util.HashMap;

/**
 * 
 * 
 * Copyright © INVE FOUNDATION. All rights reserved.
 * 
 * @Description: see intervalue.yaml.template
 * @author: Francis.Deng [francis_xiiiv@163.com]
 * @date: Sep 29, 2019 8:18:35 PM
 * @version: V1.0
 */
public class DBConnectionDescriptorsConf extends HashMap<String, String> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -722688840466971260L;

	public DBConnectionDescriptorsConf(int initialCapacity) {
		super(initialCapacity);
	}

	public void put(Object equation) {
		String parts[] = equation.toString().split("=");

		this.put(parts[0], parts[1]);
	}

	public String getDSURL(String dbId) {
		return this.get("spring.datasource.url" + dbId);
	}

	public String getDSUserName(String dbId) {
		return this.get("spring.datasource.username" + dbId);
	}

	public String getDSPassword(String dbId) {
		return this.get("spring.datasource.password" + dbId);
	}

}

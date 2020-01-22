package one.inve.localfullnode2.nodes;

import one.inve.localfullnode2.utilities.Log4jSystem;

/**
 * 
 * Copyright © INVE FOUNDATION. All rights reserved.
 * 
 * @ClassName: Chronicle
 * @Description: is used to dump/restore message
 * @author Francis.Deng [francis_xiiiv@163.com]
 * @date Jan 17, 2020
 *
 */
public class Chronicle {

	public static void main(String[] args) {
		Log4jSystem log4jSystem = new Log4jSystem();
		log4jSystem.overDefault(args);

		ChronicleWithSeed chronicle = new ChronicleWithSeed();
		chronicle.start(args);
	}
}

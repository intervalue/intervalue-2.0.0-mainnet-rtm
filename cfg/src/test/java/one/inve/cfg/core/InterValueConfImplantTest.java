package one.inve.cfg.core;

import org.junit.BeforeClass;
import org.junit.Test;

import one.inve.cfg.localfullnode.Config;

/**
 * 
 * 
 * Copyright © INVE FOUNDATION. All rights reserved.
 * 
 * @Description: load yaml configuration test case
 * @author: Francis.Deng [francis_xiiiv@163.com]
 * @date: Sep 12, 2019 2:35:28 AM
 * @version: V1.0
 */
public class InterValueConfImplantTest {
	private static InterValueConfImplant implant;

	@BeforeClass
	public static void init() {
		String[] params = {
				"intervalue_conf_file=/home/francis/workgroup/chxx/src/github.com/intervalue/intervalue-2.0.0-mainnet-rtm/localfullnode2/src/main/resources/intervalue.yaml.template" };
		implant = new InterValueConfImplant();
		implant.init(params);

		DBConnectionDescriptorsConf desConf = implant.conf.getLocalfullnode2Conf().getDbConnection();
		System.out.println(desConf);
	}

	@Test
	public void testImplantZerocConf() {
//		public static void main(String[] args) {
//		String[] params = { "intervalue_conf_file=./intervalue.yaml.template" };
//		InterValueYamlReader r = new InterValueYamlReader();
//		IInterValueConf conf = r.read(params);
//		System.out.println(conf);
//	}
//		String[] params = { "intervalue_conf_file=./intervalue.yaml.template" };
//		InterValueConfImplant implant = new InterValueConfImplant();
//		implant.init(params);
		String[] iceConfig = implant.implantZerocConf();

		System.out.println(iceConfig);
	}

	@Test
	public void testImplantStaticConfig() {
		System.out.println(Config.WHITE_LIST);
		implant.implantStaticConfig();
		System.out.println(Config.WHITE_LIST);
	}
}

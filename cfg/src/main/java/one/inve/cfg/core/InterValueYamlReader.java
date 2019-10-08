package one.inve.cfg.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.yaml.snakeyaml.Yaml;

/**
 * 
 * Copyright © INVE FOUNDATION. All rights reserved.
 * 
 * @Description: read the yaml configuration file
 * @author: Francis.Deng [francis_xiiiv@163.com]
 * @date: Sep 11, 2019 11:35:10 PM
 * @version: V1.0
 * @see intervalue.yaml.template
 */
public class InterValueYamlReader implements IInterValueConfigurationReader {

	@SuppressWarnings("rawtypes")
	@Override
	public IInterValueConf read(String[] args) {
		Yaml yaml = new Yaml();
		InputStream is = null;
		try {
			is = new FileInputStream(prioritize(args));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}

		Map m = yaml.loadAs(is, Map.class);
		// abandon the usage of bean classes
		return new IInterValueConf() {

			@Override
			public String getZerocContent() {
				return m.get("zeroc").toString();
			}

			@Override
			public ILocalfullnode2Conf getLocalfullnode2Conf() {
				return new ILocalfullnode2Conf() {

					@Override
					public String getPubIP() {
						Map m0 = (Map) m.get("localfullnode2");
						return m0.get("pubIP").toString();
					}

					@Override
					public String getGossipPort() {
						Map m0 = (Map) m.get("localfullnode2");
						return m0.get("gossipPort").toString();
					}

					@Override
					public String getRpcPort() {
						Map m0 = (Map) m.get("localfullnode2");
						return m0.get("rpcPort").toString();
					}

					@Override
					public String getHttpPort() {
						Map m0 = (Map) m.get("localfullnode2");
						return m0.get("httpPort").toString();
					}

					@SuppressWarnings("unchecked")
					@Override
					public List<String> getWhitelist() {
						Map m0 = (Map) m.get("localfullnode2");
						return (List<String>) m0.get("whitelist");
					}

					@Override
					public String getPrefix() {
						Map m0 = (Map) m.get("localfullnode2");
						return m0.get("prefix").toString();
					}

					@Override
					public String getSeedPubIP() {
						Map m0 = (Map) m.get("localfullnode2");
						return m0.get("seedPubIP").toString();
					}

					@Override
					public String getSeedGossipPort() {
						Map m0 = (Map) m.get("localfullnode2");
						return m0.get("seedGossipPort").toString();
					}

					@Override
					public String getSeedRpcPort() {
						Map m0 = (Map) m.get("localfullnode2");
						return m0.get("seedRpcPort").toString();
					}

					@Override
					public String getSeedHttpPort() {
						Map m0 = (Map) m.get("localfullnode2");
						return m0.get("seedHttpPort").toString();
					}

					@Override
					@SuppressWarnings("unchecked")
					public DBConnectionDescriptorsConf getDbConnection() {
						DBConnectionDescriptorsConf desConf = new DBConnectionDescriptorsConf(30);
						Map m0 = (Map) m.get("localfullnode2");
						List dbConnectionDescriptors = (List) m0.get("dbConnectionDescriptors");

						dbConnectionDescriptors.stream().forEach((t) -> {
							Map m = (Map) t;
							desConf.put(m.get("url"));
							desConf.put(m.get("un"));
							desConf.put(m.get("pw"));
						});

						return desConf;
					}

				};
			}

			@Override
			public IP2PClusterConf getP2PClusterConf() {
				return new IP2PClusterConf() {
					@Override
					public String getICPort() {
						Map m0 = (Map) m.get("p2pcluster");
						return m0.get("icport").toString();
					}

				};
			}

			@Override
			public Map<String, String> getEnv() {
				Map<String, String> result = new HashMap<>();
				Map m0 = (Map) m.get("env");
				Iterator iterator = m0.entrySet().iterator();
				while (iterator.hasNext()) {
					Map.Entry entry = (Map.Entry) iterator.next();
					result.put(entry.getKey().toString(), entry.getValue().toString());
				}
				return result;
			}

		};

	}

	private File parsePath(String path) {
		File f = null;
		Path p = Paths.get(path);

		if (Files.exists(p)) {
			p.normalize();
			f = p.toFile();
		}

		return f;
	}

	protected File prioritize(String[] args) {
		File finalConfigurationFile = null;

		if ((finalConfigurationFile = fromCommandLine(args)) == null) {
			if ((finalConfigurationFile = fromBootFolder()) == null) {
				if ((finalConfigurationFile = fromEnv()) == null) {
					throw new RuntimeException(
							"no configuration file has been seen in any of comand line,boot folder,env");
				}
			}
		}

		return finalConfigurationFile;
	}

	protected File fromCommandLine(String[] args) {
		File f = null;

		for (int i = 0; i < args.length; i++) {
			String p = args[i];

			if (p.startsWith(IInterValueConfigurationReader.INTERVALUE_CONF_FILE_VARIABLE_NAME)) {
				String[] pair = p.split("=");
				f = parsePath(pair[1]);
			}
		}

		return f;
	}

	protected File fromBootFolder() {
		return parsePath(IInterValueConfigurationReader.INTERVALUE_CONF_FILE_VARIABLE_NAME + ".yaml");
	}

	protected File fromEnv() {
		File f = null;
		String confFile = System.getenv(IInterValueConfigurationReader.INTERVALUE_CONF_FILE_VARIABLE_NAME);

		if (confFile != null) {
			f = parsePath(System.getenv(IInterValueConfigurationReader.INTERVALUE_CONF_FILE_VARIABLE_NAME));
		}
		return f;
	}

	private String cd() {
		String cd = null;
		File directory = new File(".");
		try {
			cd = directory.getCanonicalPath();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return cd;
	}

}

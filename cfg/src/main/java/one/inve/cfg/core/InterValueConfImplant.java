package one.inve.cfg.core;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONArray;

import one.inve.bean.node.GossipAddress;
import one.inve.cfg.fullnode.Parameters;
import one.inve.cfg.localfullnode.Config;
import one.inve.cfg.localfullnode.NodeParameters;

/**
 * 
 * 
 * Copyright © INVE FOUNDATION. All rights reserved.
 * 
 * @Description: call {@code init} at first and later call implant** method.
 * @author: Francis.Deng [francis_xiiiv@163.com]
 * @date: Sep 12, 2019 12:33:14 AM
 * @version: V1.0
 * @version: V1.1 automatically append my public address in white list
 * @version: V1.2 replace seed info
 */
public class InterValueConfImplant implements IConfImplant {
	private static final Logger logger = LoggerFactory.getLogger(InterValueConfImplant.class);

	IInterValueConf conf;

	@Override
	public void init(String[] args) {
		IInterValueConfigurationReader configurationReader = IInterValueConfigurationReader.getDefaultImpl();
		conf = configurationReader.read(args);

	}

	@Override
	public String[] implantZerocConf() {
		Random r = new Random();
//		int rand = r.nextInt(1000);
//		String zccFileName = "zcc" + rand;
		String zccFileName = "zcc";
		File cur = new File(".");
		String canonicalPath = null;
		try {
			File zerocConfigFile = File.createTempFile(zccFileName, null, cur);
			zerocConfigFile.deleteOnExit();
			canonicalPath = zerocConfigFile.getCanonicalPath();
			writeToFileNIOWay(zerocConfigFile, conf.getZerocContent());
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		return new String[] { "--Ice.Config=" + canonicalPath };
	}

	@Override
	public Parameters implantParameters(boolean isSeed) {
		Parameters p = new Parameters();

//		GossipAddress seedGA = new GossipAddress();
//		GossipAddress selfGA = new GossipAddress();

		p.seedGossipAddress = new GossipAddress();
		p.selfGossipAddress = new GossipAddress();

		p.seedGossipAddress.pubIP = one.inve.cfg.fullnode.Config.DEFAULT_SEED_PUBIP;
		p.seedGossipAddress.gossipPort = one.inve.cfg.fullnode.Config.DEFAULT_SEED_GOSSIP_PORT;
		p.seedGossipAddress.rpcPort = one.inve.cfg.fullnode.Config.DEFAULT_SEED_RPC_PORT;
		p.seedGossipAddress.httpPort = one.inve.cfg.fullnode.Config.DEFAULT_SEED_HTTP_PORT;

//		seedGA.pubIP = one.inve.cfg.fullnode.Config.DEFAULT_SEED_PUBIP;
//		seedGA.gossipPort = one.inve.cfg.fullnode.Config.DEFAULT_SEED_GOSSIP_PORT;
//		seedGA.rpcPort = one.inve.cfg.fullnode.Config.DEFAULT_SEED_RPC_PORT;
//		seedGA.httpPort = one.inve.cfg.fullnode.Config.DEFAULT_SEED_HTTP_PORT;
//		p.seedGossipAddress = seedGA;

		// p.selfGossipAddress.pubIP = one.inve.cfg.fullnode.Config.DEFAULT_SEED_PUBIP;

		if (isSeed) {
			p.selfGossipAddress.pubIP = conf.getSeedConf().getPubIP();
			p.selfGossipAddress.gossipPort = Integer.parseInt(conf.getSeedConf().getGossipPort());
			p.selfGossipAddress.rpcPort = Integer.parseInt(conf.getSeedConf().getRpcPort());
			p.selfGossipAddress.httpPort = Integer.parseInt(conf.getSeedConf().getHttpPort());
		} else {
			p.selfGossipAddress.pubIP = conf.getFullNodeConf().getPubIP();
			p.selfGossipAddress.gossipPort = Integer.parseInt(conf.getFullNodeConf().getGossipPort());
			p.selfGossipAddress.rpcPort = Integer.parseInt(conf.getFullNodeConf().getRpcPort());
			p.selfGossipAddress.httpPort = Integer.parseInt(conf.getFullNodeConf().getHttpPort());
		}

		if (!isSeed) {
			p.prefix = conf.getFullNodeConf().getPrefix();
		} else {
			p.prefix = "";
		}
		p.clearDb = false;
		// p.multiple = 1;

		p.staticSharding = true;

		p.dbFile = p.prefix + one.inve.cfg.fullnode.Config.DEFAULT_REAL_SQLITE_FILE;
		p.keysFile = p.prefix + one.inve.cfg.fullnode.Config.DEFAULT_KEYS_FILE;
		p.walletFile = p.prefix + one.inve.cfg.fullnode.Config.DEFAULT_WALLET_FILE;
		p.shardSize = one.inve.cfg.fullnode.Config.DEFAULT_SHARD_SIZE;
		p.shardNodeSize = one.inve.cfg.fullnode.Config.DEFAULT_SHARD_NODE_SIZE;
		p.neighborMaxSize = one.inve.cfg.fullnode.Config.DEFAULT_NEIGHBOR_SIZE;
		p.pbftNodeMinSize = one.inve.cfg.fullnode.Config.DEFAULT_PBFT_NODE_MIN_SIZE;
		p.gossipInterval = one.inve.cfg.fullnode.Config.DEFAULT_NODE_GOSSIP_INTERVAL;
		p.relayAliveTimeout = one.inve.cfg.fullnode.Config.DEFAULT_RELAY_ALIVE_TIME_OUT;
		p.nodeUpdateMinInterval = one.inve.cfg.fullnode.Config.DEFAULT_NODE_UPDATE_MIN_INTERVAL;
		p.ratioUpdateMinInterval = one.inve.cfg.fullnode.Config.DEFAULT_RATIO_UPDATE_MIN_INTERVAL;
		p.exchangeFeeRatio = one.inve.cfg.fullnode.Config.DEFAULT_EXCHANGE_FEE_RATIO;

		p.whiteList = one.inve.cfg.fullnode.Config.DEFAULT_WHITE_LIST;
		p.blackList = one.inve.cfg.fullnode.Config.DEFAULT_BLACK_LIST;

		logger.info("node ip        : {}", p.selfGossipAddress.pubIP);
		logger.info("node gossipPort: {}", p.selfGossipAddress.gossipPort);
		logger.info("node rpcPort   : {}", p.selfGossipAddress.rpcPort);
		logger.info("node httpPort  : {}", p.selfGossipAddress.httpPort);
		logger.info("test.clearDb   : {}", p.clearDb);
		logger.info("test.env       : {}", p.env);
		logger.info("test.prefix    : {}", p.prefix);
		logger.info("test.whiteList : {}", JSONArray.toJSONString(p.whiteList));

		return p;
	}

	@Override
	public NodeParameters implantNodeParameters() {
		NodeParameters np = new NodeParameters();

		np.seedGossipAddress = new GossipAddress();
		np.selfGossipAddress = new GossipAddress();

		np.seedGossipAddress.pubIP = Config.DEFAULT_SEED_PUBIP;
		np.seedGossipAddress.gossipPort = Integer.parseInt(Config.DEFAULT_SEED_GOSSIP_PORT);
		np.seedGossipAddress.rpcPort = Integer.parseInt(Config.DEFAULT_SEED_RPC_PORT);
		np.seedGossipAddress.httpPort = Integer.parseInt(Config.DEFAULT_SEED_HTTP_PORT);

		np.selfGossipAddress.pubIP = conf.getLocalfullnode2Conf().getPubIP();
		np.selfGossipAddress.gossipPort = Integer.parseInt(conf.getLocalfullnode2Conf().getGossipPort());
		np.selfGossipAddress.rpcPort = Integer.parseInt(conf.getLocalfullnode2Conf().getRpcPort());
		np.selfGossipAddress.httpPort = Integer.parseInt(conf.getLocalfullnode2Conf().getHttpPort());

		np.clearDb = 0;
		np.multiple = 1;
		np.prefix = conf.getLocalfullnode2Conf().getPrefix();

		return np;
	}

	@Override
	public void implantStaticConfig() {
		try {
			String myPublicAddr = null;
			ILocalfullnode2Conf localfullnode2Conf = conf.getLocalfullnode2Conf();
			String seedPubIP, seedGossipPort, seedRpcPort, seedHttpPort;

			List<String> whiteList = localfullnode2Conf.getWhitelist();
			if ((myPublicAddr = getPublicAddr()) != null) {
				whiteList.add(myPublicAddr);
			}

			seedPubIP = localfullnode2Conf.getSeedPubIP();
			seedGossipPort = localfullnode2Conf.getSeedGossipPort();
			seedRpcPort = localfullnode2Conf.getSeedRpcPort();
			seedHttpPort = localfullnode2Conf.getSeedHttpPort();

			ReflectionUtils.setStaticField(Config.class, "WHITE_LIST", whiteList);
			// setStaticField(Config.class, "ENABLE_SNAPSHOT", false);// disable snapshot or
			// not
			ReflectionUtils.setStaticField(Config.class, "DEFAULT_SEED_PUBIP", seedPubIP);
			ReflectionUtils.setStaticField(Config.class, "DEFAULT_SEED_GOSSIP_PORT", seedGossipPort);
			ReflectionUtils.setStaticField(Config.class, "DEFAULT_SEED_RPC_PORT", seedRpcPort);
			ReflectionUtils.setStaticField(Config.class, "DEFAULT_SEED_HTTP_PORT", seedHttpPort);

		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}

	}

	// inject seed information into one.inve.cfg.fullnode.Config object
	@Override
	public void implantStaticConfig(boolean isSeed) {// implant seed or fullnode configuration
		try {
			String myPublicAddr = null;

//			if (isSeed) {
//				IFullnodeConf sFullnodeConf = conf.getSeedConf();
//				String sPubIP, sGossipPort, sRpcPort, sHttpPort;
//
//				List<String> sWhiteList = sFullnodeConf.getWhitelist();
//				if ((myPublicAddr = getPublicAddr()) != null) {
//					sWhiteList.add(myPublicAddr);
//				}
//
//				sPubIP = sFullnodeConf.getPubIP();
//				sGossipPort = sFullnodeConf.getGossipPort();
//				sRpcPort = sFullnodeConf.getRpcPort();
//				sHttpPort = sFullnodeConf.getHttpPort();
//
//				ReflectionUtils.setStaticField(one.inve.cfg.fullnode.Config.class, "WHITE_LIST", sWhiteList);
//				ReflectionUtils.setStaticField(one.inve.cfg.fullnode.Config.class, "DEFAULT_SEED_PUBIP", sPubIP);
//				ReflectionUtils.setStaticField(one.inve.cfg.fullnode.Config.class, "DEFAULT_SEED_GOSSIP_PORT",
//						sGossipPort);
//				ReflectionUtils.setStaticField(one.inve.cfg.fullnode.Config.class, "DEFAULT_SEED_RPC_PORT", sRpcPort);
//				ReflectionUtils.setStaticField(one.inve.cfg.fullnode.Config.class, "DEFAULT_SEED_HTTP_PORT", sHttpPort);
//			} else {
//				IFullnodeConf fullnodeConf = conf.getFullNodeConf();
//				String pubIP, gossipPort, rpcPort, httpPort;
//
//				List<String> whiteList = fullnodeConf.getWhitelist();
//				if ((myPublicAddr = getPublicAddr()) != null) {
//					whiteList.add(myPublicAddr);
//				}
//
//				pubIP = fullnodeConf.getPubIP();
//				gossipPort = fullnodeConf.getGossipPort();
//				rpcPort = fullnodeConf.getRpcPort();
//				httpPort = fullnodeConf.getHttpPort();
//
//				ReflectionUtils.setStaticField(one.inve.cfg.fullnode.Config.class, "WHITE_LIST", whiteList);
//				ReflectionUtils.setStaticField(one.inve.cfg.fullnode.Config.class, "DEFAULT_SEED_PUBIP", pubIP);
//				ReflectionUtils.setStaticField(one.inve.cfg.fullnode.Config.class, "DEFAULT_SEED_GOSSIP_PORT",
//						gossipPort);
//				ReflectionUtils.setStaticField(one.inve.cfg.fullnode.Config.class, "DEFAULT_SEED_RPC_PORT", rpcPort);
//				ReflectionUtils.setStaticField(one.inve.cfg.fullnode.Config.class, "DEFAULT_SEED_HTTP_PORT", httpPort);
//			}
			IFullnodeConf sFullnodeConf = conf.getSeedConf();
			String sPubIP, sGossipPort, sRpcPort, sHttpPort;

			List<String> sWhiteList = sFullnodeConf.getWhitelist();
			if ((myPublicAddr = getPublicAddr()) != null) {
				sWhiteList.add(myPublicAddr);
			}

			sPubIP = sFullnodeConf.getPubIP();
			sGossipPort = sFullnodeConf.getGossipPort();
			sRpcPort = sFullnodeConf.getRpcPort();
			sHttpPort = sFullnodeConf.getHttpPort();

			ReflectionUtils.setStaticField(one.inve.cfg.fullnode.Config.class, "DEFAULT_WHITE_LIST", sWhiteList);
			ReflectionUtils.setStaticField(one.inve.cfg.fullnode.Config.class, "DEFAULT_SEED_PUBIP", sPubIP);
			ReflectionUtils.setStaticField(one.inve.cfg.fullnode.Config.class, "DEFAULT_SEED_GOSSIP_PORT",
					Integer.parseInt(sGossipPort));
			ReflectionUtils.setStaticField(one.inve.cfg.fullnode.Config.class, "DEFAULT_SEED_RPC_PORT",
					Integer.parseInt(sRpcPort));
			ReflectionUtils.setStaticField(one.inve.cfg.fullnode.Config.class, "DEFAULT_SEED_HTTP_PORT",
					Integer.parseInt(sHttpPort));

			if (!isSeed) {
				IFullnodeConf fullnodeConf = conf.getFullNodeConf();

				List<String> whiteList = fullnodeConf.getWhitelist();
				if ((myPublicAddr = getPublicAddr()) != null) {
					whiteList.add(myPublicAddr);
				}

				// ReflectionUtils.setStaticField(one.inve.cfg.fullnode.Config.class,
				// "WHITE_LIST", whiteList);
				ReflectionUtils.setStaticField(one.inve.cfg.fullnode.Config.class, "DEFAULT_WHITE_LIST", whiteList);
			}

		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}

	}

	// get public ip address from https://api.ipify.org or http://seedip:30911
	@SuppressWarnings("resource")
	protected String getPublicAddr() {
		try (java.util.Scanner s = new java.util.Scanner(new java.net.URL("https://api.ipify.org").openStream(),
				"UTF-8").useDelimiter("\\A")) {
			// System.out.println("My current IP address is " + s.next());
			return s.next();
		} catch (java.io.IOException e) {
			e.printStackTrace();
		}

		// start ips in the command line,which listening to 30911
		try (java.util.Scanner s = new java.util.Scanner(
				new java.net.URL(String.format("http://%s:30911", Config.DEFAULT_SEED_PUBIP)).openStream(), "UTF-8")
						.useDelimiter("\\A")) {
			// System.out.println("My current IP address is " + s.next());
			return s.next();
		} catch (java.io.IOException e) {
			e.printStackTrace();
		}

		return null;
	}

	// register system properties from configuration
	@Override
	public void implantEnv() {
		Map<String, String> kvPair = conf.getEnv();
		for (Map.Entry<String, String> entry : kvPair.entrySet()) {
			System.setProperty(entry.getKey(), entry.getValue());
		}
	}

	// Changing static final fields via reflection
//	private void setStaticField(Class clazz, String fieldName, Object value)
//			throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
//		Field field = clazz.getDeclaredField(fieldName);
//
//		Field modifiersField = Field.class.getDeclaredField("modifiers");
//		boolean isModifierAccessible = modifiersField.isAccessible();
//		modifiersField.setAccessible(true);
//		modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
//
//		boolean isAccessible = field.isAccessible();
//		field.setAccessible(true);
//
//		field.set(null, value);
//
//		field.setAccessible(isAccessible);
//		modifiersField.setAccessible(isModifierAccessible); // Might not be very useful resetting the value, really. The
//															// harm is already done.
//	}

	private void writeToFileNIOWay(File file, String messageToWrite) throws IOException {
		FileOutputStream fileOutputStream = new FileOutputStream(file, true);
		FileChannel fileChannel = fileOutputStream.getChannel();
		ByteBuffer byteBuffer = null;

		byteBuffer = ByteBuffer.wrap(messageToWrite.getBytes(Charset.forName("UTF-8")));
		fileChannel.write(byteBuffer);

		fileChannel.close();
		fileOutputStream.close();

	}

	@Override
	public DBConnectionDescriptorsConf getDbConnection() {
		return conf.getLocalfullnode2Conf().getDbConnection();
	}

}

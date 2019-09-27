package one.inve.localfullnode2.conf;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.Random;

import one.inve.bean.node.GossipAddress;
import one.inve.localfullnode2.utilities.ReflectionUtils;

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
 */
public class InterValueConfImplant implements IConfImplant {
	private IInterValueConf conf;

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
	public NodeParameters implantNodeParameters() {
		NodeParameters np = new NodeParameters();

		np.seedGossipAddress = new GossipAddress();
		np.selfGossipAddress = new GossipAddress();

		np.seedGossipAddress.pubIP = Config.DEFAULT_SEED_PUBIP;
		np.seedGossipAddress.gossipPort = Integer.parseInt(Config.DEFAULT_SEED_GOSSIP_PORT);
		np.seedGossipAddress.rpcPort = Integer.parseInt(Config.DEFAULT_SEED_RPC_PORT);
		np.seedGossipAddress.httpPort = Integer.parseInt(Config.DEFAULT_SEED_HTTP_PORT);

		np.selfGossipAddress.pubIP = Config.DEFAULT_SEED_PUBIP;
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

			List<String> whiteList = conf.getLocalfullnode2Conf().getWhitelist();
			if ((myPublicAddr = getPublicAddr()) != null) {
				whiteList.add(myPublicAddr);
			}
			ReflectionUtils.setStaticField(Config.class, "WHITE_LIST", whiteList);
			// setStaticField(Config.class, "ENABLE_SNAPSHOT", false);// disable snapshot or
			// not

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

}

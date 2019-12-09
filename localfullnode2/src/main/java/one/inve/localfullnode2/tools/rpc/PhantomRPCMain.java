package one.inve.localfullnode2.tools.rpc;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.zeroc.Ice.Communicator;
import com.zeroc.Ice.Util;

import one.inve.localfullnode2.rpc.GossipObj;
import one.inve.localfullnode2.rpc.Local2localPrx;
import one.inve.localfullnode2.rpc.mgmt.PhantomRPCResponderRPCInvocationDriver;

/**
 * 
 * Copyright © INVE FOUNDATION. All rights reserved.
 * 
 * @ClassName: PhantomRPCMain
 * @formatter:off
 * @Description: "java -cp localfullnode2-2.0.0.jar one.inve.localfullnode2.tools.rpc.PhantomRPCMain -p 35530"
 * 
 *               "java -cp localfullnode2-2.0.0.jar one.inve.localfullnode2.tools.rpc.PhantomRPCMain -a 192.168.207.130 -p 35530"
 * @author Francis.Deng [francis_xiiiv@163.com]
 * @formatter:on
 * @see PhantomRPCResponderRPCInvocationDriver
 * @date Dec 5, 2019
 *
 */
public class PhantomRPCMain {
	private static final Logger logger = LoggerFactory.getLogger(PhantomRPCMain.class);

	public static void main(String[] args) {
		options(args);
	}

	public static void options(String[] args) {
		Options options = new Options();
		Option opt = new Option("p", "port", true, "the port listening");
		opt.setRequired(true);
		options.addOption(opt);

		opt = new Option("a", "address", true, "the address connecting to,which imply the client side");
		opt.setRequired(false);
		options.addOption(opt);

		opt = new Option("d", "delay", true, "specifies the delay(seconds) between screen updates");
		opt.setRequired(false);
		options.addOption(opt);

		opt = new Option("h", "help", false, "PhantomRPC short help");
		opt.setRequired(false);
		options.addOption(opt);

		HelpFormatter hf = new HelpFormatter();
		hf.setWidth(110);
		CommandLine commandLine = null;
		CommandLineParser parser = new DefaultParser();
		try {
			commandLine = parser.parse(options, args);

//			HelpFormatter formatter = new HelpFormatter();
//			formatter.printHelp("ant", options);

			String sPort = commandLine.getOptionValue('p');
			int port = Integer.parseInt(sPort);

			if (commandLine.hasOption('a')) {
				// client side

				String address = commandLine.getOptionValue('a');
				String sDelay = commandLine.getOptionValue('d') == null ? "30" : commandLine.getOptionValue('d');

				executeLoop(address, sPort, sDelay);

			} else {
				// server side
				Communicator communicator = Util.initialize();

				PhantomRPCResponderRPCInvocationDriver driver = new PhantomRPCResponderRPCInvocationDriver(communicator,
						port);

				if (driver.registerServices()) {
					driver.activateServices();

					logger.info("activate PhantomRPC service in {}", port);

					driver.waitForShutdown();
				}
			}
		} catch (ParseException e) {
			hf.printHelp("testApp", options, true);
		}
	}

	// attempt to simulate invocation in a loop
	protected static void executeLoop(String address, String sPort, String sDelay) {
		Communicator communicator = Util.initialize();

		while (true) {
			try {

				PhantomRPCResponderRPCInvocationDriver driver = new PhantomRPCResponderRPCInvocationDriver(
						communicator);

				Local2localPrx local2localPrx = driver.getRemoteLocal2localPrx(address, sPort);
				CompletableFuture<GossipObj> f = local2localPrx
						.gossipMyMaxSeqList4ConsensusAsync(UUID.randomUUID().toString(), null, null, null, new long[0]);

				GossipObj gossipObj = (GossipObj) f.get(30000, TimeUnit.MILLISECONDS);
				logger.info(JSON.toJSONString(gossipObj));

				TimeUnit.SECONDS.sleep(Integer.parseInt(sDelay));
			} catch (Exception e) {
				logger.error("inside a loop - {}", e.toString());
			}

		}
	}

}

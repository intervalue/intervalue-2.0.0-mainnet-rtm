package one.inve.localfullnode2.rpc.mgmt;

import java.lang.reflect.Constructor;
import java.util.Iterator;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zeroc.Ice.Communicator;

import one.inve.cfg.core.ReflectionUtils;
import one.inve.cfg.localfullnode.Config;
import one.inve.cluster.Member;
import one.inve.localfullnode2.nodes.LocalFullNode1GeneralNode;
import one.inve.localfullnode2.rpc.Local2localPrx;

/**
 * 
 * Copyright © INVE FOUNDATION. All rights reserved.
 * 
 * @ClassName: LocalFullNode2RPCInvocationDriver
 * @Description: The class is designed to be used in product
 *               environment,Zeroc-ice technology is behind the hood.
 * @author Francis.Deng [francis_xiiiv@163.com]
 * @version 1.1 support service class to have the constructor parameter
 *          "LocalFullNode1GeneralNode" passed by or not
 * @date Dec 4, 2019
 *
 */
public class LocalFullNode2RPCInvocationDriver extends IceRemoteProcedureCallDriverTemplate {
	private static final Logger logger = LoggerFactory.getLogger(LocalFullNode2RPCInvocationDriver.class);

	private LocalFullNode1GeneralNode node;

	public LocalFullNode2RPCInvocationDriver(Communicator iceCommunicator, int port, LocalFullNode1GeneralNode node) {
		super(iceCommunicator, "LocalFullNodeAdapter", port);
		this.node = node;
	}

	public LocalFullNode2RPCInvocationDriver(Communicator iceCommunicator) {
		super(iceCommunicator);
	}

	@Override
	public boolean registerServices() {
		try {
			// load service configuration specified by "RPC_SERVICES_MAPPING_LIST" property
			// support service class having "LocalFullNode1GeneralNode" passed by or not
			Iterator<Entry<String, String>> iter = Config.RPC_SERVICES_MAPPING_LIST.entrySet().iterator();
			while (iter.hasNext()) {
				Entry<String, String> entry = iter.next();
				String classID = entry.getKey();
				String className = entry.getValue();

				Class<?> t = Class.forName(className);

				if (ReflectionUtils.findConstructor(t, LocalFullNode1GeneralNode.class) != null) {
					Constructor<com.zeroc.Ice.Object> cons = (Constructor<com.zeroc.Ice.Object>) t
							.getConstructor(LocalFullNode1GeneralNode.class);
					com.zeroc.Ice.Object object = cons.newInstance(node);

					addServiceObject(object, classID);
				} else if (ReflectionUtils.findConstructor(t, new Class[0]) != null) {
					Constructor<com.zeroc.Ice.Object> cons = (Constructor<com.zeroc.Ice.Object>) t.getConstructor();
					com.zeroc.Ice.Object object = cons.newInstance();

					addServiceObject(object, classID);
				}

			}
//			for (int i = 0; i < Config.SERVICE_ARRAY.length; i++) {
//				Class<?> t = Class.forName(Config.SERVICE_ARRAY[i]);
//				Constructor<Object> cons = (Constructor<Object>) t.getConstructor(LocalFullNode1GeneralNode.class);
//				Object object = cons.newInstance(node);
//
//				String identity = Config.SERVICE_ARRAY[i].substring(Config.SERVICE_ARRAY[i].lastIndexOf('.') + 1);
//				if (identity.toLowerCase().endsWith("impl")) {
//					identity = identity.substring(0, identity.length() - 4);
//				}
//				adapter.add(object, Util.stringToIdentity(identity));
//			}

		} catch (Exception e) {
			logger.error("error while registering rpc services: {}", e);
			return false;
		}

		return true;
	}

	public Local2localPrx getRemoteLocal2localPrx(Member member) {

		String str2Proxy = String.format("Local2local:default -h %s -p %s", member.address().host(),
				member.metadata().get("rpcPort"));
		Local2localPrx local2localPrx = null;
		try {
			logger.info("attempt to build a rpc connection with info [{}]", str2Proxy);
			local2localPrx = Local2localPrx.checkedCast(stringToProxy(str2Proxy));

		} catch (Exception e) {
			logger.error("getRemoteLocal2localPrx(): LocalFullNode rpc connection to [{} - {}] is not reached.",
					member.address(), member.metadata());
		}
		return local2localPrx;
	}

}

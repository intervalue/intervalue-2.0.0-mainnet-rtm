package one.inve.localfullnode2.rpc.mgmt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zeroc.Ice.Communicator;

import one.inve.localfullnode2.rpc.Local2localPrx;
import one.inve.localfullnode2.rpc.impl.PhantomRPCResponder;

/**
 * 
 * Copyright © INVE FOUNDATION. All rights reserved.
 * 
 * @ClassName: PhantomRPCResponderRPCInvocationDriver
 * @Description: provide an approach to register and invoke
 *               "PhantomRPCResponder" service which is used to auto-check
 * @author Francis.Deng [francis_xiiiv@163.com]
 * @see PhantomRPCResponder
 * @date Dec 5, 2019
 *
 */
public class PhantomRPCResponderRPCInvocationDriver extends IceRemoteProcedureCallDriverTemplate {
	private static final Logger logger = LoggerFactory.getLogger(PhantomRPCResponderRPCInvocationDriver.class);
	private String classID = "PhantomRPCResponder";

	// launch service
	public PhantomRPCResponderRPCInvocationDriver(Communicator iceCommunicator, int port) {
		super(iceCommunicator, "AutoDectionSys", port);
	}

	// serve client invocation
	public PhantomRPCResponderRPCInvocationDriver(Communicator iceCommunicator) {
		super(iceCommunicator);
	}

	@Override
	public boolean registerServices() {
		try {
			PhantomRPCResponder phantomRPCResponderInst = new PhantomRPCResponder();

			addServiceObject(phantomRPCResponderInst, classID);

		} catch (Exception e) {
			logger.error("error while registering rpc services: {}", e);
			return false;
		}

		return true;
	}

	public Local2localPrx getRemoteLocal2localPrx(String sHost, String sPort) {

		String str2Proxy = String.format("%s:default -h %s -p %s", classID, sHost, sPort);
		Local2localPrx local2localPrx = null;
		try {
			local2localPrx = Local2localPrx.checkedCast(stringToProxy(str2Proxy));

			logger.info("build a rpc proxy,connection info is [{}]", str2Proxy);
		} catch (Exception e) {
			logger.error("getRemoteLocal2localPrx(): LocalFullNode rpc connection to {}:{} is not reached out.", sHost,
					sPort);
		}
		return local2localPrx;
	}

	public void setClassID(String classID) {
		this.classID = classID;
	}

}

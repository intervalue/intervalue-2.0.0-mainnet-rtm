package one.inve.localfullnode2.rpc.mgmt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zeroc.Ice.Communicator;
import com.zeroc.Ice.ObjectAdapter;
import com.zeroc.Ice.ObjectPrx;
import com.zeroc.Ice.Util;

/**
 * 
 * Copyright © INVE FOUNDATION. All rights reserved.
 * 
 * @ClassName: IceRemoteProcedureCallDriverTemplate
 * @Description: a template about RPC driver using zeroc-ice
 * @author Francis.Deng [francis_xiiiv@163.com]
 * @date Dec 4, 2019
 *
 */
public abstract class IceRemoteProcedureCallDriverTemplate {
	private static final Logger logger = LoggerFactory.getLogger(IceRemoteProcedureCallDriverTemplate.class);

	private int port;
	private ObjectAdapter adapter;

	protected IceRemoteProcedureCallDriverTemplate(Communicator iceCommunicator, String adapterName, int port) {
		this.port = port;
		this.adapter = genAdapter(iceCommunicator, adapterName, port);
	}

	protected ObjectAdapter genAdapter(Communicator communicator, String adapterName, int port) {
		ObjectAdapter adapter = null;
		try {
			adapter = communicator.createObjectAdapterWithEndpoints(adapterName, "default -p " + port);
		} catch (Exception e) {
			logger.error("error while binding rpc port {},{}", port, e);
		}

		return adapter;
	}

	protected void addServiceObject(com.zeroc.Ice.Object serviceObj, String serviceObjID) {
		adapter.add(serviceObj, Util.stringToIdentity(serviceObjID));
	}

	public abstract boolean registerServices();

	public boolean activateServices() {
		adapter.activate();

		return true;
	}

	public void waitForShutdown() {
		getCommunicator().waitForShutdown();
	}

	public Communicator getCommunicator() {
		return adapter.getCommunicator();
	}

	// the following is running as client-side program

	private Communicator iceCommunicatorInClient;

	public ObjectPrx stringToProxy(String str) {
		return iceCommunicatorInClient.stringToProxy(str);
	}

	protected IceRemoteProcedureCallDriverTemplate(Communicator iceCommunicator) {
		this.iceCommunicatorInClient = iceCommunicator;
	}

}

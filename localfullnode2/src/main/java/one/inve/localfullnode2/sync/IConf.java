package one.inve.localfullnode2.sync;

import com.zeroc.Ice.Communicator;

public interface IConf {
	IContext getDefaultContext();

	Communicator getCommunicator();
}

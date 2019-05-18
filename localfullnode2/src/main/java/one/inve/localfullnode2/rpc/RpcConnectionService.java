package one.inve.localfullnode2.rpc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zeroc.Ice.Communicator;

import one.inve.cluster.Member;

public class RpcConnectionService {
	private static final Logger logger = LoggerFactory.getLogger(RpcConnectionService.class);

	/**
	 * 与局部全节点建立连接
	 *
	 * @param member 局部全节点
	 * @return rpc连接
	 */
	public static Local2localPrx buildConnection2localFullNode(Communicator communicator, Member member) {
		String str2Proxy = String.format("Local2local:default -h %s -p %s", member.address().host(),
				member.metadata().get("rpcPort"));
		Local2localPrx local2localPrx = null;
		try {
			local2localPrx = Local2localPrx.checkedCast(communicator.stringToProxy(str2Proxy));

			logger.info("build a rpc proxy,connection info is [{}]", str2Proxy);
		} catch (Exception e) {
			logger.error("buildConnection2localFullNode(): local full node {} is not connected.", member.address());
		}
		return local2localPrx;
	}

	/**
	 * 与局部全节点建立连接
	 *
	 * @param ip   局部全节点IP
	 * @param port 局部全节点rpc端口
	 * @return rpc连接
	 */
	public static Local2localPrx buildConnection2localFullNode(Communicator communicator, String ip, int port) {
		logger.info("buildConnection2localFullNode...");
		Local2localPrx prx = null;
		try {
			prx = Local2localPrx
					.checkedCast(communicator.stringToProxy("Local2local:default -h " + ip + " -p " + port));
		} catch (Exception e) {
			logger.error("", e);
		}
		if (null == prx) {
			logger.error(">>>>>> Can not build connnection to local full node {}:{}", ip, port);
			throw new Error("Can not build connnection to local full node.");
		}
		return prx;
	}

	/**
	 * 与全节点建立连接
	 *
	 * @param seedPubIP   seed公网IP
	 * @param seedRpcPort seed rpc端口
	 * @return lianjie
	 */
	public static RegisterPrx buildConnection2Seed(Communicator communicator, String seedPubIP, String seedRpcPort) {
		RegisterPrx registerPrx;
		try {
			String connInfo = "Register:default -h " + seedPubIP + " -p " + seedRpcPort;
			registerPrx = RegisterPrx.checkedCast(communicator.stringToProxy(connInfo));
			int i = 0;
		} catch (Exception e) {
			logger.info(">>>>>> Full node is not connected, please wait...");
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
			return buildConnection2Seed(communicator, seedPubIP, seedRpcPort);
		}
		if (null == registerPrx) {
			logger.error(">>>>>> Invalid Full node Connnection Object.");
			throw new Error("Invalid Full node Connnection Object.");
		}
		return registerPrx;
	}
}

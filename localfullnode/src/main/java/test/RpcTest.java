package test;

import one.inve.rpc.localfullnode.Light2localPrx;

public class RpcTest {
    public static void main(String[] args) {
        try(com.zeroc.Ice.Communicator communicator = com.zeroc.Ice.Util.initialize(args)) {
            String connInfo = "Light2local:default -h 34.220.63.1 -p 20504";
            Light2localPrx light2localPrx = null;
            try {
                light2localPrx = Light2localPrx.checkedCast(communicator.stringToProxy(connInfo));
                if (null == light2localPrx) {
                    throw new Error("Invalid Object");
                }
            } catch (Exception e) {
                System.out.println("connection failure!");
            }

            communicator.waitForShutdown();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

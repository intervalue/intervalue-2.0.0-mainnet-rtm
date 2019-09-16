package one.inve.localfullnode2.sync;

public class Addr {
	private String ip;
	private int port;

	public Addr(String ipAndPort) {
		String pair[] = ipAndPort.split(":");
		if (pair.length == 1) {
			pair = ipAndPort.split(" ");
		}

		ip = pair[0];
		port = Integer.parseInt(pair[1]);
	}

	public String getIp() {
		return ip;
	}

	public int getPort() {
		return port;
	}

}

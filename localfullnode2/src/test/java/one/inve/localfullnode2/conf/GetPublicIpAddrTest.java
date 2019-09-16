package one.inve.localfullnode2.conf;

import org.junit.Test;

/**
 * 
 * 
 * Copyright © INVE FOUNDATION. All rights reserved.
 * 
 * @Description: get public ip address from https://api.ipify.org or
 *               http://seedip:30911
 * @author: Francis.Deng [francis_xiiiv@163.com]
 * @date: Sep 15, 2019 8:24:28 PM
 * @version: V1.0
 */
public class GetPublicIpAddrTest {
	@Test
	public void fromIPS() {
		try (java.util.Scanner s = new java.util.Scanner(
				new java.net.URL(String.format("http://%s:30911", Config.DEFAULT_SEED_PUBIP)).openStream(), "UTF-8")
						.useDelimiter("\\A")) {
			System.out.println(String.format("My current IP address[ips] is '%s'", s.next()));
		} catch (java.io.IOException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void fromIPFY() {
		try (java.util.Scanner s = new java.util.Scanner(new java.net.URL("https://api.ipify.org").openStream(),
				"UTF-8").useDelimiter("\\A")) {
			System.out.println(String.format("My current IP address[IPIFY] is '%s'", s.next()));
		} catch (java.io.IOException e) {
			e.printStackTrace();
		}
	}
}

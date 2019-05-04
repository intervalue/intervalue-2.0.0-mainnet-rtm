package one.inve.localfullnode2.hashnet;

/**
 * 
 * Copyright © CHXX Co.,Ltd. All rights reserved.
 * 
 * @Description: push available events to hashnet model by sharding
 *               <p>
 *               <code>one.inve.threads.localfullnode.EventBody2HashnetThread</code>
 * @author: Francis.Deng
 * @date: May 3, 2019 7:15:51 PM
 * @version: V1.0
 */
public class HN {
	public void pull(HNDependent dep) {
		for (int i = 0; i < dep.getShardCount(); i++) {
			dep.addToHashnet(i);
		}
	}
}

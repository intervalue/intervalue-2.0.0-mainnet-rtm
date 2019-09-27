package one.inve.localfullnode2.hashnet;

/**
 * 
 * Copyright © INVE FOUNDATION. All rights reserved.
 * 
 * @Description: push available events to hashnet model by sharding
 * @author: Francis.Deng
 * @see EventBody2HashnetThread
 * @date: May 3, 2019 7:15:51 PM
 * @version: V1.0
 */
public class HashneterUpstream {
	public void pull(HashneterUpstreamDependent dep) {
		for (int i = 0; i < dep.getShardCount(); i++) {
			dep.addToHashnet(i);
		}
	}
}

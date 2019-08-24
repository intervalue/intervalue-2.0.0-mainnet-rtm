package one.inve.localfullnode2.sync;

/**
 * 
 * Copyright © INVE FOUNDATION. All rights reserved.
 * 
 * @ClassName: DistributedO
 * @Description: important returned value to represent object distribution and
 *               object.
 * @author Francis.Deng
 * @mailbox francis_xiiiv@163.com
 * @date Aug 23, 2019
 * 
 */
public class DistributedO<O> {
	private Distribution dist;
	private O[] objects;

	public DistributedO(Distribution dist, O[] objects) {
		super();
		this.dist = dist;
		this.objects = objects;
	}

	public Distribution getDist() {
		return dist;
	}

	public O[] getObjects() {
		return objects;
	}

}

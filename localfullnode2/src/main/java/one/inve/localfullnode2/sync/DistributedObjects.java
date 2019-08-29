package one.inve.localfullnode2.sync;

import one.inve.localfullnode2.sync.measure.Distribution;

/**
 * 
 * Copyright © INVE FOUNDATION. All rights reserved.
 * 
 * @ClassName: DistributedObjects
 * @Description: important returned value to represent object distribution and
 *               object.
 * @author Francis.Deng
 * @mailbox francis_xiiiv@163.com
 * @date Aug 23, 2019
 * 
 */
public class DistributedObjects<O> {
	private Distribution dist;
	private O[] objects;
//	private byte[] rootHash;

	public DistributedObjects(Distribution dist, O[] objects) {
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

//	public byte[] getRootHash() {
//		return rootHash;
//	}
//
//	public void setRootHash(byte[] rootHash) {
//		this.rootHash = rootHash;
//	}

}

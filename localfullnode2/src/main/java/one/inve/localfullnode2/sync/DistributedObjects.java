package one.inve.localfullnode2.sync;

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
public class DistributedObjects<D, O> {
	private D dist;
	private O[] objects;
//	private byte[] rootHash;

	public DistributedObjects(D dist, O[] objects) {
		super();
		this.dist = dist;
		this.objects = objects;
	}

	public D getDist() {
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

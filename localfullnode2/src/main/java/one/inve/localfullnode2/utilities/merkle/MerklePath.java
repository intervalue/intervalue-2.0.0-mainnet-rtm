package one.inve.localfullnode2.utilities.merkle;

import one.inve.localfullnode2.utilities.GenericArray;

/**
 * 
 * Copyright © INVE FOUNDATION. All rights reserved.
 * 
 * @ClassName: MerklePath
 * @Description: is used to verify the data chunk
 * @author Francis.Deng
 * @date Aug 20, 2019
 * @see MerkleTree
 *
 */
public class MerklePath {
	private GenericArray<byte[]> path;
	private GenericArray<String> index;// "r" or "l"

	public MerklePath(GenericArray<byte[]> path, GenericArray<String> index) {
		super();
		this.path = path;
		this.index = index;
	}

	public byte[][] path() {
		byte[][] result = new byte[path.length()][];
		int i = 0;

		for (byte[] bytes : path) {
			result[i] = bytes;
			i++;
		}

		return result;
	}

	public String[] index() {
		String[] result = new String[index.length()];
		int i = 0;

		for (String idx : index) {
			result[i] = idx;
			i++;
		}

		return result;

	}

}

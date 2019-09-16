package one.inve.localfullnode2.utilities.merkle;

import java.util.Arrays;

import one.inve.localfullnode2.utilities.ByteUtil;
import one.inve.localfullnode2.utilities.GenericArray;
import one.inve.localfullnode2.utilities.Hash;

/**
 * 
 * Copyright © INVE FOUNDATION. All rights reserved.
 * 
 * @ClassName: MerklePath
 * @Description: is used to verify the data chunk
 * @author Francis.Deng
 * @mailbox francis_xiiiv@163.com
 * @date Aug 20, 2019
 * @see MerkleTree
 * 
 * @version 1.1 - provide the method {@code validate} to compare calculated root
 *          hash with famed root hash.
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

	public MerklePath(byte[][] path, String[] index) {
		super();
		this.path = new GenericArray<byte[]>();
		this.path.append(path);
		this.index = new GenericArray<String>();
		this.index.append(index);
	}

	// core method to validate block which implements {@code INodeContent}
	public boolean validate(INodeContent content, byte[] expectedRootHash) {
		byte[] blockHash = content.hash();
		byte[] calculatedRootHash = recalculateRoot(blockHash);

		return Arrays.equals(calculatedRootHash, expectedRootHash);
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

	protected byte[] recalculateRoot(byte[] leaf) {
		byte[][] path = path();
		String[] index = index();
		int i = 0;
		byte[] chash = leaf;

		for (byte[] bytes : path) {
			if (index[i].equals("r")) {
				chash = ByteUtil.appendByte(chash, bytes);
			} else {
				chash = ByteUtil.appendByte(bytes, chash);
			}

			chash = Hash.hash(chash);
			i++;
		}

		return chash;
	}

}

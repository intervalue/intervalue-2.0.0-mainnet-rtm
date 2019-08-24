package one.inve.localfullnode2.utilities;

import java.util.Arrays;

import org.junit.Test;

import one.inve.localfullnode2.utilities.merkle.INodeContent;
import one.inve.localfullnode2.utilities.merkle.MerklePath;
import one.inve.localfullnode2.utilities.merkle.MerkleTree;
import one.inve.localfullnode2.utilities.merkle.Node;

/**
 * 
 * Copyright © INVE FOUNDATION. All rights reserved.
 * 
 * @ClassName: MerkleTreeTest
 * @Description: carefully look at {@code validateSyncData},which is attempted
 *               to demonstrate how to validate block chunk with merkle path.
 * @author Francis.Deng
 * @mailbox francis_xiiiv@163.com
 * @date Aug 21, 2019
 *
 */
public class MerkleTreeTest {
	@Test
	public void getRootHash() {
		INodeContent[] blocks = buildBlocks();

		MerkleTree mt = MerkleTree.create(blocks);
		Node root = mt.getRoot();

		System.out.println("root hash:" + Arrays.toString(root.getHash()));
	}

	@SuppressWarnings("unused")
	@Test
	public void validateSyncData() {
		INodeContent[] blocks = buildBlocks();
		INodeContent[] tamperedBlocks = buildTamperedBlocks();

		MerkleTree mt = MerkleTree.create(blocks);
		Node root = mt.getRoot();
		byte[] famedRootHash = root.getHash();// which is provided by 3rd famed host

		MerklePath mp = mt.getMerklePath(blocks[2]);
		INodeContent tamperedBlock = tamperedBlocks[2];
		INodeContent integralBlock = blocks[2];

		// ignore these processes:
		// .......serialize (famedRootHash,mp,tamperedBlock,integralBlock)
		// ..........................................network transmission
		// .....deserialize (famedRootHash,mp,tamperedBlock,integralBlock)

		if (!mp.validate(tamperedBlock, famedRootHash)) {
			System.out.println("bad news 1: tampered item");// this would be displayed.
		}

		if (!mp.validate(integralBlock, famedRootHash)) {
			System.out.println("bad news 2: tampered item");// this wouldn't be displayed.
		}

//		byte[][] path = mp.path();
//
//		byte[] calculatedRoot = recalculateRoot(tamperedBlock.hash(), mp);
//		if (!Arrays.equals(famedRootHash, calculatedRoot)) {
//			System.out.println("bad news: tampered item");
//		}
//
//		calculatedRoot = recalculateRoot(integralBlock.hash(), mp);
//		if (Arrays.equals(famedRootHash, calculatedRoot)) {
//			System.out.println("good news: integral item");
//		}

	}

//	protected byte[] recalculateRoot(byte[] leaf, MerklePath mp) {
//		byte[][] path = mp.path();
//		String[] index = mp.index();
//		int i = 0;
//		byte[] chash = leaf;
//
//		for (byte[] bytes : path) {
//			if (index[i].equals("r")) {
//				chash = ByteUtil.appendByte(chash, bytes);
//			} else {
//				chash = ByteUtil.appendByte(bytes, chash);
//			}
//
//			chash = Hash.hash(chash);
//			i++;
//		}
//
//		return chash;
//	}

	protected INodeContent[] buildBlocks() {
		INodeContent[] blocks = new INodeContent[3];

		blocks[0] = new Block("hello", 1);
		blocks[1] = new Block("blockchain", 2);
		blocks[2] = new Block("world", 3);

		return blocks;
	}

	protected INodeContent[] buildTamperedBlocks() {
		INodeContent[] blocks = new INodeContent[3];

		blocks[0] = new Block("hello", 1);
		blocks[1] = new Block("blockchain", 2);
		blocks[2] = new Block("worlds", 3);// tampered item

		return blocks;
	}

	// the simplest block
	private static class Block implements INodeContent {

		private String transactions;
		private int id;

		public Block(String transactions, int id) {
			super();
			this.transactions = transactions;
			this.id = id;
		}

		public int getId() {
			return id;
		}

		@Override
		public byte[] hash() {
			return Hash.hash(transactions);
		}

		@Override
		public boolean equals(INodeContent content) {
			return content instanceof Block && ((Block) content).getId() == id;
		}

	}
}

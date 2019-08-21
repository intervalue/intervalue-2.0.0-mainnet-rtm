package one.inve.localfullnode2.utilities.merkle;

import java.util.Arrays;

import one.inve.localfullnode2.utilities.ByteUtil;
import one.inve.localfullnode2.utilities.GenericArray;
import one.inve.localfullnode2.utilities.Hash;

/**
 * 
 * Copyright © INVE FOUNDATION. All rights reserved.
 * 
 * @ClassName: MerkleTree
 * @Description: MerkleTree complies to the convention of
 *               {@link https://en.wikipedia.org/wiki/Merkle_tree}
 * @author Francis.Deng
 * @date Aug 20, 2019
 *
 */
public class MerkleTree {
	private Node root;
	private byte[] merkleRoot;
	private GenericArray<Node> leaves;

	public static MerkleTree create(INodeContent[] contents) {
		return buildWithContent(contents);
	}

	// mostly build leaves at the bottom and pass leaves into {@code
	// buildWithLeaves}
	private static MerkleTree buildWithContent(INodeContent[] contents) {
		if (contents == null || contents.length == 0) {
			throw new MerkleTreeException("cannot construct tree with no content");
		}

		GenericArray<Node> leaves = new GenericArray<Node>();
		for (INodeContent content : contents) {
			byte[] hash = content.hash();

			leaves.append(new Node().setHash(hash).setContent(content).setLeaf(true));
		}

		if (leaves.length() % 2 == 1) {
			Node duplicate = new Node().setHash(leaves.last().getHash()).setContent(leaves.last().getContent())
					.setLeaf(true).setDup(true);
			leaves.append(duplicate);
		}

		Node root = buildWithLeaves(leaves);
		MerkleTree mt = new MerkleTree();
		mt.root = root;
		mt.merkleRoot = root.getHash();
		mt.leaves = leaves;

		return mt;

	}

	// build inner branches or root with a given list of leaves
	private static Node buildWithLeaves(GenericArray<Node> nl) {
		GenericArray<Node> branches = new GenericArray<>();

		for (int i = 0; i < branches.length(); i += 2) {
			int left = i;
			int right = i + 1;
			if (i + 1 == nl.length()) {
				right = i;
			}

			byte[] chash = ByteUtil.appendByte(nl.get(left).getHash(), nl.get(right).getHash());
			Node node = new Node().setLeft(nl.get(left)).setRight(nl.get(right)).setHash(Hash.hash(chash));

			branches.append(node);
			nl.get(left).setParent(node);
			nl.get(right).setParent(node);

			if (nl.length() == 2) {
				return node;
			}
		}

		return buildWithLeaves(branches);
	}

	public boolean verify() {
		byte[] mr = root.verify();

		return Arrays.equals(merkleRoot, mr);
	}

	// indicates whether a given content is in the tree and the hashes are valid for
	// that content
	public boolean verifyContent(INodeContent content) {
		for (Node node : leaves) {
			if (node.getContent().equals(content)) {
				Node parent = node.getParent();

				while (parent != null) {
					byte[] rBytes = parent.getRight().getHash();
					byte[] lBytes = parent.getLeft().getHash();

					byte[] chash = ByteUtil.appendByte(rBytes, lBytes);
					if (!Arrays.equals(chash, parent.getHash())) {
						return false;
					}

					parent = parent.getParent();
				}
			}

			return true;
		}

		return false;
	}

	// it's how to get so-called merkle path in our mind.
	public MerklePath getMerklePath(INodeContent content) {
		for (Node node : leaves) {
			Node current = node;
			if (current.getContent().equals(content)) {
				Node curParent = current.getParent();
				GenericArray<byte[]> path = new GenericArray<>();
				GenericArray<String> index = new GenericArray<>();
				;

				while (curParent != null) {
					if (Arrays.equals(curParent.getLeft().getHash(), node.getHash())) {
						path.append(curParent.getRight().getHash());
						index.append("r");// right-side
					} else {
						path.append(curParent.getLeft().getHash());
						index.append("l");// left-side
					}

					current = curParent;
					curParent = curParent.getParent();
				}

				return new MerklePath(path, index);
			}
		}

		return null;
	}

}

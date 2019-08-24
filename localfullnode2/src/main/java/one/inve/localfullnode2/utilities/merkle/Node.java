package one.inve.localfullnode2.utilities.merkle;

import one.inve.localfullnode2.utilities.ByteUtil;
import one.inve.localfullnode2.utilities.Hash;

/**
 * 
 * Copyright © INVE FOUNDATION. All rights reserved.
 * 
 * @ClassName: Node
 * @Description: represent a node,root or leaf in the merkle tree
 * @author Francis.Deng
 * @date Aug 20, 2019
 *
 */
public class Node {
	private Node parent;
	private Node left;
	private Node right;

	private boolean leaf;
	private INodeContent content;
	private byte[] hash;
	private boolean dup;

	// walks down the merkle tree until hitting a leaf
	public byte[] verify() {
		if (leaf) {
			return content.hash();
		}

		byte[] rBytes = right.verify();
		byte[] lBytes = left.verify();

		byte[] chash = ByteUtil.appendByte(rBytes, lBytes);

		return Hash.hash(chash);
	}

	public Node getParent() {
		return parent;
	}

	public Node setParent(Node parent) {
		this.parent = parent;
		return this;
	}

	public Node getLeft() {
		return left;
	}

	public Node setLeft(Node left) {
		this.left = left;
		return this;
	}

	public Node getRight() {
		return right;
	}

	public Node setRight(Node right) {
		this.right = right;
		return this;
	}

	public boolean isLeaf() {
		return leaf;
	}

	public Node setLeaf(boolean leaf) {
		this.leaf = leaf;
		return this;
	}

	public INodeContent getContent() {
		return content;
	}

	public Node setContent(INodeContent content) {
		this.content = content;
		return this;
	}

	public byte[] getHash() {
		return hash;
	}

	public Node setHash(byte[] hash) {
		this.hash = hash;
		return this;
	}

	public boolean isDup() {
		return dup;
	}

	public Node setDup(boolean dup) {
		this.dup = dup;
		return this;
	}

}

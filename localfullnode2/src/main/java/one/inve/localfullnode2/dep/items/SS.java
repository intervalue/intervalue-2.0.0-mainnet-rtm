package one.inve.localfullnode2.dep.items;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.HashSet;

import one.inve.bean.message.Contribution;
import one.inve.bean.message.SnapshotMessage;
import one.inve.bean.message.SnapshotPoint;
import one.inve.localfullnode2.dep.DependentItem;

/**
 * 
 * 
 * Copyright © CHXX Co.,Ltd. All rights reserved.
 * 
 * @Description: That is Snapshot(SS)
 * @author: Francis.Deng
 * @date: Jun 4, 2019 8:40:27 PM
 * @version: V1.0
 */
public class SS extends DependentItem {
	BigInteger currSnapshotVersion;

	HashSet<Contribution> contributions;

	HashMap<BigInteger, String> treeRootMap;

	HashMap<BigInteger, SnapshotPoint> snapshotPointMap;

	String msgHashTreeRoot;

	BigInteger totalFeeBetween2Snapshots;

	SnapshotMessage snapshotMessage;

	public BigInteger getCurrSnapshotVersion() {
		return currSnapshotVersion;
	}

	public void setCurrSnapshotVersion(BigInteger currSnapshotVersion) {
		this.currSnapshotVersion = currSnapshotVersion;
		nodifyAll();
	}

	public HashSet<Contribution> getContributions() {
		return contributions;
	}

	public void setContributions(HashSet<Contribution> contributions) {
		this.contributions = contributions;
		nodifyAll();
	}

	public HashMap<BigInteger, String> getTreeRootMap() {
		return treeRootMap;
	}

	public void setTreeRootMap(HashMap<BigInteger, String> treeRootMap) {
		this.treeRootMap = treeRootMap;
		nodifyAll();
	}

	public HashMap<BigInteger, SnapshotPoint> getSnapshotPointMap() {
		return snapshotPointMap;
	}

	public void setSnapshotPointMap(HashMap<BigInteger, SnapshotPoint> snapshotPointMap) {
		this.snapshotPointMap = snapshotPointMap;
		nodifyAll();
	}

	public String getMsgHashTreeRoot() {
		return msgHashTreeRoot;
	}

	public void setMsgHashTreeRoot(String msgHashTreeRoot) {
		this.msgHashTreeRoot = msgHashTreeRoot;
		nodifyAll();
	}

	public BigInteger getTotalFeeBetween2Snapshots() {
		return totalFeeBetween2Snapshots;
	}

	public void setTotalFeeBetween2Snapshots(BigInteger totalFeeBetween2Snapshots) {
		this.totalFeeBetween2Snapshots = totalFeeBetween2Snapshots;
		nodifyAll();
	}

	public SnapshotMessage getSnapshotMessage() {
		return snapshotMessage;
	}

	public void setSnapshotMessage(SnapshotMessage snapshotMessage) {
		this.snapshotMessage = snapshotMessage;
		nodifyAll();
	}

}

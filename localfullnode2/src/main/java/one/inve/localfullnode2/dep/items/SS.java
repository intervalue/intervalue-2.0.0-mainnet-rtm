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
	private HashSet<Contribution> contributions;
	private HashMap<BigInteger, String> treeRootMap;
	private HashMap<BigInteger, SnapshotPoint> snapshotPointMap;
	private String msgHashTreeRoot;
	private BigInteger totalFeeBetween2Snapshots;
	private SnapshotMessage snapshotMessage;

	public BigInteger getCurrSnapshotVersion() {
		return (null == snapshotMessage) ? BigInteger.ONE : BigInteger.ONE.add(snapshotMessage.getSnapVersion());
	}

	public HashSet<Contribution> getContributions() {
		return contributions;
	}

	public void setContributions(HashSet<Contribution> contributions) {
		this.contributions = contributions;
		nodifyAll();
	}

    public void addContribution(Contribution contribution) {
        this.contributions.add(contribution);
        nodifyAll();
    }

	public HashMap<BigInteger, String> getTreeRootMap() {
		return treeRootMap;
	}

	public void setTreeRootMap(HashMap<BigInteger, String> treeRootMap) {
		this.treeRootMap = treeRootMap;
		nodifyAll();
	}

    public void putTreeRootMap(BigInteger snapVersion, String msgHashTreeRoot) {
        this.treeRootMap.put(snapVersion, msgHashTreeRoot);
        nodifyAll();
    }

	public void removeTreeRootMap(BigInteger snapVersion) {
		this.treeRootMap.remove(snapVersion);
		nodifyAll();
	}

	public HashMap<BigInteger, SnapshotPoint> getSnapshotPointMap() {
		return snapshotPointMap;
	}

	public void setSnapshotPointMap(HashMap<BigInteger, SnapshotPoint> snapshotPointMap) {
		this.snapshotPointMap = snapshotPointMap;
		nodifyAll();
	}

    public void putSnapshotPointMap(BigInteger snapVersion, SnapshotPoint snapshotPoint) {
        this.snapshotPointMap.put(snapVersion, snapshotPoint);
        nodifyAll();
    }

	public void removeSnapshotPointMap(BigInteger snapVersion) {
		this.snapshotPointMap.remove(snapVersion);
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

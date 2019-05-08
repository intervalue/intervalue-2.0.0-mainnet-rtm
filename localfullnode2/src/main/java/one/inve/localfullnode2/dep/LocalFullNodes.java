package one.inve.localfullnode2.dep;

import java.util.List;

import one.inve.bean.node.LocalFullNode;

public class LocalFullNodes extends DependentItem {
	private List<LocalFullNode> localFullNodes;

	public void set(List<LocalFullNode> localFullNodes) {
		this.localFullNodes = localFullNodes;
		nodifyAll();
	}

	public List<LocalFullNode> get() {
		return localFullNodes;
	}
}

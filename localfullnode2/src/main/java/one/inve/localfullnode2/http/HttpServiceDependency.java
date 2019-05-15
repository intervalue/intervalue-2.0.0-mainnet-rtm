package one.inve.localfullnode2.http;

import one.inve.localfullnode2.LocalFullNode1GeneralNode;
import one.inve.localfullnode2.utilities.http.HttpServiceImplsDependent;

public class HttpServiceDependency implements HttpServiceImplsDependent {
	private LocalFullNode1GeneralNode node;

	public LocalFullNode1GeneralNode getNode() {
		return node;
	}

	public void setNode(LocalFullNode1GeneralNode node) {
		this.node = node;
	}

}

package one.inve.localfullnode2.dep.items;

import java.util.List;

import one.inve.cluster.Member;
import one.inve.localfullnode2.dep.DependentItem;

public class Members extends DependentItem {
	private List<Member> members;

	public List<Member> get() {
		return members;
	}

	public void set(List<Member> members) {
		this.members = members;
	}

}

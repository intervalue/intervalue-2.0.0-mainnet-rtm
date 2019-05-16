package one.inve.localfullnode2.dep.items;

import java.util.List;

import one.inve.cluster.Member;
import one.inve.localfullnode2.conf.Config;
import one.inve.localfullnode2.dep.DependentItem;

public class Members extends DependentItem {

	private List<Member> membersInSharding;
	private List<Member> membersGlobally;

	public List<Member> get(int inShardingOrGlobally) {
		return inShardingOrGlobally == Config.GOSSIP_GLOBAL_SHARD ? membersGlobally : membersInSharding;
	}

	public void setInSharding(List<Member> members) {
		this.membersInSharding = members;
		nodifyAll();
	}

	public void setGlobally(List<Member> members) {
		this.membersGlobally = members;
		nodifyAll();
	}

}

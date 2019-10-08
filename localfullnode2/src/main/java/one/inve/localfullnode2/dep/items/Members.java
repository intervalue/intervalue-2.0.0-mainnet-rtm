package one.inve.localfullnode2.dep.items;

import java.util.Collections;
import java.util.List;

import one.inve.cfg.localfullnode.Config;
import one.inve.cluster.Member;
import one.inve.localfullnode2.dep.DependentItem;

public class Members extends DependentItem {

	private List<Member> membersInSharding;
	private List<Member> membersGlobally;

	public List<Member> get(int inShardingOrGlobally) {
		return inShardingOrGlobally == Config.GOSSIP_GLOBAL_SHARD ? membersGlobally : membersInSharding;
	}

	/**
	 * introduce thread-safe list
	 */

	public void setInSharding(List<Member> members) {
		this.membersInSharding = Collections.synchronizedList(members);
		nodifyAll();
	}

	public void setGlobally(List<Member> members) {
		this.membersGlobally = Collections.synchronizedList(members);
		nodifyAll();
	}

}

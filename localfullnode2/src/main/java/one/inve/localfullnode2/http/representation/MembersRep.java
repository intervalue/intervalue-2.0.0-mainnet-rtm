package one.inve.localfullnode2.http.representation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 
 * Copyright © INVE FOUNDATION. All rights reserved.
 * 
 * @ClassName: MembersRep
 * @Description: the class is referenced by {@code getInshardMembers}
 * @author Francis.Deng [francis_xiiiv@163.com]
 * @date Feb 26, 2020
 *
 */
public class MembersRep {
	public static class MemberRep {
		private String address;
		private Map<String, String> meta;

		public MemberRep() {
			this.meta = new HashMap<>();
		}

		public String getAddress() {
			return address;
		}

		public void setAddress(String address) {
			this.address = address;
		}

		public Map<String, String> getMeta() {
			return meta;
		}

		public MemberRep putMeta(String key, String value) {
			meta.put(key, value);
			return this;
		}

	}

	private List<MemberRep> members = Collections.synchronizedList(new ArrayList<MemberRep>());

	public MembersRep addMember(MemberRep member) {
		members.add(member);
		return this;
	}

	public List<MemberRep> getMembers() {
		return members;
	}

}

package one.inve.localfullnode2.sync.criteria;

import java.util.LinkedList;
import java.util.List;

public class RangeRing {
	List<Range> ranges = new LinkedList<>();

	public void increase(Range r) {
		if (ranges.size() == 0) {
			ranges.add(r);
			return;
		}

		for (Range range : ranges) {
			boolean merged = range.attemptToMerge(r);
			if (merged)
				return;
		}

		ranges.add(r);
	}

	public void decrease(Range r) {

	}
}

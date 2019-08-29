package one.inve.localfullnode2.sync.measure;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * Copyright © INVE FOUNDATION. All rights reserved.
 * 
 * @ClassName: Range
 * @Description: represents long list.For example, Range(1,9,2) means a list of
 *               "1,3,5,7",Range(10) means a list of "0,1,2,3,4,5,6,7,8,9"
 * @author Francis.Deng
 * @mailbox francis_xiiiv@163.com
 * @date Aug 21, 2019
 *
 */
public class Range {
	private long start;
	private long stop;
	// private long step;

//	public Range(long stop) {
//		this(0l, stop);
//	}
//
	public Range(long stop) {
		this(0l, stop);
	}

	public Range(long start, long stop) {
		this.start = start;
		this.stop = stop;
	}

	public long getStart() {
		return start;
	}

	public long getStop() {
		return stop;
	}

	public boolean isUseless() {
		return start == stop;
	}

	public boolean attemptToMerge(Range r) {
		boolean overlapped = false;
		if (start <= r.getStop()) {
			if (start <= r.getStart())
				start = r.getStart();
			overlapped = true;
		}

		if (stop > r.getStart()) {
			if (stop <= r.getStop())
				stop = r.getStop();
			overlapped = true;
		}

		return overlapped;
	}

	public List<Range> attemptToExclude(Range r) {
		List<Range> ranges = new ArrayList<>();

		//@formatter:off
		// ++++++++++
		//              -------
		
		//              ++++++++++
		// -------		
		//@formatter:on	
		if (stop <= r.getStart() || r.getStop() <= start) {
			ranges.add(this);
			return ranges;
		}

		//@formatter:off
		//       +++++++
		// --------------------
		//@formatter:on
		if (start >= r.getStart() && stop <= r.getStop()) {
			return ranges;
		}

		//@formatter:off
		// ++++++++++++++++++++
		//        -------
		//@formatter:on
		if (start <= r.getStart() && stop >= r.getStop()) {
			ranges.add(new Range(start, r.getStart()));// possibly useless
			ranges.add(new Range(r.getStop(), stop));// possibly useless
		}

		//@formatter:off
		// ++++++++++++++++++++
		//                   -------
		//@formatter:on	
		if (start < r.getStart() && r.getStart() < stop) {
			ranges.add(new Range(start, r.getStart()));
		}

		//@formatter:off
		//           ++++++++++++++++++++
		//        -------
		//@formatter:on			
		if (r.getStart() < start && start < r.getStop()) {
			ranges.add(new Range(r.getStop(), stop));
		}

		return ranges;
	}

}
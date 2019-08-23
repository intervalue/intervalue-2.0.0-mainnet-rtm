package one.inve.localfullnode2.sync;

import one.inve.localfullnode2.sync.criteria.Range;
import one.inve.localfullnode2.utilities.GenericArray;

public class Distribution {
	public static class Column {
		private GenericArray<Range> ranges = new GenericArray<>();

		public void add(Range r) {
			for (Range range : ranges) {
				boolean b = range.attemptToMerge(r);
				if (b)
					return;
			}

			ranges.append(r);
		}
	}

	private Column[] columns;

	public Distribution(int columnNum) {
		columns = new Column[columnNum];

	}

	public void addLabeledRange(int label, Range r) {
		columns[label].add(r);
	}

}

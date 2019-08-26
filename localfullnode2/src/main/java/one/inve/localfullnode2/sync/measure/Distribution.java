package one.inve.localfullnode2.sync.measure;

import one.inve.localfullnode2.utilities.GenericArray;

/**
 * 
 * Copyright © INVE FOUNDATION. All rights reserved.
 * 
 * @ClassName: Distribution
 * @Description: Distribution constitutes 1 or a couple of {@code Column},the
 *               former is message distribution
 * @author Francis.Deng
 * @mailbox francis_xiiiv@163.com
 * @date Aug 23, 2019
 *
 */
public class Distribution {
	public static class Column {
		private GenericArray<Range> ranges = new GenericArray<>();

		public GenericArray<Range> getRanges() {
			return ranges;
		}

		public void add(Range r) {
			for (Range range : ranges) {
				boolean b = range.attemptToMerge(r);
				if (b)
					return;
			}

			ranges.append(r);
		}

		public void add(Column column) {
			for (Range range : column.getRanges()) {
				add(range);
			}
		}
	}

	private Column[] columns;

	public Column[] getColumns() {
		return columns;
	}

	public Distribution(int columnNum) {
		columns = new Column[columnNum];

	}

	public void addLabeledRange(int label, Range r) {
		columns[label].add(r);
	}

	// merge another distribution
	public void addDistribution(Distribution dist) {
		for (int index = 0; index < dist.getColumns().length; index++) {
			columns[index].add(dist.getColumns()[index]);
		}
	}

}

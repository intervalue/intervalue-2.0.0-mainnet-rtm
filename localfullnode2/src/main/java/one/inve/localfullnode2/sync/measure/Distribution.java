package one.inve.localfullnode2.sync.measure;

import com.alibaba.fastjson.JSON;

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

//	public static class Column {
//		private GenericArray<Range> ranges = new GenericArray<>();
//
//		public GenericArray<Range> getRanges() {
//			return ranges;
//		}
//
//		public void add(Range r) {
//			for (Range range : ranges) {
//				boolean b = range.attemptToMerge(r);
//				if (b)
//					return;
//			}
//
//			ranges.append(r);
//		}
//
//		public void add(Column column) {
//			for (Range range : column.getRanges()) {
//				add(range);
//			}
//		}
//
//		public Range nextRange(int step) {
//			Range hasMaxStop = null;
//			for (Range range : ranges) {
//				if (hasMaxStop == null) {
//					hasMaxStop = range;
//					continue;
//				}
//
//				if (range.getStop() > hasMaxStop.getStop()) {
//					hasMaxStop = range;
//				}
//			}
//
//			return new Range(hasMaxStop.getStop(), hasMaxStop.getStop() + step);
//		}
//	}

	private Column[] columns;
	private int columnNum;

	public Column[] getColumns() {
		return columns;
	}

	public Distribution(int columnNum) {
		initIfPossible(columnNum);

	}

	public boolean isNull() {
		boolean isNull = true;

		if (columns != null) {
			for (Column c : columns) {
				if (!c.isNull()) {
					return false;
				}
			}
		}

		return isNull;
	}

	// if the object is created in this approach,call {@code initIfPossible} later
//	public Distribution() {
//
//	}

	public void initIfPossible(int columnNum) {
		if (columns == null) {
			columns = new Column[columnNum];
			this.columnNum = columnNum;
			for (int index = 0; index < columnNum; index++) {
				columns[index] = new Column();
			}
		}
	}

	public Distribution(Column[] columns) {
		this.columns = columns;
		this.columnNum = columns.length;
	}

//	public GenericArray<Range> nextRangesInColumns(int step) {
//		GenericArray<Range> ranges = new GenericArray<>();
//
//		for (Column column : columns) {
//			ranges.append(column.nextRange(step));
//		}
//
//		return ranges;
//	}

	public static Distribution build(int columnNum, long[] fromPositions, int increase) {
		Column[] cols = new Column[columnNum];

		for (int index = 0; index < columnNum; index++) {
			Range r = new Range(fromPositions[index], fromPositions[index] + increase);
			cols[index] = new Column();
			cols[index].add(r);
		}

		return new Distribution(cols);
	}

	public Distribution next(int increase) {
		// Distribution newDistribution = new Distribution(columns.length);
		Column[] cols = new Column[columns.length];

		for (int index = 0; index < columns.length; index++) {
			Range r = columns[index].nextRange(increase);
			Column col = new Column();
			col.add(r);

			cols[index] = col;
		}

		return new Distribution(cols);
	}

	public Range nextRangeInColumn(int columnth, int step) {
		Column column = columns[columnth];
		return column.nextRange(step);
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

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("Distribution {");

		for (Column column : columns) {
			sb.append(column.toString() + " ");
		}

		sb.append("}");
		return sb.toString();
	}

	public static Distribution fromString(String text) {
		return JSON.parseObject(text, Distribution.class);
	}

}

package one.inve.localfullnode2.sync.measure;

import java.util.ArrayList;
import java.util.List;

public class Column {
	private List<Range> ranges = new ArrayList<Range>();

	public List<Range> getRanges() {
		return ranges;
	}

	public void add(Range r) {
		for (Range range : ranges) {
			boolean b = range.attemptToMerge(r);
			if (b)
				return;
		}

		ranges.add(r);
	}

	public boolean isNull() {
		return ranges.size() <= 0;
	}

	public void add(Column column) {
		for (Range range : column.getRanges()) {
			add(range);
		}
	}

	public Range nextRange(int step) {
		Range hasMaxStop = null;
		for (Range range : ranges) {
			if (hasMaxStop == null) {
				hasMaxStop = range;
				continue;
			}

			if (range.getStop() > hasMaxStop.getStop()) {
				hasMaxStop = range;
			}
		}

		return new Range(hasMaxStop.getStop(), hasMaxStop.getStop() + step);
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("Column <");

		for (Range range : ranges) {
			sb.append(range.toString() + " ");
		}

		sb.append(">");
		return sb.toString();
	}

}

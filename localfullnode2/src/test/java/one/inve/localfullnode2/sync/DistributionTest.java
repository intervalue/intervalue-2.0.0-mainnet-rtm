package one.inve.localfullnode2.sync;

import org.junit.Test;

import com.google.gson.Gson;

import one.inve.localfullnode2.sync.measure.Distribution;
import one.inve.localfullnode2.sync.measure.Range;

public class DistributionTest {
	@Test
	public void test() {
		Gson gson = new Gson();

		Distribution dist = new Distribution(4);
		dist.addLabeledRange(0, new Range(0, 5));
		String distJson = gson.toJson(dist);
		Distribution requestSideDist = gson.fromJson(distJson, Distribution.class);
		System.out.println(requestSideDist);

		Distribution dist1 = new Distribution(4);
		String distJson1 = gson.toJson(dist1);
		Distribution requestSideDist1 = gson.fromJson(distJson1, Distribution.class);
		System.out.println(requestSideDist1.isNull());

		// test the following cases
//		if (requestSideDist.isNull()) {
//			nextDist = Distribution.build(nValue, firstSeqsInThisShard, _eventSize);
//		} else {
//			nextDist = requestSideDist.next(_eventSize);
//		}
		Distribution nextDist = new Distribution(4);
		;
		if (nextDist.isNull()) {
			nextDist = Distribution.build(4, new long[] { 1, 12, 8, 0 }, 50);
		} else {
			nextDist = requestSideDist.next(100);
		}
		Distribution nextNextDist = nextDist.next(100);
		System.out.println(nextNextDist);
	}
}

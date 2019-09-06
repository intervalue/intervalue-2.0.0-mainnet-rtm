package one.inve.localfullnode2.sync;

import java.util.ArrayList;
import java.util.Arrays;

import org.junit.Test;

import one.inve.localfullnode2.sync.measure.ChunkDistribution;
import one.inve.localfullnode2.sync.measure.ChunkDistribution.Session;

public class ChunkDistributionTest {
	private static ThreadLocal<Session<String>> threadLocal = new ThreadLocal<>();

	@Test
	public void wholeCycle() {
//		ChunkDistribution<String> dist = new ChunkDistribution();
//
//		String[] msgIds = new String[] { "1", "2", "3", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14" };
//
//		if (dist.isNull()) {
//			dist = new ChunkDistribution<String>(msgIds);
//		}
//
//		// client-side
//		dist.movetoNext();
//		Session<String> ss = (Session<String>) dist.save();
//		dist.cleanUp();
//
//		// server-side
//		ChunkDistribution<String> nextDist = null;
//		if (!dist.isNull()) {
//			// dist.restore(ss);
//			nextDist = dist.next();
//		} else {
//			return;
//		}
//
//		// client-side
//		nextDist.restore(ss);
//		nextDist.movetoNext();
//		nextDist.cleanUp();
//
//		// server-side
//		ChunkDistribution<String> nextNextDist = null;
//		if (!nextDist.isNull()) {
//			// dist.restore(ss);
//			nextNextDist = nextDist.next();
//		} else {
//			return;
//		}

		ChunkDistribution<String> dist = clientSideAtFirst();

		do {
			dist = serverSide(dist);
			dist = clientSide(dist);
		} while (dist != null);
	}

	protected ChunkDistribution<String> clientSideAtFirst() {
		ChunkDistribution<String> dist = new ChunkDistribution();
		return dist;
	}

	protected ChunkDistribution<String> clientSide(ChunkDistribution<String> dist) {

		if (threadLocal.get() == null) {
			Session<String> ss = (Session<String>) dist.save();
			threadLocal.set(ss);

		} else {
			dist.restore(threadLocal.get());

		}

		if (!dist.prepareNextRound(6))// no more elements at all
			return null;
		ArrayList<String> ids = dist.getNextPartOfElements();
		System.out.println("client prepares for :" + Arrays.toString(ids.toArray(new String[ids.size()])));

		dist.cleanUp();

		return dist.isNull() ? null : dist;
	}

	protected ChunkDistribution<String> serverSide(ChunkDistribution<String> dist) {
		if (dist.isNull()) {
			String[] msgIds = new String[] { "1", "2", "3", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14" };
			dist = new ChunkDistribution<String>(msgIds);
		} else {
			dist = dist.next();
			ArrayList<String> ids = dist.getNextPartOfElements();
			System.out.println("server produces :" + Arrays.toString(ids.toArray(new String[ids.size()])));
		}

		return dist;
	}
}

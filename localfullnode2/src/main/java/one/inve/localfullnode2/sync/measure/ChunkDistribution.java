package one.inve.localfullnode2.sync.measure;

import java.util.ArrayList;

public class ChunkDistribution<T> {
	public static ChunkDistribution<?> Empty = new ChunkDistribution<>();

	protected T[] allElementsInChunk = null;

	private ArrayList<T> nextPartOfElements = null;
	private long nextNotPickupIndex = 0;

	public ChunkDistribution(ArrayList<T> nextPartOfElements, long nextNotPickupIndex) {
		super();
		this.nextPartOfElements = nextPartOfElements;
		this.nextNotPickupIndex = nextNotPickupIndex;
	}

	public ChunkDistribution() {
	}

	public ChunkDistribution(T[] allElementsInChunk) {
		this.allElementsInChunk = allElementsInChunk;
	}

	public boolean isNull() {
		return nextPartOfElements == null || nextPartOfElements.size() <= 0;
	}

	// client picked up a lot of element out of {@code allElementsInChunk},put them
	// in the {@code nextPartOfElements}
	public ChunkDistribution<T> next() {
		return new ChunkDistribution(nextPartOfElements, nextNotPickupIndex);
	}

	// invoked by client
	public boolean prepareNextRound(long fetchSize) {

		long nextIndex = (nextNotPickupIndex + fetchSize) > allElementsInChunk.length ? allElementsInChunk.length
				: (nextNotPickupIndex + fetchSize);

		nextPartOfElements = new ArrayList<>();
		for (long i = nextNotPickupIndex; i < nextIndex; i++) {
			nextPartOfElements.add(allElementsInChunk[(int) i]);
		}

		nextNotPickupIndex = nextIndex;

		return nextPartOfElements.size() > 0;
	}

	// invoked by client
	public void addDistribution(ChunkDistribution<T> dist) {
		ArrayList<T> thisBatchElements = dist.getNextPartOfElements();
		nextNotPickupIndex += thisBatchElements.size();
	}

	// first touch functions
	public ArrayList<T> getNextPartOfElements() {
		return nextPartOfElements;
	}

	public Session<T> save() {
		return new Session<T>(this);
	}

	public void cleanUp() {
		allElementsInChunk = null;
	}

	public void restore(Session<T> session) {
		this.allElementsInChunk = session.allElementsInChunk;
	}

	public static class Session<Y> {
		private Y[] allElementsInChunk;

		public Session(ChunkDistribution<Y> setDistribution) {
			this.allElementsInChunk = setDistribution.allElementsInChunk;
		}
	}
}

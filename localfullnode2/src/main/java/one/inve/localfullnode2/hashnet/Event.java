package one.inve.localfullnode2.hashnet;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;

import one.inve.localfullnode2.utilities.Hash;
import one.inve.localfullnode2.utilities.Utilities;

public class Event {
	private static final AtomicLong numEventsInMemory = new AtomicLong(0L);

	private int shardId;
	private long creatorId;
	private long creatorSeq;
	private long otherId;
	private long otherSeq;

	private Instant timeCreated;

	long generation;

	Instant consensusTimestamp;
	private Hash hash;
	byte[] signature;
	private byte[][] transactions;
	private boolean cleared = false;

	private long consensusOrder;

	long roundCreated;
	long roundReceived;
	RoundInfo.ElectionRound firstElection;

	private Event selfParent;
	private Event otherParent;

	boolean isWitness;
	boolean isFameDecided;
	boolean isFamous;
	boolean isConsensus;

	/**
	 * 支持快速算法的变量：主要表达see这个概念
	 * first-表示本分片每个creator可以see到本event的最大的seq值，默认为最大整数，因为实际上没有event可以看到本event（除了本event自己之外）
	 * last
	 * -表示本event可以see到的本分片各个creator的最大seq值，默认为-1，当存在parent时，值为两个parent的last每个creator对应分量中lastSeq的最大值
	 */
	Event[] lastEvents;
	Event[] firstEvents;
	int[] lastSeqs;
	int[] firstSeqs;

	public synchronized byte[] getHash() {
		return this.hash == null ? null : this.hash.hash;
	}

	public long getGeneration() {
		return generation;
	}

	public byte[] getSignature() {
		return this.signature;
	}

	static long getNumEventsInMemory() {
		return numEventsInMemory.get();
	}

	public synchronized boolean isCleared() {
		return this.cleared;
	}

	public synchronized boolean isWitness() {
		return this.isWitness;
	}

	public synchronized boolean isFameDecided() {
		return this.isFameDecided;
	}

	public synchronized boolean isFamous() {
		return this.isFamous;
	}

	public synchronized boolean isConsensus() {
		return this.isConsensus;
	}

	public synchronized Instant getConsensusTimestamp() {
		return this.consensusTimestamp;
	}

	public synchronized byte[][] getTransactions() {
		return this.transactions;
	}

	public synchronized long getOtherId() {
		return this.otherId;
	}

	public synchronized long getOtherSeq() {
		return this.otherSeq;
	}

	public synchronized Event getSelfParent() {
		return this.selfParent;
	}

	public synchronized Event getOtherParent() {
		return this.otherParent;
	}

	public synchronized long getRoundCreated() {
		return this.roundCreated;
	}

	public synchronized int getShardId() {
		return this.shardId;
	}

	public synchronized long getCreatorId() {
		return this.creatorId;
	}

	public synchronized long getCreatorSeq() {
		return this.creatorSeq;
	}

	synchronized long getSeq() {
		return this.creatorSeq;
	}

	public synchronized Instant getTimeCreated() {
		return this.timeCreated;
	}

	public synchronized long getRoundReceived() {
		return this.roundReceived;
	}

	public synchronized long getConsensusOrder() {
		return this.consensusOrder;
	}

	/**
	 * 设置共识顺序，并检查各项约束是否满足，如果不满足则报错
	 * 
	 * @param n 序号
	 */
	synchronized void setConsensusOrder(long n) {
		this.consensusOrder = n;
	}

	Event(int shardId, long creatorId, long creatorSeq, long otherId, long otherSeq, Event selfParent,
			Event otherParent, Instant timeCreated, byte[] signature, long generation, byte[] hash,
			byte[][] transactions) {
		this.shardId = shardId;
		this.creatorId = creatorId;
		this.creatorSeq = creatorSeq;
		this.otherId = otherId;
		this.otherSeq = otherSeq;
		this.timeCreated = timeCreated;
		this.isConsensus = false;
		this.consensusOrder = -1L;
		this.selfParent = selfParent;
		this.otherParent = otherParent;
		this.firstElection = null;
		this.signature = signature.clone();
		this.generation = generation;
		this.hash = new Hash(hash);
		this.transactions = Utilities.deepClone(transactions);

		numEventsInMemory.incrementAndGet();
	}

	void clear() {
		this.cleared = true;
		numEventsInMemory.decrementAndGet();
		this.selfParent = null;
		this.otherParent = null;
		this.lastEvents = null;
		this.firstEvents = null;
	}
}

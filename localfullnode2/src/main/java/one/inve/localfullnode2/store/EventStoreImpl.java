package one.inve.localfullnode2.store;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicLongArray;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import one.inve.cfg.localfullnode.Config;
import one.inve.core.EventBody;
import one.inve.localfullnode2.store.rocks.RocksJavaUtil;

/**
 * 
 * Copyright © INVE FOUNDATION. All rights reserved.
 * 
 * @Description: {@code lastSeq} is a key to record lastSeqs changes.
 * @author: Francis.Deng
 * @date: May 14, 2018 11:59:44 PM
 * @version: V1.0
 */
public class EventStoreImpl implements IEventStore {
	private static final Logger logger = LoggerFactory.getLogger(EventStoreImpl.class);

	private EventStoreDependent dep;
//	private int shardCount;
//	private int n;
//	private int selfId;
	private ConcurrentHashMap<EventKeyPair, EventBody> existEvents;
	private LinkedBlockingQueue<EventKeyPair> existEventKeys;

	// replace it with {@link LastSeqHolder} instance
	// private final ConcurrentHashMap<Integer, AtomicLongArray> lastSeq = new
	// ConcurrentHashMap<>();

	private final SeqsHolder lastSeq = new SeqsHolder();

//	private final class LastSeqHolder implements AtomicLongArrayWrapper.WriteNotifiable {
//		private final ConcurrentHashMap<Integer, AtomicLongArrayWrapper> lastSeq = new ConcurrentHashMap<>();
//
//		public void put(int shardId, AtomicLongArray lastSeqs) {
//			AtomicLongArrayWrapper atomicLongArrayWrapper = AtomicLongArrayWrapper.of(lastSeqs);
//			atomicLongArrayWrapper.setNotifier(this);
//			lastSeq.put(shardId, AtomicLongArrayWrapper.of(lastSeqs));
//
//			notifyDeps(lastSeq);
//		}
//
//		public AtomicLongArrayWrapper get(int shardId) {
//			return lastSeq.get(shardId);
//		}
//
//		@Override
//		public void notify(AtomicLongArrayWrapper atomicLongArrayWrapper) {
//			notifyDeps(lastSeq);
//		}
//
//		private void notifyDeps(ConcurrentHashMap<Integer, AtomicLongArrayWrapper> lastSeq) {
//			LastSeqs lastSeqs = DepItemsManager.getInstance().attachLastSeqs(null);
//			lastSeqs.set(lastSeq);
//		}
//
//	}

	public EventStoreImpl(EventStoreDependent dep) {
		this.dep = dep;
		this.existEvents = new ConcurrentHashMap<>();
		this.existEventKeys = new LinkedBlockingQueue<>();
//		this.shardCount = node.getShardCount();
//		this.n = node.getnValue();
//		this.selfId = (int) node.getCreatorId();
		for (int i = 0; i < dep.getShardCount(); i++) {
			AtomicLongArray lastSeqs = new AtomicLongArray(dep.getnValue());
			RocksJavaUtil rocksJavaUtil = new RocksJavaUtil(dep.getDbId());
			for (int j = 0; j < dep.getnValue(); j++) {
				String key = i + "_" + j;
				byte[] value = rocksJavaUtil.get(key);
				if (null != value && value.length > 0) {
					lastSeqs.set(j, Integer.parseInt(new String(value)));
				} else {
					lastSeqs.set(j, -1);
				}
			}
			this.lastSeq.put(i, lastSeqs);
		}
	}

//	public EventStoreImpl(int shardCount, int n, int selfId) {
//		this.existEvents = new ConcurrentHashMap<>();
//		this.existEventKeys = new LinkedBlockingQueue<>();
//		this.shardCount = shardCount;
//		this.n = n;
//		this.selfId = selfId;
//		for (int i = 0; i < this.shardCount; i++) {
//			AtomicLongArray lastSeqs = new AtomicLongArray(this.n);
//			RocksJavaUtil rocksJavaUtil = new RocksJavaUtil(node.nodeParameters.dbId);
//			for (int j = 0; j < this.n; j++) {
//				String key = i + "_" + j;
//				byte[] value = rocksJavaUtil.get(key);
//				if (null != value && value.length > 0) {
//					lastSeqs.set(j, -1);
//				} else {
//					lastSeqs.set(j, -1);
//				}
//			}
//			this.lastSeq.put(i, lastSeqs);
//		}
//	}

	@Override
	public void initCache() {
//    	 for (int i = 0; i < n; ++i) {
//	            this.lastSeq.set(i, -1);
//	        }
//    	 existEvents.clear();
//    	 existEventKeys.clear();
	}

	@Override
	public EventBody getEvent(int shardId, long creatorId, long creatorSeq) {
		EventBody event = getEventInMem(shardId, creatorId, creatorSeq);
		if (event == null) {
			if (logger.isDebugEnabled()) {
				logger.debug("not in memory, query from database...");
			}
			RocksJavaUtil rocksJavaUtil = new RocksJavaUtil(dep.getDbId());
			EventKeyPair pair = new EventKeyPair(shardId, creatorId, creatorSeq);
			byte[] evt = rocksJavaUtil.get(pair.toString());
			if (null != evt && evt.length > 0) {
				event = JSONObject.parseObject(new String(evt), EventBody.class);
			}
		}
		return event;
	}

	@Override
	public EventBody getEventInMem(int shardId, long creatorId, long creatorSeq) {
		EventKeyPair pair = new EventKeyPair(shardId, creatorId, creatorSeq);
		EventBody eb = existEvents.get(pair);
		RocksJavaUtil rocksJavaUtil = new RocksJavaUtil(dep.getDbId());
		if (eb == null) {
			if (logger.isDebugEnabled()) {
				logger.debug("not in memory, query from database...");
			}
			byte[] evt = rocksJavaUtil.get(pair.toString());
			if (null != evt && evt.length > 0) {
				eb = JSONObject.parseObject(new String(evt), EventBody.class);
			}
		}
		if (eb == null) {
			return null;
		}
		if (creatorSeq > 0) {
			EventKeyPair otherPair = new EventKeyPair(eb.getShardId(), eb.getOtherId(), eb.getOtherSeq());
			EventBody otherParent = existEvents.get(otherPair);
			if (otherParent != null) {
				eb.setOtherHash(otherParent.getHash());
			} else {
				byte[] evt = rocksJavaUtil.get(otherPair.toString());
				if (null != evt && evt.length > 0) {
					otherParent = JSONObject.parseObject(new String(evt), EventBody.class);
				}
				eb.setOtherHash(otherParent == null ? null : otherParent.getHash());
				if (otherParent == null) {
					logger.error("(shardId, creatorId, creatorSeq)=({}, {}, {}) other parent is null", shardId,
							creatorId, creatorSeq);
				}
			}
			EventKeyPair selfPair = new EventKeyPair(eb.getShardId(), eb.getCreatorId(), eb.getCreatorSeq() - 1);
			EventBody selfParent = existEvents.get(selfPair);
			if (selfParent != null) {
				eb.setParentHash(selfParent.getHash());
			} else {
				byte[] evt = rocksJavaUtil.get(selfPair.toString());
				if (null != evt && evt.length > 0) {
					selfParent = JSONObject.parseObject(new String(evt), EventBody.class);
				}
				eb.setParentHash(selfParent == null ? null : selfParent.getHash());
				if (selfParent == null) {
					logger.error("(shardId, creatorId, creatorSeq)=({}, {}, {}) self parent is null", shardId,
							creatorId, creatorSeq);
				}
			}
		}
		return eb;
	}

	/**
	 * reload时添加数据库已经存在的Event.
	 * 
	 * @param creatorId 柱子ID
	 * @return
	 */
	@Override
	public long getLastSeq(int shardId, long creatorId) {
		if (creatorId >= 0) {
			return this.lastSeq.get(shardId).get((int) creatorId);
		} else {
			return -1;
		}
	}

	/**
	 * 更新分片shardId节点creatorId的lastseq
	 * 
	 * @param shardId   分片号
	 * @param creatorId 节点ID
	 */
	@Override
	public void setLastSeq(int shardId, long creatorId, long creatorSeq) {
		this.lastSeq.get(shardId).set((int) creatorId, creatorSeq);
	}

	@Override
	public long[] getLastSeqsByShardId(int shardId) {
		// AtomicLongArray lastSeqs = this.lastSeq.get(shardId);
		AtomicLongArrayWrapper lastSeqs = this.lastSeq.get(shardId);

		int len = lastSeqs.length();
		long[] result = new long[len];

		for (int i = 0; i < len; ++i) {
			result[i] = lastSeqs.get(i);
		}
		return result;
	}

	@Override
	public Iterator<EventBody> genOrderedIterator(int shardId, int n) {
		return new EventIterator<>(shardId, (int) dep.getCreatorId(), n, dep.getDbId());
	}

	@Override
	public void addEvent(EventBody eb) {
		if (existEvents.size() >= Config.DEFAULT_EVENTS_MAP_SIZE) {
			if (logger.isDebugEnabled()) {
				logger.debug("Too many event body in memory, delete the first");
			}
			logger.error("remove first EventKeyPair(>={}) in cache,which is fatal.", Config.DEFAULT_EVENTS_MAP_SIZE);

			EventKeyPair pair = existEventKeys.poll();
			if (pair != null) {
				existEvents.remove(pair);
			}
		}
		EventKeyPair pair = new EventKeyPair(eb.getShardId(), eb.getCreatorId(), eb.getCreatorSeq());
		existEvents.put(pair, eb);
		existEventKeys.add(pair);

		// 事件
		try {
			// Francis.Deng 4/2/2019
			// diagnosing system problem - tracking vivid messages(DSPTVM)
			if (eb.getTrans() != null) {
				logger.info("DSPTVM - eb ({}) with txs ===> EventSaveQueue using by (addEvent) ",
						JSONObject.toJSONString(eb));
			}

			dep.getEventSaveQueue().put(eb);
		} catch (Exception e) {
			logger.error("save new event error: {}", e);
		}

		if (eb.getCreatorSeq() != this.lastSeq.get(eb.getShardId()).get((int) eb.getCreatorId()) + 1) {
			logger.error("Event store add event seq error.");
		}
		this.lastSeq.get(eb.getShardId()).set((int) eb.getCreatorId(), eb.getCreatorSeq());
	}

	private class EventIterator<E> implements Iterator<E> {
		int shardId;
		int selfId;
		int n;

		BigInteger lastSeqs[];

		int index = 0;
		int page = 0;
		int sizePerHashnetNode = 0;
		int sizePerCycle = 0;
		int num = 0;
		String dbId = null;
		boolean flag = false;
		List<EventBody> eventBodys = null;

		EventIterator(int shardId, int selfId, int n, String dbId) {
			this.selfId = selfId;
			this.shardId = shardId;
			this.n = n;
			this.dbId = dbId;

			// 获取和计算lastSeq
			this.getlastSeqs();

			// 分片shardId的hashnet的每个柱子的一轮读取最大数量
			this.sizePerHashnetNode = Config.READ_SIZE_FROM_DB_PER_HASHNETNODE;
			// 一轮读取Event的最大数量
			this.sizePerCycle = this.sizePerHashnetNode * this.n;

			// 读取一轮Event
			eventBodys = this.getAllEvent4DB(page);
			num = eventBodys.size();
//            if(num == sizePerCycle) {
//                flag = true;
//            } else {
//                logger.warn("node-({}, {}): num = {}, sizePerCycle-{}",
//                        this.shardId, this.selfId, num, sizePerCycle);
//            }
//			while (num == 0) {
//				page++;
//				eventBodys = getAllEvent4DB(page);
//				num = eventBodys.size();
//            }
		}

		@Override
		public boolean hasNext() {
//            logger.info("node-({}, {}): hasNext(), index: {}, num: {}, flag: {}", this.shardId, this.selfId, index, num, flag);
			if (flag && index == num) {
				getNewEventBodys();
			}
			return index < num;
		}

		@Override
		public E next() {
//            logger.info("node-({}, {}): next(), index: {}, num: {}, flag: {}", this.shardId, this.selfId, index, num, flag);
			if (flag && index == num) {
				getNewEventBodys();
			}
			if (index < num) {
				return (E) eventBodys.get(index++);
			} else {
				index++;
				return null;
			}
		}

		private void getlastSeqs() {
			lastSeqs = new BigInteger[n];
			RocksJavaUtil rocksJavaUtil = new RocksJavaUtil(this.dbId);
			for (int j = 0; j < n; j++) {
				byte[] seqByte = rocksJavaUtil.get(this.shardId + "_" + j);
				if (null != seqByte && seqByte.length > 0) {
					lastSeqs[j] = new BigInteger(new String(seqByte));

					BigInteger lastSeq = lastSeqs[j];
					EventKeyPair pair = new EventKeyPair(this.shardId, j, lastSeq.longValue());
					byte[] ebByte = rocksJavaUtil.get(pair.toString());
					if (null == ebByte || ebByte.length <= 0) {
						// 向前获取存在的Event
						while (null == ebByte || ebByte.length <= 0) {
							lastSeq = lastSeq.subtract(BigInteger.ONE);
							if (lastSeq.compareTo(BigInteger.ZERO) <= 0) {
								break;
							}
							pair = new EventKeyPair(this.shardId, j, lastSeq.longValue());
							ebByte = rocksJavaUtil.get(pair.toString());
						}
					} else {
						// 向后获取更新的Event
						while (null != ebByte && ebByte.length > 0) {
							lastSeq = lastSeq.add(BigInteger.ONE);

							pair = new EventKeyPair(this.shardId, j, lastSeq.longValue());
							ebByte = rocksJavaUtil.get(pair.toString());
						}
						lastSeq = lastSeq.subtract(BigInteger.ONE);
					}

					// Francis.Deng 4/22/2019
					// abandon storage retainment if new lastSeqs is lower than old one
					// if (!lastSeqs[j].equals(lastSeq)) {
					if (lastSeqs[j].compareTo(lastSeq) > 0) {
						logger.warn("node-({}, {}): ({}, {}) lastSeq diff: db-{}, calcu-{}", this.shardId, this.selfId,
								this.shardId, j, lastSeqs[j], lastSeq);
						lastSeqs[j] = lastSeq;
						rocksJavaUtil.put(this.shardId + "_" + j, lastSeqs[j].toString());
					}
				} else {
					lastSeqs[j] = BigInteger.valueOf(-1);
				}
			}
			logger.warn("node-({}, {}): lastSeqs-{}", this.shardId, this.selfId, JSONObject.toJSONString(lastSeqs));

		}

		private void getNewEventBodys() {
			logger.warn("node-({}, {}): getNewEventBodys()...", this.shardId, this.selfId);
			page++;
			eventBodys = getAllEvent4DB(page);
			num = eventBodys.size();
			index = 0;
//            flag = (num==sizePerCycle);
		}

		private List<EventBody> getAllEvent4DB(int page) {
			RocksJavaUtil rocksJavaUtil = new RocksJavaUtil(this.dbId);
			ArrayList<EventBody> list = new ArrayList<>();
			int n = 0;
			for (int creatorId = 0; creatorId < this.n; creatorId++) {
//				BigInteger startSeq = BigInteger.valueOf(page).multiply(BigInteger.valueOf(this.sizePerHashnetNode));
//				BigInteger endSeq = BigInteger.valueOf(page + 1).multiply(BigInteger.valueOf(this.sizePerHashnetNode));
				BigInteger startSeq = BigInteger.valueOf(page).multiply(BigInteger.valueOf(this.sizePerHashnetNode))
						.add(lastSeqs[creatorId]).subtract(new BigInteger("10000"));
				BigInteger endSeq = BigInteger.valueOf(page + 1).multiply(BigInteger.valueOf(this.sizePerHashnetNode))
						.add(startSeq);
				if (endSeq.compareTo(lastSeqs[creatorId]) >= 0) {
					n++;
					endSeq = lastSeqs[creatorId].add(BigInteger.ONE);
				} else {
					flag = true;
				}
				for (BigInteger seq = startSeq; seq.compareTo(endSeq) < 0; seq = seq.add(BigInteger.ONE)) {
					EventKeyPair pair = new EventKeyPair(this.shardId, creatorId, seq.longValue());
					byte[] evt = rocksJavaUtil.get(pair.toString());
					if (null != evt && evt.length > 0) {
						list.add(JSONObject.parseObject(new String(evt), EventBody.class));
//                        } else {
//                            logger.warn("node-({}, {}): getAllEvent4DB() : event-({},{},{}) id missing in database!!!",
//                                    this.shardId, this.selfId, this.shardId, creatorId, seq);
					}
				}

			}
			if (n == this.n) {
				flag = false;
			}
			logger.warn("node-({}, {}): getAllEvent4DB() : lastseqs: {}, n = {}, page = {}, event size: {}",
					this.shardId, this.selfId, JSONArray.toJSONString(lastSeqs), this.n, page, list.size());
			return list;
		}
	}

	@Override
	public void delEventInCache(int shardId, long creatorId, long creatorSeq) {
		EventKeyPair keyPair = new EventKeyPair(shardId, creatorId, creatorSeq);
		this.existEvents.remove(keyPair);
	}

	@Override
	public Iterator<EventBody> genOrderedIterator(int shardId, int n, BigInteger firstSeq) {
		return new EvtIterator<>(shardId, (int) dep.getCreatorId(), n, dep.getDbId(), firstSeq);
	}

	private class EvtIterator<E> implements Iterator<E> {
		int shardId;
		int selfId;
		int n;
		BigInteger firstSeq;

		BigInteger lastSeqs[];

		int index = 0;
		int page = 0;
		int sizePerHashnetNode = 0;
		int sizePerCycle = 0;
		int num = 0;
		String dbId = null;
		boolean flag = false;
		List<EventBody> eventBodys = null;

		EvtIterator(int shardId, int selfId, int n, String dbId, BigInteger firstSeq) {
			this.selfId = selfId;
			this.shardId = shardId;
			this.n = n;
			this.dbId = dbId;
			this.firstSeq = firstSeq;
			if (page == 0) {
				this.page = firstSeq.divide(new BigInteger("3000")).compareTo(BigInteger.ONE) <= 0 ? 0
						: firstSeq.divide(new BigInteger("3000")).intValue();
			}
			// 获取和计算lastSeq
			this.getlastSeqs();

			// 分片shardId的hashnet的每个柱子的一轮读取最大数量
			this.sizePerHashnetNode = Config.READ_SIZE_FROM_DB_PER_HASHNETNODE;
			// 一轮读取Event的最大数量
			this.sizePerCycle = this.sizePerHashnetNode * this.n;

			// 读取一轮Event
			eventBodys = this.getAfterEvent4DB(page);
			num = eventBodys.size();
		}

		@Override
		public boolean hasNext() {
//            logger.info("node-({}, {}): hasNext(), index: {}, num: {}, flag: {}", this.shardId, this.selfId, index, num, flag);
			if (flag && index == num) {
				getNewEventBodys();
			}
			return index < num;
		}

		@Override
		public E next() {
//            logger.info("node-({}, {}): next(), index: {}, num: {}, flag: {}", this.shardId, this.selfId, index, num, flag);
			if (flag && index == num) {
				getNewEventBodys();
			}
			if (index < num) {
				return (E) eventBodys.get(index++);
			} else {
				index++;
				return null;
			}
		}

		private void getlastSeqs() {
			lastSeqs = new BigInteger[n];
			RocksJavaUtil rocksJavaUtil = new RocksJavaUtil(this.dbId);
			for (int j = 0; j < n; j++) {
				byte[] seqByte = rocksJavaUtil.get(this.shardId + "_" + j);
				if (null != seqByte && seqByte.length > 0) {
					lastSeqs[j] = new BigInteger(new String(seqByte));

					BigInteger lastSeq = lastSeqs[j];
					EventKeyPair pair = new EventKeyPair(this.shardId, j, lastSeq.longValue());
					byte[] ebByte = rocksJavaUtil.get(pair.toString());
					if (null == ebByte || ebByte.length <= 0) {
						// 向前获取存在的Event
						while (null == ebByte || ebByte.length <= 0) {
							lastSeq = lastSeq.subtract(BigInteger.ONE);
							if (lastSeq.compareTo(BigInteger.ZERO) <= 0) {
								break;
							}
							pair = new EventKeyPair(this.shardId, j, lastSeq.longValue());
							ebByte = rocksJavaUtil.get(pair.toString());
						}
					} else {
						// 向后获取更新的Event
						while (null != ebByte && ebByte.length > 0) {
							lastSeq = lastSeq.add(BigInteger.ONE);

							pair = new EventKeyPair(this.shardId, j, lastSeq.longValue());
							ebByte = rocksJavaUtil.get(pair.toString());
						}
						lastSeq = lastSeq.subtract(BigInteger.ONE);
					}

					// Francis.Deng 4/22/2019
					// abandon storage retainment if new lastSeqs is lower than old one
					// if (!lastSeqs[j].equals(lastSeq)) {
					if (lastSeqs[j].compareTo(lastSeq) > 0) {
						logger.warn("node-({}, {}): ({}, {}) lastSeq diff: db-{}, calcu-{}", this.shardId, this.selfId,
								this.shardId, j, lastSeqs[j], lastSeq);
						lastSeqs[j] = lastSeq;
						rocksJavaUtil.put(this.shardId + "_" + j, lastSeqs[j].toString());
					}
				} else {
					lastSeqs[j] = BigInteger.valueOf(-1);
				}
			}
			logger.warn("node-({}, {}): lastSeqs-{}", this.shardId, this.selfId, JSONObject.toJSONString(lastSeqs));

		}

		private void getNewEventBodys() {
			logger.warn("node-({}, {}): getNewEventBodys()...", this.shardId, this.selfId);
			page++;
			eventBodys = getAfterEvent4DB(page);
			num = eventBodys.size();
			index = 0;
//            flag = (num==sizePerCycle);
		}

		private List<EventBody> getAfterEvent4DB(int page) {
			RocksJavaUtil rocksJavaUtil = new RocksJavaUtil(this.dbId);
			ArrayList<EventBody> list = new ArrayList<>();
			int n = 0;
			for (int creatorId = 0; creatorId < this.n; creatorId++) {
				BigInteger startSeq = BigInteger.valueOf(page).multiply(BigInteger.valueOf(this.sizePerHashnetNode));
				BigInteger endSeq = BigInteger.valueOf(page + 1).multiply(BigInteger.valueOf(this.sizePerHashnetNode));
				if (endSeq.compareTo(lastSeqs[creatorId]) >= 0) {
					n++;
					endSeq = lastSeqs[creatorId].add(BigInteger.ONE);
				} else {
					flag = true;
				}
				for (BigInteger seq = startSeq; seq.compareTo(endSeq) < 0; seq = seq.add(BigInteger.ONE)) {
					EventKeyPair pair = new EventKeyPair(this.shardId, creatorId, seq.longValue());
					byte[] evt = rocksJavaUtil.get(pair.toString());
					if (null != evt && evt.length > 0) {
						list.add(JSONObject.parseObject(new String(evt), EventBody.class));
					}
				}

			}
			if (n == this.n) {
				flag = false;
			}
			logger.warn("node-({}, {}): getAfterEvent4DB() : lastseqs: {}, n = {}, page = {}, event size: {}",
					this.shardId, this.selfId, JSONArray.toJSONString(lastSeqs), this.n, page, list.size());
			return list;
		}
	}
}

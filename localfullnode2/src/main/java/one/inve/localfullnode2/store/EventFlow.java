package one.inve.localfullnode2.store;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;

import one.inve.localfullnode2.utilities.Cryptos;
import one.inve.localfullnode2.utilities.Hash;
import one.inve.localfullnode2.utilities.HnKeyUtils;
import one.inve.localfullnode2.utilities.Utilities;
import one.inve.utils.DSA;

public class EventFlow {
	private static final Logger logger = LoggerFactory.getLogger(EventFlow.class);

	private final EventStore eventStore;
	private final PublicKey[][] pubKeys;
	private final PrivateKey privKey;

	private ConcurrentHashMap<Integer, LinkedBlockingQueue<EventBody>> eventFlowQueues;

	public PublicKey[][] getPubKeys() {
		return pubKeys;
	}

	public EventFlow(PublicKey[][] pubKeys, PrivateKey privKey, EventStore eventStore) {
		this.eventStore = eventStore;
		this.pubKeys = pubKeys;
		this.privKey = privKey;
		this.eventFlowQueues = new ConcurrentHashMap<>();
		for (int i = 0; i < this.pubKeys.length; i++) {
			this.eventFlowQueues.put(i, new LinkedBlockingQueue<>());
		}
	}

	public EventBody[] getAllQueuedEvents(int shardId) {
		ArrayList<EventBody> retCollection = new ArrayList<>();
		this.eventFlowQueues.get(shardId).drainTo(retCollection);
		return retCollection.toArray(new EventBody[0]);
	}

	EventBody getQueuedEventBlocking(int shardId) {
		return this.eventFlowQueues.get(shardId).poll();
	}

	// newEvent和addEvent不能多线程同时执行，否则会导致两个函数插入的event互相交叉
	// 导致顺序不正确，所以直接使用object的同步
	// 新建Event，签名并添加到存储中
	public synchronized EventBody newEvent(int shardId, long creatorId, long otherId, byte[][] trans) {
		EventBody eb = new EventBody();
		eb.setShardId(shardId);
		eb.setCreatorId(creatorId);
		eb.setOtherId(otherId);
		eb.setCreatorSeq(this.eventStore.getLastSeq(shardId, creatorId) + 1);
		eb.setOtherSeq(this.eventStore.getLastSeq(shardId, otherId));

		EventBody selfParent = (eb.getCreatorSeq() <= 0) ? null
				: eventStore.getEvent(eb.getShardId(), eb.getCreatorId(), eb.getCreatorSeq() - 1);
		EventBody otherParent = (eb.getOtherId() < 0 && eb.getOtherSeq() < 0) ? null
				: eventStore.getEvent(eb.getShardId(), eb.getOtherId(), eb.getOtherSeq());

		if (selfParent == null && eb.getCreatorSeq() > 1) {
			logger.error("New event self parent missing");
		}

		if (trans != null && trans.length == 0) {
			logger.error("New event Trans length 0, not null");
			trans = null;
		}

		// 创建时间为当前时间
		Instant timeCreated = Instant.now();
		long n = (selfParent != null && selfParent.getTrans() != null) ? (long) selfParent.getTrans().length : 1L;
		Instant nextTime = (selfParent == null) ? null : selfParent.getTimeCreated().plusNanos(n);
		// 要确保建立的Event，其建立时间在selfParent的建立时间之后
		if (selfParent != null && timeCreated.isBefore(nextTime)) {
			timeCreated = nextTime;
		}
		eb.setTimeCreated(timeCreated);

		eb.setGeneration(1L + (Math.max((selfParent == null) ? -1L : selfParent.getGeneration(),
				(otherParent == null) ? -1L : otherParent.getGeneration())));

		byte[] selfParentHash = (selfParent == null) ? null : selfParent.getHash();
		byte[] otherParentHash = (otherParent == null) ? null : otherParent.getHash();

		eb.setTrans((trans == null) ? null : Utilities.deepClone(trans));
		eb.setHash(Hash.hash(eb.getShardId(), eb.getCreatorId(), eb.getCreatorSeq(), selfParentHash, otherParentHash,
				eb.getTimeCreated(), trans));
		eb.setSignature(Cryptos.sign(eb.getHash(), this.privKey));
		try {
			// Francis.Deng 4/2/2019
			// diagnosing system problem - tracking vivid messages(DSPTVM)
			if (eb.getTrans() != null) {
				logger.info("DSPTVM - eb ({}) with txs is gonna to been put into eventFlowQueue using by hashnet ",
						JSONObject.toJSONString(eb));
			}

			this.eventFlowQueues.get(shardId).put(eb);
		} catch (Exception e) {
			logger.error("put into eventFlowQueue error: {}", e);
		}
		// 同时存储
		eventStore.addEvent(eb);
		return eb;
	}

	public synchronized void addEvent2Store(EventBody eb) {
		this.eventStore.addEvent(eb);
	}

	/**
	 * 添加通过gossip得到的外部Event，验证签名以后添加到存储
	 * 
	 * @param eb 事件
	 * @return 成功还是失败
	 */
	public synchronized Map<String, String> addEvent(EventBody eb) {
		Map<String, String> resultMap = new HashMap<>();
		if (eb.getTrans() != null && eb.getTrans().length == 0) {
			logger.error("Add event trans lengh 0, not null");
			eb.setTrans(null);
		}
//        if (this.eventStore.getEvent(eb.creatorId, eb.creatorSeq) != null) {
		if (this.eventStore.getLastSeq(eb.getShardId(), eb.getCreatorId()) >= eb.getCreatorSeq()) {
			// 如果本地已经存在给定creatorid和seq的Event，则返回false
			// 这是很可能发生的，因为网络上可能有两个节点同时建一个本地不存在的
			// Event发送过来了
//            logger.error(">>>>>> failed to instatiate add Event because ("+eb.creatorId+", "+eb.creatorSeq+") already exist");
			resultMap.put("result", "exist");
			return resultMap;
		}

		EventBody selfParent = this.eventStore.getEvent(eb.getShardId(), eb.getCreatorId(), eb.getCreatorSeq() - 1);
		EventBody otherParent = this.eventStore.getEvent(eb.getShardId(), eb.getOtherId(), eb.getOtherSeq());

		if (selfParent == null && eb.getCreatorSeq() - 1L != -1L) {
			logger.error(">>>>>> failed to instatiate add Event because selfParent (id,seq) of (" + eb.getCreatorId()
					+ ", " + (eb.getCreatorSeq()) + ") is missing");
			resultMap.put("result", "selfMissing");
			return resultMap;
		}
		if (otherParent == null && eb.getOtherId() != -1L && eb.getOtherSeq() != -1L) {
			logger.error(">>>>>> failed to instatiate add Event because otherParent (id, seq) of (" + eb.getOtherId()
					+ ", " + eb.getOtherSeq() + ") is missing ");
			resultMap.put("result", "otherMissing");
			return resultMap;
		}
		// 如果创建时间比自己的父节点还早，则仍然不合法
		if (selfParent != null && !eb.getTimeCreated().isAfter(selfParent.getTimeCreated())) {
			logger.error(">>>>>> failed to instatiate add Event because (id, seq) of (" + eb.getCreatorId() + ", "
					+ eb.getCreatorSeq() + ")  timecreated is before than its parent ");
			resultMap.put("result", "timeCreatedErr");
			return resultMap;
		}
		// 本地数据
		byte[] selfParentHash = selfParent == null ? null : selfParent.getHash();
		// 本地数据
		byte[] otherParentHash = otherParent == null ? null : otherParent.getHash();
		// 本地数据
		byte[] selfHash = Hash.hash(eb.getShardId(), eb.getCreatorId(), eb.getCreatorSeq(), selfParentHash,
				otherParentHash, eb.getTimeCreated(), eb.getTrans());
		// hash验证是否被篡改

		// eb.hash = selfHash;
		// 验证签名
		PublicKey publicKey = this.pubKeys[eb.getShardId()][(int) eb.getCreatorId()];

		// 调用线程池验证签名合法性，完成以后设置签名合法标志并继续调用addEventForCons
		if (!Cryptos.verifySignature(selfHash, eb.getSignature(), publicKey)) {
			logger.warn("Warning: the system suffered from split attack due to unmatched event's signature");

			String localSelfPaHash = null == selfParentHash ? null : DSA.encryptBASE64(selfParentHash);
			String localOtherPaHash = null == otherParentHash ? null : DSA.encryptBASE64(otherParentHash);
			String ebSelfPaHash = null == eb.getParentHash() ? null : DSA.encryptBASE64(eb.getParentHash());
			String ebOtherPaHash = null == eb.getOtherHash() ? null : DSA.encryptBASE64(eb.getOtherHash());
			String timeCreatedStr = eb.getTimeCreated() == null ? null
					: eb.getTimeCreated().getEpochSecond() + "." + eb.getTimeCreated().getNano();
			String hash1 = DSA.encryptBASE64(Hash.hash(eb.getShardId(), eb.getCreatorId(), eb.getCreatorSeq(),
					selfParentHash, eb.getOtherHash(), eb.getTimeCreated(), eb.getTrans()));
			String hash2 = DSA.encryptBASE64(Hash.hash(eb.getShardId(), eb.getCreatorId(), eb.getCreatorSeq(),
					eb.getParentHash(), otherParentHash, eb.getTimeCreated(), eb.getTrans()));
			logger.error(
					"\neb.creatorId: {}\neb.creatorSeq: {}\nlocalselfParentHash: {}\nlocalotherParentHash: {}\n"
							+ "eb.getOtherHash(): {}\neb.getParentHash(): {}\neb.timeCreated: {}\neb.trans: {}\n"
							+ "hash(replace selfParentHash): {}\nhash(replace otherParentHash): {}\neb.hash: {}\n",
					eb.getCreatorId(), eb.getCreatorSeq(), localSelfPaHash, localOtherPaHash, ebOtherPaHash,
					ebSelfPaHash, timeCreatedStr, (eb.getTrans()), hash1, hash2, DSA.encryptBASE64(eb.getHash()));

			if (Cryptos.verifySignature(eb.getHash(), eb.getSignature(), publicKey)) {
				if (DSA.encryptBASE64(eb.getHash()).equals(hash1)
						&& !DSA.encryptBASE64(otherParentHash).equals(DSA.encryptBASE64(eb.getOtherHash()))) {
					resultMap.put("splitType", "otherChild");
				} else if (DSA.encryptBASE64(eb.getHash()).equals(hash2)
						&& !DSA.encryptBASE64(selfParentHash).equals(DSA.encryptBASE64(eb.getParentHash()))) {
					resultMap.put("splitType", "selfChild");
				} else {
					logger.error(
							">>>>>> failed to instatiate add Event because (id, seq) of ({},{}) invalid signature ",
							eb.getCreatorId(), eb.getCreatorSeq());
					logger.error("{}===={}===={}", DSA.encryptBASE64(eb.getHash()),
							DSA.encryptBASE64(eb.getSignature()), HnKeyUtils.getString4PublicKey(publicKey));
					resultMap.put("result", "signatureErr");
					return resultMap;
				}
				logger.error(">>>>>> failed to instatiate add Event because (id, seq) of ({},{}) invalid hash ",
						eb.getCreatorId(), eb.getCreatorSeq());
				resultMap.put("result", "hashErr");
				return resultMap;
			} else {
				logger.error(">>>>>> failed to instatiate add Event because (id, seq) of ({},{}) invalid signature ",
						eb.getCreatorId(), eb.getCreatorSeq());
				logger.error("{}===={}===={}", DSA.encryptBASE64(eb.getHash()), DSA.encryptBASE64(eb.getSignature()),
						HnKeyUtils.getString4PublicKey(publicKey));
				resultMap.put("result", "signatureErr");
				return resultMap;
			}
		}

		long newSeq = this.eventStore.getLastSeq(eb.getShardId(), eb.getCreatorId()) + 1L;
		if (newSeq != eb.getCreatorSeq()) {
			logger.error(">>>>>> failed to instatiate add Event because otherParent (id, seq) of (" + eb.getOtherId()
					+ ", " + eb.getCreatorSeq() + ") invalid seq");
			logger.error("newSeq:{}   eb.getCreatorSeq():{}", newSeq, eb.getCreatorSeq());
			resultMap.put("result", "seqErr");
			return resultMap;
		}
		try {
			this.eventFlowQueues.get(eb.getShardId()).put(eb);
		} catch (Exception e) {
			logger.error("error: {}", e);
		}

		this.eventStore.addEvent(eb);
		resultMap.put("result", "true");
		return resultMap;
	}

	public synchronized boolean checkSplitEvent(EventBody eb1, byte[] otherHash1, EventBody eb2, byte[] otherHash2,
			byte[] parentHash) {
		if (eb1 == null || otherHash1 == null || eb2 == null || otherHash2 == null || parentHash == null
				|| eb1.getCreatorId() != eb2.getCreatorId() || eb1.getCreatorSeq() != eb2.getCreatorSeq()) {
			return false;
		}
		byte[] selfHash1 = Hash.hash(eb1.getShardId(), eb1.getCreatorId(), eb1.getCreatorSeq(), parentHash, otherHash1,
				eb1.getTimeCreated(), eb1.getTrans());
		byte[] selfHash2 = Hash.hash(eb2.getShardId(), eb2.getCreatorId(), eb1.getCreatorSeq(), parentHash, otherHash2,
				eb2.getTimeCreated(), eb2.getTrans());
		PublicKey publicKey = this.pubKeys[eb1.getShardId()][(int) eb1.getCreatorId()];
		boolean flag1 = Cryptos.verifySignature(selfHash1, eb1.getSignature(), publicKey);
		boolean flag2 = Cryptos.verifySignature(selfHash2, eb2.getSignature(), publicKey);
		if (flag1 && flag2) {
			return true;
		} else {
			return false;
		}
	}

	public synchronized void initSnapEvent(List<EventBody> snapEvents, List<EventBody> cacheEvents) {
		for (EventBody eb : snapEvents) {
			if (eb != null) {
				eb.setCreatorSeq(0L);
				eb.setOtherId(-1L);
				try {
					this.eventFlowQueues.get(eb.getShardId()).put(eb);
				} catch (Exception e) {
					logger.error("error: {}", e);
				}
				this.eventStore.addEvent(eb);
			}
		}
		for (EventBody eb : cacheEvents) {
			if (eb != null) {
				try {
					this.eventFlowQueues.get(eb.getShardId()).put(eb);
				} catch (Exception e) {
					logger.error("error: {}", e);
				}
				this.eventStore.addEvent(eb);
			}
		}
	}

}

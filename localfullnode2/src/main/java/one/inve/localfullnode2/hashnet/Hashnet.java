package one.inve.localfullnode2.hashnet;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicLong;

import one.inve.cfg.localfullnode.Config;
import one.inve.core.EventBody;
import one.inve.localfullnode2.store.EventKeyPair;
import one.inve.localfullnode2.utilities.Utilities;

public class Hashnet {
	private final int numNodes;
	private final ConcurrentHashMap<Integer, AtomicLong> numConsensuses;

	private final ConcurrentHashMap<Integer, ConcurrentHashMap<Long, RoundInfo>> rounds;
	private final ConcurrentHashMap<Integer, AtomicLong> roundToDeletes;
	private final ConcurrentHashMap<Integer, AtomicLong> fameDecidedBelows;
	private final ConcurrentHashMap<Integer, AtomicLong> maxRounds;
	private final ConcurrentHashMap<Integer, AtomicLong> minRounds;

	private final int shardCount;
	private final ConcurrentHashMap<EventKeyPair, Event> eventsByKeypair;
	private final ConcurrentHashMap<Integer, LinkedBlockingQueue<Event>> consEventsQueues;

	public Hashnet(int shardCount, int numNodes) {
		this.numNodes = numNodes;
		this.numConsensuses = new ConcurrentHashMap<>();
		this.rounds = new ConcurrentHashMap<>();
		this.roundToDeletes = new ConcurrentHashMap<>();
		this.fameDecidedBelows = new ConcurrentHashMap<>();
		this.maxRounds = new ConcurrentHashMap<>();
		this.minRounds = new ConcurrentHashMap<>();
		this.eventsByKeypair = new ConcurrentHashMap<>();
		this.consEventsQueues = new ConcurrentHashMap<>();
		this.shardCount = shardCount;
		for (int i = 0; i < this.shardCount; i++) {
			this.consEventsQueues.put(i, new LinkedBlockingQueue<>());
			this.roundToDeletes.put(i, new AtomicLong(-1L));
			this.fameDecidedBelows.put(i, new AtomicLong(0L));
			this.maxRounds.put(i, new AtomicLong(-1L));
			this.minRounds.put(i, new AtomicLong(-1L));
			this.rounds.put(i, new ConcurrentHashMap<>());
			this.numConsensuses.put(i, new AtomicLong(0L));
		}
	}

	public void initBlockQueue(int shardId) {
		this.consEventsQueues.get(shardId).clear();
	}

	/**
	 * 获取所有已经达成共识的Event，获取到的Event会按照共识顺序排列
	 * 
	 * @return
	 */
	public Event[] getAllConsEvents(int shardId) {
		ArrayList<Event> retCollection = new ArrayList<>();
		this.consEventsQueues.get(shardId).drainTo(retCollection);
		return retCollection.toArray(new Event[0]);
	}

	public Event[] getAllEvents(int shardId) {
		ArrayList<Event> all = new ArrayList<>();
		long r = this.minRounds.get(shardId).get();
		long k = this.minRounds.get(shardId).get();
		for (; r <= k; ++r) {
			RoundInfo info = this.rounds.get(shardId).get(r);
			if (info != null) {
				all.addAll(info.allEvents);
			}
		}
		return all.toArray(new Event[0]);
	}

	/**
	 * 获取下一个达成共识的Event，如果没有则会堵塞等待
	 * 
	 * @return
	 */
	public Event getConsEventsBlocking(int shardId) {
		return this.consEventsQueues.get(shardId).poll();
	}

	public Event getEventByCreatorSeq(int shardId, long creatorId, long seq) {
		EventKeyPair pair = new EventKeyPair(shardId, creatorId, seq);
		return this.eventsByKeypair.get(pair);
	}

	long getLastRoundDecided(int shardId) {
		return this.fameDecidedBelows.get(shardId).get();
	}

	long getMinRound(int shardId) {
		return this.minRounds.get(shardId).get();
	}

	long getMaxRound(int shardId) {
		return this.maxRounds.get(shardId).get();
	}

	/**
	 * 由外部调用，说明到给定round已经可以删除了 hashnet内部还会判断，确保没有未达成共识Event才能删除
	 * 
	 * @param round
	 */
	void canDeleteRound(int shardId, long round) {
		this.roundToDeletes.get(shardId).set(Math.max(this.roundToDeletes.get(shardId).get(), round));
	}

	long getDeleteRound(int shardId) {
		return this.roundToDeletes.get(shardId).get();
	}

	/**
	 * 向hashnet中增加一个event
	 * 
	 * @param event event
	 */
	protected void consRecordEvent(Event event) {
		int numMembers = this.numNodes;
		// 初始化给定event快速算法变量
		this.consSetFastVars(event, numMembers);
		// 更新给定事件所有祖先节点的firstseq值
		this.consSetAncestorFirstEvent(event);
		// 检查和统计event可以stronglySeen的所有witness,并保存在stronlySeen中
		// 如果超过2/3，则创建新的round
		ArrayList<Event> stronglySeen = new ArrayList<>();
		RoundInfo roundInfo = this.consSetRoundCreated(event, stronglySeen);
		// 检查并设置是否为witness
		this.consSetIsWitness(event, roundInfo);
		// 基于前面得到的stronglySeen列表，更新roundInfo内的投票信息
		this.consVote(event, roundInfo, stronglySeen);
		roundInfo.allEvents.add(event);
		roundInfo.nonConsensusEvents.add(event);
	}

	/**
	 * 初始化支持快速hashnet算法的变量，主要是first-seq和last-seq的增量构建
	 * 
	 * @param event
	 * @param numMembers
	 */
	private void consSetFastVars(Event event, int numMembers) {
		if (!event.isCleared()) {
			event.isWitness = false;
			event.isFameDecided = false;
			event.isFamous = false;
			event.isConsensus = false;
			// first表示各个creator可以看到本event的最大的seq值，初始时
			// 为最大整数，因为实际上没有event可以看到本event
			event.firstEvents = new Event[numMembers];
			event.firstSeqs = new int[numMembers];
			Arrays.fill(event.firstSeqs, Integer.MAX_VALUE);
			// last表示本event可以看到的各个creator的最大seq值
			// 为两个父节点的大者
			int i;
			if (event.getSelfParent() == null && event.getOtherParent() == null) {
				// 如果2个父节点都为null，则last都为-1
				event.lastEvents = new Event[numMembers];
				event.lastSeqs = new int[numMembers];
				Arrays.fill(event.lastSeqs, -1);
			} else if (event.getSelfParent() == null) {
				// 如果sellParent为空,则使用不空的otherParent
				event.lastEvents = event.getOtherParent().lastEvents.clone();
				event.lastSeqs = event.getOtherParent().lastSeqs.clone();
			} else if (event.getOtherParent() == null) {
				// 如果othterParent为空，则使用不空的selfParent
				event.lastEvents = event.getSelfParent().lastEvents.clone();
				event.lastSeqs = event.getSelfParent().lastSeqs.clone();
			} else {
				// 都不空，则使用两者的大值
				event.lastEvents = event.getSelfParent().lastEvents.clone();
				event.lastSeqs = event.getSelfParent().lastSeqs.clone();

				for (i = 0; i < numMembers; ++i) {
					if (event.lastSeqs[i] < event.getOtherParent().lastSeqs[i]) {
						event.lastSeqs[i] = event.getOtherParent().lastSeqs[i];
						event.lastEvents[i] = event.getOtherParent().lastEvents[i];
					}
				}
			}

			i = (int) event.getCreatorId();
			int mySeq = (int) event.getCreatorSeq();
			event.lastSeqs[i] = mySeq;
			event.lastEvents[i] = event;
			event.firstSeqs[i] = mySeq;
			event.firstEvents[i] = event;
		}
	}

	// 更新指定事件所有祖先节点的firstseq值，由于本事件增加以后
	// 所有祖先event的firstseq对应本event所属creator id位置处的
	// 值会发生变化，所以需要更新，全部更新为本event的seq值
	private void consSetAncestorFirstEvent(Event event) {
		if (!event.isCleared()) {
			int myId = (int) event.getCreatorId();
			int mySeq = (int) event.getCreatorSeq();

			for (int i = 0; i < event.lastEvents.length; ++i) {
				for (Event x = event.lastEvents[i]; x != null && x.firstEvents != null
						&& x.firstEvents[myId] == null; x = x.getSelfParent()) {
					x.firstSeqs[myId] = mySeq;
					x.firstEvents[myId] = event;
				}
			}
		}
	}

	// 检查指定event所有可以stronglySeen的witness，并保存在stronlySeen中
	// 如果超过2/3，创建新的round
	private RoundInfo consSetRoundCreated(Event event, ArrayList<Event> stronglySeen) {
		int numMembers = this.numNodes;
		long selfParentRound = event.getSelfParent() == null ? 0L : event.getSelfParent().roundCreated;
		long otherParentRound = event.getOtherParent() == null ? 0L : event.getOtherParent().roundCreated;
		// 当前event所属round的最小值为自身父节点和另一个父节点的更大者
		event.roundCreated = Math.max(selfParentRound, otherParentRound);
		// 获取roundinfo
		RoundInfo roundInfo = this.consGetOrCreateRoundInfo(event.getShardId(), event.roundCreated);

		// 统计所有当前event可以stronglySeen的本round witness
		Iterator<Event> iter = roundInfo.witnesses.iterator();
		while (iter.hasNext()) {
			Event e = iter.next();
			if ((double) this.consDotProduct(e.firstSeqs, event.lastSeqs) < (double) numMembers / 3.0D) {
				stronglySeen.add(e);
			}
		}

		// 如果可以stronglySeen的witness数量超过2/3，则创建新的round
		if ((double) stronglySeen.size() > 2.0D * (double) numMembers / 3.0D) {
			++event.roundCreated;
			// 创建新的round
			roundInfo = this.consGetOrCreateRoundInfo(event.getShardId(), event.roundCreated);
		}

		return roundInfo;
	}

	// 设置指定event是否为witness
	private void consSetIsWitness(Event event, RoundInfo roundInfo) {
		// 如果witness和其自身父亲在同一个round内，则必然不是witness（因为每一轮的witness Event即这轮每个柱子上的第一个Event）
		if (event.getSelfParent() != null && event.getRoundCreated() == event.getSelfParent().getRoundCreated()) {
			event.isWitness = false;
		} else {
			// 否则必然是witness，每个creator中，第一个与其父节点round不同的
			// event都是witness
			event.isWitness = true;
			roundInfo.witnesses.add(event);
			++roundInfo.numWitnesses;
			// 如果age大2的round已经存在，则该event的famous必然为false
			// 因为已经没有办法给它投票了
			if (this.rounds.get(event.getShardId()).get(event.roundCreated + 2L) != null) {
				this.consSetFamous(event, roundInfo, false);
			} else {
				// 否则在age+1的round中新建针对该event的投票
				// 针对一个witness的投票建立有两个时机：
				// 1 添加该witness时，如果Round+1已经存在，则在Round+1内部建立
				// 2 如果Round+1不存在，则在Round+1建立时，再行建立
				++roundInfo.numUnknownFame;
				RoundInfo nextRound = this.rounds.get(event.getShardId()).get(roundInfo.round + 1L);
				if (nextRound != null) {
					this.consNewElection(event, nextRound, null);
				}
			}
		}
	}

	/**
	 * 使用指定event对其所有可以stronglySeen的witness进行投票 即更新election内的vote值
	 * 
	 * @param event
	 * @param roundInfo
	 * @param stronglySeen
	 */
	private void consVote(Event event, RoundInfo roundInfo, ArrayList<Event> stronglySeen) {
		// 自己必须是witness才可以投票
		if (event.isWitness) {
			int myId = (int) event.getCreatorId();

			// 更新round内所有elections指定event对应的creatorid的值
			for (RoundInfo.ElectionRound e = roundInfo.elections; e != null; e = e.nextElection) {
				long numMembers = this.numNodes;
				if (e.age == 1L) {
					// 如果age为1，则可以看到待投票的witness即为真
					e.vote[myId] = (long) e.event.firstSeqs[myId] <= event.getCreatorSeq();
				} else {
					// 否则统计给定event所有可以stronglySeen的witness针对该event的投票
					long yesCount = 0L;
					long noCount = 0L;
					Iterator<Event> iter = stronglySeen.iterator();

					while (iter.hasNext()) {
						Event w = iter.next();
						// 使用该witness上一轮的投票
						if (e.prevRound.vote[(int) w.getCreatorId()]) {
							++yesCount;
						} else {
							++noCount;
						}
					}

					// 计算是否绝大多数同意或是否认
					boolean superMajority = (double) yesCount > 2.0D * (double) numMembers / 3.0D
							|| (double) noCount > 2.0D * (double) numMembers / 3.0D;
					// 更新本轮投票结果
					e.vote[myId] = yesCount >= noCount;
					// 如果是coin round，
					if (e.age % (long) Config.coinFreq == 0L) {
						// 如果没有形成绝大多数意见，则2倍次设置为真，一倍次随机投票
						// 也就是如果犹豫2倍coinFreq round数，则必然接受
						if (!superMajority) {
							if (e.age % (long) (2 * Config.coinFreq) == (long) Config.coinFreq) {
								e.vote[myId] = true;
							} else {
								e.vote[myId] = (event.getSignature()[event.getSignature().length / 2] & 1) == 1;
							}
						}
					} else if (superMajority) {
						// 否则如果形成绝大多数意见，则设置fame属性
						this.consSetFamous(e.event, this.rounds.get(event.getShardId()).get(e.event.roundCreated),
								e.vote[myId]);
					}
				}
			}
		}
	}

	// 获取或建立给定round对象
	private RoundInfo consGetOrCreateRoundInfo(int shardId, long round) {
		RoundInfo roundInfo = this.rounds.get(shardId).get(round);
		if (roundInfo != null) {
			return roundInfo;
		} else {
			// 如果当前不存在则建立，并添加到map中
			roundInfo = new RoundInfo(round);
			this.rounds.get(shardId).put(round, roundInfo);
			this.maxRounds.get(shardId).set(Math.max(this.maxRounds.get(shardId).get(), round));
			// 这里需要注意的是新建的round，可能比minRound还小
			// 这也是引发missing的主要情况
			// 例如节点0
			// if(this.minRound > round){
			// log.fatal("error {}, {}, {}", this.selfId, this.minRound, round);
			// }
			this.minRounds.get(shardId).set(this.minRounds.get(shardId).get() == -1L ? round
					: Math.min(this.minRounds.get(shardId).get(), round));
			// 第一个round的election是空的
			// 从上一个round中拷贝关于famours witness选举的数据
			RoundInfo oldRoundInfo = this.rounds.get(shardId).get(round - 1L);
			if (oldRoundInfo != null) {
				// 对round内部所有的election在新的roundInfo内
				// 构建新一个round的election
				// 这些election都是在上一个Round没有达成绝大多数的
				for (RoundInfo.ElectionRound e = oldRoundInfo.elections; e != null; e = e.nextElection) {
					// 以上一轮所有投票为prevRound建立新的投票
					this.consNewElection(e.event, roundInfo, e);
				}

				// 为上一个round所有wintness在本Round建立election
				Iterator<Event> iter = oldRoundInfo.witnesses.iterator();
				while (iter.hasNext()) {
					Event witness = iter.next();
					// 所有针对给定event新建的election都没有前序round节点
					this.consNewElection(witness, roundInfo, null);
				}
			}

			return roundInfo;
		}
	}

	// 针对指定event建立关于fame的投票
	// 通过调整链表结构来加入到投票链表中
	private RoundInfo.ElectionRound consNewElection(Event witness, RoundInfo roundInfo,
			RoundInfo.ElectionRound prevRound) {
		RoundInfo.ElectionRound election = new RoundInfo.ElectionRound(roundInfo, this.numNodes, witness,
				roundInfo.round - witness.roundCreated);
		election.nextRound = null;
		election.prevRound = prevRound;
		election.prevElection = null;
		election.nextElection = roundInfo.elections;
		if (witness.firstElection == null) {
			// witness的firstElection引用针对该witness进行投票的
			// 第一个election
			witness.firstElection = election;
		}

		if (prevRound != null) {
			prevRound.nextRound = election;
		}

		if (roundInfo.elections != null) {
			roundInfo.elections.prevElection = election;
		}

		// roundInfo的elections通过nextElection引用
		// 属于该Round的所有投票
		roundInfo.elections = election;
		return election;
	}

	// 计算first-seq和last-seq的内积，用来检查是否可以stronglySeen
	private int consDotProduct(int[] x, int[] y) {
		int n = 0;

		// 如果x是witness的first，y是event的last，x>y时，event无法看到
		// witness，否则可以看到，所以如果总数求和的值小于1/3则表示可以
		// stronglySeen
		for (int i = 0; i < x.length; ++i) {
			if (x[i] > y[i]) {
				++n;
			}
		}

		return n;
	}

	// 给定Fame没有Fame不确定的Witness以后，
	// 将给定Round的faceDecided设为true
	// 向后寻找所有Fame决定的Round，并逐个Round寻找可以接收的Event
	// 最后删除可以删除的Round
	// 这里值得思考的是，为什么要向后搜索，如果后续的round fame确定了
	// 为什么不是早就处理完了，并且faceDeciedBelow应该也不能变小
	private void consSetRoundFameDecidedTrue(int shardId, RoundInfo roundInfo) {
		roundInfo.fameDecided = true;

		for (long round = roundInfo.round; this.fameDecidedBelows.get(shardId).get() == round
				&& roundInfo.fameDecided; roundInfo = this.rounds.get(shardId).get(round)) {
			this.consFindReceivedInRound(shardId, roundInfo);
			++round;
			this.fameDecidedBelows.get(shardId).set(round);
		}

		this.consDelRounds(shardId);
	}

	// 根据给定Round内famours witness寻找所有接收的Event
	private void consFindReceivedInRound(int shardId, RoundInfo roundInfo) {
		List<Event> famous = roundInfo.famousWitnesses;

		for (int i = 0; i < roundInfo.whitening.length; ++i) {
			roundInfo.whitening[i] = 0;
		}

		Iterator<Event> iter = famous.iterator();

		while (iter.hasNext()) {
			Event e = iter.next();
			int mn = Math.min(roundInfo.whitening.length, e.getSignature().length);

			for (int i = 0; i < mn; ++i) {
				roundInfo.whitening[i] ^= e.getSignature()[i];
			}
		}

		byte[] whitening = roundInfo.whitening;
		ArrayList consensus = new ArrayList();

		// 在给定Round之前的所有Round内寻找
		long min = this.minRounds.get(shardId).get();
		for (long r = roundInfo.round - 1L; r >= min; --r) {
			ArrayList cons1 = new ArrayList();
			// 此处需要判断获取的roundinfo是否为空
			// 因为不一定从minRound到maxRound中间所有round都存在
			// 不存在的跳过就可以了
			RoundInfo tr = this.rounds.get(shardId).get(r);
			if (tr == null) {
				continue;
			}
			Iterator<Event> consIter = tr.nonConsensusEvents.iterator();

			// 对于该Round内所有未达成共识Event
			while (consIter.hasNext()) {
				Event e1 = consIter.next();
				int c = 0;
				Iterator<Event> famousIter = famous.iterator();

				// 遍历给定Round内的所有Famous witness
				while (famousIter.hasNext()) {
					Event e2 = famousIter.next();
					if ((long) e1.firstSeqs[(int) e2.getCreatorId()] <= e2.getCreatorSeq()) {
						++c;
					}
				}

				// 如果所有famous witness都可以看到该Event，则该Event被接收
				if (c >= famous.size()) {
					// 设置给定Event被接收，并计算共识时戳以供后续排序
					this.consSetIsConsensusTrue(e1, roundInfo);
					cons1.add(e1);
				}
			}

			consensus.addAll(cons1);
			this.rounds.get(shardId).get(r).nonConsensusEvents.removeAll(cons1);
		}

		// 对所有达成共识的event进行排序
		Collections.sort(consensus, (Event e1x, Event e2x) -> {
			// 首先使用共识时戳进行排序
			int c = e1x.getConsensusTimestamp().compareTo(e2x.getConsensusTimestamp());
			if (c != 0) {
				return c;
			} else {
				// 然后是共识Round和签名来排序
				c = (int) (e1x.generation - e2x.generation);
				return c != 0 ? c : Utilities.arrayCompare(e1x.getSignature(), e2x.getSignature(), whitening);
			}
		});

		// 设置Event的共识顺序
		this.consSetConsensusOrder(shardId, consensus);

//        logger.info("consEventsQueue size: " + consEventsQueue.size());
		// 使用同步接口来一次性添加
		Collection<Event> syncEventsQueue = Collections.synchronizedCollection(this.consEventsQueues.get(shardId));
		syncEventsQueue.addAll(consensus);
	}

	// 设置Event共识顺序，前面已经排过序，所以这里只需要递增即可
	private void consSetConsensusOrder(int shardId, Collection events) {
		long i = this.numConsensuses.get(shardId).get();
		Iterator iter = events.iterator();

		while (iter.hasNext()) {
			Event e = (Event) iter.next();
			// 设置共识顺序
			e.setConsensusOrder(i);
			++i;
		}

		this.numConsensuses.get(shardId).set(this.numConsensuses.get(shardId).get() + events.size());
	}

	// 设置给定Event达成共识，并计算共识时戳以供后续排序
	private void consSetIsConsensusTrue(Event event, RoundInfo receivedRoundInfo) {
		if (!event.isCleared()) {
			// 记录接收的Round，并设置状态
			event.roundReceived = receivedRoundInfo.round;
			event.isConsensus = true;

			// 遍历所有给定Event接收的Round内所有Famous Witness
			// 并将可以看到给定Event的witness的timeCreated排序
			ArrayList<Instant> times = new ArrayList<>();
			Iterator<Event> iter = receivedRoundInfo.famousWitnesses.iterator();

			while (iter.hasNext()) {
				Event e2 = iter.next();
				if ((long) event.firstSeqs[(int) e2.getCreatorId()] <= e2.getCreatorSeq()) {
					times.add(event.firstEvents[(int) e2.getCreatorId()].getTimeCreated());
				}
			}

			Collections.sort(times);
			// 取中间值为Event的接收时戳
			event.consensusTimestamp = times.get(times.size() / 2);
		}
	}

	// 设置event的famous属性，并更新roundinfo
	private void consSetFamous(Event event, RoundInfo roundInfo, boolean isFamous) {
		event.isFamous = isFamous;
		event.isFameDecided = true;
		if (isFamous) {
			// 增加round内famous数量
			roundInfo.famousWitnesses.add(event);
		}

		// 减少fame不确定的数量
		--roundInfo.numUnknownFame;

		// 删除针对给定event的所有electionRound信息
		// 从Event的第一个election开始，在round方向上纵向遍历所有
		// election，逐个在ROUND内横向上删除，删除的方法就是将
		// prevElection的nextElection指向待删除者的nextElection
		// 将nextElection的prevElection指向待删除者的prevElectioin
		// 就是一次双向链表的元素删除操作
		for (RoundInfo.ElectionRound e = event.firstElection; e != null; e = e.nextRound) {
			if (e.prevElection == null) {
				e.roundInfo.elections = e.nextElection;
			} else {
				e.prevElection.nextElection = e.nextElection;
			}

			if (e.nextElection != null) {
				e.nextElection.prevElection = e.prevElection;
			}
		}

		event.firstElection = null;
		// 如果round内所有witness的fame都已经确定，则设置round
		// face decided
		if (roundInfo.numUnknownFame == 0) {
			this.consSetRoundFameDecidedTrue(event.getShardId(), roundInfo);
		}
	}

	// 删除可以删除掉的Round
	private void consDelRounds(int shardId) {
		// 修改一下，总是删除可以删除的所有Round
		long min = this.minRounds.get(shardId).get();
		long max = this.maxRounds.get(shardId).get();
		for (long r = min; r <= max; ++r) {
			RoundInfo info = this.rounds.get(shardId).get(r);
			if (info != null) {
				// 如果仍然有未达成共识的Event则不能删除
				// 一直删除到仍然没有达成共识的事件
				if (!info.nonConsensusEvents.isEmpty()) {
					break;
				}

				this.rounds.get(shardId).remove(r);
				Iterator<Event> iter = info.allEvents.iterator();

				// 删除Round内所有Event
				while (iter.hasNext()) {
					Event e = iter.next();
					// 此处可以进行clear，因为虽然此时Event可能在共识列表里面
					// 但是clear只是将selfParent,otherParent,lastSeqs,firstSeqs
					// 清空，不影响用户获取其他信息，同时clear可以帮助统计仍然
					// 留在内存的Event总数
					e.clear();
					this.eventsByKeypair.remove(new EventKeyPair(e.getShardId(), e.getCreatorId(), e.getCreatorSeq()));
				}
			}
			this.minRounds.get(shardId).set(r + 1L);
		}
	}

	// 这些参数也是进行序列化必须的参数
	// 同时要增加一个本地生成的generation信息，以便从磁盘加载以后
	// 网络重建
	@Deprecated
	public synchronized boolean addEvent(EventBody eb) {
//        logger.info("this.selfid: "+this.selfId
//                            +", creator: "+eb.creatorId+", "+eb.creatorSeq+ ", generation: "+ eb.generation+"; "
//                            +"other: "+eb.otherId+", "+eb.otherSeq);

		Event selfParent = getEventByCreatorSeq(eb.getShardId(), eb.getCreatorId(), eb.getCreatorSeq() - 1);
		Event otherParent = getEventByCreatorSeq(eb.getShardId(), eb.getOtherId(), eb.getOtherSeq());
		// 两个parent可以为空，前提是这两个parent是因为达成共识才为空，而不是还没有
		// 加入图中，所以仍然是要按顺序加入的，否则会导致parent event无法达成共识
//        if ((selfParent == null && eb.creatorSeq > 0) ) {
//            logger.warn(">>>>>> Add event, selfid " + this.selfId
//                            +", creator: "+eb.creatorId+", "+eb.creatorSeq+"; "
//                            +"other: "+eb.otherId+", "+eb.otherSeq+", missing self parent");
//        }
//        if ((otherParent == null && eb.otherSeq > -1)) {
//            logger.warn(">>>>>> Add event, selfid " + this.selfId
//                            +", creator: "+eb.creatorId+", "+eb.creatorSeq+"; "
//                            +"other: "+eb.otherId+", "+eb.otherSeq+", missing other parent");
//        }

		Event evt = new Event(eb.getShardId(), eb.getCreatorId(), eb.getCreatorSeq(), eb.getOtherId(), eb.getOtherSeq(),
				selfParent, otherParent, eb.getTimeCreated(), eb.getSignature(), eb.getGeneration(), eb.getHash(),
				eb.getTrans());

		// 将Event加入到Map中
		EventKeyPair pair = new EventKeyPair(eb.getShardId(), eb.getCreatorId(), eb.getCreatorSeq());
		this.eventsByKeypair.put(pair, evt);
		// 将Event在net中进行记录，这是共识算法执行的入口
		this.consRecordEvent(evt);
		return true;
	}

	public ConcurrentHashMap<EventKeyPair, Event> eventsByKeypair() {
		return eventsByKeypair();
	}
}

package one.inve.localfullnode2.store;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLongArray;

public class EventStoreInMem implements IEventStore {
    private static final Logger logger = LoggerFactory.getLogger(EventStoreInMem.class);

    private int shardCount;
    private int n;
    private int selfId;
    private ConcurrentHashMap<EventKeyPair, EventBody> eventsByCreatorSeq;

    private final ConcurrentHashMap<Integer, AtomicLongArray> lastSeq = new ConcurrentHashMap<>();

    public EventStoreInMem(int shardCount, int n, int selfId) {
        this.shardCount = shardCount;
        this.n = n;
        this.selfId = selfId;
        for (int i = 0; i < this.shardCount; i++) {
            AtomicLongArray lastSeqs = new AtomicLongArray(this.n);
            for (int j = 0; j < this.n; j++) {
                lastSeqs.set(j, -1);
            }
            this.lastSeq.put(i, lastSeqs);
        }
        this.eventsByCreatorSeq = new ConcurrentHashMap<>();
    }

    @Override
    public EventBody getEventInMem(int shardId, long creatorId, long creatorSeq) {
        EventKeyPair pair = new EventKeyPair(shardId, creatorId, creatorSeq);
        return this.eventsByCreatorSeq.get(pair);
    }

    @Override
    public EventBody getEvent(int shardId, long creatorId, long creatorSeq) {
        EventKeyPair pair = new EventKeyPair(shardId, creatorId, creatorSeq);
        return this.eventsByCreatorSeq.get(pair);
    }

    @Override
    public long getLastSeq(int shardId, long creatorId) {
        if (creatorId >= 0) {
            return this.lastSeq.get(shardId).get((int) creatorId);
        } else {
            return -1;
        }
    }

    @Override
    public void setLastSeq(int shardId, long creatorId, long creatorSeq) {
        this.lastSeq.get(shardId).set((int) creatorId, creatorSeq);
    }

    @Override
    public long[] getLastSeqsByShardId(int shardId) {
        int len = this.lastSeq.get(shardId).length();
        long[] result = new long[len];

        for (int i = 0; i < len; ++i) {
            result[i] = this.lastSeq.get(shardId).get(i);
        }

        return result;
    }


    @Override
    public Iterator<EventBody> genOrderedIterator(int shardId, int n) {
        List<EventBody> c = new ArrayList<EventBody>(eventsByCreatorSeq.values());
        c.sort((e1, e2) -> {
            return (int) (e1.getGeneration() - e2.getGeneration());
        });
        return c.iterator();
    }

    public Iterator<EventBody> consOrderedIterator() {
        List<EventBody> c = new ArrayList<EventBody>(eventsByCreatorSeq.values());
        c.sort(Comparator.comparing(EventBody::getConsTimestamp));
        return c.iterator();
    }

    //添加Event
    @Override
    public void addEvent(EventBody eb) {
        this.eventsByCreatorSeq.put(
                new EventKeyPair(eb.getShardId(), eb.getCreatorId(), eb.getCreatorSeq()), eb);

        this.lastSeq.get(eb.getShardId()).set((int) eb.getCreatorId(), eb.getCreatorSeq());
    }

    @Override
    public void initCache() {

    }

    @Override
    public void delEventInCache(int shardId, long creatorId, long creatorSeq) {

    }
}

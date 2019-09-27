package one.inve.localfullnode2.store;

import java.math.BigInteger;
import java.util.Iterator;
import one.inve.core.EventBody;

public interface IEventStore {
  /**
   * 根据creatorId和creatorSeq获取EventBody
   * @param creatorId
   * @param creatorSeq
   * @return
   */
  public EventBody getEventInMem(int shardId, long creatorId, long creatorSeq);
  /**
   * 根据creatorId和creatorSeq获取EventBody
   * @param creatorId
   * @param creatorSeq
   * @return
   */
  public EventBody getEvent(int shardId, long creatorId, long creatorSeq);
  /**
   * 添加Event
   * @param eb
   */
  public void addEvent(EventBody eb);

  /**
   * 这两个值如果经过一次遍历，则container是可以建立的，
   * 但是考虑未来可能不会在宕机以后遍历所有event，则container
   * 无法得到该信息，所以由event store提供
   * @param creatorId 柱子ID
   * @return 柱子最新seq
   */
  public long getLastSeq(int shardId, long creatorId);

  /**
   * 更新分片节点(shardId,creatorId)的lastseq
   * @param shardId 分片号
   * @param creatorId 节点ID
   */
  public void setLastSeq(int shardId, long creatorId, long creatorSeq);

  /**
   * 获取分片shardId的所有节点的lastSeq
   * @param shardId 分片号
   * @return 分片shardId的所有节点的lastSeq
   */
  public long[] getLastSeqsByShardId(int shardId);

  /**
   * 返回按照generation排序的EventBody迭代器，用于重启恢复时使用，遍历所有Event
   * @return eb迭代器
   */
  Iterator<EventBody> genOrderedIterator(int shardId, int n);

  /**
   * 生成快照后需要初始化缓存
   */
  public void initCache();

    public void  delEventInCache(int shardId, long creatorId, long creatorSeq);

  /**
   * 返回按照generation排序的EventBody迭代器，用于重启恢复时使用，遍历startSeq之后的Event
   * @return eb迭代器
   */
  Iterator<EventBody> genOrderedIterator(int shardId, int n, BigInteger firstSeq);
}

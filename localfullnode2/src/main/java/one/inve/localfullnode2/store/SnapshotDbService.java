package one.inve.localfullnode2.store;


import one.inve.bean.message.SnapshotMessage;
import java.util.Map;

public interface SnapshotDbService {

    /**
     * 查询最新快照消息
     *
     * @param dbId 数据库ID
     * @return 最新快照消息
     */
    public SnapshotMessage queryLatestSnapshotMessage(String dbId);

    /**
     * 查询最新快照消息的hash值
     *
     * @param dbId 数据库ID
     * @return 最新快照消息的hash值
     */
    public String queryLatestSnapshotMessageHash(String dbId);

    /**
     * 根据快照消息hash值，查询对应的快照消息，并以字符串形式返回
     *
     * @param dbId 数据库ID
     * @param hash 快照消息hash值
     * @return 快照消息（以字符串形式返回）
     */
    public String querySnapshotMessageFormatStringByHash(String dbId, String hash);

    /**
     * 根据快照消息hash值，查询对应的快照消息，并以实体对象形式返回
     *
     * @param dbId 数据库ID
     * @param hash 快照消息hash值
     * @return 快照消息（以实体对象形式返回）
     */
    public SnapshotMessage querySnapshotMessageByHash(String dbId, String hash);

    /**
     * 根据快照消息版本号，查询对应的快照消息，并以字符串形式返回
     *
     * @param dbId    数据库ID
     * @param version 快照消息版本号
     * @return 版本{version}快照消息，并以字符串形式返回
     */
    public String querySnapshotMessageFormatStringByVersion(String dbId, String version);

    /**
     * 根据快照消息hash值，查询对应的快照消息，并以实体对象形式返回
     *
     * @param dbId    数据库ID
     * @param version 快照消息版本号
     * @return 版本{version}快照消息（以实体对象形式返回）
     */
    public SnapshotMessage querySnapshotMessageByVersion(String dbId, String version);

    /**
     * 根据快照消息版本号，查询对应的快照消息hash值
     *
     * @param dbId    数据库ID
     * @param version 快照消息版本号
     * @return 版本{version}快照消息hash值
     */
    public String querySnapshotMessageHashByVersion(String dbId, String version);

    /**
     * 删除快照点Event之前的所有Event
     *
     * @param dbId   数据库ID
     * @param eb     快照点Event
     * @param nValue 分片总节点数
     */
    public void deleteEventsBeforeSnapshotPointEvent(String dbId, one.inve.core.EventBody eb, int nValue);


    /**
     * 递归获取每根柱子上离得最近的第一个event
     *
     * @param dbId   数据库ID
     * @param eb
     * @param map
     * @param nValue 分片总节点数
     * @return 每根柱子上离指定event最近的前一个event
     */
    public Map<Long, EventKeyPair> getPrevEventKeyPairsForEachNode(String dbId,
                                                                   one.inve.core.EventBody eb,
                                                                   Map<Long, EventKeyPair> map,
                                                                   int nValue);
}

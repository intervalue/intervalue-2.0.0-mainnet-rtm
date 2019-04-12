package one.inve.node;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import one.inve.bean.node.FullNode;
import one.inve.bean.node.LocalFullNode;
import one.inve.bean.node.NodeTypes;
import one.inve.bean.node.RelayNode;
import one.inve.core.Block;
import one.inve.core.Hash;
import one.inve.util.PathUtils;
import one.inve.util.StringUtils;
import one.inve.utils.DSA;
import org.apache.log4j.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

/**
 * seed中相关数据访问层
 * @author Clarelau61803@gmail.com
 * @date 2018/10/11 0011 上午 10:44
 **/
public class SqliteDAO {
    public final static Logger logger = Logger.getLogger("SqliteDAO.class");

    /**
     * 保存所有全节点
     * @param fullnodes 全节点列表
     * @return 操作结果： true-成功， false-失败
     */
    public static boolean addFullNodeList(String dbFileName, List<FullNode> fullnodes) {
        logger.info("addFullNodeList...");
        clearFullNodeList(dbFileName);

        Connection conn = null;
        Statement stmt = null;
        StringBuilder sql = new StringBuilder();
        try {
            String url = "jdbc:sqlite:" + PathUtils.getDataFileDir() + dbFileName;
            conn = DriverManager.getConnection(url);
            stmt = conn.createStatement();
            for (FullNode fullnode : fullnodes) {
                sql.append(String.format("insert into fullnode (pubkey, ip, rpcPort, httpPort, state) values('%s', '%s', %d, %d, '%s');",
                        fullnode.getPubkey(), fullnode.getIp(), fullnode.getRpcPort(), fullnode.getHttpPort(), fullnode.getStatus()));
            }
            stmt.executeUpdate(sql.toString());
            return true;
        } catch (Exception e) {
            logger.error("addFullNodeList exception", e);
            return false;
        } finally {
            try {
                if (null != stmt) {
                    stmt.close();
                }
            } catch (SQLException e) {
                logger.error("addFullNodeList preparedStatement close error", e);
            }
            try {
                if (null != conn) {
                    conn.close();
                }
            } catch (SQLException e) {
                logger.error("addFullNodeList conn close error", e);
            }
        }
    }

    public static boolean clearFullNodeList(String dbFileName) {
        logger.info("clearFullNodeList...");
        Connection conn = null;
        Statement stmt = null;
        try {
            String url = "jdbc:sqlite:" + PathUtils.getDataFileDir() + dbFileName;
            conn = DriverManager.getConnection(url);
            stmt = conn.createStatement();
            String sql = String.format("delete from fullnode;");
            stmt.executeUpdate(sql);
            return true;
        } catch (Exception e) {
            logger.error("clearFullNodeList exception", e);
            return false;
        } finally {
            try {
                if (null != stmt) {
                    stmt.close();
                }
            } catch (SQLException e) {
                logger.error("clearFullNodeList statement close error", e);
            }
            try {
                if (null != conn) {
                    conn.close();
                }
            } catch (SQLException e) {
                logger.error("clearFullNodeList conn close error", e);
            }
        }
    }

    public static boolean saveFullNode(String dbFileName, FullNode fullnode) {
        try {
            if (existFullNode(dbFileName, fullnode.getPubkey())) {
                updateFullNode(dbFileName, fullnode);
            } else {
                addFullNode(dbFileName, fullnode);
            }
            return true;
        } catch (Exception e) {
            logger.error("", e);
            return false;
        }
    }

    /**
     * 插入新的全节点
     * @param fullnode 新节点信息
     * @return 操作结果： true-成功， false-失败
     */
    public static boolean addFullNode(String dbFileName, FullNode fullnode) {
        logger.info("addFullNode...");
        Connection conn = null;
        Statement stmt = null;
        try {
            String url = "jdbc:sqlite:" + PathUtils.getDataFileDir() + dbFileName;
            conn = DriverManager.getConnection(url);
            stmt = conn.createStatement();
            String sql = String.format("insert into fullnode (pubkey, ip, rpcPort, httpPort, status) values('%s', '%s', %d, %d, %d);",
                    fullnode.getPubkey(), fullnode.getIp(), fullnode.getRpcPort(), fullnode.getHttpPort(), fullnode.getStatus());
            stmt.executeUpdate(sql);
            return true;
        } catch (Exception e) {
            logger.error("addFullNode exception", e);
            return false;
        } finally {
            try {
                if (null != stmt) {
                    stmt.close();
                }
            } catch (SQLException e) {
                logger.error("addFullNode statement close error", e);
            }
            try {
                if (null != conn) {
                    conn.close();
                }
            } catch (SQLException e) {
                logger.error("addFullNode conn close error", e);
            }
        }
    }

    /**
     * 更新全节点
     * @param fullnode 节点信息
     * @return 操作结果： true-成功， false-失败
     */
    public static boolean updateFullNode(String dbFileName, FullNode fullnode) {
        logger.info("updateFullNode...");
        Connection conn = null;
        Statement stmt = null;
        try {
            String url = "jdbc:sqlite:" + PathUtils.getDataFileDir() + dbFileName;
            conn = DriverManager.getConnection(url);
            stmt = conn.createStatement();
            String sql = String.format("update fullnode set ip='%s', rpcPort=%d, httpPort=%d, status=%d, hash='%s' where pubkey='%s';",
                    fullnode.getIp(), fullnode.getRpcPort(), fullnode.getHttpPort(), fullnode.getStatus(),
                    fullnode.getHash(), fullnode.getPubkey());
            stmt.executeUpdate(sql);
            return true;
        } catch (Exception e) {
            logger.error("updateFullNode exception", e);
            return false;
        } finally {
            try {
                if (null != stmt) {
                    stmt.close();
                }
            } catch (SQLException e) {
                logger.error("updateFullNode statement close error", e);
            }
            try {
                if (null != conn) {
                    conn.close();
                }
            } catch (SQLException e) {
                logger.error("updateFullNode conn close error", e);
            }
        }
    }

    /**
     * 从数据库查询所有全节点
     * @param dbFileName 数据库文件名
     * @param pubkey seed全节点公钥，用于判断全节点类型
     * @return 所有全节点
     */
    public static List<FullNode> queryFullNodes(String dbFileName, String pubkey) {
        logger.info("queryFullNodes...");
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        List<FullNode> fullNodes = new ArrayList<>();
        try {
            String url = "jdbc:sqlite:" + PathUtils.getDataFileDir() + dbFileName;
            conn = DriverManager.getConnection(url);
            stmt = conn.createStatement();
            String sql = "select * from fullnode;";
            rs = stmt.executeQuery(sql);
            while (rs.next()) {
                FullNode node = new FullNode.Builder()
                        .pubkey(rs.getString("pubkey"))
                        .ip(rs.getString("ip"))
                        .httpPort(rs.getInt("httpPort"))
                        .rpcPort(rs.getInt("rpcPort"))
                        .status(rs.getInt("status"))
                        .type((rs.getString("pubkey").equals(pubkey)) ? NodeTypes.SEED : NodeTypes.FULLNODE)
                        .hash(rs.getString("hash"))
                        .build();
                fullNodes.add(node);
            }
            return fullNodes;
        } catch (Exception e) {
            logger.error("queryFullNodes exception", e);
            return fullNodes;
        } finally {
            try {
                if (null != rs) {
                    rs.close();
                }
            } catch (SQLException e) {
                logger.error("queryFullNodes resultset close error", e);
            }
            try {
                if (null != stmt) {
                    stmt.close();
                }
            } catch (SQLException e) {
                logger.error("queryFullNodes statement close error", e);
            }
            try {
                if (null != conn) {
                    conn.close();
                }
            } catch (SQLException e) {
                logger.error("queryFullNodes conn close error", e);
            }
        }
    }

    /**
     * 根据pubkey查询全节点
     * @param dbFileName 数据库文件名
     * @param pubkey 全节点公钥
     * @return 全节点信息
     */
    public static FullNode queryFullNode(String dbFileName, String pubkey) {
        logger.info("queryFullNode...");
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            String url = "jdbc:sqlite:" + PathUtils.getDataFileDir() + dbFileName;
            conn = DriverManager.getConnection(url);
            stmt = conn.createStatement();
            String sql = String.format("select * from fullnode where pubkey='%s';", pubkey);
            rs = stmt.executeQuery(sql);
            if (rs.next()) {
                FullNode node = new FullNode.Builder()
                        .pubkey(rs.getString("pubkey"))
                        .ip(rs.getString("ip"))
                        .rpcPort(rs.getInt("rpcPort"))
                        .httpPort(rs.getInt("httpPort"))
                        .status(rs.getInt("status"))
                        .type((rs.getString("pubkey").equals(pubkey)) ? NodeTypes.SEED : NodeTypes.FULLNODE)
                        .hash(rs.getString("hash"))
                        .build();
                return node;
            } else {
                return null;
            }
        } catch (Exception e) {
            logger.error("queryFullNode exception", e);
            return null;
        } finally {
            try {
                if (null != rs) {
                    rs.close();
                }
            } catch (SQLException e) {
                logger.error("queryFullNode resultset close error", e);
            }
            try {
                if (null != stmt) {
                    stmt.close();
                }
            } catch (SQLException e) {
                logger.error("queryFullNode statement close error", e);
            }
            try {
                if (null != conn) {
                    conn.close();
                }
            } catch (SQLException e) {
                logger.error("queryFullNode conn close error", e);
            }
        }
    }

    /**
     * 判断是否已存在这个全节点
     * @param dbFileName 数据库文件名
     * @param pubkey 全节点公钥
     * @return true-是, false-否
     */
    public static boolean existFullNode(String dbFileName, String pubkey) {
        logger.info("existFullNode...");
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            String url = "jdbc:sqlite:" + PathUtils.getDataFileDir() + dbFileName;
            conn = DriverManager.getConnection(url);
            stmt = conn.createStatement();
            String sql = String.format("select * from fullnode where pubkey='%s';", pubkey);
            rs = stmt.executeQuery(sql);
            return rs.next();
        } catch (Exception e) {
            logger.error("existFullNode exception", e);
            return false;
        } finally {
            try {
                if (null != rs) {
                    rs.close();
                }
            } catch (SQLException e) {
                logger.error("existFullNode resultset close error", e);
            }
            try {
                if (null != stmt) {
                    stmt.close();
                }
            } catch (SQLException e) {
                logger.error("existFullNode statement close error", e);
            }
            try {
                if (null != conn) {
                    conn.close();
                }
            } catch (SQLException e) {
                logger.error("existFullNode conn close error", e);
            }
        }
    }

    /**
     * 删除一个全节点
     * @param pubkey 删除节点的公钥
     * @return 操作结果： true-成功， false-失败
     */
    public static boolean deleteFullNode(String dbFileName, String pubkey) {
        logger.info("deleteFullNode...");
        Connection conn = null;
        Statement stmt = null;
        try {
            String url = "jdbc:sqlite:" + PathUtils.getDataFileDir() + dbFileName;
            conn = DriverManager.getConnection(url);
            stmt = conn.createStatement();
            String sql = String.format("delete from fullnode where pubkey='%s';", pubkey);
            System.out.println(sql);
            stmt.executeUpdate(sql);
            return true;
        } catch (Exception e) {
            logger.error("deleteFullNode exception", e);
            return false;
        } finally {
            try {
                if (null != stmt) {
                    stmt.close();
                }
            } catch (SQLException e) {
                logger.error("deleteFullNode statement close error", e);
            }
            try {
                if (null != conn) {
                    conn.close();
                }
            } catch (SQLException e) {
                logger.error("deleteFullNode conn close error", e);
            }
        }
    }



    public static boolean saveRelayNode(String dbFileName, RelayNode relayNode) {
        try {
            RelayNode existNode = queryRelayNode(dbFileName, relayNode.getPubkey());
            if (null==existNode) {
                addRelayNode(dbFileName, relayNode);
            } else {
                updateRelayNode(dbFileName, relayNode);
            }
            return true;
        } catch (Exception e) {
            logger.error("", e);
            return false;
        }
    }
    /**
     * 插入新的中继节点
     * @param relayNode 新节点信息
     * @return 操作结果： true-成功， false-失败
     */
    public static boolean addRelayNode(String dbFileName, RelayNode relayNode) {
        logger.info("addRelayNode...");
        Connection conn = null;
        Statement stmt = null;
        try {
            String url = "jdbc:sqlite:" + PathUtils.getDataFileDir() + dbFileName;
            conn = DriverManager.getConnection(url);
            stmt = conn.createStatement();
            String sql = String.format("insert into relaynode (pubkey, ip, url, port, status, hash, feeRatio, name, phone, email, addresses, " +
                            "registerTimestamp, lastAliveTimestamp) values('%s', '%s', '%s', %d, %d, '%s', '%s', '%s', '%s', '%s', '%s', %d, %d);",
                    relayNode.getPubkey(), relayNode.getIp(),
                    StringUtils.isEmpty(relayNode.getUrl()) ? "" : relayNode.getUrl(),
                    relayNode.getHttpPort(), relayNode.getStatus(), relayNode.getHash(), relayNode.getFeeRatio(),
                    relayNode.getName(), relayNode.getPhone(), relayNode.getEmail(), JSON.toJSONString(relayNode.getAddresses()),
                    relayNode.getRegisterTimestamp(), relayNode.getLastAliveTimestamp());
            stmt.executeUpdate(sql);
            return true;
        } catch (Exception e) {
            logger.error("addRelayNode exception", e);
            return false;
        } finally {
            try {
                if (null != stmt) {
                    stmt.close();
                }
            } catch (SQLException e) {
                logger.error("addRelayNode statement close error", e);
            }
            try {
                if (null != conn) {
                    conn.close();
                }
            } catch (SQLException e) {
                logger.error("addRelayNode conn close error", e);
            }
        }
    }
    /**
     * 更新中继节点节点
     * @param relayNode 节点信息
     * @return 操作结果： true-成功， false-失败
     */
    public static boolean updateRelayNode(String dbFileName, RelayNode relayNode) {
        logger.info("updateRelayNode...");
        Connection conn = null;
        Statement stmt = null;
        try {
            String url = "jdbc:sqlite:" + PathUtils.getDataFileDir() + dbFileName;
            conn = DriverManager.getConnection(url);
            stmt = conn.createStatement();
            String sql = String.format("update relaynode set ip='%s', port=%d, status=%d, hash='%s', feeRatio=%s, name='%s', phone='%s', email='%s', addresses='%s', registerTimestamp=%d, lastAliveTimestamp=%d where pubkey='%s';",
                    relayNode.getIp(), relayNode.getHttpPort(), relayNode.getStatus(),
                    relayNode.getHash(), relayNode.getFeeRatio(), relayNode.getName(),
                    relayNode.getPhone(), relayNode.getEmail(), JSON.toJSONString(relayNode.getAddresses()),
                    relayNode.getRegisterTimestamp(), relayNode.getLastAliveTimestamp(), relayNode.getPubkey());
            stmt.executeUpdate(sql);
            return true;
        } catch (Exception e) {
            logger.error("updateRelayNode exception", e);
            return false;
        } finally {
            try {
                if (null != stmt) {
                    stmt.close();
                }
            } catch (SQLException e) {
                logger.error("updateRelayNode statement close error", e);
            }
            try {
                if (null != conn) {
                    conn.close();
                }
            } catch (SQLException e) {
                logger.error("updateRelayNode conn close error", e);
            }
        }
    }

    /**
     * 从数据库查询所有全节点
     * @param dbFileName 数据库文件名
     * @return events
     */
    public static List<RelayNode> queryRelayNodes(String dbFileName) {
        logger.info("queryRelayNodes...");
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        List<RelayNode> relayNodes = new ArrayList<>();
        try {
            String url = "jdbc:sqlite:" + PathUtils.getDataFileDir() + dbFileName;
            conn = DriverManager.getConnection(url);
            stmt = conn.createStatement();
            String sql = "select * from relaynode;";
            rs = stmt.executeQuery(sql);
            while (rs.next()) {
                HashMap<String, String> addresses = null;
                try {
                    addresses = JSON.parseObject(rs.getString("addresses"),
                            new TypeReference<HashMap<String, String>>() {});
                } catch (Exception e) {
                    logger.error("param address parser error: ", e);
                }
                RelayNode node = new RelayNode.Builder()
                        .pubkey(rs.getString("pubkey"))
                        .ip(rs.getString("ip"))
                        .httpPort(rs.getInt("port"))
                        .status(rs.getInt("status"))
                        .type(NodeTypes.RELAYNODE)
                        .feeRatio(rs.getDouble("feeRatio"))
                        .hash(rs.getString("hash"))
                        .phone(rs.getString("phone"))
                        .email(rs.getString("email"))
                        .addresses(addresses)
                        .registerTimestamp(rs.getLong("registerTimestamp"))
                        .lastAliveTimestamp(rs.getLong("lastAliveTimestamp"))
                        .build();
                relayNodes.add(node);
            }
            return relayNodes;
        } catch (Exception e) {
            logger.error("queryRelayNodes exception", e);
            return relayNodes;
        } finally {
            try {
                if (null != rs) {
                    rs.close();
                }
            } catch (SQLException e) {
                logger.error("queryRelayNodes resultset close error", e);
            }
            try {
                if (null != stmt) {
                    stmt.close();
                }
            } catch (SQLException e) {
                logger.error("queryRelayNodes statement close error", e);
            }
            try {
                if (null != conn) {
                    conn.close();
                }
            } catch (SQLException e) {
                logger.error("queryRelayNodes conn close error", e);
            }
        }
    }

    /**
     * 从数据库查询所有全节点
     * @param dbFileName 数据库文件名
     * @return events
     */
    public static RelayNode queryRelayNode(String dbFileName, String pubkey) {
        logger.info("queryRelayNode...");
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            String url = "jdbc:sqlite:" + PathUtils.getDataFileDir() + dbFileName;
            conn = DriverManager.getConnection(url);
            stmt = conn.createStatement();
            String sql = String.format("select * from relaynode where pubkey='%s';", pubkey);
            rs = stmt.executeQuery(sql);
            if (rs.next()) {
                HashMap<String, String> addresses = null;
                try {
                    addresses = JSON.parseObject(rs.getString("addresses"),
                            new TypeReference<HashMap<String, String>>() {});
                } catch (Exception e) {
                    logger.error("param address parser error: ", e);
                }
                return new RelayNode.Builder()
                        .pubkey(rs.getString("pubkey"))
                        .ip(rs.getString("ip"))
                        .httpPort(rs.getInt("port"))
                        .status(rs.getInt("status"))
                        .type(NodeTypes.RELAYNODE)
                        .feeRatio(rs.getDouble("feeRatio"))
                        .hash(rs.getString("hash"))
                        .phone(rs.getString("phone"))
                        .email(rs.getString("email"))
                        .addresses(addresses)
                        .registerTimestamp(rs.getLong("registerTimestamp"))
                        .lastAliveTimestamp(rs.getLong("lastAliveTimestamp"))
                        .build();
            } else {
                return null;
            }
        } catch (Exception e) {
            logger.error("queryRelayNode exception", e);
            return null;
        } finally {
            try {
                if (null != rs) {
                    rs.close();
                }
            } catch (SQLException e) {
                logger.error("queryRelayNode resultset close error", e);
            }
            try {
                if (null != stmt) {
                    stmt.close();
                }
            } catch (SQLException e) {
                logger.error("queryRelayNode statement close error", e);
            }
            try {
                if (null != conn) {
                    conn.close();
                }
            } catch (SQLException e) {
                logger.error("queryRelayNode conn close error", e);
            }
        }
    }

    /**
     * 从数据库查询所有全节点
     * @param dbFileName 数据库文件名
     * @return events
     */
    public static boolean existRelayNode(String dbFileName, String pubkey) {
        logger.info("existRelayNode...");
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            String url = "jdbc:sqlite:" + PathUtils.getDataFileDir() + dbFileName;
            conn = DriverManager.getConnection(url);
            stmt = conn.createStatement();
            String sql = String.format("select * from relaynode where pubkey='%s';", pubkey);
            rs = stmt.executeQuery(sql);

            return rs.next();
        } catch (Exception e) {
            logger.error("existRelayNode exception", e);
            return false;
        } finally {
            try {
                if (null != rs) {
                    rs.close();
                }
            } catch (SQLException e) {
                logger.error("existRelayNode resultset close error", e);
            }
            try {
                if (null != stmt) {
                    stmt.close();
                }
            } catch (SQLException e) {
                logger.error("existRelayNode statement close error", e);
            }
            try {
                if (null != conn) {
                    conn.close();
                }
            } catch (SQLException e) {
                logger.error("existRelayNode conn close error", e);
            }
        }
    }

    /**
     * 删除一个全节点
     * @param pubkey 删除节点的公钥
     * @return 操作结果： true-成功， false-失败
     */
    public static boolean deleteRelayNode(String dbFileName, String pubkey) {
        logger.info("deleteRelayNode...");
        Connection conn = null;
        Statement stmt = null;
        try {
            String url = "jdbc:sqlite:" + PathUtils.getDataFileDir() + dbFileName;
            conn = DriverManager.getConnection(url);
            stmt = conn.createStatement();
            String sql = String.format("delete from relaynode where pubkey='%s';", pubkey);
            System.out.println(sql);
            stmt.executeUpdate(sql);
            return true;
        } catch (Exception e) {
            logger.error("deleteRelayNode exception", e);
            return false;
        } finally {
            try {
                if (null != stmt) {
                    stmt.close();
                }
            } catch (SQLException e) {
                logger.error("deleteRelayNode statement close error", e);
            }
            try {
                if (null != conn) {
                    conn.close();
                }
            } catch (SQLException e) {
                logger.error("deleteRelayNode conn close error", e);
            }
        }
    }


    /**
     * 保存分片信息 todo preparedStatement 写不进去，也不报错，数据没有问题，需要思考问题在哪
     * @param dbFileName 数据库文件名
     * @param localFullNodes 分片信息
     */
    public static boolean saveLocalfullnode2Database(String dbFileName, Collection<LocalFullNode> localFullNodes) {
        logger.info("saveLocalfullnode2Database...");
        Connection conn = null;
        Statement stmt = null;
        StringBuilder sql = new StringBuilder();
        try {
            String url = "jdbc:sqlite:" + PathUtils.getDataFileDir() + dbFileName;
            conn = DriverManager.getConnection(url);
            stmt = conn.createStatement();
            for (LocalFullNode localFullNode : localFullNodes) {
                sql.append(String.format("insert into localfullnode (pubkey, address, shard, idx, hash, status) "
                                + "values('%s', '%s', '%s', '%s','%s', %d);",
                        localFullNode.getPubkey(), localFullNode.getAddress(), localFullNode.getShard(), localFullNode.getIndex(),
                        localFullNode.getHash(), localFullNode.getStatus()) );
            }
            stmt.executeUpdate(sql.toString());
            return true;
        } catch (Exception e) {
            logger.error("saveLocalfullnode2Database exception", e);
            return false;
        } finally {
            try {
                if (null != stmt) {
                    stmt.close();
                }
            } catch (SQLException e) {
                logger.error("saveLocalfullnode2Database stmt close error", e);
            }
            try {
                if (null != conn) {
                    conn.close();
                }
            } catch (SQLException e) {
                logger.error("saveLocalfullnode2Database conn close error", e);
            }
        }
    }

    /**
     * 保存局部全节点信息
     * @param dbFileName 数据库文件名
     * @param localFullNode 局部全节点
     */
    public static boolean saveLocalfullnode(String dbFileName, LocalFullNode localFullNode) {
        logger.info("saveLocalfullnode...");
        Connection conn = null;
        Statement stmt = null;
        StringBuilder sql = new StringBuilder();
        try {
            String url = "jdbc:sqlite:" + PathUtils.getDataFileDir() + dbFileName;
            conn = DriverManager.getConnection(url);
            stmt = conn.createStatement();
            sql.append(String.format("insert into localfullnode (pubkey, address, shard, idx, hash, status) "
                            + "values('%s', '%s', '%s', '%s','%s', %d);",
                    localFullNode.getPubkey(), localFullNode.getAddress(), localFullNode.getShard(), localFullNode.getIndex(),
                    localFullNode.getHash(), localFullNode.getStatus()) );
            stmt.executeUpdate(sql.toString());
            return true;
        } catch (Exception e) {
            logger.error("saveLocalfullnode exception", e);
            return false;
        } finally {
            try {
                if (null != stmt) {
                    stmt.close();
                }
            } catch (SQLException e) {
                logger.error("saveLocalfullnode stmt close error", e);
            }
            try {
                if (null != conn) {
                    conn.close();
                }
            } catch (SQLException e) {
                logger.error("saveLocalfullnode conn close error", e);
            }
        }
    }

    /**
     * 更新全节点
     * @param localFullNode 节点信息
     * @return 操作结果： true-成功， false-失败
     */
    public static boolean updateLocalFullNode(String dbFileName, LocalFullNode localFullNode) {
        logger.info("updateLocalFullNode...");
        Connection conn = null;
        Statement stmt = null;
        try {
            String url = "jdbc:sqlite:" + PathUtils.getDataFileDir() + dbFileName;
            conn = DriverManager.getConnection(url);
            stmt = conn.createStatement();
            String sql = String.format("update localfullnode set ip='%s', rpcPort=%d, httpPort=%d, shard='%s', idx='%s', status=%d where pubkey='%s';",
                    localFullNode.getIp(), localFullNode.getRpcPort(), localFullNode.getHttpPort(), localFullNode.getShard(),
                    localFullNode.getIndex(), localFullNode.getStatus(), localFullNode.getPubkey());
            stmt.executeUpdate(sql);
            return true;
        } catch (Exception e) {
            logger.error("updateLocalFullNode exception", e);
            return false;
        } finally {
            try {
                if (null != stmt) {
                    stmt.close();
                }
            } catch (SQLException e) {
                logger.error("updateLocalFullNode statement close error", e);
            }
            try {
                if (null != conn) {
                    conn.close();
                }
            } catch (SQLException e) {
                logger.error("updateLocalFullNode conn close error", e);
            }
        }
    }

    /**
     * 从数据库读取所有ShardInfo
     * @param dbFileName 数据库文件名
     * @return events
     */
    public static List<LocalFullNode> queryLocalFullNodes(String dbFileName) {
        logger.info("queryLocalFullNodes...");
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        List<LocalFullNode> nodes = new ArrayList<>();
        try {
            String url = "jdbc:sqlite:" + PathUtils.getDataFileDir() + dbFileName;
            conn = DriverManager.getConnection(url);
            stmt = conn.createStatement();
            String sql = "select * from localfullnode order by shard;";
            rs = stmt.executeQuery(sql);
            while (rs.next()) {
                LocalFullNode localFullNode = new LocalFullNode.Builder()
                        .pubkey(rs.getString("pubkey"))
                        .address(rs.getString("address"))
                        .shard(rs.getString("shard"))
                        .index(rs.getString("idx"))
                        .status(rs.getInt("status"))
                        .type(NodeTypes.LOCALFULLNODE)
                        .build();
                nodes.add(localFullNode);
            }
            return nodes;
        } catch (Exception e) {
            logger.error("queryLocalFullNodes exception", e);
            return nodes;
        } finally {
            try {
                if (null != rs) {
                    rs.close();
                }
            } catch (SQLException e) {
                logger.error("queryLocalFullNodes resultset close error", e);
            }
            try {
                if (null != stmt) {
                    stmt.close();
                }
            } catch (SQLException e) {
                logger.error("queryLocalFullNodes statement close error", e);
            }
            try {
                if (null != conn) {
                    conn.close();
                }
            } catch (SQLException e) {
                logger.error("queryLocalFullNodes conn close error", e);
            }
        }
    }
    /**
     * 从数据库读取所有ShardInfo
     * @param dbFileName 数据库文件名
     * @return events
     */
    public static LocalFullNode queryLocalFullNode(String dbFileName, String pubkey) {
        logger.info("queryLocalFullNode...");
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            String url = "jdbc:sqlite:" + PathUtils.getDataFileDir() + dbFileName;
            conn = DriverManager.getConnection(url);
            stmt = conn.createStatement();
            String sql = String.format("select * from localfullnode where pubkey='%d';", pubkey);
            rs = stmt.executeQuery(sql);
            if (rs.next()) {
                LocalFullNode localFullNode = new LocalFullNode.Builder()
                        .pubkey(rs.getString("pubkey"))
                        .address(rs.getString("address"))
                        .shard(rs.getString("shard"))
                        .index(rs.getString("idx"))
                        .status(rs.getInt("status"))
                        .type(NodeTypes.LOCALFULLNODE)
                        .build();
                return localFullNode;
            } else {
                return null;
            }
        } catch (Exception e) {
            logger.error("queryLocalFullNodes exception", e);
            return null;
        } finally {
            try {
                if (null != rs) {
                    rs.close();
                }
            } catch (SQLException e) {
                logger.error("queryLocalFullNodes resultset close error", e);
            }
            try {
                if (null != stmt) {
                    stmt.close();
                }
            } catch (SQLException e) {
                logger.error("queryLocalFullNodes statement close error", e);
            }
            try {
                if (null != conn) {
                    conn.close();
                }
            } catch (SQLException e) {
                logger.error("queryLocalFullNodes conn close error", e);
            }
        }
    }

    /**
     * 共识后的新block存入数据库
     * @param dbFileName 数据库文件名
     * @return 最新block
     */
    public static boolean addBlock(String dbFileName, Block block) {
        logger.info("addBlock...");
        Connection conn = null;
        Statement stmt = null;
        try {
            String url = "jdbc:sqlite:" + PathUtils.getDataFileDir() + dbFileName;
            conn = DriverManager.getConnection(url);
            stmt = conn.createStatement();
            String sql = String.format("insert into block (id, timestamp, preHash, hash, signature, data, isValid) " +
                            "values (%d, %d, '%s', '%s', '%s', '%s', %d);",
                    block.getIndex(), block.getTimestamp(),
                    (null==block.getPreHash()) ? "" : DSA.encryptBASE64(block.getPreHash().getHash()),
                    DSA.encryptBASE64(block.getHash().getHash()),
                    DSA.encryptBASE64(block.getSignature()),
                    DSA.encryptBASE64(block.getData()), 1 );
            stmt.executeUpdate(sql);
            return true;
        } catch (Exception e) {
            logger.error("queryLastBlockFromDatabase exception", e);
            return false;
        } finally {
            try {
                if (null != stmt) {
                    stmt.close();
                }
            } catch (SQLException e) {
                logger.error("addBlock statement close error", e);
            }
            try {
                if (null != conn) {
                    conn.close();
                }
            } catch (SQLException e) {
                logger.error("addBlock conn close error", e);
            }
        }
    }
    /**
     * 从数据库读取最新block
     * @param dbFileName 数据库文件名
     * @return 最新block
     */
    public static Block queryLastBlockFromDatabase(String dbFileName) {
        logger.info("queryLastBlockFromDatabase...");
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        Block block = null;
        try {
            String url = "jdbc:sqlite:" + PathUtils.getDataFileDir() + dbFileName;
            conn = DriverManager.getConnection(url);
            stmt = conn.createStatement();
            String sql = "select * from block where id = (SELECT max(id) from block);";
            rs = stmt.executeQuery(sql);
            if (rs.next()) {
                block = new Block.Builder()
                        .index(rs.getLong("id"))
                        .timestamp(rs.getLong("timestamp"))
                        .hash(new Hash(DSA.decryptBASE64(rs.getString("hash"))))
                        .signature(DSA.decryptBASE64(rs.getString("signature")))
                        .data(DSA.decryptBASE64(rs.getString("data")))
                        .build();
                if (StringUtils.isNotEmpty(rs.getString("preHash"))) {
                    block.setPreHash(new Hash(DSA.decryptBASE64(rs.getString("preHash"))));
                }
            }
            return block;
        } catch (Exception e) {
            logger.error("queryLastBlockFromDatabase exception", e);
            return block;
        } finally {
            try {
                if (null != rs) {
                    rs.close();
                }
            } catch (SQLException e) {
                logger.error("queryLastBlockFromDatabase resultset close error", e);
            }
            try {
                if (null != stmt) {
                    stmt.close();
                }
            } catch (SQLException e) {
                logger.error("queryLastBlockFromDatabase statement close error", e);
            }
            try {
                if (null != conn) {
                    conn.close();
                }
            } catch (SQLException e) {
                logger.error("queryLastBlockFromDatabase conn close error", e);
            }
        }
    }
    /**
     * 从数据库读取最新block
     * @param dbFileName 数据库文件名
     * @return 最新block
     */
    public static Block queryBlockFromDatabase(String dbFileName, String id) {
        logger.info("queryBlockFromDatabase...");
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        Block block = null;
        try {
            String url = "jdbc:sqlite:" + PathUtils.getDataFileDir() + dbFileName;
            conn = DriverManager.getConnection(url);
            stmt = conn.createStatement();
            String sql = String.format("select * from block where id = '%s';", id);
            rs = stmt.executeQuery(sql);
            if (rs.next()) {
                block = new Block.Builder()
                        .index(rs.getLong("id"))
                        .timestamp(rs.getLong("timestamp"))
                        .hash(new Hash(DSA.decryptBASE64(rs.getString("hash"))))
                        .signature(DSA.decryptBASE64(rs.getString("signature")))
                        .data(DSA.decryptBASE64(rs.getString("data")))
                        .build();
                if (StringUtils.isNotEmpty(rs.getString("preHash"))) {
                    block.setPreHash(new Hash(DSA.decryptBASE64(rs.getString("preHash"))));
                }
            }
            return block;
        } catch (Exception e) {
            logger.error("queryBlockFromDatabase exception", e);
            return block;
        } finally {
            try {
                if (null != rs) {
                    rs.close();
                }
            } catch (SQLException e) {
                logger.error("queryBlockFromDatabase resultset close error", e);
            }
            try {
                if (null != stmt) {
                    stmt.close();
                }
            } catch (SQLException e) {
                logger.error("queryBlockFromDatabase statement close error", e);
            }
            try {
                if (null != conn) {
                    conn.close();
                }
            } catch (SQLException e) {
                logger.error("queryBlockFromDatabase conn close error", e);
            }
        }
    }
}

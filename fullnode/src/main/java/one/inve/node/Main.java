package one.inve.node;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.zeroc.Ice.ObjectAdapter;
import com.zeroc.Ice.Util;
import one.inve.bean.wallet.Keys;
import one.inve.bean.wallet.Wallet;
import one.inve.bean.wallet.WalletBuilder;
import one.inve.bean.node.*;
import one.inve.core.Block;
import one.inve.http.NettyHttpServer;
import one.inve.rpc.fullnode.RegisterPrx;
import one.inve.threads.ExchangeRateThread;
import one.inve.threads.GossipNodeThread;
import one.inve.threads.PbftThread;
import one.inve.util.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * 种子全节点
 * Created by Clare  on 2018/6/9 0009.
 */
public class Main extends GeneralNode {
    private String onlineTime;

    public String getOnlineTime() {
        return onlineTime;
    }

    public void setOnlineTime(String onlineTime) {
        this.onlineTime = onlineTime;
    }

    /**
     * 启动gossip网络
     */
    private void startGossipNetwork() {
        GossipNodeThread thread = new GossipNodeThread(this);
        thread.start();
    }

    /**
     * 启动rpc服务
     * @param adapter
     */
    private void runRpcService(ObjectAdapter adapter) {
        logger.info("start rpc service...");
        this.loadRPC(this, adapter);
    }

    /**
     * 单进程节点判断
     * @return 是否已存在一个运行中进程
     */
    private boolean isRunning() {
        logger.info(">>>>>> check if running a process.");
        FileLockUtils fileLockUtils = new FileLockUtils(PathUtils.getDataFileDir()+ parameters.keysFile);
        try {
            return !fileLockUtils.Lock();
        } catch (IOException e) {
            logger.error("file lock error", e);
            return false;
        }
    }

    /**
     * 初始化钱包
     * @throws Exception 异常
     */
    private void initWallet() throws Exception {
        logger.info(">>>>>> init wallet...");
        File dir = new File(PathUtils.getDataFileDir());
        if (!dir.isDirectory()) {
            if (!dir.mkdirs()) {
                logger.error("create cache dir failed!!! exit...");
                System.exit(-1);
            }
        }
        String fileName = PathUtils.getDataFileDir() + parameters.walletFile;
        logger.info(">>>>>> path: " + fileName);
        File file = new File(fileName);
        if (!file.exists()) {
            newWallet(fileName);
        } else {
            reloadWallet(fileName);
            if (null==wallet) {
                newWallet(fileName);
            }
        }
    }

    /**
     * 创建钱包
     * @param fileName 钱包基本信息保存文件
     */
    private void newWallet(String fileName) throws Exception {
        wallet = WalletBuilder.generateWallet();
        // 写入文件
        FileOutputStream outputStream = null;
        try {
            outputStream  = new FileOutputStream(new File(fileName));
            outputStream.write(JSONObject.toJSONString(wallet).getBytes());
            outputStream.close();
        } catch (Exception e) {
            logger.error(e);
            System.exit(-1);
        } finally {
            if (null!=outputStream) {
                outputStream.close();
            }
        }
    }

    /**
     * 重载钱包
     * @param fileName 钱包基本信息保存文件
     */
    private void reloadWallet(String fileName) {
        FileInputStream inputStream = null;
        byte[] data = null;
        try {
            inputStream = new FileInputStream(new File(fileName));
            data = new byte[inputStream.available()];
            inputStream.read(data);
        } catch (Exception e) {
            logger.error(e);
            System.exit(-1);
        } finally {
            if (null!=inputStream) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        if (null == data) {
            logger.warn("reload wallet is empty.");
            return;
        }
        JSONObject walletObj = JSONObject.parseObject(new String(data));
        Keys extKeys = JSONObject.parseObject(walletObj.getString("extKeys"), Keys.class);
        Keys keys = JSONObject.parseObject(walletObj.getString("keys"), Keys.class);
        wallet = new Wallet.Builder()
                .mnemonic(walletObj.getString("mnemonic"))
                .extKeys(extKeys)
                .keys(keys)
                .address(walletObj.getString("address"))
                .build() ;
    }

    /**
     * 向seed请求全节点列表
     * @param pubkey 自己公钥
     * @return 全节点列表
     */
    public List<FullNode> getFullNodeList(String pubkey) {
        List<FullNode> fullNodes = null;
        try {
            String connInfo = "Register:default -h " + this.parameters.seedGossipAddress.pubIP
                    + " -p " + this.parameters.seedGossipAddress.rpcPort;
            RegisterPrx prx = RegisterPrx.checkedCast(communicator.stringToProxy(connInfo));

            String baseNodes = prx.getFullNodeList(pubkey);
            JSONObject object = JSONObject.parseObject(baseNodes);
            if (object.getString("code").equalsIgnoreCase("200")) {
                String fullNodesStr = object.getString("data");
                if (!StringUtils.isEmpty(fullNodesStr)) {
                    return JSONObject.parseArray(fullNodesStr, FullNode.class);
                } else {
                    return null;
                }
            }
        } catch (Exception e) {
            logger.error(e);
            return getFullNodeList(pubkey);
        }
        return fullNodes;
    }

    /**
     * 判断自己是否已注册成为全节点
     * @return 是否已注册：true-已注册， false-未注册
     */
    private boolean isRegistered() {
        String pubkey = HnKeyUtils.getString4PublicKey(publicKey);
        List<FullNode> fullNodes = getFullNodeList(pubkey);
        fullNodes.forEach(n -> {
            this.getFullNodeList().put(n.getPubkey(), n);
        });
        return null!=this.getFullNodeList().get(pubkey);
    }

    /**
     * 注册成为全节点
     * @return 注册是否成功：true-成功，false-失败
     */
    private boolean register() {
        String pubkey = HnKeyUtils.getString4PublicKey(publicKey);
        String result = "failure";
        try {
            String connInfo = "Register:default -h " + this.parameters.seedGossipAddress.pubIP
                    + " -p " + this.parameters.seedGossipAddress.rpcPort;
            RegisterPrx prx = RegisterPrx.checkedCast(communicator.stringToProxy(connInfo));

            result = prx.registerFullNode(this.parameters.selfGossipAddress.pubIP,
                    this.parameters.selfGossipAddress.rpcPort, this.parameters.selfGossipAddress.httpPort, pubkey);
            JSONObject object = JSONObject.parseObject(result);
            if (object.getString("code").equals("200")) {
                return true;
            } else {
                logger.warn(object.getString("data"));
                return false;
            }
        } catch (Exception e) {
            logger.error(e);
            return false;
        }
    }

    /**
     * 重构currBlock
     */
    private void restructCurrBlock() {
        Block block = SqliteDAO.queryLastBlockFromDatabase(parameters.dbFile);
        if (null != block ) {
            this.setCurrBlock(block);
        }
    }

    /**
     * 初始化数据库
     */
    private void initDatabase() {
        File dbfile = new File(PathUtils.getDataFileDir() + this.parameters.dbFile);
        if (!dbfile.exists()) {
            // 创建数据
            DbUtils.createDatabase(PathUtils.getDataFileDir(), this.parameters.dbFile);
        } else {
            // 重构当前分片区块
            this.restructCurrBlock();
            String pubkey = HnKeyUtils.getString4PublicKey(this.publicKey);
            // 重构localfullNodeList
            List<LocalFullNode> localFullNodes = SqliteDAO.queryLocalFullNodes(this.parameters.dbFile);
            localFullNodes.forEach(n -> this.getLocalFullNodeList().put(n.getPubkey(), n));
            // 重构fullNodeList
            List<FullNode> fullNodes = SqliteDAO.queryFullNodes(this.parameters.dbFile, pubkey);
            fullNodes.forEach(n -> this.getFullNodeList().put(n.getPubkey(), n));
            // 重构relayNodeList
            List<RelayNode> relayNodes = SqliteDAO.queryRelayNodes(this.parameters.dbFile);
            relayNodes.forEach(n -> this.getRelayNodeList().put(n.getPubkey(), n));
        }
    }

    /**
     * 从seed更新全节点列表
     */
    private void updateFullNodeList() {
        // 更新全节点列表并入库
        RegisterPrx prx = buildRegisterConnection2FullNode();
        while (null==prx) {
            prx = buildRegisterConnection2FullNode();
        }
        String pubkey = HnKeyUtils.getString4PublicKey(publicKey);
        List<FullNode> fullNodes = getFullNodeList(pubkey);
        fullNodes.forEach(n -> {
            SqliteDAO.saveFullNode(this.parameters.dbFile, n);
            this.getFullNodeList().put(n.getPubkey(), n);
        });
    }

    /**
     * 初始化
     * @param args 命令行参数
     */
    private void init(String args[]) {
        try {
            //读取配置文件
            communicator = Util.initialize(args);
            if (null == this.parameters) {
                this.parameters = new Parameters();
            }
            this.parameters.init(getCommunicator(), args);
            logger.warn(JSON.toJSONString(this.parameters));

            // 初始化钱包
            initWallet();

            // 初始化key
            initHnKey();
            if (isRunning()) {
                logger.error("There is already a running process!");
                System.exit(0);
            }

            // 初始化数据库
            initDatabase();

            // 注册成为全节点
            if (!isRegistered()){
                boolean isSuccess = register();
                if (isSuccess) {
                    // 从seed获取全节点列表并更新
                    this.updateFullNodeList();
                } else {
                    logger.warn("Register failed. exit...");
                    System.exit(-1);
                }
            }

            // 启动Gossip网络
            startGossipNetwork();

            // 启动rpc接口服务: 全节点注册退出
            ObjectAdapter adapter = generateAdapter(getCommunicator(), "FullNodeAdapter",
                    parameters.selfGossipAddress.rpcPort);
            runRpcService(adapter);

            // 开放局部全节点注册接口
            openServiceState = 1;
            while (!hasSufficientFullnodes() || StringUtils.isEmpty(this.getSeedPubkey())) {
                logger.warn("Whether has a sufficient number of fullnodes: " + hasSufficientFullnodes());
                logger.warn("seed pubkey: " + this.getSeedPubkey());
                updateFullNodeList();
                Thread.sleep(parameters.gossipInterval);
            }
            new PbftThread(this).start();
            new ExchangeRateThread(this).start();

            // 开放查询局部全节点信息接口
            openServiceState = 2;

            // 启动http接口
            NettyHttpServer.boostrapHttpService(this);

            communicator.waitForShutdown();
        } catch (Exception e) {
            logger.error("main init error.", e);
        }
    }

    public static void main(String[] args) {
        Main main = new Main();
        main.init(args);
    }
}

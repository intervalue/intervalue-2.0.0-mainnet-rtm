package one.inve.contract.provider;

import one.inve.contract.Contract;
import one.inve.contract.ethplugin.core.Repository;
import one.inve.contract.inve.INVERepositoryManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

/**
 * 底层数据库提供类：根据需要选择不同的底层实现方案
 * @author 肖毅
 * @since 2019-01-15
 */
public class RepositoryProvider {
    private static final Logger logger = LoggerFactory.getLogger("RepositoryProvider");

    private static final int DENG = 0;  // 邓辉的实现方案
    private static final int HAO = 1;   // 蒿师兄实现方案
    private static int flag = HAO;
    private static HashMap<String, Repository> TRACK;

    public static Repository getTrack(String dbId) {
        if(TRACK == null) {
            TRACK = new HashMap<>();
        }

        Repository track = TRACK.get(dbId);
        if(track == null) {
            switch (flag) {
                case DENG:
                    track = Contract.getInstance(null).getRepositoryMix().getTrack();
                    TRACK.put(dbId, track);
                    break;
                case HAO:
                    track = INVERepositoryManager.getRepoRoot(dbId);
                    TRACK.put(dbId, track);
                    break;
                default:
                    logger.error("flag not supposed.");
                    break;
            }

        }
        return track;
    }
}
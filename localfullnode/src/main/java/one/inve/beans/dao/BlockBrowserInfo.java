package one.inve.beans.dao;

public class BlockBrowserInfo {
    /**
     * 账户数量
     */
    private Long userCount;
    /**
     *
     */
    private String tps;

    /**
     * 节点数量
     */
    private String shardNumber;
    /**
     * 开始运行时间
     */
    private Long runTime;

    public Long getUserCount() {
        return userCount;
    }

    public void setUserCount(Long userCount) {
        this.userCount = userCount;
    }

    public String getTps() {
        return tps;
    }

    public void setTps(String tps) {
        this.tps = tps;
    }

    public String getShardNumber() {
        return shardNumber;
    }

    public void setShardNumber(String shardNumber) {
        this.shardNumber = shardNumber;
    }

    public Long getRunTime() {
        return runTime;
    }

    public void setRunTime(Long runTime) {
        this.runTime = runTime;
    }
}

package one.inve.beans.report;

/**
 * @author Clare
 * @date   2018/9/3 0003.
 */
public class GossipStat {
    // ip_port
    public String addr;

    // 每轮gossip event占用空间进行汇总，总体最大值，最小值，平均值，当前值，总值
    public long minEventSpace;
    public long avgEventSpace;
    public long maxEventSpace;
    public long currEventSpace;
    public long totalEventSpace;

    // 每轮gossip event数量进行汇总，总体最大值，最小值，平均值，当前值，总值
    public long minEventNum;
    public long avgEventNum;
    public long maxEventNum;
    public long currEventNum;
    public long totalEventNum;

    // 每轮gossip 间隔进行汇总，总体最大值，最小值，平均值，当前值
    public long minGossipInterval;
    public long avgGossipInterval;
    public long maxGossipInterval;
    public long currGossipInterval;
    public long totalGossipInterval;

    // 成功连接总次数、失败总次数
    public long successConnNum;
    public long failedConnNum;

    // 每轮generation最值
    public long minGeneration;
    public long maxGeneration;

    public GossipStat() {
    }

    private GossipStat(Builder builder) {
        addr = builder.addr;
        minEventSpace = builder.minEventSpace;
        avgEventSpace = builder.avgEventSpace;
        maxEventSpace = builder.maxEventSpace;
        currEventSpace = builder.currEventSpace;
        totalEventSpace = builder.totalEventSpace;
        minEventNum = builder.minEventNum;
        avgEventNum = builder.avgEventNum;
        maxEventNum = builder.maxEventNum;
        currEventNum = builder.currEventNum;
        totalEventNum = builder.totalEventNum;
        minGossipInterval = builder.minGossipInterval;
        avgGossipInterval = builder.avgGossipInterval;
        maxGossipInterval = builder.maxGossipInterval;
        currGossipInterval = builder.currGossipInterval;
        successConnNum = builder.successConnNum;
        failedConnNum = builder.failedConnNum;
        minGeneration = builder.minGeneration;
        maxGeneration = builder.maxGeneration;
    }

    public static final class Builder {
        private String addr;
        private long minEventSpace;
        private long avgEventSpace;
        private long maxEventSpace;
        private long currEventSpace;
        private long totalEventSpace;
        private long minEventNum;
        private long avgEventNum;
        private long maxEventNum;
        private long currEventNum;
        private long totalEventNum;
        private long minGossipInterval;
        private long avgGossipInterval;
        private long maxGossipInterval;
        private long currGossipInterval;
        private long successConnNum;
        private long failedConnNum;
        private long minGeneration;
        private long maxGeneration;

        public Builder() {
        }

        public Builder addr(String val) {
            addr = val;
            return this;
        }

        public Builder minEventSpace(long val) {
            minEventSpace = val;
            return this;
        }

        public Builder avgEventSpace(long val) {
            avgEventSpace = val;
            return this;
        }

        public Builder maxEventSpace(long val) {
            maxEventSpace = val;
            return this;
        }

        public Builder currEventSpace(long val) {
            currEventSpace = val;
            return this;
        }

        public Builder totalEventSpace(long val) {
            totalEventSpace = val;
            return this;
        }

        public Builder minEventNum(long val) {
            minEventNum = val;
            return this;
        }

        public Builder avgEventNum(long val) {
            avgEventNum = val;
            return this;
        }

        public Builder maxEventNum(long val) {
            maxEventNum = val;
            return this;
        }

        public Builder currEventNum(long val) {
            currEventNum = val;
            return this;
        }

        public Builder totalEventNum(long val) {
            totalEventNum = val;
            return this;
        }

        public Builder minGossipInterval(long val) {
            minGossipInterval = val;
            return this;
        }

        public Builder avgGossipInterval(long val) {
            avgGossipInterval = val;
            return this;
        }

        public Builder maxGossipInterval(long val) {
            maxGossipInterval = val;
            return this;
        }

        public Builder currGossipInterval(long val) {
            currGossipInterval = val;
            return this;
        }

        public Builder successConnNum(long val) {
            successConnNum = val;
            return this;
        }

        public Builder failedConnNum(long val) {
            failedConnNum = val;
            return this;
        }

        public Builder minGeneration(long val) {
            minGeneration = val;
            return this;
        }

        public Builder maxGeneration(long val) {
            maxGeneration = val;
            return this;
        }

        public GossipStat build() {
            return new GossipStat(this);
        }
    }
}

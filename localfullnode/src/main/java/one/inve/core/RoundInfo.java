package one.inve.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class RoundInfo {
    List<Event> allEvents = Collections.synchronizedList(new ArrayList());
    //当前round所有选举的链表，通过ElectionRound内的next和prev构建链表
    ElectionRound elections = null;
    boolean fameDecided = false;
    List<Event> famousWitnesses = Collections.synchronizedList(new ArrayList());
    List<Event> nonConsensusEvents = Collections.synchronizedList(new ArrayList());
    int numUnknownFame = 0;
    int numWitnesses = 0;
    long round = 0;
    byte[] whitening = new byte[104];
    List<Event> witnesses = Collections.synchronizedList(new ArrayList());

    //针对witness的election
    static class ElectionRound {
        final long age;
        final Event event;
        //通过这个四个引用构成类似结构
        //Round0: e0 <-> e1 <-> e2 ...
        //         ^     ^      ^
        //         |     |      |
        //Round1: e0 <-> e1 <-> e2 ...
        //本Round内的下一个和上一个投票
        ElectionRound nextElection = null;
        ElectionRound prevElection = null;
        //针对本Event的上一个和下一个Round的投票
        ElectionRound nextRound = null;
        ElectionRound prevRound = null;
        final RoundInfo roundInfo;
        final boolean[] vote;

        ElectionRound(RoundInfo roundInfo, int numMembers, Event event, long age) {
            this.vote = new boolean[numMembers];
            this.event = event;
            this.age = age;
            this.roundInfo = roundInfo;
        }
    }

    public RoundInfo(long round) {
        this.round = round;
    }
}

module one{
    module inve{
        module rpc {
            module localfullnode {
                struct Balance {
                    int stable;
                    int pending;
                };

                struct Hash {
                    string hash;
                    int hashMapSeed;
                };

                sequence<byte> Message;
                sequence<Message> MessageList;
                sequence<byte> Signature;
                sequence<byte> HashnetHash;

                struct Event {
                    int shardId;
                    long selfId;
                    long selfSeq;
                    long otherId;
                    long otherSeq;
                    MessageList messages;
                    long timeCreatedSecond;
                    int  timeCreatedNano;
                    Signature sign;
                    bool isFamous;
                    HashnetHash hash;
                    long generation;
                    long consensusTimestampSecond;
                    int  consensusTimestampNano;
                    HashnetHash  otherHash;
                    HashnetHash  parentHash;
                };
                sequence<Event> EventList;
                struct GossipObj{
                	string snapVersion;
                	EventList events;
                	HashnetHash snapHash;
                };

                struct SnapObj {
                	string snapMessage;
                	string messages;
                };

                struct AppointEvent{
                    string snapVersion;
                    Event  event;
                }

                sequence<long> LastSeqOneShard;
                interface Local2local {
                    GossipObj gossipMyMaxSeqList4Consensus(string pubkey, string sig, string snapVersion, string snapHash, LastSeqOneShard seqs);
                    GossipObj gossipMyMaxSeqList4Sync(string pubkey, string sig, int otherShardId, string snapVersion, string snapHash, LastSeqOneShard seqs);
                    SnapObj  gossipMySnapVersion4Snap(string pubkey, string sig, string hash, string transCount);
                    AppointEvent   gossip4AppointEvent(string pubkey, string sig,int shardId,long creatorId,long creatorSeq);
                    bool  gossipReport4split(string pubkey, string sig,string data,int shardId,string event);
                    bool  gossip4SplitDel(string pubkey, string sig,string data,int shardId,long creatorId,long creatorSeq,string eventHash,bool isNeedGossip2Center);
                };

                interface Light2local {
                    ["amd"]string sendMessage(string message);
                    string getTransactionHistory(string gossipAddress);
                };
            };

            module fullnode {
                interface Register {
                    string getLocalFullNodeList(string pubkey);                     // for full node
                    string getNodeShardInfo(string pubkey);                         // for local full node
                    string getShardInfoList();                                      // for new full node
                    string registerLocalFullNode(string pubkey, string address);    // for local full node applicator
                    string getNrgPrice();                                           // for all
                }
            };
        };
    };
};
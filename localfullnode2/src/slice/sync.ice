module one{
    module inve{
        module localfullnode2 {
            module sync {
				module rpc {
					module gen {
						sequence<byte> Message;
						sequence<Message> MessageList;
						sequence<byte> Signature;
						sequence<byte> HashnetHash;
						sequence<byte> MerkleTreeRootHash;

						struct SyncEvent {
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
						
						struct MerkleTreeizedSyncEvent {
							SyncEvent syncEvent;
							string merklePathJson;
						};
						
						sequence<MerkleTreeizedSyncEvent> MerkleTreeizedSyncEventList;	
						
						struct DistributedEventObjects {
							string distJson;
							MerkleTreeizedSyncEventList events;
							MerkleTreeRootHash rootHash;						
						};
						
						struct Localfullnode2InstanceProfile {
							string shardId;
							string creatorId;
							string nValue;						
						};

						interface DataSynchronization {
							DistributedEventObjects getNotInDistributionEvents(string distJson);
							Localfullnode2InstanceProfile getLocalfullnode2InstanceProfile();     //retrieve LFN2 instance profile
						};			
					
					};				
				};
            };

        };
    };
};
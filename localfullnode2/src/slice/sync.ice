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
						
						sequence<byte> Bytes;
						sequence<Bytes> BytesArray;
						sequence<string> StringArray;

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
							BytesArray merklePath;
							StringArray merklePathIndex;
							
						};
						
						struct MerkleTreeizedSyncMessage {
							string json;
							BytesArray merklePath;
							StringArray merklePathIndex;
							
						};
						
						struct MerkleTreeizedSyncSysMessage {
							string json;
							BytesArray merklePath;
							StringArray merklePathIndex;
							
						};						
						
						sequence<MerkleTreeizedSyncEvent> MerkleTreeizedSyncEventList;	
						sequence<MerkleTreeizedSyncMessage> MerkleTreeizedSyncMessageList;	
						sequence<MerkleTreeizedSyncSysMessage> MerkleTreeizedSyncSysMessageList;	
						
						struct DistributedEventObjects {
							string distJson;
							MerkleTreeizedSyncEventList events;
							MerkleTreeRootHash rootHash;						
						};
						
						struct DistributedMessageObjects {
							string distJson;
							MerkleTreeizedSyncMessageList events;
							MerkleTreeRootHash rootHash;						
						};
						
						struct DistributedSysMessageObjects {
							string distJson;
							MerkleTreeizedSyncSysMessageList events;
							MerkleTreeRootHash rootHash;						
						};						
						
						struct Localfullnode2InstanceProfile {
							int shardId;
							int creatorId;
							int nValue;	
							string dbId;					
						};

						interface DataSynchronization {
							DistributedEventObjects getNotInDistributionEvents(string distJson);
							DistributedMessageObjects getNotInDistributionMessages(string distJson);
							DistributedSysMessageObjects getNotInDistributionSysMessages(string distJson);
							Localfullnode2InstanceProfile getLocalfullnode2InstanceProfile();     //retrieve LFN2 instance profile
						};			
					
					};				
				};
            };

        };
    };
};
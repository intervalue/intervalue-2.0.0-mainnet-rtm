package one.inve.localfullnode2.store.rocks.key;

/**
 * 
 * Copyright © INVE FOUNDATION. All rights reserved.
 * 
 * @ClassName: LastSeqKey
 * @Description: wrap up the lastSeq key generation.
 * @author Francis.Deng [francis_xiiiv@163.com]
 * @see {@code EventStoreImpl}
 * @date Aug 27, 2019
 *
 */
public class LastSeqKey implements RocksdbKey {
	private String key;

	// shardId - shard id,starting from 0
	// idInShard - id in shard,generally speaking,less than nValue
	public LastSeqKey(int shardId, int idInShard) {
		key = shardId + "_" + idInShard;
	}

	@Override
	public byte[] toBytes() {
		return key.getBytes();
	}

}

package one.inve.localfullnode2.store.rocks.key;

/**
 * 
 * Copyright © INVE FOUNDATION. All rights reserved.
 * 
 * @ClassName: FirstSeqKey
 * @Description: wrap up the fistSeq key generation.
 * @author Francis.Deng [francis_xiiiv@163.com]
 * @date Aug 27, 2019
 *
 */
public class FirstSeqKey implements RocksdbKey {
	private String key;

	// shardId - shard id,starting from 0
	// idInShard - id in shard,generally speaking,less than nValue
	public FirstSeqKey(int shardId, int idInShard) {
		key = "f" + shardId + "_" + idInShard;
	}

	@Override
	public byte[] toBytes() {
		return key.getBytes();
	}

}

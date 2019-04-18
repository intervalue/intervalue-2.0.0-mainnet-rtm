package one.inve.rocksDB;

import java.util.Arrays;
import java.util.List;

/**
 * 
 * 
 * Copyright © CHXX Co.,Ltd. All rights reserved.
 * 
 * @Description: The class is capable of cache some pair if keys is specified.
 *               The pair would been directly saved if key doesn't fall within
 *               the scope.
 * @author: Francis.Deng
 * @date: 2019年4月18日 下午3:53:38
 * @version: V1.0
 */
public class CachedRockPutter extends SnailRockPutter {

	private List<String> keys;

	public CachedRockPutter(String dbId, String... keys) {
		this(dbId, 5L, 1000L, keys);
	}

	// An outsider is able to customize initial values.
	public CachedRockPutter(String dbId, long capacity, long intervalInMilli, String... keys) {
		super(dbId, 5L, 1000L);
		this.keys = Arrays.asList(keys);
	}

	@Override
	public void set(String key) {
		if (shouldPairBeenCached(key)) {
			this.put(key.getBytes(), key.getBytes());
		} else {
			this.set(key);
		}
	}

	@Override
	public void put(String key, String value) {
		if (shouldPairBeenCached(key)) {
			this.put(key.getBytes(), value.getBytes());
		} else {
			this.put(key, value);
		}
	}

	@Override
	public void put(String key, byte[] value) {
		if (shouldPairBeenCached(key)) {
			this.put(key.getBytes(), value);
		} else {
			this.put(key, value);
		}
	}

	private boolean shouldPairBeenCached(String key) {
		return keys.contains(key);
	}

}

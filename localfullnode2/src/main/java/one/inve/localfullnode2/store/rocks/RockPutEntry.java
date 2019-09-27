package one.inve.localfullnode2.store.rocks;

import java.util.Arrays;

/**
 * 
 * 
 * Copyright © INVE FOUNDATION. All rights reserved.
 * 
 * @Description: The class was designed to replace old-rusty one with new one if
 *               they has the same key.
 * @author: Francis.Deng
 * @date: 2019年4月17日 上午11:40:40
 * @version: V1.0
 */
public class RockPutEntry {
	private byte[] key;
	private byte[] value;

	public RockPutEntry(byte[] key, byte[] value) {
		this.key = key;
		this.value = value;
	}

	public byte[] key() {
		return key;
	}

	public byte[] value() {
		return value;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(key);
		// result = prime * result + Arrays.hashCode(value);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RockPutEntry other = (RockPutEntry) obj;
		if (!Arrays.equals(key, other.key))
			return false;

		return true;
	}

	@Override
	public String toString() {
		return "RockPutEntry [key=" + Arrays.toString(key) + ", value=" + Arrays.toString(value) + "]";
	}

}

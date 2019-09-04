package one.inve.localfullnode2.store.rocks;

import java.util.Map;

public interface INosql {
	void set(String key);

	void put(String key, String value);

	void put(String key, byte[] value);

	byte[] get(String key);

	void delete(String key);

	Map<byte[], byte[]> startWith(byte[] prefix);

	boolean isPrefixKeyExisted(byte[] prefix);
}

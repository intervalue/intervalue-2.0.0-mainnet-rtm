package one.inve.localfullnode2.utilities;

import java.util.Map;
import java.util.Map.Entry;

import org.junit.Test;

import one.inve.localfullnode2.store.rocks.RocksJavaUtil;
import one.inve.localfullnode2.store.rocks.key.MessageIndexes;

public class RocksJavaUtilTest {
	// @Test
	public void testScanPrefix() {
		RocksJavaUtil rocks = new RocksJavaUtil("0000");
		rocks.put("msgs$hellow", "world");
		rocks.put("msgs$fuck", "gaxi");

		Map<byte[], byte[]> m = rocks.startWith("msgs".getBytes());
		for (Entry<byte[], byte[]> entry : m.entrySet()) {
			System.out.println(new String(entry.getKey()));
			System.out.println(" - ");
			System.out.println(new String(entry.getValue()));
		}

	}

	// @Test
	public void testEmptyValue() {
		RocksJavaUtil rocks = new RocksJavaUtil("0000");
		System.out.println(rocks.isPrefixKeyExisted("msgs$".getBytes()));

		rocks.put("msgs$Jellow".getBytes(), new byte[0]);
		rocks.put("msgs$Guck".getBytes(), new byte[0]);

		Map<byte[], byte[]> m = rocks.startWith("msgs".getBytes());
		for (Entry<byte[], byte[]> entry : m.entrySet()) {
			System.out.println(new String(entry.getKey()));
			System.out.println(" - ");
			System.out.println(new String(entry.getValue()));
		}
	}

	@Test
	public void testAllByPrefix() {
		RocksJavaUtil rocks = new RocksJavaUtil("0_7");

		Map<byte[], byte[]> m0 = rocks.startWith(MessageIndexes.getMessageHashPrefix().getBytes());
		for (Entry<byte[], byte[]> entry : m0.entrySet()) {
			System.out.println(new String(entry.getKey()));
			System.out.println(" - ");
			System.out.println(new String(entry.getValue()));
		}

		Map<byte[], byte[]> m1 = rocks.startWith(MessageIndexes.getSysMessageTypeIdPrefix().getBytes());

		System.out.println("size of message : " + m0.keySet().size());
		System.out.println("size of sysmessage : " + m1.keySet().size());

	}
}

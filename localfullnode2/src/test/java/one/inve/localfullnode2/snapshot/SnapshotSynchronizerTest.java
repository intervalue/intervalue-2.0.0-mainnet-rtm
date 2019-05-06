package one.inve.localfullnode2.snapshot;

import java.math.BigInteger;
import java.security.PublicKey;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Test;

import com.alibaba.fastjson.JSONObject;

import one.inve.bean.message.SnapshotMessage;
import one.inve.cluster.Member;
import one.inve.localfullnode2.gossip.vo.GossipObj;
import one.inve.localfullnode2.snapshot.vo.SnapObj;
import one.inve.localfullnode2.utilities.HnKeyUtils;
import one.inve.transport.Address;

/**
 * 
 * 
 * Copyright © CHXX Co.,Ltd. All rights reserved.
 * 
 * @Description: test case for {@code SnapshotSynchronizer}
 * @author: Francis.Deng
 * @date: May 6, 2019 5:49:39 AM
 * @version: V1.0
 */
public class SnapshotSynchronizerTest {
	@Test
	public void testSynchronize() {
		BlockingQueue queue = new ArrayBlockingQueue(500);
		SnapshotSynchronizerDependency dep = new SnapshotSynchronizerDependency(queue, 300);
		Map meta1 = new HashMap<String, String>() {
			{
				put("pubkey", "m1");
			}
		};
		Member m1 = new Member("1", Address.from("127.0.0.1:1"), meta1);

		Map meta2 = new HashMap<String, String>() {
			{
				put("pubkey", "m2");
			}
		};

		Member m2 = new Member("2", Address.from("127.0.0.1:2"), meta2);

		Map meta3 = new HashMap<String, String>() {
			{
				put("pubkey", "m3");
			}
		};

		Member m3 = new Member("3", Address.from("127.0.0.1:3"), meta3);

		Map meta4 = new HashMap<String, String>() {
			{
				put("pubkey", "m4");
			}
		};

		Member m4 = new Member("4", Address.from("127.0.0.1:4"), meta4);

		SnapObj snapObj = new SnapObj("p1", "p2");
		dep.setExpectedSnapObj(snapObj);

		GossipObj gossipObj = new GossipObj("317", null, "317".getBytes());

		SnapshotSynchronizer synchronizer = new SnapshotSynchronizer();
		synchronizer.synchronize(dep, m1, gossipObj);
		synchronizer.synchronize(dep, m2, gossipObj);
		synchronizer.synchronize(dep, m2, gossipObj);

		Assert.assertFalse(synchronizer.synchronize(dep, m1, gossipObj));
		Assert.assertFalse(synchronizer.synchronize(dep, m2, gossipObj));
		Assert.assertTrue(synchronizer.synchronize(dep, m3, gossipObj));
	}

	protected static class SnapshotSyncCommunication implements SnapshotSyncConsumable {
		private SnapshotSynchronizerDependency dep;

		public SnapshotSyncCommunication(SnapshotSynchronizerDependency dep) {
			this.dep = dep;
		}

		@Override
		public CompletableFuture<SnapObj> gossipMySnapVersion4SnapAsync(Member neighbor, String pubkey, String sig,
				String hash, String transCount) {

			return CompletableFuture.supplyAsync(() -> {
				try {
					TimeUnit.SECONDS.sleep(2000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				return dep.getExpectedSnapObj();
			});
		}

	}

	protected static class SnapshotSynchronizerDependency implements SnapshotSynchronizerDependent {

		@SuppressWarnings("unused")
		private final SnapshotSyncConsumable snapshotSyncCommunication;
		@SuppressWarnings("unused")
		private final BlockingQueue consMessageVerifyQueue;
		@SuppressWarnings("unused")
		private final int currSnapshotVersion;

		private SnapObj expectedSnapObj;

		public SnapshotSynchronizerDependency(BlockingQueue detinationQueue, int currSnapshotVersion) {
			this.snapshotSyncCommunication = new SnapshotSyncCommunication(this);
			this.consMessageVerifyQueue = detinationQueue;
			this.currSnapshotVersion = currSnapshotVersion;
		}

		public SnapObj getExpectedSnapObj() {
			return expectedSnapObj;
		}

		public void setExpectedSnapObj(SnapObj expectedSnapObj) {
			this.expectedSnapObj = expectedSnapObj;
		}

		@Override
		public BigInteger getCurrSnapshotVersion() {
			return BigInteger.valueOf(currSnapshotVersion);
		}

		@Override
		public int getShardId() {
			return 0;
		}

		@Override
		public int getShardCount() {
			return 1;
		}

		@Override
		public int getnValue() {
			return 4;
		}

		@Override
		public BigInteger getConsMessageMaxId() {
			return BigInteger.valueOf(1000);
		}

		@Override
		public SnapshotSyncConsumable getSnapshotSync() {
			return snapshotSyncCommunication;
		}

		@Override
		public PublicKey getPublicKey() {
			return getPublicKeys()[0][0];
		}

		@Override
		public BlockingQueue<JSONObject> getConsMessageVerifyQueue() {
			return consMessageVerifyQueue;
		}

		@Override
		public void refresh(SnapshotMessage syncedSnapshotMessage) {
			System.out.println(JSONObject.toJSONString(syncedSnapshotMessage));

		}

		@Override
		public boolean execute(SnapObj snapObj) {
			System.out.println(snapObj);
			return false;
		}

		protected PublicKey[][] getPublicKeys() {
			PublicKey[][] publicKeys = null;

			try {
				publicKeys = new PublicKey[1][10];

				publicKeys[0][0] = HnKeyUtils.getPublicKey4String(
						"MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQC5bARpAKqMejcxwMXJpNW0abl7TiI2Mgxx3ZPUNxqBZjKcOTCtRwWpwTRrMNG7NrVnQOVaDYx8GVE6a9yexolHQPP6tWZQa7wyvB8qpLHZdnKAyL4zYTHmG1L1E6vr+deeTXfCXspsucgTh81KIpLAO52kmRnsD/74F0/lmMCw2wIDAQAB");
				publicKeys[0][1] = HnKeyUtils.getPublicKey4String(
						"MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCE8svr39JWPKaCDGtaidHXG0saiNfUEF1mgnJRNjxtoj25pf4bcsiXF4qa32HYW1vvKHS3BXSNV8qkrIRqZloCPFCiyHIT3T6uHkXJYAP+/Vctn3dlNtcg5MsuentH4WM6uxyhy5ym0n7lh9EIbdVo+9/1baPTF3bdSlioXu0aDwIDAQAB");
				publicKeys[0][2] = HnKeyUtils.getPublicKey4String(
						"MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCDHOm8oTVbtMQbcMiF9d1YjRnJ+120uQ1hmWt00qXxyFJ1HkECw5eeTyWuvNztOSrj4Kc1HtzXoRPPuY0H4X5oB/WSj16UeWeLXkNkm8dopCHYM5MDNlhBWdkYh3MofJ1VgkZJ8JW8x96kiaJIk6UOkLESWAuIYYqcwndV3NPFQwIDAQAB");
				publicKeys[0][3] = HnKeyUtils.getPublicKey4String(
						"MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCqPk/Rf+/18mOX1qzsc4/3AZRE2Ggl+sg6JkfXrXgkZRGcm/0b1e8OOyR5zMiPj0aVheB5LPR6QeXxWs5HUnceEbgdTgdJq4jzz55RBvIgKISq0MtcQKj7wQO/oy6RqjS1KlkfJ2vArXuaNAAeariCd2Mew6tC3QhmJwCaKUZQDQIDAQAB");
				publicKeys[0][4] = HnKeyUtils.getPublicKey4String(
						"MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDfstb7HwOv4PoebdEKV+QwqkG5TqX47EC8+j7SnlyrAKMQ0fkkIqix3DavoZt3CD0skGs4z3PEM4bt5O0xU0WYqsnrTvOvl2hN91P3vqEeN/txE+sPaO3rRGWbrcxwf8Efdj8hdXSQcd+jxlsdfRfqKia8StPNJiAj4Ad87A2uzQIDAQAB");
				publicKeys[0][5] = HnKeyUtils.getPublicKey4String(
						"MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCOImyQLjDl70rwsI6M2GIelVm4yaC9DZcLAcnpx0f8/pAkcUNYOgIYajiIrJy93sKzFJTTcrVB4TEI9RRsuCBTDebn53vGqjLVk+7Iw97IPh0NpyRc7ZCsXKtUVL2CVCmqN9/noZaOAUm1Ckn2kAaeD+YVx2E1rIDtygBkNQB5kQIDAQAB");
				publicKeys[0][6] = HnKeyUtils.getPublicKey4String(
						"MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQD4ze04jpMEPYhZMRjbnMPswEE9fWYKoLMIG35OC+pvv9svM0VZk0I4L5578klNIK62jd1EkV9lWTQz5S83J4oqB9jpt0xfJjB0VezLA7Ht9oJi1x0hJNCt6agubJ6JtrA+omLIkMEW082xAjkwDFrPUniCX2PHD3chGPDOqO69ZQIDAQAB");
				publicKeys[0][7] = HnKeyUtils.getPublicKey4String(
						"MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCDYx1QbWifvt0P5qEJyvQbVgwl2g7GQyaWXSenKxA0GhE4Ca/NIsnY8krqgDwndnCQ8ILJFx0QflH91ZG3Ajeib9FSXfgSa4NPyAUPV2ChC5FfK+XDAGZEspqkFMQQTupt69bsAxWckHhsWrdJcO6iXjFMPInfbKVbNol7OH8tZQIDAQAB");
				publicKeys[0][8] = HnKeyUtils.getPublicKey4String(
						"MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCGmr0Pv/nQiPfrqK2juQ7O4/s0zkzc+MRP63wmgZdccbzwAQqrBgTYlIK5uJQQqUdnYOBqX7J8KIzKFPYczCykhklafQ4RPdbup9iTtWofmS9fJR8zhJ/KH0WNZAQr3JPDkUCgG8S/HlM7gHELKg12KNxnBZhGeh01beAyhpzHpwIDAQAB");
				publicKeys[0][9] = HnKeyUtils.getPublicKey4String(
						"MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDRIcGafO3nJg8NOINvySmTzQYRAn8YLjJ7NcMfs2d9CaKNid7UXzyIn4G3oeitUTMb3yl3aTotc1JlT3Zw7HfPLYu4oK7U++aSSyV8gD3Flp4y49Rzt74lbxxebm1Ip7p9jelCdDVzWA9KRnG1UAMvhyTmddRbAvLYurq77wQZBwIDAQAB");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			return publicKeys;
		}

	}
}

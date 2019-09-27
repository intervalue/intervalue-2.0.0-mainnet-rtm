package one.inve.localfullnode2.sync;

import org.junit.Test;

public class SyncWorksAssemblerTest {
	// @Test
	public void testCallRemoteService() {
		SyncConfiguration conf = new SyncConfiguration();
		SyncWorksAssembler assembler = new SyncWorksAssembler();
		ISyncContext context = conf.getDefaultContext();
		assembler.run(context);
	}

	@Test
	public void testRunNativeRunner() {
		SyncConfiguration conf = new SyncConfiguration();
		SyncWorksAssembler assembler = new SyncWorksAssembler();
		ISyncContext context = conf.getDefaultContext();
		assembler.run(context);
	}

	public static class SyncConfiguration extends DefSyncTemplate {
		@Override
		public String[] getLFNHostList() {
			// 192.168.207.129:35801
			// 172.17.2.120:35795
			return new String[] { "172.17.2.120:35795" };
		}

		@Override
		public String[] getSynchronizationWorkClassNames() {
			// return new String[] {
			// "one.inve.localfullnode2.sync.partofwork.EventIterativePart" };
			return new String[] { "one.inve.localfullnode2.sync.partofwork.EventIterativePart" };
		}

	}
}

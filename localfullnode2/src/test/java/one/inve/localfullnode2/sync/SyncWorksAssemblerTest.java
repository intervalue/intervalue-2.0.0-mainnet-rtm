package one.inve.localfullnode2.sync;

import org.junit.Test;

public class SyncWorksAssemblerTest {
	@Test
	public void testRun() {
		SyncConfiguration conf = new SyncConfiguration();
		SyncWorksAssembler assembler = new SyncWorksAssembler();
		ISyncContext context = conf.getDefaultContext();
		assembler.run(context);
	}

	public static class SyncConfiguration extends DefSyncTemplate {
		@Override
		public String[] getLFNHostList() {
			return new String[] { "192.168.207.129:35801" };
		}

		@Override
		public String[] getSynchronizationWorkClassNames() {
			return new String[] { "one.inve.localfullnode2.sync.partofwork.EventIterativePart" };
		}

	}
}

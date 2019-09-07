package one.inve.localfullnode2.sync;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import one.inve.localfullnode2.sync.SyncWorksInLab.IterativePart;
import one.inve.localfullnode2.sync.SyncWorksInLab.SynchronizationWorkInitial;
import one.inve.localfullnode2.sync.measure.ChunkDistribution;
import one.inve.localfullnode2.sync.measure.ChunkDistribution.Session;
import one.inve.localfullnode2.sync.measure.Distribution;
import one.inve.localfullnode2.sync.source.ILFN2Profile;
import one.inve.localfullnode2.sync.source.ISyncSource;
import one.inve.localfullnode2.sync.source.ProxiedSyncSource;
import one.inve.localfullnode2.utilities.ReflectionUtils;

/**
 * 
 * Copyright © INVE FOUNDATION. All rights reserved.
 * 
 * @ClassName: IContext
 * @Description: Context collects system runtime status and create system
 *               variables based on configuration.
 * @author Francis.Deng
 * @mailbox francis_xiiiv@163.com
 * @date Aug 23, 2019
 *
 */
public interface ISyncContext {
	// String SOURCE_PROFILE = "SOURCE_PROFILE";

	ISyncConf getConf();

	void join(Distribution newDist);

	Distribution getDistribution();

	boolean joinMessageDistribution(ChunkDistribution<String> newDist);

	ChunkDistribution<String> getMessageDistribution();

	boolean joinSysMessageDistribution(ChunkDistribution<String> newDist);

	ChunkDistribution<String> getSysMessageDistribution();

	// the class have to extend {@cod ProxiedSyncSource}
	ISyncSource getSyncSourceProxy();

	IterativePart[] getSynchronizationWorkParts();

	SynchronizationWorkInitial getSynchronizationInitializer();

	void setProfile(ILFN2Profile profile);

	ILFN2Profile getProfile();

	public static ISyncContext getDefault(ISyncConf conf) {
		return conf.getDefaultContext();
	}

	public static <T> Key<T> newKey(String identifier, Class<T> type) {
		return new Key<T>(identifier, type);
	}

	public static class DefSyncContext implements ISyncContext {
		private static ThreadLocal<Session<String>> threadLocal = new ThreadLocal<>();

		private final Map<Key<?>, Object> values = new HashMap<>();
		private ISyncConf conf;

		private Distribution dist;

		private ChunkDistribution<String> messageDist = null;

		private ILFN2Profile profile;

		protected DefSyncContext(ISyncConf conf) {
			this.conf = conf;
		}

		@Override
		public ISyncConf getConf() {
			return conf;
		}

		@Override
		public IterativePart[] getSynchronizationWorkParts() {
			String[] classNames = conf.getSynchronizationWorkClassNames();
			ArrayList<IterativePart> parts = new ArrayList<>();

			for (String clazzName : classNames) {
				parts.add((IterativePart) ReflectionUtils.getInstanceByClassName(clazzName));
			}

			return parts.toArray(new IterativePart[parts.size()]);
		}

		public <T> void with(Key<T> key, T value) {
			values.put(key, value);
		}

		public <T> T of(Key<T> key) {
			return key.type.cast(values.get(key));
		}

		@Override
		public void join(Distribution newDist) {
			dist.addDistribution(newDist);
		}

		@Override
		public Distribution getDistribution() {
			if (dist == null)
				dist = new Distribution(profile.getNValue());
			return dist;
		}

		@Override
		public SynchronizationWorkInitial getSynchronizationInitializer() {
			String clazzName = conf.getSynchronizationInitializerClassName();
			return (SynchronizationWorkInitial) ReflectionUtils.getInstanceByClassName(clazzName);
		}

		@Override
		public ISyncSource getSyncSourceProxy() {
			String clazzName = conf.getSyncSourceProxyClassName();
			ProxiedSyncSource syncSource = (ProxiedSyncSource) ReflectionUtils.getInstanceByClassName(clazzName);

			syncSource.setCommunicator(conf.getCommunicator());
			syncSource.setAddresses(conf.getLFNHostList());

			return syncSource;
		}

		@Override
		public void setProfile(ILFN2Profile profile) {
			this.profile = profile;

		}

		@Override
		public ILFN2Profile getProfile() {
			return profile;
		}

		@Override
		public ChunkDistribution<String> getMessageDistribution() {
			if (messageDist == null) {
				messageDist = new ChunkDistribution<>();
			}

			return messageDist;
		}

		@Override
		public ChunkDistribution<String> getSysMessageDistribution() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public boolean joinMessageDistribution(ChunkDistribution<String> newDist) {
			if (threadLocal.get() == null) {
				Session<String> ss = (Session<String>) newDist.save();
				threadLocal.set(ss);

			} else {
				newDist.restore(threadLocal.get());

			}

			if (!newDist.prepareNextRound(500))// no more elements at all
				return false;
//			ArrayList<String> ids = dist.getNextPartOfElements();
//			System.out.println("client prepares for :" + Arrays.toString(ids.toArray(new String[ids.size()])));

			newDist.cleanUp();
			messageDist = newDist;

			return true;
		}

		@Override
		public boolean joinSysMessageDistribution(ChunkDistribution<String> newDist) {
			return false;

		}

	}

	public class Key<T> {

		final String identifier;
		final Class<T> type;

		public Key(String identifier, Class<T> type) {
			this.identifier = identifier;
			this.type = type;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((identifier == null) ? 0 : identifier.hashCode());
			result = prime * result + ((type == null) ? 0 : type.hashCode());
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
			Key other = (Key) obj;
			if (identifier == null) {
				if (other.identifier != null)
					return false;
			} else if (!identifier.equals(other.identifier))
				return false;
			if (type == null) {
				if (other.type != null)
					return false;
			} else if (!type.getTypeName().equals(other.type.getTypeName()))
				return false;
			return true;
		}

	}

	<T> void with(Key<T> key, T value);

	<T> T of(Key<T> key);
}

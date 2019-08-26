package one.inve.localfullnode2.sync;

import java.util.HashMap;
import java.util.Map;

import one.inve.localfullnode2.sync.SynchronizationWork.IterativePart;
import one.inve.localfullnode2.sync.SynchronizationWork.Whole;
import one.inve.localfullnode2.sync.measure.Distribution;
import one.inve.localfullnode2.sync.source.ISyncSource;

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
public interface IContext {
	String SOURCE_PROFILE = "SOURCE_PROFILE";

	IConf getConf();

	void joinDistribution(Distribution newDist);

	Distribution getDistribution();

	ISyncSource getSyncSource();

	IterativePart[] getSynchronizationWorkParts();

	Whole getSynchronizationInitializer();

	static DefContext context = null;

	public static IContext getDefault(IConf conf) {
		return context;
	}

	public static <T> Key<T> newKey(String identifier, Class<T> type) {
		return new Key<T>(identifier, type);
	}

	public class DefContext implements IContext {
		private final Map<Key<?>, Object> values = new HashMap<>();

		@Override
		public IConf getConf() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public ISyncSource getSyncSource() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public IterativePart[] getSynchronizationWorkParts() {
			// TODO Auto-generated method stub
			return null;
		}

		public <T> void with(Key<T> key, T value) {
			values.put(key, value);
		}

		public <T> T of(Key<T> key) {
			return key.type.cast(values.get(key));
		}

		@Override
		public void joinDistribution(Distribution newDist) {
			// TODO Auto-generated method stub

		}

		@Override
		public Distribution getDistribution() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Whole getSynchronizationInitializer() {
			// TODO Auto-generated method stub
			return null;
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

package one.inve.contract;

import one.inve.contract.conf.ContractConfigurable;
import one.inve.contract.ethplugin.core.Repository;
import one.inve.contract.ethplugin.invocation.Constructors;
import one.inve.contract.ethplugin.invocation.ConstructorsConstructor;
import one.inve.contract.shell.ContractShell;
import one.inve.node.Main;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.util.Optional;

/**
 * 
 * 
 * Copyright © CHXX Co.,Ltd. All rights reserved.
 * 
 * @Description: facade class to give access to inner contract objects
 * @author: Francis.Deng
 * @date: 2018年11月2日 上午11:11:40
 * @version: V1.0
 * @version: v1.1 set up hostNodeRef inside optional container
 * @version: v1.2 introduce vm's Repository and file mapping memory
 * @version: v1.3 fix the defect of "missing DbFlushManager"
 * @version: v1.4 introduce RepositoryMixed along with getRepositoryMixed to
 *           keep two types of Repository
 * @version: v1.5 repository adjustment
 */
public class Contract {
	private static final Logger logger = LoggerFactory.getLogger(Contract.class);
	private static volatile Contract self;
	private ContractConfigurable configurator;
	private ContractShell shell;

	private Optional<Main> hostNodeRef;

	private Constructors c;

	private RepositoryMixed repositoryMixed;

	private Contract() {
		this(null);
	}

	private Contract(Main hostNode) {
		// Prevent form the reflection api.
		if (self != null) {
			throw new IllegalArgumentException("Use getInstance() method to get the single instance of this class.");
		}

		// hostNodeRef = hostNode;
		hostNodeRef = Optional.ofNullable(hostNode);
		shell = new ContractShell(this);
		c = ConstructorsConstructor.getConstructors();
	}

	// to support out-of-fashion caller
	public static Contract getInstance() {
		return getInstance(null);
	}

	public static Contract getInstance(Main hostNode) {
		// Double check locking pattern
		if (self == null) { // Check for the first time
			synchronized (Contract.class) { // Check for the second time.
				// if there is no instance available... create new one
				if (self == null) {
					self = new Contract(hostNode);
				}
			}
		}

		return self;
	}

	public Main getHostNode() {
//		if (hostNodeRef.isEmpty()) {
//			throw new NullPointerException("did not fill contract with host node object at the first beginning");
//		}
//		return hostNodeRef;
		return hostNodeRef.orElseThrow(
				() -> new NullPointerException("did not fill contract with host node object at the first beginning"));
	}

	public ContractConfigurable getConfigurator() {
		return configurator;
	}

	public ContractShell getShell() {
		return shell;
	}

	// Note that track is applicable to streaming in,in the case of streaming
	// out,use cacheTrack which executed by "cacheTrack = track.startTracking();"
//	public Repository getTrack() {
//		return c.getRepository();
//	}

	// Note that it is a resource which should been closed if finished.
//	public one.inve.contract.ethplugin.invocation.Appendable getByteBuf() {
//		return c.getAppendedBuf();
//	}

//	public DbFlushManager getDbFlushManager() {
//		return c.getDbFlushManager();
//	}

	public static interface RepositoryMixed {
//		Repository getWritableRepository();// applicable to the cases of writing data

		Repository getTrack();// applicable to the cases of querying data

//		boolean isPrehistoric();

//		DbFlushManager getDbFlushManager();
	}

	public synchronized RepositoryMixed getRepositoryMix() {

		if (repositoryMixed == null) {
			repositoryMixed = new RepositoryMixed() {
//				private boolean isPrehistoric = false;

				private Repository trackRef;
//				private Repository writableRepositoryRef;

				@Override
				public synchronized Repository getTrack() {// read latest repository root from memory-mapping file to
															// recover state.

					if (trackRef == null) {
						// byte[] bytes = c.getAppendedBuf().readLast();

						// fix:turn append buffer into last root record
						byte[] bytes = c.getAppendedBuf().readFirst();
						Repository snap = null;

						// significant change of making use of Repository
						//
//						if (bytes == null) {
//							isPrehistoric = true;
//
//							snap = c.getRepository().startTracking();
//						} else {
//							// ????
//							c.getRepository().syncToRoot(bytes);
//							snap = c.getRepository();
//						}
						if (bytes == null) {// is prehistory
							snap = c.getRepository();
						} else {// rebuild state trie
							snap = c.getRepository().getSnapshotTo(bytes);
						}

						trackRef = snap;
					}

					// return snap;
					return trackRef;
				}

//				@Override
//				public synchronized Repository getWritableRepository() {
//
//					if (writableRepositoryRef == null) {
//						Repository track = c.getRepository();
//						Repository cacheTrack = track.startTracking();
//
//						writableRepositoryRef = cacheTrack;
//					}
//
//					// return cacheTrack;
//					return writableRepositoryRef;
//				}
//
//				@Override
//				public boolean isPrehistoric() {
//					return isPrehistoric;
//				}

//				@Override
//				public DbFlushManager getDbFlushManager() {
//					return c.getDbFlushManager();
//				}
			};
		}
		return repositoryMixed;
	}

	// flush repository to the disk,and save the root of repository into mem-mapping
	// file
	public void flushRepository() {
//		byte[] bytes = c.getRepository().getRoot();
//		c.getAppendedBuf().write(bytes);
//
//		c.getRepository().commit();
//		c.getDbFlushManager().flush();
//		c.getDbFlushManager().commit();
		byte[] bytes = getRepositoryMix().getTrack().getRoot();
		c.getAppendedBuf().write(bytes);

		getRepositoryMix().getTrack().commit();
		c.getDbFlushManager().flush();
		c.getDbFlushManager().commit();

	}

	public void dispose() {
		if (Closeable.class.isAssignableFrom(c.getAppendedBuf().getClass())) {
			try {
				((Closeable) c.getAppendedBuf()).close();
			} catch (IOException e) {
				e.printStackTrace();
				logger.error("error: {}", e);
			}
		}
	}

}

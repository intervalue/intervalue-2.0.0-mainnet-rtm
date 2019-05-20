package one.inve.contract.ethplugin.invocation;

import one.inve.contract.ethplugin.core.Repository;
import one.inve.contract.ethplugin.db.DbFlushManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * 
 * 
 * Copyright © CHXX Co.,Ltd. All rights reserved.
 *
 * @author: Francis.Deng
 * @date: Nov 16, 2018 12:03:28 AM
 * @version: V1.0
 */
@Component
public class Constructors implements IConstructor {

	Constructors() {

	}

	@Autowired
	@Qualifier("defaultRepository")
	private Repository repository;

	@Autowired
	private DbFlushManager bbFlushManager;

	@Autowired
	// @Qualifier("ordinaryBinFile")
	private Appendable appendedBuf;

	// WorldManager.repository
	// \
	// RepositoryWrapper
	// \
	// BlockchainImpl.repository
	// \
	// new RepositoryRoot(stateSource(), null)
	@Override
	public Repository getRepository() {
		// CommonConfig config = new CommonConfig();
		//
		// return config.defaultRepository();
		return repository;
	}

	@Override
	public DbFlushManager getDbFlushManager() {
		return bbFlushManager;
	}

	@Override
	public Appendable getAppendedBuf() {
		return appendedBuf;
	}

}

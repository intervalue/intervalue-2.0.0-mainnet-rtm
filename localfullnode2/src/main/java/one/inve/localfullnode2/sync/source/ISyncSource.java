package one.inve.localfullnode2.sync.source;

import one.inve.core.EventBody;
import one.inve.localfullnode2.sync.DistributedObjects;
import one.inve.localfullnode2.sync.ISyncContext;
import one.inve.localfullnode2.sync.measure.ChunkDistribution;
import one.inve.localfullnode2.sync.measure.Distribution;

/**
 * 
 * Copyright © INVE FOUNDATION. All rights reserved.
 * 
 * @ClassName: ISyncSource
 * @Description: from the perspective of client,the class provide access to rpc
 *               interface.
 * @author Francis.Deng
 * @mailbox francis_xiiiv@163.com
 * @date Aug 15, 2019
 *
 */
public interface ISyncSource {
	ILFN2Profile getProfile(ISyncContext context);// get localfullnode2 metadata and keep it inside the context

	DistributedObjects<Distribution, EventBody> getNotInDistributionEvents(Distribution dist);

	DistributedObjects<ChunkDistribution<String>, String> getNotInDistributionMessages(ChunkDistribution<String> dist);

	DistributedObjects<ChunkDistribution<String>, String> getNotInDistributionSysMessages(
			ChunkDistribution<String> dist);
}

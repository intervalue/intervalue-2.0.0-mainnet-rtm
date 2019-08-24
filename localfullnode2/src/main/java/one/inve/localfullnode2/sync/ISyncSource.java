package one.inve.localfullnode2.sync;

import com.alibaba.fastjson.JSONObject;

import one.inve.core.EventBody;

/**
 * 
 * Copyright © INVE FOUNDATION. All rights reserved.
 * 
 * @ClassName: ISyncSource
 * @Description: the impls should take on responsibility to retrieve good blocks
 *               if tampered blocks are detected.
 * @author Francis.Deng
 * @mailbox francis_xiiiv@163.com
 * @date Aug 15, 2019
 *
 */
public interface ISyncSource {
	ISyncSourceProfile getSyncSourceProfile();// call it once

	DistributedO<EventBody> getNotInDistributionEvents(Distribution dist);

	DistributedO<JSONObject> getNotInDistributionMessages(Distribution dist);
}

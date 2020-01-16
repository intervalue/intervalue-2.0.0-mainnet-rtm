package one.inve.localfullnode2.chronicle.rpc.service;

/**
 * 
 * Copyright © INVE FOUNDATION. All rights reserved.
 * 
 * @ClassName: IServicesRuntime
 * @Description: TODO
 * @author Francis.Deng [francis_xiiiv@163.com]
 * @date Jan 14, 2020
 *
 */
public interface IServicesRuntime {
	Iterable<String> messageHashesIter();

	Iterable<String> sysMessageHashesIter();

	IMessageQuery getMessageQuery();

	IMessagePersister getMessagePersister();

	public static interface IMessageQuery {
		byte[] byHash(String hash);
	}

	public static interface IMessagePersister {
		void persist(byte[] wrappedMessageBytes);

		@Deprecated
		void persistSys(byte[] wrappedMessageBytes);
	}
}

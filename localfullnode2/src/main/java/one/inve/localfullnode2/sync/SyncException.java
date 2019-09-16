package one.inve.localfullnode2.sync;

/**
 * 
 * 
 * Copyright © INVE FOUNDATION. All rights reserved.
 * 
 * @Description: sync exception
 * @author: Francis.Deng [francis_xiiiv@163.com]
 * @date: Aug 31, 2019 2:52:42 AM
 * @version: V1.0
 */
public class SyncException extends RuntimeException {

	private static final long serialVersionUID = 8502619116964097909L;

	public SyncException() {
		super();
	}

	public SyncException(String message) {
		super(message);
	}

	public SyncException(String message, Throwable cause) {
		super(message, cause);
	}

	public SyncException(Throwable cause) {
		super(cause);
	}
}

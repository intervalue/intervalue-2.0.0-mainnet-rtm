package one.inve.localfullnode2.lc;

/**
 * 
 * Copyright © INVE FOUNDATION. All rights reserved.
 * 
 * @Description: We see handling process,thread execution... as a life cycle
 *               which could been started,stopped,knowing running
 *               state{@code isRunning()}.
 * @author: Francis.Deng
 * @date: Jan 20, 2019 8:27:50 PM
 * @version: V1.0
 */
public interface ILifecycle {

	void start();

	void stop();

	boolean isRunning();

}

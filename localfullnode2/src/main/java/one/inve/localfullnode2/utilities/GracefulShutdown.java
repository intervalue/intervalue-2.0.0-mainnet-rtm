package one.inve.localfullnode2.utilities;

import java.util.ArrayDeque;
import java.util.Collection;

import one.inve.localfullnode2.lc.ILifecycle;
import sun.misc.Signal;
import sun.misc.SignalHandler;

/**
 * 
 * 
 * Copyright © CHXX Co.,Ltd. All rights reserved.
 * 
 * @Description: At least the system need to handle signal of
 *               {@code SIGKILL	9	Kill(can't be caught or ignored)},{@code SIGTERM	15	Termination (ANSI)}
 * @author: Francis.Deng
 * @see {@code java.lang.IllegalArgumentException: Signal already used by VM or OS: SIGKILL},which
 *      indicates that "SIGTERM" is only reasonable option
 * @date: May 20, 2019 11:20:48 PM
 * @version: V1.0
 */
public class GracefulShutdown implements SignalHandler {

	private Collection<ILifecycle> lcs = new ArrayDeque<>();

	private SignalHandler oldHandler;

	public static GracefulShutdown with(String sigName) {
		Signal diagSignal = new Signal(sigName);
		GracefulShutdown instance = new GracefulShutdown();
		instance.oldHandler = Signal.handle(diagSignal, instance);
		return instance;
	}

	@Override
	public void handle(Signal signal) {
		try {

			lcs.parallelStream().forEach((e) -> {
				e.stop();
				while (e.isRunning()) {
					// empty loop until shutdown is completed.
				}
			});

			// Chain back to previous handler, if one exists
			if (oldHandler != SIG_DFL && oldHandler != SIG_IGN) {
				oldHandler.handle(signal);
			}

		} catch (Exception e) {
			System.out.println("handle|Signal handler failed, reason " + e.getMessage());
			e.printStackTrace();
		}

	}

	public void addLcs(ILifecycle lc) {
		lcs.add(lc);
	}

	// java -cp localfullnode2-2.0.0.jar
	// one.inve.localfullnode2.utilities.GracefulShutdown
//	public static void main(String[] args) {
//		GracefulShutdown gs = GracefulShutdown.with("TERM");
//
//		gs.addLcs(new Nothing());
//
//		sleep(200);
//	}
//
//	public static class Nothing implements ILifecycle {
//		private boolean isRunning = false;
//
//		@Override
//		public void start() {
//			isRunning = true;
//			sleep(5);
//			System.out.println("start");
//
//		}
//
//		@Override
//		public void stop() {
//			System.out.println("stop");
//			sleep(5);
//			isRunning = false;
//
//		}
//
//		@Override
//		public boolean isRunning() {
//			return isRunning;
//		}
//
//	}
//
//	static void sleep(int seconds) {
//		try {
//			TimeUnit.SECONDS.sleep(seconds);
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}

}

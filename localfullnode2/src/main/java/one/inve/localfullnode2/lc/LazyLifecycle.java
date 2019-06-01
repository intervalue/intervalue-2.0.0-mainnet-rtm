package one.inve.localfullnode2.lc;

import java.util.concurrent.TimeUnit;

public class LazyLifecycle implements ILifecycle {
	protected boolean isRunning = false;

	@Override
	public void start() {
		isRunning = true;

	}

	@Override
	public void stop() {
		isRunning = false;

	}

	@Override
	public boolean isRunning() {
		return isRunning;
	}

	protected void sleep(int seconds) {
		try {
			TimeUnit.SECONDS.sleep(seconds);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	protected void sleepMilliSeconds(long milliSeconds) {
		try {
			TimeUnit.MILLISECONDS.sleep(milliSeconds);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}

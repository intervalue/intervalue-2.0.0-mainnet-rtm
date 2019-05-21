package one.inve.localfullnode2.lc;

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

}

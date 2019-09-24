package one.inve.localfullnode2.hashnet;

public class Stop {
	private boolean stop = false;

	public boolean get() {
		return stop;
	}

	public void flip() {
		stop = !stop;
	}
}

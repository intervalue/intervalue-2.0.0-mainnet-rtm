package one.inve.localfullnode2.cons;

public class EventsBuilder {
	public Hashnet[] build(Hashnet[] hashnets, int sleepingInMilliseconds, IThreadGroup tg) {
		Stop stopIt = new Stop();
		Flag flag = new Flag(hashnets);

//		buildGrabFlagThread(0, 0, stopIt, flag, hashnets.length);
//		buildGrabFlagThread(0, 1, stopIt, flag, hashnets.length);
//		buildGrabFlagThread(0, 2, stopIt, flag, hashnets.length);
//		buildGrabFlagThread(0, 3, stopIt, flag, hashnets.length);
		tg.startThreads();

		try {
			Thread.currentThread().sleep(sleepingInMilliseconds);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		stopIt.flip();

		try {
			Thread.currentThread().sleep(1 * 1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return flag.getHashnet();
	}

	public static interface IThreadGroup {
		void startThreads();
	}

}

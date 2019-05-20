package one.inve.localfullnode2.hashnet;

public interface IHashneter {
	void initHashnet(HashneterDependent dep) throws InterruptedException;

	void addToHashnet(HashneterDependent dep, int shardId);

	Event[] getAllConsEvents(int shardId);
}

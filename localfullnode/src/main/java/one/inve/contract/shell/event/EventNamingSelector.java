package one.inve.contract.shell.event;

/**
 * 
 * 
 * Copyright © CHXX Co.,Ltd. All rights reserved.
 * 
 * @Description: simply select the events of the same naming along with some
 *               handy event selectors.
 * @author: Francis.Deng
 * @date: 2018年11月2日 上午11:31:45
 * @version: V1.0
 */
public class EventNamingSelector implements IEventSelector {
	public static final String ConsensusCompletionEventNaming = "consensus-completion-event";
	public static final EventNamingSelector ConsensusCompletionEventSelector = new EventNamingSelector(
			ConsensusCompletionEventNaming);

	// be applied to mock environment
	public static final String ConsensusCompletionInMockEnvironmentEventNaming = "consensus-completion-in-mock-environment-event";
	public static final EventNamingSelector ConsensusCompletionInMockEnvironmentEventSelector = new EventNamingSelector(
			ConsensusCompletionInMockEnvironmentEventNaming);

	private String naming;

	@SuppressWarnings("unused")
	public EventNamingSelector(String naming) {
		if (naming == null && naming == "") {
			throw new IllegalArgumentException("not meaningful name");
		}

		this.naming = naming;
	}

	@Override
	public boolean qualified(IEvent event) {
		boolean ret = false;

		if (event.getName().equals(event.getName()))
			ret = true;

		return ret;
	}

}

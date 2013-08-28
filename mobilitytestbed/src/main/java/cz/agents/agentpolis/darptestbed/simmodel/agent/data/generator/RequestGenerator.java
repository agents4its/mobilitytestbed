package cz.agents.agentpolis.darptestbed.simmodel.agent.data.generator;

import java.util.Set;

public interface RequestGenerator {

	/**
	 * Start generating request for the passenger.
	 */
	public abstract void start(String agentId, Set<String> additionalRequirements,
			RequestGeneratorCall requestGeneratorCall);

	/**
	 * This method can be called by a timer (e.g. PassengerTimer) at regular
	 * intervals.
	 */
	public abstract void timerSignal();

}
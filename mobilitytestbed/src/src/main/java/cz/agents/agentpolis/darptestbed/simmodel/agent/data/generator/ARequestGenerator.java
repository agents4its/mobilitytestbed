package cz.agents.agentpolis.darptestbed.simmodel.agent.data.generator;

import cz.agents.agentpolis.darptestbed.simmodel.agent.passenger.PassengerProfile;
import cz.agents.agentpolis.simmodel.environment.model.query.AgentPositionQuery;

/**
 * Generator, which makes up requests for rides. It generates request according
 * to a passenger's profile.
 * 
 * @author Lukas Canda
 */
public abstract class ARequestGenerator implements RequestGenerator {

	// protected String passengerId;
	// /**
	// * The profile of the passenger, who we generate requests for
	// */
	protected PassengerProfile passengerProfile;
	/**
	 * An object, that helps us find the passenger's current position.
	 */
	protected AgentPositionQuery positionQuery;

	// /**
	// * Our connection to the passenger. Its method helps me send requests
	// * through the passenger.
	// */
	// protected RequestGeneratorCall requestGeneratorCall;

	public ARequestGenerator(PassengerProfile passengerProfile, AgentPositionQuery positionQuery) {

		this.passengerProfile = passengerProfile;
		this.positionQuery = positionQuery;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see cz.agents.agentpolis.darptestbed.simmodel.agent.data.generator.
	 * RequestGenerator#timerSignal()
	 */
	@Override
	public void timerSignal() {
	}

}

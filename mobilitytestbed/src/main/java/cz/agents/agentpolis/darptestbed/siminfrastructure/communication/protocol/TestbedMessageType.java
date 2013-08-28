package cz.agents.agentpolis.darptestbed.siminfrastructure.communication.protocol;

import cz.agents.agentpolis.ondemandtransport.siminfrastructure.communication.protocol.data.MessageType;

/**
 * All message types used in the testbed application.
 * 
 * @author Lukas Canda
 */
@Deprecated
public enum TestbedMessageType implements MessageType {

	PASSENGER_REQUESTS_TAXI_FROM_DISPATCHING, PASSENGER_REQUESTS_TAXI_DIRECTLY, PASSENGER_ACCEPTS_PROPOSAL, PASSENGER_REJECTS_PROPOSAL, PASSENGER_IS_IN_TAXI, PASSENGER_SAYS_TAXI_IS_TOO_LATE, DISPATCHING_SENDS_TAXI_TO_PASSENGER, DISPATCHING_SENDS_CONFIRMATION_TO_PASSENGER, TAXI_ARRIVED_TO_PASSENGER, TAXI_PROPOSES_TO_PASSENGER, TAXI_SENDS_CONFIRMATION_TO_PASSENGER;
}

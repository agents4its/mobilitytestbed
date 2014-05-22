package cz.agents.agentpolis.darptestbed.simmodel.agent.passenger.logic;

import java.util.List;

import cz.agents.agentpolis.darptestbed.global.Utils;
import cz.agents.agentpolis.darptestbed.global.data.DriverAndDistance;
import cz.agents.agentpolis.darptestbed.siminfrastructure.communication.driver.protocol.DriverMessageProtocol;
import cz.agents.agentpolis.darptestbed.siminfrastructure.communication.passenger.message.Proposal;
import cz.agents.agentpolis.darptestbed.siminfrastructure.communication.requestconsumer.message.ProposalAccept;
import cz.agents.agentpolis.darptestbed.siminfrastructure.communication.requestconsumer.message.ProposalReject;
import cz.agents.agentpolis.darptestbed.siminfrastructure.communication.requestconsumer.protocol.RequestConsumerMessageProtocol;
import cz.agents.agentpolis.darptestbed.siminfrastructure.logger.RequestLogger;
import cz.agents.agentpolis.darptestbed.simmodel.agent.activity.movement.TestbedPassengerActivity;
import cz.agents.agentpolis.darptestbed.simmodel.agent.data.Request;
import cz.agents.agentpolis.darptestbed.simmodel.agent.passenger.PassengerProfile;
import cz.agents.agentpolis.darptestbed.simmodel.environment.model.TestbedModel;
import cz.agents.agentpolis.simmodel.agent.activity.TimeSpendingActivity;
import cz.agents.agentpolis.simmodel.environment.model.query.AgentPositionQuery;

/**
 * The basic features of a PassengerAgent, especially his communication protocol
 * that enables him to contact other agents.
 * 
 * The decentralized communication means that the passenger communicates
 * directly with taxi drivers.
 * 
 * @author Lukas Canda
 */
public abstract class PassengerDecentralizedLogic extends PassengerLogicWithRequestConsumerMessageProtocol {

	public PassengerDecentralizedLogic(String agentId, RequestConsumerMessageProtocol sender,
                                       DriverMessageProtocol driverMessageProtocol, TestbedModel taxiModel, AgentPositionQuery positionQuery,
                                       Utils utils, PassengerProfile passengerProfile, TestbedPassengerActivity passengerActivity,
                                       TimeSpendingActivity timeSpendingActivity, RequestLogger logger) {

		super(agentId, sender, driverMessageProtocol, taxiModel, positionQuery, utils, passengerProfile,
				passengerActivity, timeSpendingActivity, logger);
	}

	/**
	 * Sends a request either to all drivers (the version with diversion) OR
	 * only to free drivers. Either only to the closest ones or to all of them.
	 * 
	 * This method is usually called only by a request generator through the
	 * passenger.
	 * 
	 * @param request
	 *            request to be sent
	 */
	public void sendRequest(Request request) {
		super.sendRequest(request);
	}

	/**
	 * Process the queue of proposals (usually choose the best one and accept it
	 * and reject others). This method is usually indirectly called by a timer
	 * at regular intervals.
	 */
	public abstract void processProposals();

	/**
	 * Sends a message "your proposal has been accepted" as a reply to the best
	 * proposal
	 */
	protected void sendProposalAccepted(Proposal proposal) {
		sender.sendMessage(proposal.getDriverId(), new ProposalAccept(proposal));

	}

	/**
	 * Send a message "your proposal has been rejected" as a reply to all the
	 * taxi drivers from the list
	 * 
	 * @param taxiDriversIds
	 *            receivers of the rejection
	 */
	protected void sendProposalRejected(List<String> taxiDriversIds, Proposal proposal) {
		for (String taxiDriverId : taxiDriversIds) {

			sender.sendMessage(taxiDriverId, new ProposalReject(proposal));
		}
	}
	
	/**
	 * Return the id of the taxi driver closest to node with nodeId
	 * 
	 * @param nodeId
	 *            node from which we measure distances
	 * @param useEndOfTripPositions
	 *            compare using taxi's end of trip position, instead of current position  
	 */
	protected String getClosestDriverId(long nodeId, boolean useEndOfTripPositions) {
		DriverAndDistance[] driverAndDistances = getClosestDrivers(nodeId, useEndOfTripPositions);
		if (driverAndDistances != null && driverAndDistances.length > 0) {
			return driverAndDistances[0].getTaxiDriverId();
		}

		return null;

	}

	protected DriverAndDistance[] getClosestDrivers(long nodeId, boolean useEndOfTripPositions) {
		return utils.getDistMapForPassenger(nodeId, taxiModel.getAllTaxiDrivers(),
				useEndOfTripPositions);
	}

    public final boolean isDecentralized() {
        return true;
    }
}

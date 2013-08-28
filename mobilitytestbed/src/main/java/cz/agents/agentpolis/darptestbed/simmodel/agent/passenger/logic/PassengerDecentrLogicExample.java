package cz.agents.agentpolis.darptestbed.simmodel.agent.passenger.logic;

import java.util.ArrayList;

import org.h2.command.dml.Set;

import com.sun.istack.logging.Logger;

import cz.agents.agentpolis.darptestbed.global.Utils;
import cz.agents.agentpolis.darptestbed.siminfrastructure.communication.driver.protocol.DriverMessageProtocol;
import cz.agents.agentpolis.darptestbed.siminfrastructure.communication.passenger.message.Proposal;
import cz.agents.agentpolis.darptestbed.siminfrastructure.communication.passenger.message.RequestReject;
import cz.agents.agentpolis.darptestbed.siminfrastructure.communication.requestconsumer.message.ProposalAccept;
import cz.agents.agentpolis.darptestbed.siminfrastructure.communication.requestconsumer.protocol.RequestConsumerMessageProtocol;
import cz.agents.agentpolis.darptestbed.siminfrastructure.logger.RequestLogger;
import cz.agents.agentpolis.darptestbed.simmodel.agent.activity.movement.TestbedPassengerActivity;
import cz.agents.agentpolis.darptestbed.simmodel.agent.data.Request;
import cz.agents.agentpolis.darptestbed.simmodel.agent.driver.logic.DriverLogic;
import cz.agents.agentpolis.darptestbed.simmodel.agent.passenger.PassengerProfile;
import cz.agents.agentpolis.darptestbed.simmodel.environment.model.TestbedModel;
import cz.agents.agentpolis.simmodel.agent.activity.TimeSpendingActivity;
import cz.agents.agentpolis.simmodel.environment.model.query.AgentPositionQuery;

/**
 *	Example of decentralized control mechanism. Related classes: DriverDecentrLogicExample 
 */
public class PassengerDecentrLogicExample extends PassengerDecentrLogic {

	public PassengerDecentrLogicExample(String agentId,
			RequestConsumerMessageProtocol sender,
			DriverMessageProtocol driverMessageProtocol,
			TestbedModel taxiModel, AgentPositionQuery positionQuery,
			Utils utils, PassengerProfile passengerProfile,
			TestbedPassengerActivity passengerActivity,
			TimeSpendingActivity timeSpendingActivity, RequestLogger logger) {
		super(agentId, sender, driverMessageProtocol, taxiModel, positionQuery, utils,
				passengerProfile, passengerActivity, timeSpendingActivity, logger);
	}

	// list in which this passenger remembers which drivers rejected him in the past
	private ArrayList<String> driversThatRejectedMe = new ArrayList<String>();
	
	/**
	 * Sends a request to closest free driver, that has never rejected us or give up after too many rejections.
	 * 
	 * @param request
	 *            request to be sent
	 */
	public void sendRequest(Request request) {
		
		// if too many drivers rejected us, don't even try
		if (driversThatRejectedMe.size() > 10) {
			LOGGER.debug(request.getPassengerId()+"giving up after 10 rejections.");
			return;
		}
		
		// find the closest free taxi driver (while skipping those that rejected us in the past)
		String closestDriver = null;
		Double closestDist = 0.0;
		for (String driverId : taxiModel.getTaxiDriversFree()) {
			double thisDist = utils.computeDistance(this.getCurrentPositionNode(), positionQuery.getCurrentPositionByNodeId(driverId));
			if ((!driversThatRejectedMe.contains(driverId)) && (closestDriver == null || thisDist < closestDist)) {
				closestDist = thisDist;
				closestDriver = driverId;
			}
		}
		
		// send a request
		if (closestDriver != null)	{
			super.sendRequest(request);
			sender.sendMessage(closestDriver, request);
		}
		
	}
	
	// accept any received proposal
	@Override
	public void processProposal(Proposal proposal) {
		// send the "I accept" message
		sender.sendMessage(proposal.getDriverId(), new ProposalAccept(proposal));
		// and start waiting for a given driver/vehicle
		this.startWaiting(proposal.getDriverId(), proposal.getVehicleId(), proposal.getRequest().getAdditionalRequirements());
	}

	// if our request is rejected, send it to someone else
	@Override
	public void processRejection(RequestReject r) {
		logger.logRequestRejected(passengerId);
		// remember that this driver rejected us
		if (!driversThatRejectedMe.contains(r.rejectReceivedFrom)) driversThatRejectedMe.add(r.rejectReceivedFrom);
		// and send the request again (now to someone else)
		this.sendRequest(r.request);
	}

	// not used right now
	@Override
	public void processProposals() {
	}


}

package cz.agents.agentpolis.darptestbed.simmodel.agent.driver.logic;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import cz.agents.agentpolis.darptestbed.global.Utils;
import cz.agents.agentpolis.darptestbed.siminfrastructure.communication.passenger.message.MessageDriverArrived;
import cz.agents.agentpolis.darptestbed.siminfrastructure.communication.passenger.message.OrderConfirmation;
import cz.agents.agentpolis.darptestbed.siminfrastructure.communication.passenger.message.RequestReject;
import cz.agents.agentpolis.darptestbed.siminfrastructure.communication.passenger.message.Proposal;
import cz.agents.agentpolis.darptestbed.siminfrastructure.communication.passenger.protocol.PassengerMessageProtocol;
import cz.agents.agentpolis.darptestbed.simmodel.agent.data.FlexiblePlan;
import cz.agents.agentpolis.darptestbed.simmodel.agent.data.Request;
import cz.agents.agentpolis.darptestbed.simmodel.agent.data.TripInfo;
import cz.agents.agentpolis.darptestbed.simmodel.entity.vehicle.TestbedVehicle;
import cz.agents.agentpolis.darptestbed.simmodel.environment.model.TestbedModel;
import cz.agents.agentpolis.siminfrastructure.planner.TripPlannerException;
import cz.agents.agentpolis.simmodel.agent.activity.movement.DriveVehicleActivity;
import cz.agents.agentpolis.simmodel.environment.model.citymodel.transportnetwork.AllNetworkNodes;
import cz.agents.agentpolis.simmodel.environment.model.query.AgentPositionQuery;

/**
 * The basic features of a DriverAgent, especially his communication protocol
 * that enables him to contact other agents.
 * 
 * The decentralized communication means that the taxi driver communicates
 * directly with passengers.
 * 
 * @author Lukas Canda
 */

public abstract class DriverDecentrLogic extends DriverLogic<PassengerMessageProtocol> {

	protected static final Logger LOGGER = Logger.getLogger(DriverDecentrLogic.class);

	public DriverDecentrLogic(String agentId, PassengerMessageProtocol sender, TestbedModel taxiModel,
			AgentPositionQuery positionQuery, AllNetworkNodes allNetworkNodes, Utils utils, TestbedVehicle vehicle,
			DriveVehicleActivity drivingActivity) {

		super(agentId, sender, taxiModel, positionQuery, allNetworkNodes, utils, vehicle, drivingActivity);
	}

	/**
	 * Process a request, that's been just received from a passenger.
	 * 
	 * @param request
	 *            the new request
	 */
	public abstract void processNewRequest(Request request);

	/**
	 * Process the queue of requests (e.g. by a planning algorithm). This method
	 * is usually indirectly called by a timer at regular intervals.
	 */
	public abstract void processRequests();

	/**
	 * Sends a proposal for a ride to a passenger.
	 * 
	 * @param proposal
	 *            proposal to be send (it contains also the id of the passenger)
	 */
	protected void sendProposalToPassenger(Proposal proposal) {
		sender.sendMessage(proposal.getPassengerId(), proposal);
	}
	
	/**
	 * Sends a rejection to passenger's request in reply to his request
	 * 
	 * @param request
	 *            request to which we are replying
	 */
	protected void sendRequestRejectionToPassenger(Request request) {
		sender.sendMessage(request.getPassengerId(), new RequestReject(request,this.getDriverId()) );
	}

	// TAXI_PROPOSES_TO_PASSENGER

	/**
	 * Received a message from the passenger we proposed to. He's accepted it.
	 * 
	 * @param proposal
	 *            the proposal that's been accepted
	 * @throws TripPlannerException 
	 */
	public abstract void processNewAcceptance(Proposal proposal);

	/**
	 * Received a message from the passenger we proposed to. He's rejected it.
	 * 
	 * @param proposal
	 *            the proposal that's been rejected
	 */
	public abstract void processNewRejection(Proposal proposal); // TODO: Check
																	// this
																	// method -
																	// it is not
																	// used
																	// anywhere

	/**
	 * Process the queues of accepted and rejected proposals (e.g. by a planning
	 * algorithm). This method is usually indirectly called by a timer at
	 * regular intervals.
	 */
	public abstract void processAcceptancesAndRejections();

	/**
	 * Transfers proposals into requests
	 * 
	 * @param proposals
	 *            proposals to be trasferred
	 * @return requests
	 */
	protected List<Request> transferProposIntoReqs(List<Proposal> proposals) {
		List<Request> listOfReqs = new ArrayList<Request>();
		for (Proposal prop : proposals) {
			listOfReqs.add(prop.getRequest());
		}
		return listOfReqs;
	}

	/**
	 * Searches for given requests in the given plan
	 * 
	 * @param reqsToSearch
	 *            the requests we're looking for
	 * @param plan
	 *            the plan we're searching in
	 * @return those requests from reqsToSearch, that are contained in the plan
	 */
	protected List<Request> searchRequestsInPlan(List<Request> reqsToSearch, FlexiblePlan plan) {

		List<Request> foundReqs = new ArrayList<Request>();

		for (Request req : plan.getRequests()) {
			if (reqsToSearch.contains(req)) {
				foundReqs.add(req);
			}
		}
		return foundReqs;
	}

	/**
	 * When we set out to serve some passengers, we have to notify them of it
	 * using this message.
	 * 
	 * @param requests
	 *            requests with passengers to be confirmed
	 */
	protected void sendTripConfirmationToPassengers(List<Request> requests) {

		for (Request req : requests) {
			sender.sendMessage(req.getPassengerId(), new OrderConfirmation(new TripInfo(getDriverId(), this.getVehicle().getId())));
		}

		// print out
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(getDriverId() + " has set out to work for ");
		for (Request req : requests) {
			stringBuilder.append(req.getPassengerId() + " ");
		}

		LOGGER.info(stringBuilder.toString());
	}

	@Override
	protected void sendTaxiArrived(String passengerId) {
		sender.sendMessage(passengerId, new MessageDriverArrived(getDriverId(), new TripInfo(getDriverId(), this.getVehicle().getId())));
	}
}

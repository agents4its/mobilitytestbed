package cz.agents.agentpolis.darptestbed.simmodel.agent.driver.logic;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Maps;

import cz.agents.agentpolis.darptestbed.global.Utils;
import cz.agents.agentpolis.darptestbed.siminfrastructure.communication.passenger.message.MessageDriverArrived;
import cz.agents.agentpolis.darptestbed.siminfrastructure.communication.passenger.message.Proposal;
import cz.agents.agentpolis.darptestbed.siminfrastructure.communication.passenger.protocol.PassengerMessageProtocol;
import cz.agents.agentpolis.darptestbed.simmodel.agent.data.Request;
import cz.agents.agentpolis.darptestbed.simmodel.agent.data.TripInfo;
import cz.agents.agentpolis.darptestbed.simmodel.agent.data.TripPlan;
import cz.agents.agentpolis.darptestbed.simmodel.entity.vehicle.TestbedVehicle;
import cz.agents.agentpolis.darptestbed.simmodel.environment.model.TestbedModel;
import cz.agents.agentpolis.siminfrastructure.planner.TripPlannerException;
import cz.agents.agentpolis.siminfrastructure.planner.trip.Trip;
import cz.agents.agentpolis.siminfrastructure.planner.trip.Trips;
import cz.agents.agentpolis.simmodel.agent.activity.movement.DriveVehicleActivity;
import cz.agents.agentpolis.simmodel.environment.model.citymodel.transportnetwork.AllNetworkNodes;
import cz.agents.agentpolis.simmodel.environment.model.query.AgentPositionQuery;

/**
 *	Example of decentralized control mechanism. Related classes: PassengerDecentrLogicExample 
 */
public class DriverDecentrLogicExample extends DriverDecentrLogic {

	public DriverDecentrLogicExample(String agentId, PassengerMessageProtocol sender, TestbedModel taxiModel,
			AgentPositionQuery positionQuery, AllNetworkNodes allNetworkNodes, Utils utils, TestbedVehicle vehicle,
			DriveVehicleActivity drivingActivity) {
		super(agentId, sender, taxiModel, positionQuery, allNetworkNodes, utils, vehicle, drivingActivity);
	}

	// If we receive a request from the passenger ...
	@Override
	public void processNewRequest(Request request) {

		// debug output
		LOGGER.debug("	Request: [" + utils.toHoursAndMinutes(request.getTimeWindow().getEarliestDeparture()) + "] "
				+ request.getPassengerId() + "->"+this.getDriverId()+", latest departure: "
				+ utils.toHoursAndMinutes(request.getTimeWindow().getLatestDeparture()));

		// if we don't have some of the requested special equipment (wheelchair support, etc.), reject the request
		if (!this.getVehicle().getVehicleEquipments().containsAll(request.getAdditionalRequirements())) {
			sendRequestRejectionToPassenger(request);
			return;
		}

		// if we already have a passenger on board, reject this request
		if (taxiModel.getNumOfPassenOnBoard(this.getVehicle().getId()) > 0) {
			sendRequestRejectionToPassenger(request);
			return;
		}
		
		// if we currently have a trip plan which is not empty, reject this request 
		if (this.getTripPlan() != null && this.getTripPlan().getTrips().numTrips() > 0) {
			sendRequestRejectionToPassenger(request);
			return;
		}
		
		// if we're already busy doing something, reject the request
		if (this.isBusy()) {
			sendRequestRejectionToPassenger(request);
			return;
		}

		// compute the driving time between the passenger and the driver
		double timeToPassenger = utils.computeDrivingTime(this.getDriverId(),request.getPassengerId());
		// estimate when this driver could pick the passenger up
		long pickUpTime = Math.max(request.getTimeWindow().getEarliestDeparture(), utils.getCurrentTime() + Math.round(timeToPassenger));

		// compute the driving time between the passenger's origin and destination
		double timeToDrive = utils.computeDrivingTime(request.getFromNode(), request.getToNode());
		// estimate when this driver could drop the passenger off
		long dropOffTime = pickUpTime + Math.round(timeToDrive);

		
		// if this driver is able to pick the passenger up in time & drop him off in time ...
		if ((pickUpTime <= request.getTimeWindow().getLatestDeparture()) && (dropOffTime <= request.getTimeWindow().getLatestArrival())) {
			
			// send a proposal to the passenger
			this.sendProposalToPassenger(new Proposal(request, this.getDriverId(), this.getVehicle().getId()));
			
			// while we're waiting for the reply, flag ourselves as "busy", so we won't accept new requests
			taxiModel.setTaxiBusy(this.getVehicle().getId());
			
		} else {
			// otherwise, if we can't possibly satisfy this request within specified time windows, send a rejection
			sendRequestRejectionToPassenger(request);
		}
		
	}

	// If passenger accepts our proposal... 
	@Override
	public void processNewAcceptance(Proposal proposal){
		
		// original request
		Request request = proposal.getRequest();
		
		// plan the trips (paths) 
		// (1) from driver to passenger's departure node and 
		// (2) from origin to arrival node
		Trip toPassenger = utils.planTrip(this.getVehicle().getId(), this.getCurrentPositionNode(), request.getFromNode()); 
		Trip toDestination = utils.planTrip(this.getVehicle().getId(), request.getFromNode(), request.getToNode());

		// concatenate those two trips into a drivePath for the driver
		Trips drivePath = new Trips();
		if (toPassenger != null && toPassenger.numOfCurrentTripItems() > 1) drivePath.addTrip(toPassenger);
		if (toDestination != null && toDestination.numOfCurrentTripItems() > 1) drivePath.addTrip(toDestination);

		// if we didn't successfully find and concatenate two required trips reject the request (we can't find a way to fulfill it)
		if (drivePath.numTrips() != 2) {
				// send a rejection
				sendRequestRejectionToPassenger(request);
				// flag ourselves as "free" again
				taxiModel.setTaxiFree(this.getVehicle().getId());
				
				return;
		}
		
		// create a pickup map for this path (tells driver where he should pick up which passengers)
		Map<Long, Set<String>> pickUpMap = Maps.newHashMap();
		pickUpMap.put(request.getFromNode(), new HashSet<String>(Arrays.asList(request.getPassengerId())));

		// set the trip plan for the driver
		TripPlan tripPlan = new TripPlan(drivePath, pickUpMap);
		this.setTripPlan(tripPlan);
		LOGGER.debug("Setting trip plan for "+this.getDriverId()+" ("+this.getCurrentPassengersOnBoard()+"/"+this.getVehicle().getCapacity()+") to:\n"+tripPlan);

		// start driving
		driveNextPartOfTripPlan();
		
	}

	// Process a rejection obtained by the passenger (not used in this example - passengers accept everything).
	@Override
	public void processNewRejection(Proposal proposal) {
	}

	// This is called by a timer at regular intervals.
	// You can use this instead of processNewRequest() if you want to store the requests in some array/queue and select among them.
	@Override
	public void processRequests() {
	}

	// This is called by a timer at regular intervals.
	@Override
	public void processAcceptancesAndRejections() {
	}

	// Notify the passenger that the taxi arrived, so he can get in.
	@Override
	protected void sendTaxiArrived(String passengerId) {
		sender.sendMessage(passengerId, new MessageDriverArrived(getDriverId(), new TripInfo(getDriverId(), this.getVehicle().getId())));
	}
}

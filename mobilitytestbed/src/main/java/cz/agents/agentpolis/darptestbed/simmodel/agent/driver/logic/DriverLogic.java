package cz.agents.agentpolis.darptestbed.simmodel.agent.driver.logic;

import java.util.Set;

import org.apache.log4j.Logger;

import cz.agents.agentpolis.darptestbed.global.Utils;
import cz.agents.agentpolis.darptestbed.simmodel.agent.AgentLogic;
import cz.agents.agentpolis.darptestbed.simmodel.agent.data.FlexiblePlan;
import cz.agents.agentpolis.darptestbed.simmodel.agent.data.TripPlan;
import cz.agents.agentpolis.darptestbed.simmodel.entity.vehicle.TestbedVehicle;
import cz.agents.agentpolis.darptestbed.simmodel.environment.model.TestbedModel;
import cz.agents.agentpolis.ondemandtransport.siminfrastructure.communication.protocol2.AMessageProtocol;
import cz.agents.agentpolis.siminfrastructure.planner.TripPlannerException;
import cz.agents.agentpolis.siminfrastructure.planner.trip.Trip;
import cz.agents.agentpolis.simmodel.agent.activity.movement.VehicleDrivingActivity;
import cz.agents.agentpolis.simmodel.agent.activity.movement.callback.DrivingActivityCallback;
import cz.agents.agentpolis.simmodel.entity.vehicle.Vehicle;
import cz.agents.agentpolis.simmodel.environment.model.citymodel.transportnetwork.AllNetworkNodes;
import cz.agents.agentpolis.simmodel.environment.model.query.AgentPositionQuery;
import eu.superhub.wp4.model.simmodel.agent.activity.parking.DrivingAndParkingActivity;

/**
 * This class contains all methods, that are common for both centralized and
 * decentralized version. Mainly, this applies for communication concerning
 * notifying passenger of my arrival and also driving.
 * 
 * @author Lukas Canda
 */
public abstract class DriverLogic<TMessageProtocol extends AMessageProtocol<?>> extends AgentLogic<TMessageProtocol>
		implements DrivingActivityCallback {

	private static final Logger LOGGER = Logger.getLogger(DriverLogic.class);

	private final String driverId; 

	/**
	 * The taxi I'm driving
	 */
	private final TestbedVehicle vehicle;
	/**
	 * Using this activity I can drive a vehicle
	 */
	private final VehicleDrivingActivity drivingActivity;
	/**
	 * My trips to be gone on
	 */
	private TripPlan tripPlan = null;
	/**
	 * The number of boarding passengers we're waiting for (if there's nobody to
	 * get in, lets set it to -1)
	 */
	private int numOfPassenToGetIn = -1;
	/**
	 * The next trip to be driven (while we're waiting for boarding passengers)
	 */
	private Trip tripToDrive = null;
	/**
	 * Algorithms, that allow diversion, can use this property to re-plan the
	 * plan of a taxi on the way
	 */
	protected FlexiblePlan flexiblePlan = null;

	public DriverLogic(String agentId, TMessageProtocol sender, TestbedModel serviceModel,
			AgentPositionQuery positionQuery, AllNetworkNodes allNetworkNodes, Utils utils, TestbedVehicle vehicle,
			VehicleDrivingActivity drivingActivity) {

		super(sender, serviceModel, positionQuery, utils);
		this.driverId = agentId;
		this.vehicle = vehicle;
		this.drivingActivity = drivingActivity;
	}

	/**
	 * @return the driver's id
	 */
	protected String getDriverId() {
		return this.driverId;
	}
	/**
	 * @return the driver's current position node
	 */
	protected Long getCurrentPositionNode() {
		return this.positionQuery.getCurrentPositionByNodeId(this.getDriverId());
	}
	/**
	 * @return the current number of passengers this driver has on board
	 */
	protected int getCurrentPassengersOnBoard() {
		return taxiModel.getNumOfPassenOnBoard(this.getVehicle().getId());
	}

	/**
	 * @return the driver's vehicle instance
	 */
	protected TestbedVehicle getVehicle() {
		return this.vehicle;
	}
	
	/**
	 * @return the driver's trip plan
	 */
	protected TripPlan getTripPlan() {
		return this.tripPlan;
	}
	
	/**
	 * Set the trip plan of this driver
	 * @param newTripPlan
	 *            TripPlan object that the driver should take
	 */
	protected void setTripPlan(TripPlan newTripPlan) {
		this.tripPlan = newTripPlan;
	}

	/**
	 * Extend current trip plan of this driver by a new one
	 * @param newTripPlan
	 *            TripPlan object that the driver should take
	 */
	protected void extendTripPlan(TripPlan newTripPlan) {
		if (this.tripPlan != null) {
			this.extendTripPlan(newTripPlan);
		} else {
			this.setTripPlan(newTripPlan);
		}
	}
	
	/**
	 * @return the driver's vehicle drivingActivity
	 */
	protected VehicleDrivingActivity getDrivingActivity() {
		return this.drivingActivity;
	}

	/**
	 * After all passengers from the current node get in, the taxi can drive
	 * another part of the trip.
	 * 
	 * @param passengerId
	 *            id of the passenger that's just gotten in
	 */
	public void processPassengerGotInVehicle(String passengerId) {
		numOfPassenToGetIn--;
		if (numOfPassenToGetIn == 0) {
			driveNextPartOfTripPlan();
		}
	}

	/**
	 * The passenger notifies the taxi driver of delay
	 * 
	 * @param passengerId
	 *            message sender
	 * @param departure
	 *            true, if the delay was during departure, false if during
	 *            arrival
	 * @param delay
	 *            the delay in milliseconds
	 */
	public void processVehicleIsTooLate(String passengerId, boolean departure, long delay) {
		// lets just skip the passenger
		numOfPassenToGetIn--;
		if (numOfPassenToGetIn == 0) {
			driveNextPartOfTripPlan();
		}
	}

	/**
	 * This callback method is called after finishing driving a part of the trip
	 */
	@Override
	public void finishedDriving() {
		driveNextPartOfTripPlan();
	}

	/**
	 * After finishing a part of the trip, the driver gets on a node, where he
	 * can pick up passengers according to their boarding positions.
	 * 
	 * After all the passengers finish boarding, he will continue driving.
	 */
	protected void driveNextPartOfTripPlan() {
		if (tripPlan == null) {
			tripFinished();
			return;
		}
		Long currentPos = this.getCurrentPositionNode();
		Set<String> passengersToGetIn = this.getTripPlan().getMapOfBoardingPassengers().get(currentPos);
		// if there are passengers waiting to get in on this node
		if (passengersToGetIn != null && !isEverybodyOnBoard()) {
			this.numOfPassenToGetIn = passengersToGetIn.size();
			for (String passId : passengersToGetIn) {
				sendTaxiArrived(passId);
			}
			return;
		}
		this.numOfPassenToGetIn = -1;

		// skip null trips (e.g. if there are two passengers at the same place)
		do {
			tripToDrive = tripPlan.getTrips().getAndRemoveFirstTrip();
		} while (tripToDrive == null && tripPlan.getTrips().hasTrip());

		if (tripToDrive == null) {
			tripFinished();
		} else {
			taxiModel.setTaxiBusy(vehicle.getId());
			// only some algorithms use these properties (here, we need to
			// refresh them)
			if (this.flexiblePlan != null) {
				taxiModel.setEndOfTripPosition(vehicle.getId(), this.flexiblePlan.getLastNode());
				this.flexiblePlan.removeNodesBefore(tripToDrive.showLastTripItem().tripPositionByNodeId);
			}
			drivingActivity.drive(driverId, vehicle, tripToDrive, this);
		}
	}

	/**
	 * Notify a passenger of arriving this taxi (so he can get in)
	 * 
	 * @param passengerId
	 *            the passenger to be notified
	 */
	protected abstract void sendTaxiArrived(String passengerId);

	// TaxiArrivedToPassengerMessage
	// sender.sendTaxiArrivedToPassenger(agentId, vehicle.getId(), passengerId);
	// }

	/**
	 * This method should be called after finishing the whole trip and having
	 * nothing else to do
	 */
	private void tripFinished() {
		tripPlan = null;
		flexiblePlan = null;
		taxiModel.setTaxiFree(vehicle.getId());
		//LOGGER.debug(this.driverId + ": I've finished my trip at " + utils.toHoursAndMinutes(utils.getCurrentTime()));
	}

	/**
	 * @return true, if the driver currently has a non-empty trip plan
	 */
	protected boolean isOnTheWay() {
		if (this.tripPlan == null || this.tripPlan.getTrips().numTrips() == 0) {
			return false;
		} else {
			return true;
		}
	}
	
	/**
	 * @return true, if the driver is currently busy for some reason (on a trip or communicating with passenger)
	 */
	protected boolean isBusy() {
		if (!taxiModel.getTaxiDriversFree().contains(this.getDriverId())) {
			return true;
		} else {
			return false;
		}
	}
	/**
	 * @return true, if the driver is currently NOT busy for any reason (not on a trip or communicating with passenger)
	 */
	protected boolean isFree() {
		return !isBusy();
	}
	
	/**
	 * @return true, if everybody who was supposed to, has gotten in
	 */
	protected boolean isEverybodyOnBoard() {
		return this.numOfPassenToGetIn == 0 ? true : false;
	}
	
	
	

}

package cz.agents.agentpolis.darptestbed.global;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import cz.agents.agentpolis.darptestbed.global.data.DriverAndDistance;
import cz.agents.agentpolis.darptestbed.global.data.DriverAndDistanceComparator;
import cz.agents.agentpolis.darptestbed.siminfrastructure.logger.UtilsLogger;
import cz.agents.agentpolis.darptestbed.siminfrastructure.planner.TestbedPlanner;
import cz.agents.agentpolis.darptestbed.simmodel.agent.data.FlexiblePlan;
import cz.agents.agentpolis.darptestbed.simmodel.agent.data.PlanItem;
import cz.agents.agentpolis.darptestbed.simmodel.agent.data.Request;
import cz.agents.agentpolis.darptestbed.simmodel.agent.data.TripPlan;
import cz.agents.agentpolis.darptestbed.simmodel.agent.data.comparator.RequestLatestDepComparator;
import cz.agents.agentpolis.darptestbed.simmodel.entity.vehicle.TestbedVehicle;
import cz.agents.agentpolis.darptestbed.simmodel.environment.model.TestbedModel;
import cz.agents.agentpolis.darptestbed.simulator.initializator.osm.NodeExtendedFunction;
import cz.agents.agentpolis.siminfrastructure.planner.TripPlannerException;
import cz.agents.agentpolis.siminfrastructure.planner.trip.Trip;
import cz.agents.agentpolis.siminfrastructure.planner.trip.TripItem;
import cz.agents.agentpolis.siminfrastructure.planner.trip.Trips;
import cz.agents.agentpolis.simmodel.environment.model.citymodel.transportnetwork.AllNetworkNodes;
import cz.agents.agentpolis.simmodel.environment.model.citymodel.transportnetwork.elemets.Node;
import cz.agents.agentpolis.simmodel.environment.model.query.AgentPositionQuery;
import cz.agents.alite.common.event.EventProcessor;

/**
 * A set of independent methods, that are used throughout the whole application.
 * This class includes an insertion algorithm for planning trips.
 * 
 * @author Lukas Canda
 */
@Singleton
public class Utils {

	/**
	 * All nodes in the map.
	 */
	protected final AllNetworkNodes allNodes;
	/**
	 * A planner, which computes the shortest path between two nodes
	 */
	protected final TestbedPlanner pathPlanner;
	/**
	 * Returns an agent's current position.
	 */
	protected final AgentPositionQuery positionQuery;
	/**
	 * A storage to save all data concerning taxi drivers and passengers
	 */
	protected final TestbedModel taxiModel;
	/**
	 * The event processor is used here just to figure out the current
	 * simulation time
	 */
	protected final EventProcessor eventProcessor;
	/**
	 * This map is useful, when a passenger is looking for a few closest taxis
	 * format: Map< passengerId , TaxiAndDistance - driving time to the taxi >
	 */
	protected Map<String, DriverAndDistance[]> passenAndDistMap = null;

	private final NodeExtendedFunction nodeExtendedFunction;

	// /**
	// * True, if the previous map was recently refreshed
	// */
	// private boolean mapRefreshedRecently = false;
	/**
	 * The previous map computes with positions at the end of taxi trips
	 */
	private boolean useEndOfTripPositions = false;
	private final UtilsLogger utilsLogger;

	@Inject
	public Utils(AllNetworkNodes allNodes, TestbedPlanner pathPlanner, AgentPositionQuery positionQuery,
			TestbedModel taxiModel, EventProcessor eventProcessor, NodeExtendedFunction nodeExtendedFunction,
			UtilsLogger utilsLogger) {

		this.allNodes = allNodes;
		this.pathPlanner = pathPlanner;
		this.positionQuery = positionQuery;
		this.taxiModel = taxiModel;
		this.eventProcessor = eventProcessor;
		this.nodeExtendedFunction = nodeExtendedFunction;
		this.utilsLogger = utilsLogger;

	}

	/**
	 * Computes the length of a trip (node by node).
	 * 
	 * @param trip
	 *            trip to be measured
	 * @param allNodes
	 *            all nodes (we need this information to compute it)
	 * @return trip length in meters
	 */
	private Double computeTripLength(Trip trip) {

		Double len = 0.0;
		TripItem item = trip.getAndRemoveFirstTripItem();
		Node previosNode = allNodes.getAllNetworkNodes().get((item.tripPositionByNodeId));
		item = trip.getAndRemoveFirstTripItem();

		while (item != null) {
			Node currenctNode = allNodes.getAllNetworkNodes().get((item.tripPositionByNodeId));
			len += nodeExtendedFunction.computeDistanceBetweenNodes(previosNode.getId(), currenctNode.getId());
			previosNode = currenctNode;
			item = trip.getAndRemoveFirstTripItem();
		}
		return len;
	}

	/**
	 * Computes the distance between two nodes
	 * 
	 * @param node1
	 *            number of the first node
	 * @param node2
	 *            number of the second node
	 * @return distance between the nodes in meters
	 */
	public Double computeDistance(long node1, long node2) {
		if (node1 == node2) {
			return 0.0;
		}
		Trips trips = null;
		try {
			trips = pathPlanner.findTrip(null, node1, node2);
		} catch (TripPlannerException e) {
			e.printStackTrace();
		}
		return computeTripLength(trips.getAndRemoveFirstTrip());
	}

	/**
	 * Computes the distance between the current positions of two agents
	 * 
	 * @param agentId1
	 *            id of the first agent
	 * @param agentId2
	 *            id of the second agent
	 * @return distance between the agents in meters
	 */
	public Double computeDistance(String agentId1, String agentId2) {
		long pos1 = positionQuery.getCurrentPositionByNodeId(agentId1);
		long pos2 = positionQuery.getCurrentPositionByNodeId(agentId2);
		return computeDistance(pos1, pos2);
	}

	/**
	 * Computes how much time it takes to drive between the current positions of
	 * two agents (using the given driving velocity)
	 * 
	 * @param agentId1
	 *            id of the first agent
	 * @param agentId2
	 *            id of the second agent
	 * @param velocityKmph
	 *            driving velocity (in kilometers per hour)
	 * @return time to drive (in milliseconds)
	 */
	public long computeDrivingTime(String agentId1, String agentId2, double velocityKmph) {
		Double distance = computeDistance(agentId1, agentId2);
		return (long) (3600 * distance / velocityKmph);
	}

	public long computeDrivingTime(String agentId1, String agentId2) {
		return computeDrivingTime(agentId1, agentId2, GlobalParams.getVelocityInKmph());
	}

	/**
	 * Computes how much time it takes to drive between two nodes
	 * 
	 * @param node1
	 *            number of the first node
	 * @param node2
	 *            number of the second node
	 * @param velocityKmph
	 *            driving velocity (in kilometers per hour)
	 * @return time to drive (in milliseconds)
	 */
	public long computeDrivingTime(long node1, long node2, double velocityKmph) {
		Double distance = computeDistance(node1, node2);
		return (long) (3600 * distance / velocityKmph);
	}

	public long computeDrivingTime(long node1, long node2) {
		return computeDrivingTime(node1, node2, GlobalParams.getVelocityInKmph());
	}

	/**
	 * Returns the given amount of time as a String in HH:MM format (the number
	 * of minutes is rounded up).
	 * 
	 * @param millis
	 *            the amount of time in milliseconds
	 * @return string, e.g. 9:52
	 */
	public String toHoursAndMinutes(long millis) {
		// round up the number of minutes
		int totalMinutes = (int) Math.ceil(millis / (double) 60000);
		int hours = totalMinutes / 60;
		int minutes = totalMinutes % 60;
		if (minutes < 10) {
			return hours + ":0" + minutes;
		}
		return hours + ":" + minutes;
	}

	/**
	 * Returns the given amount of time in minutes
	 * 
	 * @param millis
	 *            the amount of time in milliseconds
	 * @return number of minutes
	 */
	public int toMinutes(long millis) {
		// round up the number of minutes
		int minutes = (int) Math.round(millis / (double) 60000);
		return minutes;
	}

	/**
	 * Returns the given amount of time in seconds
	 * 
	 * @param millis
	 *            the amount of time in milliseconds
	 * @return number of seconds
	 */
	public int toSeconds(long millis) {
		// round up the number of seconds
		int seconds = (int) Math.round(millis / (double) 1000);
		return seconds;
	}

	@Deprecated
	/**
	 * Map of distances from all passengers to all taxi drivers, sorted by the
	 * distance
	 * 
	 * @param driversIds
	 *            taxi drivers we want to measure distances to
	 * @param endOfTripPositions
	 *            true = taxi positions will be replaced by the positions at the
	 *            end of taxis trips
	 * @return map of distances
	 */
	public Map<String, DriverAndDistance[]> getPassenAndDistMap(List<String> driversIds, boolean useEndOfTripPositions) {

		if (driversIds == null || driversIds.size() == 0) {
			return null;
		}

		// mapRefreshedRecently = false;

		// check out changes in required type of counting with positions
		if (this.useEndOfTripPositions != useEndOfTripPositions) {
			passenAndDistMap = null;
		}
		this.useEndOfTripPositions = useEndOfTripPositions;

		// check out changes in required drivers (compared to the current map)
		if (passenAndDistMap != null) {
			DriverAndDistance[] taxADistArr = passenAndDistMap.get(taxiModel.getPassengers().get(0));
			if (taxADistArr.length == driversIds.size()) {
				for (DriverAndDistance taxADist : taxADistArr) {
					if (!driversIds.contains(taxADist.getTaxiDriverId())) {
						// a difference has been found, so lets refresh the map
						passenAndDistMap = null;
						break;
					}
				}
			} else {
				passenAndDistMap = null;
			}
		}

		if (passenAndDistMap != null) {
			return passenAndDistMap;
		}

		// build the whole map
		passenAndDistMap = new HashMap<String, DriverAndDistance[]>();
		List<String> passengers = this.taxiModel.getPassengers();
		DriverAndDistance[] taxiAndDistArr;
		long drivingTime;
		String driverId;

		for (String passId : passengers) {
			taxiAndDistArr = new DriverAndDistance[driversIds.size()];

			// compute distances to all taxis
			for (int i = 0; i < driversIds.size(); i++) {
				driverId = driversIds.get(i);
				long passPos = positionQuery.getCurrentPositionByNodeId(passId);
				long driverPos = positionQuery.getCurrentPositionByNodeId(driverId);
				if (useEndOfTripPositions) {
					driverPos = taxiModel.getEndOfTripPosition(taxiModel.getVehicleId(driverId));
				}
				drivingTime = this.computeDrivingTime(passPos, driverPos);
				taxiAndDistArr[i] = new DriverAndDistance(driverId, drivingTime);
			}

			// sort them by the distance
			Arrays.sort(taxiAndDistArr, new DriverAndDistanceComparator());
			passenAndDistMap.put(passId, taxiAndDistArr);
		}

		return passenAndDistMap;
	}

	public Map<String, DriverAndDistance[]> getPassenAndDistMap(List<Request> requests, List<String> driversIds,
			boolean useEndOfTripPositions) {

		if (driversIds == null || driversIds.size() == 0) {
			return null;
		}

		this.useEndOfTripPositions = useEndOfTripPositions;

		for (Request request : requests) {
			passenAndDistMap.put(request.getPassengerId(),
					getDistMapForPassenger(request.getFromNode(), driversIds, useEndOfTripPositions));
		}

		return passenAndDistMap;
	}

	public DriverAndDistance[] getDistMapForPassenger(long passengerPositionByNodeId, List<String> driversIds,
			boolean useEndOfTripPositions) {

		if (driversIds == null || driversIds.size() == 0) {
			return null;
		}

		this.useEndOfTripPositions = useEndOfTripPositions;

		DriverAndDistance[] taxiAndDistArr = new DriverAndDistance[driversIds.size()];

		// compute distances to all taxis
		for (int i = 0; i < driversIds.size(); i++) {
			String driverId = driversIds.get(i);
			long driverPos = positionQuery.getCurrentPositionByNodeId(driverId);
			if (useEndOfTripPositions) {
				driverPos = taxiModel.getEndOfTripPosition(taxiModel.getVehicleId(driverId));
			}

			long drivingTime = this.computeDrivingTime(passengerPositionByNodeId, driverPos);
			taxiAndDistArr[i] = new DriverAndDistance(driverId, drivingTime);
		}

		// sort them by the distance
		Arrays.sort(taxiAndDistArr, new DriverAndDistanceComparator());

		return taxiAndDistArr;
	}

	/**
	 * Transfer the plan changeable into a trip plan that is ready for a taxi
	 * driver
	 * 
	 * @param flexiblePlan
	 *            plan changeable (from planning algorithm)
	 * @param reqsInPlan
	 *            requests used in the plan changeable
	 * @return trip plan (for taxi driver)
	 */
	public TripPlan makeTripPlan(FlexiblePlan flexiblePlan) {

		if (flexiblePlan == null || flexiblePlan.getSize() == 0) {
			return null;
		}
		Trips trips = makeTrips(flexiblePlan);
		Map<Long, Set<String>> mapOfBoardingPassengers = new HashMap<Long, Set<String>>();

		String tmpPassen = null;
		Set<String> tmpSet;

		// make the map of boarding passengers
		for (int i = 0; i < flexiblePlan.getSize(); i++) {
			tmpPassen = flexiblePlan.getBoardingPassenger(i);
			if (tmpPassen != null) {
				tmpSet = new HashSet<String>();
				tmpSet.add(tmpPassen);
				mapOfBoardingPassengers.put(flexiblePlan.getNode(i), tmpSet);
			}
		}

		return new TripPlan(trips, mapOfBoardingPassengers, flexiblePlan);
	}

	/**
	 * Makes trips out of this plan
	 * 
	 * @return trips
	 */
	public Trips makeTrips(FlexiblePlan flexiblePlan) {
		if (flexiblePlan.getSize() == 0) {
			return null;
		}
		String vehicleId = flexiblePlan.vehicle.getId();
		Trips trips = new Trips();
		Trips tmpTrips;
		try {
			// first node
			if (flexiblePlan.firstNode >= 0) {
				trips = pathPlanner
						.findTrip(vehicleId, flexiblePlan.firstNode, flexiblePlan.planItems.get(0).getNode());
			}
			// all the other nodes
			for (int i = 1; i < flexiblePlan.getSize(); i++) {
				tmpTrips = pathPlanner.findTrip(vehicleId, flexiblePlan.planItems.get(i - 1).getNode(),
						flexiblePlan.planItems.get(i).getNode());
				trips.addEndCurrentTrips(tmpTrips.getAndRemoveFirstTrip());
			}
			// last node
			if (flexiblePlan.lastNode >= 0) {
				tmpTrips = pathPlanner.findTrip(vehicleId, flexiblePlan.planItems.get(flexiblePlan.getSize() - 1)
						.getNode(), flexiblePlan.lastNode);
				trips.addEndCurrentTrips(tmpTrips.getAndRemoveFirstTrip());
			}
		} catch (TripPlannerException e) {
			e.printStackTrace();
			// if it's run from a runner, we need to skip this attempt
			System.exit(0);
		}

		return trips;
	}

	/**
	 * @return current simulation time in milliseconds
	 */
	public long getCurrentTime() {
		return eventProcessor.getCurrentTime();
	}

	// ///////////////////// THE INSERTION ALGORITHM ///////////////////////

	/**
	 * Make a trip plan to serve as many requests as possible (with regard to
	 * their time windows).
	 * 
	 * It uses an insertion algorithm, that generates various possible sequences
	 * of get in and get off nodes and finds a local optimum (that is a plan
	 * that serves the most requests and takes the shortest time)
	 * 
	 * @param listOfReqs
	 *            requests to be served
	 * @param vehicle
	 *            vehicle to drive according to the result plan
	 * @param plan
	 *            if not null, this algorithm will try to add requests into this
	 *            plan
	 * @return plan, how to serve as many requests as possible
	 */
	public FlexiblePlan planTrips(List<Request> listOfReqs, TestbedVehicle vehicle, FlexiblePlan plan,
			boolean refreshCurrTime) {

		listOfReqs = new ArrayList<Request>(listOfReqs);
		String taxiDriverId = taxiModel.getTaxiDriverId(vehicle.getId());

		// making a new plan
		if (plan == null) {
			long currPos = this.positionQuery.getCurrentPositionByNodeId(taxiDriverId);
			if (GlobalParams.isDriverReturnsBack()) {
				plan = new FlexiblePlan(this, vehicle, getCurrentTime(), currPos, currPos);
			} else {
				plan = new FlexiblePlan(this, vehicle, getCurrentTime(), currPos);
			}
		} else if (refreshCurrTime) {
			// this usually needs to be done before changing an old plan
			// (updating the start time according to when the driver can make
			// it)
			refreshPlannerTime(plan, taxiDriverId);
		}

		// delete requests which cannot be driven to on time
		for (int i = listOfReqs.size() - 1; i >= 0; i--) {
			Request req = listOfReqs.get(i);
			long timeToDrive = computeDrivingTime(taxiDriverId, req.getPassengerId());
			if (req.getTimeWindow() != null
					&& getCurrentTime() + timeToDrive > req.getTimeWindow().getLatestDeparture()) {
				listOfReqs.remove(i);
			}
		}
		// put the most in hurry requests into the front
		Collections.sort(listOfReqs, new RequestLatestDepComparator());

		PlanItem itemGetIn;
		PlanItem itemGetOff;
		long minDepartTime;
		int minDTimeGetInIndex = -1;
		int minDTimeGetOffIndex = -1;
		long tmpDepartTime;
		// this property is never used, but it may be useful for logging
		List<Request> listOfReqsInPlan = new ArrayList<Request>();

		// try to place all requests
		for (int i = 0; i < listOfReqs.size(); i++) {
			minDepartTime = Long.MAX_VALUE;
			itemGetIn = new PlanItem(listOfReqs.get(i), true);
			itemGetOff = new PlanItem(listOfReqs.get(i), false);
			// first, lets place the node where the passenger gets in
			for (int j = 0; j <= plan.getSize(); j++) {
				tmpDepartTime = insertItem(j, itemGetIn, true, plan);
				// if the plan hasn't been broken by that
				if (tmpDepartTime >= 0) {
					// lets try to place the get off node
					for (int k = j + 1; k <= plan.getSize(); k++) {
						tmpDepartTime = insertItem(k, itemGetOff, false, plan);
						// and remember the best combination
						if (tmpDepartTime >= 0 && tmpDepartTime < minDepartTime) {
							minDepartTime = tmpDepartTime;
							minDTimeGetInIndex = j;
							minDTimeGetOffIndex = k;
						}
					}
					// change it back
					plan.removeItem(j);
				}
			}
			// if we've found at least one possible placement, put it on the
			// best
			if (minDepartTime < Long.MAX_VALUE) {
				insertItem(minDTimeGetInIndex, itemGetIn, true, plan);
				insertItem(minDTimeGetOffIndex, itemGetOff, true, plan);
				// let's not forget checking the capacity
				if (!checkOutCapacity(plan)) {
					plan.removeItem(minDTimeGetOffIndex);
					plan.removeItem(minDTimeGetInIndex);
				} else {
					listOfReqsInPlan.add(listOfReqs.get(i));
				}
			} else {
				// here, you can do something if there is a request that we
				// cannot make
				// on time (for example, try to exchange it for another one from
				// the beginning)
			}
		}

		return plan;
	}

	// ------- Insert

	/**
	 * Inserts the item into the specified index (other items will automatically
	 * move further)
	 * 
	 * @param index
	 * @param item
	 *            (with arrival time not set)
	 * @param really
	 *            true, if the item should REALLY be inserted into the plan
	 *            (false means that it just computes the difference it would make)
	 * @return -1, if the item causes itself, or some other item inconsistency
	 *         (the driver would arrive too late for it); if it's okay, it
	 *         returns the departure time at the end of the plan
	 */
	public long insertItem(int index, PlanItem item, boolean really, FlexiblePlan flexiblePlan) {
		// it only calls a different method, because we first need to check,
		// if the item is possible to insert
		long endDepartTime = insertItemBody(index, item, false, flexiblePlan);
		if (really && endDepartTime >= 0) {
			endDepartTime = insertItemBody(index, item, true, flexiblePlan);
		}
		return endDepartTime;
	}

	/**
	 * Inserts the item to the end of the plan
	 * 
	 * @param item
	 *            (with arrival time not set)
	 * @param really
	 *            true, if the item should REALLY be inserted into the plan
	 *            (false means that it just computes the difference it would make)
	 * @return -1, if the item causes itself, or some other item inconsistency
	 *         (the driver would arrive too late for it); if it's okay, it
	 *         returns the departure time at the end of the plan
	 */
	public long insertItem(PlanItem item, boolean really, FlexiblePlan flexiblePlan) {
		return insertItem(flexiblePlan.getSize(), item, really, flexiblePlan);
	}

	/**
	 * The body of the previous method...
	 */
	protected long insertItemBody(int index, PlanItem item, boolean really, FlexiblePlan flexiblePlan) {

		// wrong values
		if (index < 0 || item == null) {
			return -1;
		}
		// checking first item condition
		if (isFixedFirst(flexiblePlan) && index == 0) {
			return -1;
		}
		PlanItem prevItem = null;
		PlanItem nextItem = null;
		long drivingTime = -1;
		long departTime = -1;
		long departTimeDiff = -1;

		// compute the arrival time from the previous node
		if (index == 0) {
			if (flexiblePlan.firstNode == -1) {
				departTime = item.setArrivalTime(flexiblePlan.currentTime);
			} else {
				drivingTime = computeDrivingTime(flexiblePlan.firstNode, item.getNode());
				departTime = item.setArrivalTime(flexiblePlan.currentTime + drivingTime);
			}
		} else {
			prevItem = flexiblePlan.planItems.get(index - 1);
			drivingTime = computeDrivingTime(prevItem.getNode(), item.getNode());
			departTime = item.setArrivalTime(prevItem.getDepartureTime() + drivingTime);
		}
		if (departTime == -1) {
			return -1;
		}

		// compute the departure time difference caused in all following nodes
		if (index < flexiblePlan.getSize()) {
			nextItem = flexiblePlan.planItems.get(index);
			drivingTime = computeDrivingTime(item.getNode(), nextItem.getNode());
			long arrivalTimeDiff = (departTime + drivingTime) - nextItem.getArrivalTime();
			if (really) {
				departTimeDiff = nextItem.addTime(arrivalTimeDiff);
			} else {
				departTimeDiff = nextItem.computeDepartureDifference(arrivalTimeDiff);
			}
			for (int i = index + 1; i < flexiblePlan.getSize(); i++) {
				// the driver would arrive too late to a node
				if (departTimeDiff == -1) {
					return -1;
				}
				nextItem = flexiblePlan.planItems.get(i);
				if (really) {
					departTimeDiff = nextItem.addTime(departTimeDiff);
				} else {
					departTimeDiff = nextItem.computeDepartureDifference(departTimeDiff);
				}
			}
			if (departTimeDiff == -1) {
				return -1;
			}
		}

		// compute the arrival time into the last node (the result)
		long retVal = -1;

		if (flexiblePlan.lastNode == -1) {
			// if inserting the last node (any timeDiff hasn't been computed)
			if (index == flexiblePlan.getSize()) {
				retVal = item.getDepartureTime();
			} else {
				retVal = nextItem.getDepartureTime() + departTimeDiff;
			}
		} else {
			if (index == flexiblePlan.getSize()) {
				retVal = item.getDepartureTime() + computeDrivingTime(item.getNode(), flexiblePlan.lastNode);
			} else {
				retVal = nextItem.getDepartureTime() + departTimeDiff
						+ computeDrivingTime(nextItem.getNode(), flexiblePlan.lastNode);
			}
		}

		if (really) {
			flexiblePlan.planItems.add(index, item);
		}

		return retVal;
	}

	/**
	 * @return true, if the first PlanItem has to stay first (we mustn't insert
	 *         anything into the front of it)
	 */
	public boolean isFixedFirst(FlexiblePlan flexiblePlan) {
		if (flexiblePlan.firstNode == -1 && flexiblePlan.getSize() > 0) {
			return true;
		}
		return false;
	}

	// ------- Insert

	/**
	 * Checks out, if the capacity of the vehicle hasn't been exceeded in any
	 * node of the plan
	 * 
	 * @return true, if the capacity hasn't been exceeded
	 */
	public boolean checkOutCapacity(FlexiblePlan flexiblePlan) {
		int currentOnBoard = taxiModel.getNumOfPassenOnBoard(flexiblePlan.vehicle.getId());
		if (flexiblePlan.getSize() == 0) {
			return true;
		}
		// the driver's seat is taken
		int capacityLeft = flexiblePlan.vehicle.getCapacity() - 1 - currentOnBoard;
		for (int i = 0; i < flexiblePlan.getSize(); i++) {
			if (flexiblePlan.getPlanItems().get(i).getIn) {
				capacityLeft--;
			} else {
				capacityLeft++;
			}
			if (capacityLeft < 0) {
				return false;
			}
		}
		return true;
	}

	public FlexiblePlan planTrips(List<Request> listOfReqs, TestbedVehicle vehicle) {
		return planTrips(listOfReqs, vehicle, null, true);
	}

	/**
	 * Updates the time, from which the plan begins. Sets it up this way: the
	 * real current simulation time + time, that the driver needs to get to the
	 * first node of the plan
	 * 
	 * @param plan
	 *            the plan to be refreshed
	 * @param driverId
	 *            the driver who drives according to this plan
	 * @return true, if the time was successfully updated, false if it would
	 *         destroy the plan
	 */
	protected void refreshPlannerTime(FlexiblePlan plan, String taxiDriverId) {
		long currentTime = getCurrentTime();
		long firstNode = plan.getFirstNode();
		long currPos = this.positionQuery.getCurrentPositionByNodeId(taxiDriverId);
		long drivingTime = computeDrivingTime(currPos, firstNode);
		updateCurrentTime(currentTime + drivingTime, plan);
	}

	/**
	 * Change the current time and refresh all timings according to it (this
	 * method is used for re-planning)
	 * 
	 * @param currentTime
	 *            time at the beginning of the plan
	 * @return true, if the time was successfully updated, false if it would
	 *         destroy the plan
	 */
	public void updateCurrentTime(long currentTime, FlexiblePlan flexiblePlan) {
		// add all plan items again (to refresh their time)
		List<PlanItem> planItemsOld = flexiblePlan.planItems;
		flexiblePlan.planItems = new ArrayList<PlanItem>();

		/*
		 * note - it is possible, that the updated plan will be shorter, because
		 * some requests may not fit into the new timing
		 */
		for (int i = 0; i < planItemsOld.size(); i++) {
			PlanItem item = planItemsOld.get(i);
			item.resetArrivalTime();
			insertItem(item, true, flexiblePlan);
		}
	}

	public void logAlgRealTime(long realTime) {
		utilsLogger.logAlgRealTime(realTime);
	}
	
	/**
	 * Uses the AgentPolis path planner to find a trip from origin to destination node, using a given vehicle.
	 * 
	 * @param vehicleId
	 *            id of a vehicle to be used
	 * @param originNodeId
	 *            id of trip origin node
	 * @param destinationNodeId
	 *            id of trip destination node
	 * @return single Trip object leading from origin to destination with a given vehicle, or null if not found.
	 */
	public Trip planTrip(String vehicleId, Long originNodeId, Long destinationNodeId) {
		try {
			return pathPlanner.findTrip(vehicleId,originNodeId,destinationNodeId).getAndRemoveFirstTrip();
		} catch (TripPlannerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}


}

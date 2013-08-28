package cz.agents.agentpolis.darptestbed.simmodel.agent.data;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.common.collect.Sets;

import cz.agents.agentpolis.siminfrastructure.planner.trip.Trips;

/**
 * A trip plan to be driven by a taxi driver. It consists of the list of nodes
 * to visit, along with the lists of passengers that get in on those nodes.
 * 
 * @author Lukas Canda
 */
public class TripPlan {

	/**
	 * A list of trips to serve a few passengers. Each trip ends at a
	 * pickup/drop location.
	 */
	protected Trips trips;
	/**
	 * The map that lists passengers that get in on some nodes of the trips
	 * (couples <node number, ids of passengers>)
	 */
	protected final Map<Long, Set<String>> mapOfBoardingPassengers;
	/**
	 * The plan, that can be re-planned by a planner (optional)
	 */
	protected FlexiblePlan flexiblePlan = null;

	public TripPlan(Trips trips, Map<Long, Set<String>> mapOfBoardingPassengers) {

		this(trips, mapOfBoardingPassengers, null);
	}

	public TripPlan(Trips trips, Map<Long, Set<String>> mapOfBoardingPassengers, FlexiblePlan planForPlanner) {

		this.trips = trips;
		this.mapOfBoardingPassengers = mapOfBoardingPassengers;
		this.flexiblePlan = planForPlanner;
	}

	public Trips getTrips() {
		return trips;
	}

	public void extend(TripPlan tripPlan) {
		tripPlan = checkNotNull(tripPlan);

		Trips clone = tripPlan.getTrips().clone();
		while (clone.hasTrip()) {
			trips.addEndCurrentTrips(clone.getAndRemoveFirstTrip());
		}

		for (Entry<Long, Set<String>> tripPlanEntry : tripPlan.getMapOfBoardingPassengers().entrySet()) {
			Set<String> set = mapOfBoardingPassengers.get(tripPlanEntry.getKey());
			if (set == null) {
				set = Sets.newHashSet();
			}
			Set<String> setNew = tripPlan.getMapOfBoardingPassengers().get(tripPlanEntry.getKey());
			if (setNew == null) {
				setNew = Sets.newHashSet();
			}
			set.addAll(setNew);

			mapOfBoardingPassengers.put(tripPlanEntry.getKey(), set);
		}

		if (this.flexiblePlan != null && tripPlan.flexiblePlan != null) {
			this.flexiblePlan.currentTime = tripPlan.flexiblePlan.currentTime;
			this.flexiblePlan.lastNode = tripPlan.flexiblePlan.lastNode;
			this.flexiblePlan.planItems.addAll(tripPlan.flexiblePlan.planItems);

			if (this.flexiblePlan.vehicle.getId() != tripPlan.flexiblePlan.vehicle.getId()) {
				throw new RuntimeException("Vehicle ids of flexible plans have to the same");
			}

		}

		if (this.flexiblePlan == null) {
			this.flexiblePlan = tripPlan.flexiblePlan;
		}

	}

	public void setTrips(Trips trips) {
		this.trips = trips;
	}

	public Map<Long, Set<String>> getMapOfBoardingPassengers() {
		return mapOfBoardingPassengers;
	}

	public FlexiblePlan getFlexiblePlan() {
		return flexiblePlan;
	}

	@Override
	public String toString() {
		return TripPlan.class.getSimpleName() + " " + mapOfBoardingPassengers + trips;
	}

}

package cz.agents.agentpolis.darptestbed.siminfrastructure.planner;

import cz.agents.agentpolis.siminfrastructure.planner.TripPlannerException;
import cz.agents.agentpolis.siminfrastructure.planner.trip.Trips;

public interface TestbedPlanner {

	public Trips findTrip(String vehicleId, long startNodeById, long destinationNodeById) throws TripPlannerException;
}

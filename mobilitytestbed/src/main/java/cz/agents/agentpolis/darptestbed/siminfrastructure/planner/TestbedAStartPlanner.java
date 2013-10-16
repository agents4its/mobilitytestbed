package cz.agents.agentpolis.darptestbed.siminfrastructure.planner;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import org.jgrapht.graph.DirectedWeightedMultigraph;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import cz.agents.agentpolis.darptestbed.simulator.initializator.osm.NodeExtendedFunction;
import cz.agents.agentpolis.siminfrastructure.planner.TripPlannerException;
import cz.agents.agentpolis.siminfrastructure.planner.path.AStarShortestPath;
import cz.agents.agentpolis.siminfrastructure.planner.path.AStarShortestPath.Heuristic;
import cz.agents.agentpolis.siminfrastructure.planner.trip.TripItem;
import cz.agents.agentpolis.siminfrastructure.planner.trip.Trips;
import cz.agents.agentpolis.siminfrastructure.planner.trip.VehicleTrip;
import cz.agents.agentpolis.siminfrastructure.planner.utils.PlannerEdge;
import cz.agents.agentpolis.simmodel.environment.model.citymodel.transportnetwork.GraphType;
import cz.agents.agentpolis.utils.key.Key;

public class TestbedAStartPlanner implements TestbedPlanner, Heuristic<Long> {

	private final NodeExtendedFunction nodeExtendedFunction;
	private final DirectedWeightedMultigraph<Long, PlannerEdge> highwayGraph;
	private final GraphType highwayGraphType;

	LoadingCache<Key, Double> distanceCache = CacheBuilder.newBuilder().maximumSize(100000)
			.build(new CacheLoader<Key, Double>() {
				public Double load(Key key) {
					return nodeExtendedFunction.computeDistanceBetweenNodes(key.firstPartOfKey, key.secondPartOfKey);
				}
			});

	Cache<Key, List<PlannerEdge>> pathCache = CacheBuilder.newBuilder().maximumSize(100000).build();

	public TestbedAStartPlanner(NodeExtendedFunction nodeExtendedFunction,
			DirectedWeightedMultigraph<Long, PlannerEdge> highwayGraph, GraphType highwayGraphType) {
		super();
		this.nodeExtendedFunction = nodeExtendedFunction;
		this.highwayGraph = highwayGraph;
		this.highwayGraphType = highwayGraphType;
	}

	@Override
	public Trips findTrip(String vehicleId, long startNodeById, long destinationNodeById) throws TripPlannerException {

		return createVehicleTrip(vehicleId, findPath(startNodeById, destinationNodeById, highwayGraph));

	}

	@Override
	public double getHeuristicEstimate(Long current, Long goal) {
		try {
			return distanceCache.get(new Key(current, goal));
		} catch (ExecutionException e) {

		}

		throw new UnsupportedOperationException();
	}

	public Trips createVehicleTrip(String vehicleId, List<PlannerEdge> foundPath) {

		if (foundPath.isEmpty()) {
			return new Trips();
		}

		PlannerEdge plannerEdge = foundPath.get(0);

		LinkedList<TripItem> trip = new LinkedList<TripItem>();
		trip.add(new TripItem(plannerEdge.fromPosition));
		trip.add(new TripItem(plannerEdge.toPosition));

		for (int i = 1; i < foundPath.size(); i++) {
			plannerEdge = foundPath.get(i);
			trip.add(new TripItem(plannerEdge.toPosition));

		}

		if (vehicleId == null) {
			vehicleId = "";
		}

		Trips trips = new Trips();
		trips.addTrip(new VehicleTrip(trip, highwayGraphType, vehicleId));

		return trips;
	}

	private List<PlannerEdge> findPath(final long startNodeById, final long destinationNodeById,
			final DirectedWeightedMultigraph<Long, PlannerEdge> plannerGraph) throws TripPlannerException {

		Key fromToNodeKey = new Key(startNodeById, destinationNodeById);

		final Heuristic<Long> heuristic = this;

		List<PlannerEdge> chachedPath;
		try {
			chachedPath = pathCache.get(fromToNodeKey, new Callable<List<PlannerEdge>>() {
				@Override
				public List<PlannerEdge> call() throws TripPlannerException {
					List<PlannerEdge> plannerEdges = null;

					AStarShortestPath<Long, PlannerEdge> shortestPath = null;
					try {
						shortestPath = new AStarShortestPath<Long, PlannerEdge>(plannerGraph, startNodeById,
								destinationNodeById, heuristic);

					} catch (IllegalArgumentException exception) {
						throw new TripPlannerException(startNodeById, destinationNodeById);
					} catch (NoSuchElementException e) {
						throw new TripPlannerException(startNodeById, destinationNodeById);
					}

					plannerEdges = shortestPath.getPathEdgeList();
					if (plannerEdges == null) {
						throw new TripPlannerException(startNodeById, destinationNodeById);
					}

					return plannerEdges;
				}
			});
		} catch (ExecutionException e) {
			throw new TripPlannerException(startNodeById, destinationNodeById);
		}

		List<PlannerEdge> plannerEdges = new ArrayList<PlannerEdge>();
		for (PlannerEdge edge : chachedPath) {
			plannerEdges.add(new PlannerEdge(edge.fromPosition, edge.toPosition, edge.distance));
		}

		return plannerEdges;
	}

}

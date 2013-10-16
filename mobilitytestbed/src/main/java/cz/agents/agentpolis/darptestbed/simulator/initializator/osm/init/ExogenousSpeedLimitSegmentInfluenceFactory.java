package cz.agents.agentpolis.darptestbed.simulator.initializator.osm.init;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import au.com.bytecode.opencsv.CSVReader;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Range;
import com.google.common.collect.RangeMap;
import com.google.common.collect.Sets;
import com.google.common.collect.TreeRangeMap;
import com.google.inject.Injector;

import cz.agents.agentpolis.darptestbed.simulator.initializator.osm.NodeExtendedFunction;
import cz.agents.agentpolis.siminfrastructure.time.TimeProvider;
import cz.agents.agentpolis.simmodel.environment.model.SpeedInfluenceModels;
import cz.agents.agentpolis.simmodel.environment.model.citymodel.transportnetwork.EGraphType;
import cz.agents.agentpolis.simmodel.environment.model.citymodel.transportnetwork.Graph;
import cz.agents.agentpolis.simmodel.environment.model.citymodel.transportnetwork.elemets.highway.HighwayEdge;
import cz.agents.agentpolis.simmodel.environment.model.citymodel.transportnetwork.elemets.highway.HighwayNetwork;
import cz.agents.agentpolis.simmodel.environment.model.citymodel.transportnetwork.elemets.highway.HighwayNode;
import cz.agents.agentpolis.simmodel.environment.model.delaymodel.key.GraphTypeAndFromToNodeKey;
import cz.agents.agentpolis.simulator.creator.initializator.InitFactory;
import cz.agents.agentpolis.util.InitAndGetterUtil;

public class ExogenousSpeedLimitSegmentInfluenceFactory implements InitFactory {

	private static final Logger LOGGER = Logger.getLogger(ExogenousSpeedLimitSegmentInfluenceFactory.class);
	private static final DateTimeFormatter TIME_STAMP_FRM = DateTimeFormat.forPattern("MM/dd/YYYY HH:mm:ss");

	// d04_stations_2008_05_22
	// d04_text_station_5min_2008_05_22
	private final File stationsFile;
	private final File stationsResultsFile;
	private final long measureFrequency;

	public ExogenousSpeedLimitSegmentInfluenceFactory(File stationsFile, File stationsResultsFile, long measureFrequency) {
		super();
		this.stationsFile = stationsFile;
		this.stationsResultsFile = stationsResultsFile;
		this.measureFrequency = measureFrequency;
	}

	@Override
	public void initRestEnvironment(Injector injector) {
		NodeExtendedFunction nodeExtendedFunction = injector.getInstance(NodeExtendedFunction.class);

		BiMap<String, Long> stationIdWithItsNodeId = loadStationWithAssignedNodeId(nodeExtendedFunction);

		Graph<HighwayNode, HighwayEdge> network = injector.getInstance(HighwayNetwork.class).getNetwork();
		Map<String, Map<Long, LinkedList<Long>>> stationWitNneighbors = Maps.newHashMap();
		for (String stationId : stationIdWithItsNodeId.keySet()) {
			Long stationNodeId = stationIdWithItsNodeId.get(stationId);
			LinkedList<Long> roadSegment = Lists.newLinkedList();
			roadSegment.add(stationNodeId);
			stationWitNneighbors.put(stationId,
					bfs(network, stationNodeId, new HashSet<Long>(stationIdWithItsNodeId.values())));

			// stationWitNneighbors.put(
			// stationId,
			// determineRoadSegment(roadSegment, network, new
			// HashSet<Long>(stationIdWithItsNodeId.values()),
			// Sets.<Long> newHashSet(stationNodeId)));

		}

		// String = stationId, Long = measureTime during day, Double - avg.
		// speed
		Map<String, Map<Long, Double>> speedOnStationDuringTime = Maps.newHashMap();

		try (FileReader fileReader = new FileReader(stationsResultsFile);
				CSVReader reader = new CSVReader(fileReader, ',')) {

			for (String[] measuredData : reader.readAll()) {
				// System.out.println(Arrays.toString(measuredData));

				if (measuredData[0] == null || measuredData[1] == null || measuredData[11] == null
						|| measuredData[11].isEmpty() || measuredData[0].isEmpty() || measuredData[1].isEmpty()) {
					LOGGER.info("The follow row was skipped: " + Arrays.toString(measuredData));
					continue;
				}

				DateTime timeStamp = TIME_STAMP_FRM.parseDateTime(measuredData[0]);
				String stationId = measuredData[1];
				double avgSpeedInMph = Double.valueOf(measuredData[11]);

				Map<Long, Double> speedsInTime = InitAndGetterUtil.getDataOrInitFromMap(speedOnStationDuringTime,
						stationId, Maps.<Long, Double> newHashMap());
				speedsInTime.put((long) timeStamp.getMillisOfDay(), avgSpeedInMph);
				speedOnStationDuringTime.put(stationId, speedsInTime);

				// System.out.println(timeStamp.getMillisOfDay() + " " +
				// stationId + " " + avgSpeedInMph);
				// 12Avg Speed Flow-weighted average speed over the 5-minute
				// period across all lanes. If flow is 0, mathematical average
				// of 5-minute
				// String latitude = stationData[8];
				// String longitude = stationData[9];
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		BiMap<Long, String> stationIdWithItsNodeIdInverse = stationIdWithItsNodeId.inverse();
		// BiMap<String, Long> =
		// loadStationWithAssignedNodeId(nodeExtendedFunction);
		// Map<String, Map<Long, LinkedList<Long>>> stationWitNneighbors =
		// Maps.newHashMap();
		// Map<String, Map<Long, Double>> speedOnStationDuringTime =
		// Maps.newHashMap();
		Map<GraphTypeAndFromToNodeKey, RangeMap<Long, Double>> exogenousSpeedLimits = Maps.newHashMap();

		for (String fromStationId : stationIdWithItsNodeId.keySet()) {

			Map<Long, LinkedList<Long>> nearest = stationWitNneighbors.get(fromStationId);
			for (Long nearestStationByNodeId : nearest.keySet()) {
				String toStationId = stationIdWithItsNodeIdInverse.get(nearestStationByNodeId);
				List<Long> path = nearest.get(nearestStationByNodeId);

				Map<Long, Double> speedOnParticularFromStationDuringTime = speedOnStationDuringTime.get(fromStationId);
				Map<Long, Double> speedOnParticularToStationDuringTime = speedOnStationDuringTime.get(toStationId);
				if (speedOnParticularFromStationDuringTime == null || speedOnParticularToStationDuringTime == null
						|| path == null) {
					continue;
				}

				for (long timeInDayRange : speedOnParticularFromStationDuringTime.keySet()) {
					double fromStationAvgSpeed = speedOnParticularFromStationDuringTime.get(timeInDayRange);
					double toStationAvgSpeed = speedOnParticularToStationDuringTime.get(timeInDayRange);
					double avgSpeed = (fromStationAvgSpeed + toStationAvgSpeed) / 2;

					for (int i = 0; i < path.size() - 1; i++) {

						GraphTypeAndFromToNodeKey graphTypeAndFromToNodeKey = new GraphTypeAndFromToNodeKey(
								EGraphType.HIGHWAY, path.get(i), path.get(i + 1));

						RangeMap<Long, Double> dataOrInitFromMap = InitAndGetterUtil.getDataOrInitFromMap(
								exogenousSpeedLimits, graphTypeAndFromToNodeKey, TreeRangeMap.<Long, Double> create());

						dataOrInitFromMap.put(
								Range.<Long> closedOpen(timeInDayRange, timeInDayRange + measureFrequency), avgSpeed);

						exogenousSpeedLimits.put(graphTypeAndFromToNodeKey, dataOrInitFromMap);

					}

				}

			}

		}

		System.out.println("EEEE" + exogenousSpeedLimits.size());
		ExogenousSpeedLimitSegmentInfluence exogenousSpeedLimitSegmentInfluence = new ExogenousSpeedLimitSegmentInfluence(
				exogenousSpeedLimits, injector.getInstance(TimeProvider.class));

		SpeedInfluenceModels speedInfluenceModels = injector.getInstance(SpeedInfluenceModels.class);
		speedInfluenceModels.addSpeedInfluenceModel(exogenousSpeedLimitSegmentInfluence);

	}

	// public double computedInfluencedSpeed(GraphType graphType, long
	// fromNodeByNodeId, long toNodeByNodeId,
	// double originSpeedInmps, double influencedSpeedInmps);

	private BiMap<String, Long> loadStationWithAssignedNodeId(NodeExtendedFunction nodeExtendedFunction) {
		BiMap<String, Long> stationIdWithItsNodeId = HashBiMap.<String, Long> create();

		try (FileReader fileReader = new FileReader(stationsFile); CSVReader reader = new CSVReader(fileReader, '\t')) {

			boolean skipHeader = true;
			for (String[] stationData : reader.readAll()) {
				if (skipHeader) {
					skipHeader = false;
					LOGGER.info("The follow header was skipped: " + Arrays.toString(stationData));
					continue;
				}
				String stationId = stationData[0];
				String latitude = stationData[8];
				String longitude = stationData[9];

				if (stationId == null || latitude == null || longitude == null || stationId.isEmpty()
						|| latitude.isEmpty() || longitude.isEmpty()) {
					LOGGER.info("The follow row was skipped: " + Arrays.toString(stationData));
					continue;
				}

				long nearestNodeByNodeId = nodeExtendedFunction.getNearestNodeByNodeId(Double.valueOf(longitude),
						Double.valueOf(latitude));
				stationIdWithItsNodeId.forcePut(stationId, nearestNodeByNodeId);
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return stationIdWithItsNodeId;
	}

	// private List<LinkedList<Long>> determineRoadSegment(LinkedList<Long>
	// roadSegment,
	// Graph<HighwayNode, HighwayEdge> network, Set<Long> stations, Set<Long>
	// visitedNodes) {
	//
	// List<LinkedList<Long>> roadSegments = Lists.newArrayList();
	//
	// for (HighwayEdge highwayEdge :
	// network.getNodeOutcomingEdges(roadSegment.peekLast())) {
	// if (visitedNodes.contains(highwayEdge.getToNodeId()) == false) {
	// LinkedList<Long> newRoadSegment = (LinkedList<Long>) roadSegment.clone();
	// newRoadSegment.add(highwayEdge.getToNodeId());
	// visitedNodes.add(highwayEdge.getToNodeId());
	// if (stations.contains(highwayEdge.getToNodeId()) == false) {
	// roadSegments.addAll(determineRoadSegment(newRoadSegment, network,
	// stations, visitedNodes));
	// } else {
	// roadSegments.add(newRoadSegment);
	// }
	// }
	//
	// }
	//
	// return roadSegments;
	//
	// }

	public Map<Long, LinkedList<Long>> bfs(Graph<HighwayNode, HighwayEdge> network, long stationNodeId,
			Set<Long> stationsByNodeIds) {
		Queue<Long> queue = Lists.newLinkedList();
		// Set<Long> data = Sets.newHashSet();
		// for (Node node : network.getAllNodes()) {
		// data.add(node.getId());
		// }
		// data.remove(stationNodeId);
		// queue.addAll(data);
		queue.add(stationNodeId);
		Set<Long> markedNodeSet = Sets.newHashSet(stationNodeId);

		Map<Long, LinkedList<Long>> nearestStations = Maps.newHashMap();
		LinkedList<Long> tmp = new LinkedList<Long>();
		tmp.add(stationNodeId);
		nearestStations.put(stationNodeId, tmp);

		Map<Long, LinkedList<Long>> resultNearestStations = Maps.newHashMap();

		while (queue.size() > 0) {
			Long fromNodeId = queue.poll();
			if (stationsByNodeIds.contains(fromNodeId) && fromNodeId != stationNodeId) {
				resultNearestStations.put(fromNodeId, nearestStations.get(fromNodeId));
				continue;
			}

			LinkedList<Long> pathToNearestStation = nearestStations.remove(fromNodeId);
			for (HighwayEdge edge : network.getNodeOutcomingEdges(fromNodeId)) {
				Long toNodeId = edge.getToNodeId();
				if (markedNodeSet.contains(toNodeId) == false) {
					LinkedList<Long> pathToNearestStationTmp = new LinkedList<Long>(pathToNearestStation);
					pathToNearestStationTmp.add(toNodeId);
					nearestStations.put(toNodeId, pathToNearestStationTmp);
					markedNodeSet.add(toNodeId);
					queue.add(toNodeId);
				}

			}

		}

		return resultNearestStations;
	}

}

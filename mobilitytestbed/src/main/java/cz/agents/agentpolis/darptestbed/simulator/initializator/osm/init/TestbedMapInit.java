package cz.agents.agentpolis.darptestbed.simulator.initializator.osm.init;

import java.io.File;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import cz.agents.agentpolis.simulator.creator.initializator.InitModuleFactory;
import org.apache.log4j.Logger;
import org.jgrapht.DirectedGraph;
import org.jgrapht.alg.StrongConnectivityInspector;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.openstreetmap.osm.data.coordinates.Bounds;

import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.Provides;
import com.google.inject.Singleton;

import cz.agents.agentpolis.darptestbed.simulator.initializator.osm.selector.impl.RoadwayGraphOsmBinder;
import cz.agents.agentpolis.siminfrastructure.planner.path.ShortestPathPlanner.PlannerEdge;
import cz.agents.agentpolis.simmodel.environment.model.SpeedInfluenceModels;
import cz.agents.agentpolis.simmodel.environment.model.SpeedLimitModel;
import cz.agents.agentpolis.simmodel.environment.model.citymodel.spatialrefsys.SRID;
import cz.agents.agentpolis.simmodel.environment.model.citymodel.transportnetwork.EGraphType;
import cz.agents.agentpolis.simmodel.environment.model.citymodel.transportnetwork.Graph;
import cz.agents.agentpolis.simmodel.environment.model.citymodel.transportnetwork.GraphBuilder;
import cz.agents.agentpolis.simmodel.environment.model.citymodel.transportnetwork.GraphType;
import cz.agents.agentpolis.simmodel.environment.model.citymodel.transportnetwork.elemets.Edge;
import cz.agents.agentpolis.simmodel.environment.model.citymodel.transportnetwork.elemets.Node;
import cz.agents.agentpolis.simmodel.environment.model.citymodel.transportnetwork.elemets.highway.HighwayEdge;
import cz.agents.agentpolis.simmodel.environment.model.citymodel.transportnetwork.elemets.highway.HighwayNode;
import cz.agents.agentpolis.simmodel.environment.model.key.GraphFromToNodeKey;
import cz.agents.agentpolis.simulator.creator.initializator.MapInitFactory;
import cz.agents.agentpolis.simulator.creator.initializator.impl.MapData;
import cz.agents.agentpolis.simulator.importer.osm.OsmDataGetter;
import cz.agents.agentpolis.simulator.importer.osm.OsmImporter;
import cz.agents.agentpolis.simulator.importer.osm.simplification.EdgeFactory;
import cz.agents.agentpolis.simulator.importer.osm.simplification.GraphEdgeSimplificationToolImpl;
import cz.agents.agentpolis.simulator.importer.osm.simplification.GraphSimplificationBuilder;
import cz.agents.agentpolis.simulator.importer.osm.simplification.dto.SimplifiedGraph;
import cz.agents.agentpolis.simulator.importer.osm.speedlimit.SpeedLimitCollector;
import cz.agents.agentpolis.simulator.importer.osm.speedlimit.waytype.DefaultSpeedLimitRoadTypeProvider;
import cz.agents.agentpolis.simulator.importer.osm.task.transportnetwork.HighwayGraphImportTask;
import cz.agents.agentpolis.simulator.importer.osm.util.OSMBoundsUtil;
import cz.agents.agentpolis.utils.key.Key;

public class TestbedMapInit extends AbstractModule implements MapInitFactory, InitModuleFactory {

	private static final Logger LOGGER = Logger.getLogger(TestbedMapInit.class);

	private final int epsg;

	public TestbedMapInit(int epsg) {
		super();
		this.epsg = epsg;
	}

	@Override
	public MapData initMap(File mapFile, Injector injector) {

		OsmDataGetter osmDataGetter = OsmDataGetter.createOsmDataGetter(mapFile);
		Bounds bounds = OSMBoundsUtil.computeBoundsOfSimulationWorld(osmDataGetter);

		OsmImporter importer = new OsmImporter(osmDataGetter);

		SpeedLimitCollector speedLimitCollector = new SpeedLimitCollector<HighwayNode, HighwayEdge>(EGraphType.HIGHWAY,
				50, new DefaultSpeedLimitRoadTypeProvider());

		Graph highWay = importer.executeTaskForWay(new HighwayGraphImportTask(speedLimitCollector),
				RoadwayGraphOsmBinder.getSelector());
		highWay = connectivity(highWay);

		// --
		// Map<GraphFromToNodeKey, Double> highwayLimits =
		// importer.executeTaskForWay(
		// new SpeedLimitImportTask<HighwayEdge>(EGraphType.HIGHWAY, 50,
		// highWay.getAllEdges()),
		// HighwaySpeedLimitImportSelector.getSelector());

		Map<GraphFromToNodeKey, Double> highwayLimits = speedLimitCollector.getSpeedLimistForSpecificSegment();

		// System.out.println("Check" + highWay.getAllEdges().size() + " ** "
		// + speedLimistForSpecificSegment.keySet().size() + " -- " +
		// highwayLimits.keySet().size());

		// for (GraphFromToNodeKey toNodeKey :
		// speedLimistForSpecificSegment.keySet()) {
		//
		// if (highwayLimits.containsKey(toNodeKey) == false) {
		// System.out.println("Problem" + toNodeKey.toString());
		// }
		// }

		// -----

		GraphEdgeSimplificationToolImpl<HighwayNode, HighwayEdge> tGS = new GraphEdgeSimplificationToolImpl<HighwayNode, HighwayEdge>(
				new GraphSimplificationBuilder<HighwayNode, HighwayEdge>(new HighwayEdgeFactory()),
				new HashSet<Long>(), EGraphType.HIGHWAY, highwayLimits);

		SimplifiedGraph<HighwayNode, HighwayEdge> simplifyGraph = tGS.simplifyGraph(highWay);

		Map<GraphType, Graph> graphByType = new HashMap<GraphType, Graph>();
		graphByType.put(EGraphType.HIGHWAY, simplifyGraph.simplifiedGraph);
		graphByType.put(EGraphType.TRAMWAY, (new GraphBuilder()).createGraph());
		graphByType.put(EGraphType.METROWAY, (new GraphBuilder()).createGraph());
		graphByType.put(EGraphType.PEDESTRIAN, (new GraphBuilder()).createGraph());

		Map<Long, Node> allGraphNodes = createAllGraphNodes(graphByType);

		// System.out.println("Check" +
		// graphByType.get(EGraphType.HIGHWAY).getAllEdges().size());

		initSpeedLimits(
				makeConsistentWithSimplifiedGraph(highwayLimits, EGraphType.HIGHWAY,
						simplifyGraph.mapppingBetweenOriginNewGraph), injector);

		return new MapData(bounds, graphByType, allGraphNodes);

	}

	private static class HighwayEdgeFactory implements EdgeFactory<HighwayEdge> {

		@Override
		public HighwayEdge createEdge(long fromNodeId, long toNodeId, double lenght, HighwayEdge incomingEdge) {
			return new HighwayEdge(fromNodeId, toNodeId, lenght, "");
		}

	}

	// private SimplifiedGraph<HighwayNode, HighwayEdge>
	// createHighwayGraph(OsmDataGetter osmDataGetter) {
	//
	//
	//
	// // mapppingBetweenOriginNewGraph.put(graphType,
	// simplifiedGraph.mapppingBetweenOriginNewGraph);
	// // graphByType.put(graphType, simplifiedGraph.simplifiedGraph);
	//
	// }

	private void initSpeedLimits(Map<GraphFromToNodeKey, Double> highWayLimits, Injector injector) {
		// System.out.println("Check" + highWayLimits.size());
		Map<GraphFromToNodeKey, Double> speedLimistForSpecificSegment = new HashMap<GraphFromToNodeKey, Double>();
		speedLimistForSpecificSegment.putAll(highWayLimits);
		SpeedInfluenceModels speedInfluenceModels = injector.getInstance(SpeedInfluenceModels.class);
		speedInfluenceModels.addSpeedInfluenceModel(new SpeedLimitModel(speedLimistForSpecificSegment));

	}

	private Map<GraphFromToNodeKey, Double> makeConsistentWithSimplifiedGraph(Map<GraphFromToNodeKey, Double> speed,
			GraphType graphType, Map<Key, Key> mapppingBetweenOriginNewGraph) {

		for (Entry<Key, Key> entry : mapppingBetweenOriginNewGraph.entrySet()) {

			Key key = entry.getKey();
			Key value = entry.getValue();

			GraphFromToNodeKey originEdge = new GraphFromToNodeKey(graphType, key.firstPartOfKey, key.secondPartOfKey);
			GraphFromToNodeKey mappedEdge = new GraphFromToNodeKey(graphType, value.firstPartOfKey,
					value.secondPartOfKey);

			Double speedLimitOrgValue = speed.remove(originEdge);
			Double speedLimitMappingValue = speed.remove(mappedEdge);

			if (speedLimitMappingValue != null && speedLimitMappingValue < speedLimitOrgValue) {
				speedLimitOrgValue = speedLimitMappingValue;
			}

			speed.put(mappedEdge, speedLimitOrgValue);

		}

		return speed;
	}

	private Map<Long, Node> createAllGraphNodes(Map<GraphType, Graph> graphByGraphType) {

		Map<Long, Node> nodesFromAllGraphs = new HashMap<Long, Node>();

		for (GraphType graphType : graphByGraphType.keySet()) {
			Graph<Node, Edge> graphStorageTmp = graphByGraphType.get(graphType);
			for (Node node : graphStorageTmp.getAllNodes()) {
				nodesFromAllGraphs.put(node.getId(), node);
			}

		}

		return nodesFromAllGraphs;

	}

	@Override
	public AbstractModule injectModule(Injector injector) {
		return this;
	}

	private Graph<Node, Edge> connectivity(Graph graph) {

		DirectedGraph<Long, PlannerEdge> plannerGraph = prepareGraphForFindComponents(graph);

		StrongConnectivityInspector<Long, PlannerEdge> strongConnectivityInspector = new StrongConnectivityInspector<>(
				plannerGraph);

		if (strongConnectivityInspector.isStronglyConnected()) {
			return graph;
		}

		LOGGER.debug("The Highway map has more then one strong component, it will be selected the largest components");

		Set<Long> strongestComponents = getTheLargestGraphComponent(strongConnectivityInspector);

		return createGraphBasedOnTheLargestComponent(graph, strongestComponents);
	}

	private DirectedGraph<Long, PlannerEdge> prepareGraphForFindComponents(Graph<Node, Edge> graph) {

		DirectedGraph<Long, PlannerEdge> plannerGraph = new DefaultDirectedGraph<>(PlannerEdge.class);
		Set<Long> addedNodes = new HashSet<Long>();

		for (Node node : graph.getAllNodes()) {
			Long fromPositionByNodeId = node.getId();
			if (addedNodes.contains(fromPositionByNodeId) == false) {
				addedNodes.add(fromPositionByNodeId);
				plannerGraph.addVertex(fromPositionByNodeId);
			}

			for (Edge edge : graph.getNodeOutcomingEdges(node.getId())) {
				Long toPositionByNodeId = edge.getToNodeId();
				if (addedNodes.contains(toPositionByNodeId) == false) {
					addedNodes.add(toPositionByNodeId);
					plannerGraph.addVertex(toPositionByNodeId);
				}

				PlannerEdge plannerEdge = new PlannerEdge(null, fromPositionByNodeId, toPositionByNodeId);
				plannerGraph.addEdge(fromPositionByNodeId, toPositionByNodeId, plannerEdge);
				// plannerGraph.setEdgeWeight(plannerEdge, edge.getLenght());
			}

		}
		return plannerGraph;
	}

	private Graph<Node, Edge> createGraphBasedOnTheLargestComponent(Graph<Node, Edge> graph,
			Set<Long> strongestComponents) {
		GraphBuilder<Node, Edge> graphBuilder = new GraphBuilder<>();
		for (Long nodeId : strongestComponents) {
			graphBuilder.addNode(graph.getNodeByNodeId(nodeId));
		}

		for (Long nodeId : strongestComponents) {
			for (Edge edge : graph.getNodeOutcomingEdges(nodeId)) {
				if (strongestComponents.contains(edge.getToNodeId())) {
					graphBuilder.addEdge(graph.getEdges(nodeId, edge.getToNodeId()));
				}
			}
		}
		return graphBuilder.createGraph();
	}

	private Set<Long> getTheLargestGraphComponent(
			StrongConnectivityInspector<Long, PlannerEdge> strongConnectivityInspector) {
		List<Set<Long>> components = strongConnectivityInspector.stronglyConnectedSets();
		Collections.sort(components, new Comparator<Set<Long>>() {

			@Override
			public int compare(Set<Long> o1, Set<Long> o2) {
				return o2.size() - o1.size();
			}

		});

		return components.get(0);
	}

	@Override
	protected void configure() {
	}

	@Singleton
	@Provides
	public SRID provideEPSG() {
		return new SRID(epsg);
	}

}

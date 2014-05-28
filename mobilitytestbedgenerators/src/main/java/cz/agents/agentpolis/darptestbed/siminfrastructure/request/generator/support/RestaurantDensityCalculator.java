package cz.agents.agentpolis.darptestbed.siminfrastructure.request.generator.support;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.vividsolutions.jts.geom.Coordinate;
import cz.agents.agentpolis.darptestbed.global.Utils;
import cz.agents.agentpolis.darptestbed.siminfrastructure.planner.init.TestbedPlannerModuleFactory;
import cz.agents.agentpolis.darptestbed.simmodel.environment.TestbedEnvironmentModul;
import cz.agents.agentpolis.darptestbed.simulator.initializator.osm.KNearestNodesInitModuleFactory;
import cz.agents.agentpolis.darptestbed.simulator.initializator.osm.KNodesExtendedFunction;
import cz.agents.agentpolis.darptestbed.simulator.initializator.osm.NodeExtendedFunction;
import cz.agents.agentpolis.darptestbed.simulator.initializator.osm.init.NodeDensityMapInit;
import cz.agents.agentpolis.darptestbed.simulator.initializator.osm.init.TestbedMapInit;
import cz.agents.agentpolis.simmodel.environment.AgentPolisEnvironmentModule;
import cz.agents.agentpolis.simmodel.environment.model.citymodel.restaurantnetwork.elements.RestaurantNode;
import cz.agents.agentpolis.simmodel.environment.model.citymodel.transportnetwork.elemets.highway.HighwayNetwork;
import cz.agents.agentpolis.simmodel.environment.model.citymodel.transportnetwork.elemets.highway.HighwayNode;
import cz.agents.agentpolis.simulator.creator.initializator.InitModuleFactory;
import cz.agents.agentpolis.simulator.creator.initializator.impl.MapData;
import cz.agents.agentpolis.simulator.importer.osm.selector.impl.RestaurantImportSelector;
import cz.agents.agentpolis.utils.config.ConfigReader;
import cz.agents.alite.common.event.EventProcessor;
import org.apache.log4j.Logger;
import org.openstreetmap.osm.data.coordinates.LatLon;

import java.io.File;
import java.util.*;

public class RestaurantDensityCalculator {

    private final static Logger LOGGER = Logger.getLogger(LenientPassengerGenerator.class);

    private final Utils utils;
    private final KNodesExtendedFunction nearestNodeFinder;

    private MapData osmDTO;

    private final Collection<HighwayNode> allNetworkNodes;
    private Map<Long, RestaurantNode> allRestaurantNodes;

    public RestaurantDensityCalculator(String osmFileName, String benchmarkDir, Injector injector) {
        super();
//        Injector injector = createInjector(benchmarkDir, new File(osmFileName));
        injector = createInjector(benchmarkDir, new File(osmFileName));
        this.utils = injector.getInstance(Utils.class);
        this.nearestNodeFinder = (KNodesExtendedFunction) injector.getInstance(NodeExtendedFunction.class);
        allNetworkNodes = injector.getInstance(
                                HighwayNetwork.class).getNetwork().getAllNodes();

    }

    public RestaurantDensityCalculator(Injector injector) {
        super();
        this.utils = injector.getInstance(Utils.class);
        this.nearestNodeFinder = (KNodesExtendedFunction) injector.getInstance(NodeExtendedFunction.class);
        allNetworkNodes = injector.getInstance(
                HighwayNetwork.class).getNetwork().getAllNodes();

    }


    protected Injector createInjector(String benchmarkDir, File osmMap) {
        try {
            Injector injector = Guice.createInjector();


            // select the benchmark directory
            File experiment = new File(benchmarkDir);

            ConfigReader scenario = ConfigReader.initConfigReader(new File(experiment, "config/scenario.groovy").toURL());

            int epsg = scenario.getIntegerValueFromConfig("epsg");

            NodeDensityMapInit mapInitFactory = new NodeDensityMapInit(epsg);

            osmDTO = mapInitFactory.initMap(osmMap, injector);
            allRestaurantNodes = mapInitFactory.getRestaurantGraph();

            EventProcessor eventProcessor = new EventProcessor();
            injector = injector.createChildInjector(new AgentPolisEnvironmentModule(
                    eventProcessor, new Random(4),
                    osmDTO.graphByType, osmDTO.nodesFromAllGraphs));

            List<InitModuleFactory> initModuleFactories = new ArrayList<>();

            initModuleFactories.add(new KNearestNodesInitModuleFactory(epsg));
            initModuleFactories.add(new TestbedPlannerModuleFactory());

            injector = injector.createChildInjector(new TestbedEnvironmentModul(eventProcessor));


            for (InitModuleFactory initFactory : initModuleFactories) {
                AbstractModule module = initFactory.injectModule(injector);
                injector = injector.createChildInjector(module);
            }

            return injector;

        } catch (Exception e) {
            System.out.println(e.toString());
            return null;
        }

    }


//    protected Injector createInjector(String benchmarkDir, File osmMap) {
//        try {
//            Injector injector = Guice.createInjector();
//
//
//            // select the benchmark directory
//            File experiment = new File(benchmarkDir);
//
//            ConfigReader scenario = ConfigReader.initConfigReader(new File(experiment, "config/scenario.groovy").toURL());
//
//            int epsg = scenario.getIntegerValueFromConfig("epsg");
//
//            NodeDensityMapInit mapInitFactory = new NodeDensityMapInit(epsg);
//
//            osmDTO = mapInitFactory.initMap(osmMap, injector);
//            allRestaurantNodes = mapInitFactory.getRestaurantGraph();
//
//            EventProcessor eventProcessor = new EventProcessor();
//            injector = injector.createChildInjector(new AgentPolisEnvironmentModule(
//                    eventProcessor, new Random(4),
//                    osmDTO.graphByType, osmDTO.nodesFromAllGraphs));
//
//            List<InitModuleFactory> initModuleFactories = new ArrayList<>();
//
//            initModuleFactories.add(new KNearestNodesInitModuleFactory(epsg));
//            initModuleFactories.add(new TestbedPlannerModuleFactory());
//
//            injector = injector.createChildInjector(new TestbedEnvironmentModul(eventProcessor));
//
//
//            for (InitModuleFactory initFactory : initModuleFactories) {
//                AbstractModule module = initFactory.injectModule(injector);
//                injector = injector.createChildInjector(module);
//            }
//
//            return injector;
//
//        } catch (Exception e) {
//            System.out.println(e.toString());
//            return null;
//        }
//
//    }





    public Map<RestaurantNode, Integer> calculateFrequencies() {
        Map<RestaurantNode, Integer> frequencies = new HashMap<>();
        for (Map.Entry<Long, RestaurantNode> node : allRestaurantNodes.entrySet()) {
            List<Long> kNearestNodesByNode = nearestNodeFinder.getSquareWithNodeInCenter(node.getValue(), 500);
            int size = kNearestNodesByNode.size();
            frequencies.put(node.getValue(), size);
        }

//        for (Map.Entry<RestaurantNode, Integer> entry : frequencies.entrySet()) {
//            System.out.println(
//                    String.format("Node: %d - %d", entry.getKey().getId(), entry.getValue())
//            );
//        }

        return frequencies;
    }

    public List<Coordinate> getTransportationNodesCoordinates() {
        List<Coordinate> coordinates = new ArrayList<>();

        for (HighwayNode node : allNetworkNodes) {
            LatLon latLon = node.getLatLon();
            coordinates.add(new Coordinate(latLon.lon(), latLon.lat()));
        }

        return coordinates;
    }

    public List<Coordinate> getRestaurantNodesCoordinates() {
        List<Coordinate> coordinates = new ArrayList<>();

        for (RestaurantNode node : allRestaurantNodes.values()) {
            LatLon latLon = node.getLatLon();
            coordinates.add(new Coordinate(latLon.lon(), latLon.lat()));
        }

        return coordinates;
    }
}

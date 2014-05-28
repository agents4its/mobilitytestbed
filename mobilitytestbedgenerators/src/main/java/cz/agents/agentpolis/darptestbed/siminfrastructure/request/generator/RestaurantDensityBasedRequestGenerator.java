package cz.agents.agentpolis.darptestbed.siminfrastructure.request.generator;

import com.google.common.collect.Lists;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import cz.agents.agentpolis.darptestbed.global.Utils;
import cz.agents.agentpolis.darptestbed.siminfrastructure.planner.init.TestbedPlannerModuleFactory;
import cz.agents.agentpolis.darptestbed.siminfrastructure.request.Driver;
import cz.agents.agentpolis.darptestbed.siminfrastructure.request.GPS;
import cz.agents.agentpolis.darptestbed.siminfrastructure.request.Passenger;
import cz.agents.agentpolis.darptestbed.siminfrastructure.request.PassengerRequest;
import cz.agents.agentpolis.darptestbed.siminfrastructure.request.generator.support.GPSPositionGenerator;
import cz.agents.agentpolis.darptestbed.siminfrastructure.request.generator.support.GPSPositionGeneratorFactory;
import cz.agents.agentpolis.darptestbed.siminfrastructure.request.generator.support.PassengerGenerator;
import cz.agents.agentpolis.darptestbed.siminfrastructure.request.generator.support.PassengerGenerator.RequestTimeInfo;
import cz.agents.agentpolis.darptestbed.siminfrastructure.request.generator.support.VehicleGenerator;
import cz.agents.agentpolis.darptestbed.simmodel.environment.TestbedEnvironmentModul;
import cz.agents.agentpolis.darptestbed.simulator.initializator.osm.KNearestNodesInitModuleFactory;
import cz.agents.agentpolis.darptestbed.simulator.initializator.osm.NodeExtendedFunction;
import cz.agents.agentpolis.darptestbed.simulator.initializator.osm.init.NodeDensityMapInit;
import cz.agents.agentpolis.simmodel.environment.AgentPolisEnvironmentModule;
import cz.agents.agentpolis.simulator.creator.initializator.InitModuleFactory;
import cz.agents.agentpolis.simulator.creator.initializator.impl.MapData;
import cz.agents.agentpolis.simulator.importer.osm.OsmDataGetter;
import cz.agents.agentpolis.simulator.importer.osm.util.OSMBoundsUtil;
import cz.agents.agentpolis.utils.config.ConfigReader;
import cz.agents.agentpolis.utils.config.ConfigReaderException;
import cz.agents.alite.common.event.EventProcessor;
import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.openstreetmap.osm.data.coordinates.Bounds;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RestaurantDensityBasedRequestGenerator {

    protected static final Logger LOGGER = Logger.getLogger(RestaurantDensityBasedRequestGenerator.class);

    public GPSPositionGeneratorFactory getPositionGeneratorFactory() {
        return positionGeneratorFactory;
    }

    public Random getRandom() {
        return random;
    }

    public int getMaxNumberOfRequestPerAgent() {
        return maxNumberOfRequestPerAgent;
    }

    public ObjectMapper getMapper() {
        return mapper;
    }

    private final GPSPositionGeneratorFactory positionGeneratorFactory;
    private final Random random;
    private final int maxNumberOfRequestPerAgent;
    private final NodeExtendedFunction nearestNodeFinder;
    private final ObjectMapper mapper;
    private final Utils utils;

    public RestaurantDensityBasedRequestGenerator(GPSPositionGeneratorFactory positionGeneratorFactory, Random random,
                                                  int maxNumberOfRequestPerAgent,
                                                  String osmFileName, String benchmarkDir,
                                                  ObjectMapper mapper) {
        this(positionGeneratorFactory, random, maxNumberOfRequestPerAgent, osmFileName, benchmarkDir, mapper, null);
    }

    public RestaurantDensityBasedRequestGenerator(GPSPositionGeneratorFactory positionGeneratorFactory, Random random,
                                                  int maxNumberOfRequestPerAgent,
                                                  String osmFileName, String benchmarkDir,
                                                  ObjectMapper mapper, Injector injector) {
        super();

        this.positionGeneratorFactory = positionGeneratorFactory;
        this.random = random;
        this.maxNumberOfRequestPerAgent = maxNumberOfRequestPerAgent;

        injector = createInjector(benchmarkDir, new File(osmFileName), injector);

        utils = injector.getInstance(Utils.class);

        this.nearestNodeFinder = injector.getInstance(NodeExtendedFunction.class);

        this.mapper = mapper;
    }

    protected Injector createInjector(String benchmarkDir, File osmMap, Injector injector) {
        try {


            // select the benchmark directory
            File experiment = new File(benchmarkDir);

            ConfigReader scenario = ConfigReader.initConfigReader(new File(experiment, "config/scenario.groovy").toURL());

            int epsg = scenario.getIntegerValueFromConfig("epsg");

            NodeDensityMapInit mapInitFactory = new NodeDensityMapInit(epsg);

            MapData osmDTO = mapInitFactory.initMap(osmMap, injector);

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

        } catch (ConfigReaderException e) {
            System.out.println(e.toString());
            return null;
        } catch (MalformedURLException e) {
            System.out.println(e.toString());
            return null;
        }

    }

    public void generateDrivers(int numberOfDrivers, VehicleGenerator vehicleCapacityGenerator, String outputFileName,
                                File osmMap) throws JsonGenerationException, JsonMappingException, IOException {
        generateDrivers(numberOfDrivers, vehicleCapacityGenerator, new File(osmMap.getParentFile(), outputFileName),
                osmMap);
    }

    public void generatePassengers(int numberOfAgent, PassengerGenerator requestCallTimeGenerator,
                                   String outputFileName, File osmMap) throws JsonGenerationException, JsonMappingException, IOException {

        generatePassengers(numberOfAgent, requestCallTimeGenerator, new File(osmMap.getParentFile(), outputFileName),
                osmMap);
    }

    public void generateDrivers(int numberOfDrivers, VehicleGenerator vehicleCapacityGenerator, File generatorOutput,
                                File osmMap) throws JsonGenerationException, JsonMappingException, IOException {
        GPSPositionGenerator positionGenerator = createGPSPositionGenerator(osmMap);

        List<Driver> agentRequestsResults = Lists.newArrayList();
        for (int i = 0; i < numberOfDrivers; i++) {
            agentRequestsResults.add(new Driver("DriverId" + i, positionGenerator.generateGPSPosition(),
                    vehicleCapacityGenerator.generateVehicleCapacity(), vehicleCapacityGenerator
                    .generateVehicleEquipments()));
        }

        mapper.writeValue(generatorOutput, agentRequestsResults);
    }

    public void generatePassengers(int numberOfAgent, PassengerGenerator requestCallTimeGenerator,
                                   File generatorOutput, File osmMap) throws JsonGenerationException, JsonMappingException, IOException {
        GPSPositionGenerator positionGenerator = createGPSPositionGenerator(osmMap);

        List<Passenger> agentRequestsResults = Lists.newArrayList();
        for (int i = 0; i < numberOfAgent; i++) {
            agentRequestsResults.add(new Passenger("PassengerId" + i, requestCallTimeGenerator
                    .generateAdditionalRequirements(), generateRequest(positionGenerator, requestCallTimeGenerator)));
        }

        mapper.writeValue(generatorOutput, agentRequestsResults);
    }

    protected List<PassengerRequest> generateRequest(GPSPositionGenerator positionGenerator,
                                                     PassengerGenerator requestCallTimeGenerator) {
        List<PassengerRequest> agentRequestsResults = Lists.newArrayList();

        int numberOfRequest = random.nextInt(maxNumberOfRequestPerAgent) + 1;

        GPS fromGPS = positionGenerator.generateGPSPosition();
        long fromNode = nearestNodeFinder.getNearestNodeByNodeId(fromGPS.longitude, fromGPS.latitude);
        for (int i = 0; i < numberOfRequest; i++) {
            GPS toGPS = positionGenerator.generateGPSPosition();
            long toNode = nearestNodeFinder.getNearestNodeByNodeId(toGPS.longitude, toGPS.latitude);

            while (fromNode == toNode) {
                LOGGER.debug("Generate request: same fromNode as toNode - " + fromNode);
                toGPS = positionGenerator.generateGPSPosition();
                toNode = nearestNodeFinder.getNearestNodeByNodeId(toGPS.longitude, toGPS.latitude);
            }

            RequestTimeInfo requestTimeInfo = requestCallTimeGenerator.generateRequestTimeInfo(fromNode, toNode);

            agentRequestsResults.add(new PassengerRequest(fromGPS, toGPS, requestTimeInfo.callTimeInDayRange,
                    requestTimeInfo.fromTimeWindow, requestTimeInfo.toTimeWindow));

            fromGPS = toGPS;
            fromNode = toNode;
        }

        return agentRequestsResults;
    }

    protected GPSPositionGenerator createGPSPositionGenerator(File osmMap) {
        OsmDataGetter osmDataGetter = OsmDataGetter.createOsmDataGetter(osmMap);
        Bounds bounds = OSMBoundsUtil.computeBoundsOfSimulationWorld(osmDataGetter);

        double maxLon = bounds.getMax().lon();
        double minLon = bounds.getMin().lon();

        double maxLat = bounds.getMax().lat();
        double minLat = bounds.getMin().lat();

        return positionGeneratorFactory.createGPSPositionGenerator(minLon, minLat, maxLon, maxLat);
    }

    public Utils getUtils() {
        return utils;
    }
}

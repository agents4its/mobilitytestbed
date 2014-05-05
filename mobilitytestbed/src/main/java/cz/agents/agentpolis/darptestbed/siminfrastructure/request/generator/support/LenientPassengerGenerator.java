package cz.agents.agentpolis.darptestbed.siminfrastructure.request.generator.support;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.primitives.Doubles;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import cz.agents.agentpolis.darptestbed.global.Utils;
import cz.agents.agentpolis.darptestbed.siminfrastructure.planner.init.TestbedPlannerModuleFactory;
import cz.agents.agentpolis.darptestbed.siminfrastructure.request.GPS;
import cz.agents.agentpolis.darptestbed.siminfrastructure.request.TimeWindow;
import cz.agents.agentpolis.darptestbed.siminfrastructure.request.generator.LenientRequestGenerator;
import cz.agents.agentpolis.darptestbed.simmodel.environment.TestbedEnvironmentModul;
import cz.agents.agentpolis.darptestbed.simulator.initializator.osm.NearestNodeInitModuleFactory;
import cz.agents.agentpolis.darptestbed.simulator.initializator.osm.NodeExtendedFunction;
import cz.agents.agentpolis.darptestbed.simulator.initializator.osm.init.TestbedMapInit;
import cz.agents.agentpolis.simmodel.environment.AgentPolisEnvironmentModule;
import cz.agents.agentpolis.simmodel.environment.model.citymodel.transportnetwork.EGraphType;
import cz.agents.agentpolis.simulator.creator.initializator.InitModuleFactory;
import cz.agents.agentpolis.simulator.creator.initializator.impl.MapData;
import cz.agents.agentpolis.simulator.importer.osm.OsmDataGetter;
import cz.agents.agentpolis.simulator.importer.osm.util.OSMBoundsUtil;
import cz.agents.agentpolis.simulator.vehiclemodel.init.VehicleDataModelModulFactory;
import cz.agents.agentpolis.utils.config.ConfigReader;
import cz.agents.alite.common.event.EventProcessor;
import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.math3.distribution.AbstractRealDistribution;
import org.apache.commons.math3.random.EmpiricalDistribution;
import org.apache.commons.math3.random.Well19937c;
import org.apache.log4j.Logger;
import org.joda.time.Duration;
import org.openstreetmap.osm.data.coordinates.Bounds;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;

public class LenientPassengerGenerator implements PassengerGenerator {

    private final static Logger LOGGER = Logger.getLogger(LenientPassengerGenerator.class);

    private final static long HOUR23 = Duration.standardHours(23).getMillis();
    private final static long HOUR23_30MIN = Duration.standardMinutes(23 * 60 + 30).getMillis();
    private final static long HOUR24 = Duration.standardDays(1).getMillis();
    private final static long TIME_WINDOW_RANGE = Duration.standardMinutes(30).getMillis();
    private final static long CALL_TIME_BEFORE_REQUEST_FROM_TIME = Duration.standardHours(1).getMillis();
    private final static long REQUEST_DURATION_TIME = Duration.standardHours(2).getMillis();

    private final AbstractRealDistribution dayDistribution;
    private final Random random;

    private final GPSPositionGenerator generator;
    private final Utils utils;
    private final String benchmarkDir;
    private final NodeExtendedFunction nearestNodeFinder;

    private final List<Long> sampleNodesList;
    private MapData osmDTO;

    private final long averageDrivingTimeBetweenSamples;
    private final long AVERAGE_PASSENGER_LOAD = 3;
    private final float PERCENTAGE_OF_ALL_NODES_USED_FOR_SAMPLES = 0.01f;
    private final double DEFAULT_SPEED = 30;

    public static AbstractRealDistribution createDayMockDistribution(final int seed) {
        final EmpiricalDistribution dayDistributionMock = new EmpiricalDistribution(new Well19937c(seed));

        int[] demandsOverDay = new int[]{/* 0 */9,/* 1 */6,/* 2 */6,
        /* 3 */6,/* 4 */7,/* 5 */12,/* 6 */25,/* 7 */37,/* 8 */49,
        /* 9 */38,/* 10 */30,/* 11 */30,/* 12 */30,/* 13 */30,/* 14 */31,
        /* 15 */36,/* 16 */45,/* 17 */41,/* 18 */31,/* 19 */27,/* 20 */25,/* 21 */22,/* 22 */18,/* 23 */11};

        List<Double> dayHourDistribution = Lists.newArrayList();

        int day = 0;
        for (int demand : demandsOverDay) {
            double hourInMillis = Duration.standardHours(day).getMillis();
            for (int i = 0; i < demand; i++) {
                dayHourDistribution.add(hourInMillis);
            }
            day++;
        }

        dayDistributionMock.load(Doubles.toArray(dayHourDistribution));

        return dayDistributionMock;

    }

    public LenientPassengerGenerator(AbstractRealDistribution dayDistribution, Random random,
                                     LenientRequestGenerator generator, String osmFileName, String benchmarkDir) {
        super();
        this.dayDistribution = dayDistribution;
        this.random = random;
        this.benchmarkDir = benchmarkDir;
        this.generator = createGPSPositionGenerator(new File(osmFileName), generator.getPositionGeneratorFactory());
        Injector injector = createInjector(this.benchmarkDir, new File(osmFileName));
        this.utils = injector.getInstance(Utils.class);
        this.nearestNodeFinder = injector.getInstance(NodeExtendedFunction.class);
//        this.sampleNodesList = null;
//        this.averageDrivingTimeBetweenSamples = 0;
        this.sampleNodesList = generateSamples();
        this.averageDrivingTimeBetweenSamples = calculateAverageDrivingTimeBetweenSamples();
    }

    private long calculateAverageDrivingTimeBetweenSamples() {
        double averageDrivingTime = 0d;

        LOGGER.debug("Starting calculateAverageDrivingTimeBetweenSamples.");

        int counter = 1;
        for (long position : sampleNodesList) {
            Long drivingTime = getMultipliedAverageDrivingTimeFromSamples(position, 0.5f);

            if (drivingTime == null)
                continue;

            averageDrivingTime = averageDrivingTime / (counter + 1) * counter + drivingTime / (counter + 1);
            ++counter;


//            LOGGER.debug("Finished #" + counter + " node in mutual distance average: " +
//                    position + " " + averageDrivingTime);
        }

        return (long) averageDrivingTime;
    }

    private List<Long> generateSamples() {

        int sampleCount = (int) (osmDTO.graphByType.get(EGraphType.HIGHWAY).getAllNodes().size() *
                PERCENTAGE_OF_ALL_NODES_USED_FOR_SAMPLES);

        List<Long> samples = new ArrayList<>();

//        LOGGER.debug("Generating " + sampleCount + " samples ...");

        for (int i = 0; i < sampleCount; ++i) {
//            LOGGER.debug("Generating sample #" + i);
            GPS position = generator.generateGPSPosition();
            samples.add(nearestNodeFinder.getNearestNodeByNodeId(position.longitude, position.latitude));
        }

        return samples;
    }

    private long getMultipliedAverageDrivingTimeFromSamples(long originNode, float coef) {
        double averageDrivingTime = 0d;

        int counter = 1;
        for (long position : sampleNodesList) {
            Long drivingTime = utils.computeDrivingTime(originNode, position, DEFAULT_SPEED);

            if (drivingTime == null)
                continue;

            averageDrivingTime = averageDrivingTime / (counter + 1) * counter + drivingTime / (counter + 1);
            ++counter;

//            if (counter % (int) (sampleNodesList.size() * 0.2f) == 0)
//                LOGGER.debug("Finished #" + counter + " node in distance from a single node average: " + position +
//                        averageDrivingTime);
        }

        return (long) (averageDrivingTime * coef);
    }

    @Override
    public Set<String> generateAdditionalRequirements() {
        return Sets.newHashSet(AdditionalRequirementsVehicleEquipment.WHEELCHAIR_SUPPORT.additionalRequirements);
    }

    @Override
    public RequestTimeInfo generateRequestTimeInfo() {
        throw new NotImplementedException();
    }

    @Override
    public RequestTimeInfo generateRequestTimeInfo(GPS fromGPS, GPS toGPS) {

//        return new RequestTimeInfo(1, new TimeWindow(1, 86400000 - 1), new TimeWindow(1, 86400000 - 1));

//        time should be driving time + driving to time as 1.3 from E(dist(fromGPS, sample)) +
//        time it would take the vehicle to deliver served passengers with the average path length of
//        the average of distance between samples

        long fromNode = nearestNodeFinder.getNearestNodeByNodeId(fromGPS.longitude, fromGPS.latitude);
        long toNode = nearestNodeFinder.getNearestNodeByNodeId(toGPS.longitude, toGPS.latitude);

        Long drivingTime = this.utils.computeDrivingTime(fromNode, toNode, DEFAULT_SPEED);

        if (drivingTime == null) {
            return new RequestTimeInfo(0, new TimeWindow(0, 0), new TimeWindow(0, 0));
        }

        double drivingToPassengerTime = getMultipliedAverageDrivingTimeFromSamples(fromNode, 1.3f); // think 80% of 2*E(dist)
        double timeToFinishDrivingNPassengers = (double) (averageDrivingTimeBetweenSamples) * AVERAGE_PASSENGER_LOAD;

        long fromTimeMin = (long) dayDistribution.sample();


        long fromTimeMax = Math.min(fromTimeMin + generateTime(TIME_WINDOW_RANGE), HOUR23_30MIN);

        long toTimeMin = (long)
                ((fromTimeMax + drivingTime + drivingToPassengerTime + timeToFinishDrivingNPassengers) *
                        (0.975d + Math.random() * 0.05d));

        long requestCallTimeInDayRange = Math.max(fromTimeMin - generateTime(CALL_TIME_BEFORE_REQUEST_FROM_TIME),
                1);

        checkArgument(fromTimeMin >= 0 && fromTimeMin <= HOUR23,
                "The distribution generated value, which is outof range 0 - " + HOUR23);

        if (requestCallTimeInDayRange < 0) {
            requestCallTimeInDayRange = 0;
        }

        long toTimeMax = toTimeMin + generateTime(TIME_WINDOW_RANGE);


        fromTimeMin = Math.min(Math.max(fromTimeMin, 1), HOUR23);
        fromTimeMax = Math.max(Math.min(fromTimeMax, 86400000 - 1), HOUR23_30MIN);
        toTimeMin = Math.min(Math.max(toTimeMin, 1), HOUR23);
        toTimeMax = Math.max(Math.min(toTimeMax, 86400000 - 1), HOUR23_30MIN);


        return new RequestTimeInfo(requestCallTimeInDayRange, new TimeWindow(fromTimeMin, fromTimeMax), new TimeWindow(
                toTimeMin, toTimeMax));
    }

    @Override
    public RequestTimeInfo generateRequestTimeInfo(long fromNode, long toNode) {

//        return new RequestTimeInfo(1, new TimeWindow(1, 86400000 - 1), new TimeWindow(1, 86400000 - 1));

//        time should be driving time + driving to time as 1.3 from E(dist(fromGPS, sample)) +
//        time it would take the vehicle to deliver served passengers with the average path length of
//        the average of distance between samples

        Long drivingTime = this.utils.computeDrivingTime(fromNode, toNode, DEFAULT_SPEED);

        if (drivingTime == null) {
            return new RequestTimeInfo(0, new TimeWindow(0, 0), new TimeWindow(0, 0));
        }

        double drivingToPassengerTime = getMultipliedAverageDrivingTimeFromSamples(fromNode, 1.3f); // think 80% of 2*E(dist)
        double timeToFinishDrivingNPassengers = (double) (averageDrivingTimeBetweenSamples) * AVERAGE_PASSENGER_LOAD;

        long fromTimeMin = (long) dayDistribution.sample();


        long fromTimeMax = Math.min(fromTimeMin + generateTime(TIME_WINDOW_RANGE), HOUR23_30MIN);

        long toTimeMin = (long)
                ((fromTimeMax + drivingTime + drivingToPassengerTime + timeToFinishDrivingNPassengers) *
                        (0.975d + Math.random() * 0.05d));

        long requestCallTimeInDayRange = Math.max(fromTimeMin - generateTime(CALL_TIME_BEFORE_REQUEST_FROM_TIME),
                1);

        checkArgument(fromTimeMin >= 0 && fromTimeMin <= HOUR23,
                "The distribution generated value, which is outof range 0 - " + HOUR23);

        if (requestCallTimeInDayRange < 0) {
            requestCallTimeInDayRange = 0;
        }

        long toTimeMax = toTimeMin + generateTime(TIME_WINDOW_RANGE);


        fromTimeMin = Math.min(Math.max(fromTimeMin, 1), HOUR23);
        fromTimeMax = Math.max(Math.min(fromTimeMax, 86400000 - 1), HOUR23_30MIN);
        toTimeMin = Math.min(Math.max(toTimeMin, 1), HOUR23);
        toTimeMax = Math.max(Math.min(toTimeMax, 86400000 - 1), HOUR23_30MIN);


        return new RequestTimeInfo(requestCallTimeInDayRange, new TimeWindow(fromTimeMin, fromTimeMax), new TimeWindow(
                toTimeMin, toTimeMax));

    }

    private long generateTime(long time) {
        return (long) (random.nextDouble() * time);
    }

    protected GPSPositionGenerator createGPSPositionGenerator(File osmMap, GPSPositionGeneratorFactory positionGeneratorFactory) {
        OsmDataGetter osmDataGetter = OsmDataGetter.createOsmDataGetter(osmMap);

        Bounds bounds = OSMBoundsUtil.computeBoundsOfSimulationWorld(osmDataGetter);

        double maxLon = bounds.getMax().lon();
        double minLon = bounds.getMin().lon();

        double maxLat = bounds.getMax().lat();
        double minLat = bounds.getMin().lat();

        return positionGeneratorFactory.createGPSPositionGenerator(minLon, minLat, maxLon, maxLat);
    }

    protected Injector createInjector(String benchmarkDir, File osmMap) {
        try {
            Injector injector = Guice.createInjector();


            // select the benchmark directory
            File experiment = new File(benchmarkDir);

            ConfigReader scenario = ConfigReader.initConfigReader(new File(experiment, "config/scenario.groovy").toURL());

            // extract the filenames from scenario.groovy config file

            int epsg = scenario.getIntegerValueFromConfig("epsg");
            String vehicledatamodelPath = scenario.getStringValueFromConfig("vehicledatamodelPath");

            TestbedMapInit mapInitFactory = new TestbedMapInit(epsg);

            osmDTO = mapInitFactory.initMap(osmMap, injector);

            EventProcessor eventProcessor = new EventProcessor();
            injector = injector.createChildInjector(new AgentPolisEnvironmentModule(
                    eventProcessor, new Random(4),
                    osmDTO.graphByType, osmDTO.nodesFromAllGraphs));

            List<InitModuleFactory> initModuleFactories = new ArrayList<>();

            initModuleFactories.add(new VehicleDataModelModulFactory(new File(vehicledatamodelPath)));
            initModuleFactories.add(new NearestNodeInitModuleFactory(epsg));
            initModuleFactories.add(new TestbedPlannerModuleFactory());

            injector = injector.createChildInjector(new TestbedEnvironmentModul(eventProcessor));

            // add agents into the environment
//            List<AgentInitFactory> agentInits = new ArrayList<>();
//            agentInits.add(new DriverForBenchmarkInitFactory(new File(driverPopulationPath)));

            for (InitModuleFactory initFactory : initModuleFactories) {
                AbstractModule module = initFactory.injectModule(injector);
                injector = injector.createChildInjector(module);
            }

//            initModuleFactories.add(new DispatchingAndTimersInitFactory());
            // initialize the dispatching and timers


            return injector;

        } catch (Exception e) {
            System.out.println(e.toString());
            return null;
        }

    }
}

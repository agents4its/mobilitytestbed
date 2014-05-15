package cz.agents.agentpolis.darptestbed.siminfrastructure.request.generator;

import com.google.common.collect.Lists;
import cz.agents.agentpolis.darptestbed.global.Utils;
import cz.agents.agentpolis.darptestbed.siminfrastructure.request.Driver;
import cz.agents.agentpolis.darptestbed.siminfrastructure.request.GPS;
import cz.agents.agentpolis.darptestbed.siminfrastructure.request.Passenger;
import cz.agents.agentpolis.darptestbed.siminfrastructure.request.PassengerRequest;
import cz.agents.agentpolis.darptestbed.siminfrastructure.request.generator.support.GPSPositionGenerator;
import cz.agents.agentpolis.darptestbed.siminfrastructure.request.generator.support.GPSPositionGeneratorFactory;
import cz.agents.agentpolis.darptestbed.siminfrastructure.request.generator.support.PassengerGenerator;
import cz.agents.agentpolis.darptestbed.siminfrastructure.request.generator.support.PassengerGenerator.RequestTimeInfo;
import cz.agents.agentpolis.darptestbed.siminfrastructure.request.generator.support.VehicleGenerator;
import cz.agents.agentpolis.simulator.importer.osm.OsmDataGetter;
import cz.agents.agentpolis.simulator.importer.osm.util.OSMBoundsUtil;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.openstreetmap.osm.data.coordinates.Bounds;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Random;

public class LenientRequestGenerator {

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
    private final ObjectMapper mapper;

    public LenientRequestGenerator(GPSPositionGeneratorFactory positionGeneratorFactory, Random random,
                                   int maxNumberOfRequestPerAgent, ObjectMapper mapper) {
        super();

        this.positionGeneratorFactory = positionGeneratorFactory;
        this.random = random;
        this.maxNumberOfRequestPerAgent = maxNumberOfRequestPerAgent;
        this.mapper = mapper;
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
        for (int i = 0; i < numberOfRequest; i++) {
            GPS toGPS = positionGenerator.generateGPSPosition();

            RequestTimeInfo requestTimeInfo = requestCallTimeGenerator.generateRequestTimeInfo(fromGPS, toGPS);

            agentRequestsResults.add(new PassengerRequest(fromGPS, toGPS, requestTimeInfo.callTimeInDayRange,
                    requestTimeInfo.fromTimeWindow, requestTimeInfo.toTimeWindow));

            fromGPS = toGPS;
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

}

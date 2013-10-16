package cz.agents.agentpolis.darptestbed.siminfrastructure.request.generator;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Random;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.openstreetmap.osm.data.coordinates.Bounds;

import com.google.common.collect.Lists;

import cz.agents.agentpolis.darptestbed.siminfrastructure.request.Driver;
import cz.agents.agentpolis.darptestbed.siminfrastructure.request.GPS;
import cz.agents.agentpolis.darptestbed.siminfrastructure.request.Passenger;
import cz.agents.agentpolis.darptestbed.siminfrastructure.request.PassengerRequest;
import cz.agents.agentpolis.darptestbed.siminfrastructure.request.generator.support.GPSPositionGenerator;
import cz.agents.agentpolis.darptestbed.siminfrastructure.request.generator.support.GPSPositionGeneratorFactory;
import cz.agents.agentpolis.darptestbed.siminfrastructure.request.generator.support.PasssengerGenerator;
import cz.agents.agentpolis.darptestbed.siminfrastructure.request.generator.support.PasssengerGenerator.RequestTimeInfo;
import cz.agents.agentpolis.darptestbed.siminfrastructure.request.generator.support.VehicleGenerator;
import cz.agents.agentpolis.simulator.importer.osm.OsmDataGetter;
import cz.agents.agentpolis.simulator.importer.osm.util.OSMBoundsUtil;

public class AgentRequestGenerator {

	private final GPSPositionGeneratorFactory positionGeneratorFactory;
	private final Random random;
	private final int maxNumberOfRequestPerAgent;
	private final ObjectMapper mapper;

	public AgentRequestGenerator(GPSPositionGeneratorFactory positionGeneratorFactory, Random random,
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

	public void generatePassengers(int numberOfAgent, PasssengerGenerator requestCallTimeGenerator,
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

	public void generatePassengers(int numberOfAgent, PasssengerGenerator requestCallTimeGenerator,
			File generatorOutput, File osmMap) throws JsonGenerationException, JsonMappingException, IOException {
		GPSPositionGenerator positionGenerator = createGPSPositionGenerator(osmMap);

		List<Passenger> agentRequestsResults = Lists.newArrayList();
		for (int i = 0; i < numberOfAgent; i++) {
			agentRequestsResults.add(new Passenger("PassengerId" + i, requestCallTimeGenerator
					.generateAdditionalRequirements(), generateRequest(positionGenerator, requestCallTimeGenerator)));
		}

		mapper.writeValue(generatorOutput, agentRequestsResults);
	}

	private List<PassengerRequest> generateRequest(GPSPositionGenerator positionGenerator,
			PasssengerGenerator requestCallTimeGenerator) {
		List<PassengerRequest> agentRequestsResults = Lists.newArrayList();

		int numberOfRequest = random.nextInt(maxNumberOfRequestPerAgent) + 1;

		GPS previousGPS = positionGenerator.generateGPSPosition();
		for (int i = 0; i < numberOfRequest; i++) {
			GPS nextGPS = positionGenerator.generateGPSPosition();

			RequestTimeInfo requestTimeInfo = requestCallTimeGenerator.generateRequestTimeInfo();

			agentRequestsResults.add(new PassengerRequest(previousGPS, nextGPS, requestTimeInfo.callTimeInDayRange,
					requestTimeInfo.fromTimeWindow, requestTimeInfo.toTimeWindow));
			previousGPS = nextGPS;
		}

		return agentRequestsResults;
	}

	private GPSPositionGenerator createGPSPositionGenerator(File osmMap) {
		OsmDataGetter osmDataGetter = OsmDataGetter.createOsmDataGetter(osmMap);
		Bounds bounds = OSMBoundsUtil.computeBoundsOfSimulationWorld(osmDataGetter);

		double maxLon = bounds.getMax().lon();
		double minLon = bounds.getMin().lon();

		double maxLat = bounds.getMax().lat();
		double minLat = bounds.getMin().lat();

		return positionGeneratorFactory.createGPSPositionGenerator(minLon, minLat, maxLon, maxLat);
	}

}

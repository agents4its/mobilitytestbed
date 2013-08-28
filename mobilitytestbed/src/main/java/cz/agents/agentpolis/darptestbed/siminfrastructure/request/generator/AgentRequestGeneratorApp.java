package cz.agents.agentpolis.darptestbed.siminfrastructure.request.generator;

import java.io.File;
import java.io.IOException;
import java.util.Random;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import cz.agents.agentpolis.darptestbed.siminfrastructure.request.generator.support.GPSPositionGeneratorWithNormalDistributionFactory;
import cz.agents.agentpolis.darptestbed.siminfrastructure.request.generator.support.PasssengerGeneratorImpl;
import cz.agents.agentpolis.darptestbed.siminfrastructure.request.generator.support.RandomVehicleCapacityGenerator;

public class AgentRequestGeneratorApp {

	/**
	 * @param args
	 * @throws IOException
	 * @throws JsonMappingException
	 * @throws JsonGenerationException
	 */
	public static void main(String[] args) throws JsonGenerationException, JsonMappingException, IOException {

		Random random = new Random(0);

		AgentRequestGenerator generator = new AgentRequestGenerator(
				new GPSPositionGeneratorWithNormalDistributionFactory(0), random, 4, new ObjectMapper());
		// AgentRequestGenerator generator = new AgentRequestGenerator(
		// new GPSPositionGeneratorWithUniformDistributionFactory(0), random, 4,
		// new ObjectMapper());
		generator.generatePassengers(100,
				new PasssengerGeneratorImpl(PasssengerGeneratorImpl.createDayMockDistribution(0), random),
				"passenger-with-requests.json", new File("experiments/exp-2/data/dublin.osm"));
		generator.generateDrivers(5, new RandomVehicleCapacityGenerator(5, random), "driver.json", new File(
				"experiments/exp-2/data/dublin.osm"));

	}
}

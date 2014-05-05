package cz.agents.agentpolis.darptestbed.siminfrastructure.request.generator;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Random;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.joda.time.DateTime;
import org.joda.time.Duration;

import cz.agents.agentpolis.darptestbed.siminfrastructure.request.generator.support.PassengerGeneratorJustRequirements;
import cz.agents.agentpolis.darptestbed.siminfrastructure.request.generator.support.RandomVehicleCapacityGenerator;

public class SanFranciscoAgentRequestGeneratorApp {

	public static void main(String[] args) throws JsonGenerationException, JsonMappingException, IOException {

		Random random = new Random(0);
		SanFranciscoAgentRequestGenerator sanFranciscoAgentRequestGenerator = new SanFranciscoAgentRequestGenerator(
				new RandomVehicleCapacityGenerator(5, random), new PassengerGeneratorJustRequirements(),
				new ObjectMapper(), Duration.standardMinutes(30));

		sanFranciscoAgentRequestGenerator.generatePopulation(new File("sf-drivers-population.json"), new File(
				"sf-passenger-population.json"), Paths.get("*"), new DateTime(1212580538000l));
	}

}

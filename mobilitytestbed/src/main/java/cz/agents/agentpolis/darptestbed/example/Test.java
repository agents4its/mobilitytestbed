package cz.agents.agentpolis.darptestbed.example;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import cz.agents.agentpolis.darptestbed.siminfrastructure.request.Passenger;

public class Test {

	private static final Logger LOGGER = Logger.getLogger(Test.class);

	public static void main(String[] args) {
		loadSerializedPassengerPopulation(new File("experiments/exp-3/data/sf-passenger-population.json"));
	}

	private static List<Passenger> loadSerializedPassengerPopulation(File serializedPopulation) {
		ObjectMapper mapper = new ObjectMapper();
		try {
			return mapper.readValue(serializedPopulation, new TypeReference<List<Passenger>>() {
			});
		} catch (IOException e) {
			LOGGER.error(e);
		}

		return new ArrayList<Passenger>();

	}

}

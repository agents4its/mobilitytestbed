package cz.agents.agentpolis.darptestbed.siminfrastructure.request.generator;

import cz.agents.agentpolis.darptestbed.siminfrastructure.request.generator.support.GPSPositionGeneratorWithNormalDistributionFactory;
import cz.agents.agentpolis.darptestbed.siminfrastructure.request.generator.support.PassengerGeneratorImpl;
import cz.agents.agentpolis.darptestbed.siminfrastructure.request.generator.support.RandomVehicleCapacityGenerator;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.Random;

public class AgentRequestGeneratorApp {

    /**
     * @param args
     * @throws IOException
     * @throws JsonMappingException
     * @throws JsonGenerationException
     */
    public static void main(String[] args) throws JsonGenerationException, JsonMappingException, IOException {
        File osmMap = new File("experiments/dublin_5_drivers/data/dublin.osm");

        Random random = new Random(0);

        AgentRequestGenerator generator = new AgentRequestGenerator(
                new GPSPositionGeneratorWithNormalDistributionFactory(0), random, 4, new ObjectMapper());


        generator.generatePassengers(100,
                new PassengerGeneratorImpl(PassengerGeneratorImpl.createDayMockDistribution(0), random, null),
                "passenger-with-requests.json", osmMap);
        generator.generateDrivers(5, new RandomVehicleCapacityGenerator(5, random), "driver.json", osmMap);

    }
}

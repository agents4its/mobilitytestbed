package cz.agents.agentpolis.darptestbed.siminfrastructure.request.generator;

import cz.agents.agentpolis.darptestbed.siminfrastructure.request.generator.support.GPSPositionGeneratorWithNormalDistributionFactory;
import cz.agents.agentpolis.darptestbed.siminfrastructure.request.generator.support.LenientPassengerGenerator;
import cz.agents.agentpolis.darptestbed.siminfrastructure.request.generator.support.RandomVehicleCapacityGenerator;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.Random;

public class LenientRequestGeneratorApp {

    private static final Logger LOGGER = Logger.getRootLogger();

    static final String BENCHMARK_DIR = "prague";
    static final String OSM_FILE_NAME = "prague";
    static final int PASSENGER_REQUESTS_COUNT = 300;
    static final int DRIVERS_COUNT = 100;


    /**
     * @param args
     * @throws java.io.IOException
     * @throws org.codehaus.jackson.map.JsonMappingException
     *
     * @throws org.codehaus.jackson.JsonGenerationException
     *
     */
    public static void main(String[] args) throws JsonGenerationException, JsonMappingException, IOException {
        PropertyConfigurator.configure("log4j.properties");

        if (args.length == 0) {
            args = new String[] {BENCHMARK_DIR, OSM_FILE_NAME, Integer.toString(PASSENGER_REQUESTS_COUNT),
                    Integer.toString(DRIVERS_COUNT)};
        }


        final String osmFileName = "../mobilitytestbedvisio/experiments/" + args[0] + "/data/" + args[1] + ".osm";
        final String benchmarkDir = "../mobilitytestbedvisio/experiments/" + args[0] + "/";
        Random random = new Random(0);

        LenientRequestGenerator generator = new LenientRequestGenerator(
                new GPSPositionGeneratorWithNormalDistributionFactory(0), random, 1, new ObjectMapper());

        generator.generatePassengers(Integer.parseInt(args[3]),
                new LenientPassengerGenerator(LenientPassengerGenerator.createDayMockDistribution(0), random,
                        generator, osmFileName, benchmarkDir),
                "passenger-with-requests.json", new File(osmFileName));
        generator.generateDrivers(Integer.parseInt(args[2]), new RandomVehicleCapacityGenerator(5, random), "driver.json", new File(
                osmFileName));

    }
}

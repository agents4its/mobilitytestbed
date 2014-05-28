package cz.agents.agentpolis.darptestbed.siminfrastructure.request.generator;

import com.google.inject.Guice;
import com.google.inject.Injector;
import cz.agents.agentpolis.darptestbed.global.Utils;
import cz.agents.agentpolis.darptestbed.siminfrastructure.request.generator.support.GPSPositionGeneratorWithRestaurantDensityBasedDistributionFactory;
import cz.agents.agentpolis.darptestbed.siminfrastructure.request.generator.support.PassengerGeneratorImpl;
import cz.agents.agentpolis.darptestbed.siminfrastructure.request.generator.support.PassengerGeneratorWithContinousRequestCallTimeDistribution;
import cz.agents.agentpolis.darptestbed.siminfrastructure.request.generator.support.RandomVehicleCapacityGenerator;
import cz.agents.agentpolis.darptestbed.simulator.initializator.osm.NodeExtendedFunction;
import org.apache.commons.math3.random.Well19937c;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.xml.DOMConfigurator;
import org.apache.xalan.xsltc.DOM;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class RestaurantDensityBasedRequestGeneratorApp {

    private static final Logger LOGGER = Logger.getRootLogger();

    static final String BENCHMARK_DIR = "prague";
    static final String OSM_FILE_NAME = "prague";
    static final int PASSENGER_REQUESTS_COUNT = 300;
    static final int DRIVERS_COUNT = 100;
    public static final String LOG_LOG4J_XML = "log/log4j.xml";

//    static String BENCHMARK_DIR = "hague_20_drivers";
//    static String OSM_FILE_NAME = "haag";

    /**
     * @param args
     * @throws java.io.IOException
     * @throws org.codehaus.jackson.map.JsonMappingException
     *
     * @throws org.codehaus.jackson.JsonGenerationException
     *
     */
    public static void main(String[] args) throws IOException, SQLException {
        File logFile = new File(LOG_LOG4J_XML);
        if (logFile.exists()) {
            DOMConfigurator.configure(LOG_LOG4J_XML);
        } else {
            URL url = RestaurantDensityBasedRequestGeneratorApp.class.getResource("/log/log4j.xml");
            DOMConfigurator.configure(url);
        }

        if (args.length == 1 && args[0].equals("DEFAULT")) {
            args = new String[] {BENCHMARK_DIR, OSM_FILE_NAME, Integer.toString(PASSENGER_REQUESTS_COUNT),
                    Integer.toString(DRIVERS_COUNT)};

            System.out.printf("Using default command-line parameters: %s %s %s %s\n", args);
        } else if (args.length != 4) {
            System.out.println("Usage 1: <path-with-input-data> <osm-clean-file-name> <passenger-requests-count> " +
                    "<driver-requests-counter>");
            System.out.println("Usage 2: DEFAULT");

            return;
        } else {
            System.out.printf("Using command-line parameters: %s %s %s %s\n", args);
        }

        final String osmFileName = "experiments/" + args[0] + "/data/" + args[1] + ".osm";
        final String benchmarkDir = "experiments/" + args[0] + "/";
        Well19937c rnd = new Well19937c();
        Random random = new Random();
        Injector injector = Guice.createInjector();
        GPSPositionGeneratorWithRestaurantDensityBasedDistributionFactory positionGeneratorFactory =
                new GPSPositionGeneratorWithRestaurantDensityBasedDistributionFactory(osmFileName, benchmarkDir,
                rnd, injector);

        LOGGER.debug("PositionGenerator finished");


        RestaurantDensityBasedRequestGenerator generator = new RestaurantDensityBasedRequestGenerator(
                positionGeneratorFactory, random, 1, osmFileName, benchmarkDir, new ObjectMapper(), injector);

        LOGGER.debug("RequestGenerator finished");

        File osmMap = new File(osmFileName);
        generator.generatePassengers(Integer.parseInt(args[2]),
                new PassengerGeneratorWithContinousRequestCallTimeDistribution(
                        PassengerGeneratorWithContinousRequestCallTimeDistribution.createDayMockDistribution(0),
                        random, generator.getUtils()),
                "passenger-with-requests.json", osmMap);

        LOGGER.debug("GeneratePassengers finished");

        generator.generateDrivers(Integer.parseInt(args[3]), new RandomVehicleCapacityGenerator(5, random),
                "driver.json", osmMap);

        LOGGER.debug("GenerateDrivers finished");
    }
}

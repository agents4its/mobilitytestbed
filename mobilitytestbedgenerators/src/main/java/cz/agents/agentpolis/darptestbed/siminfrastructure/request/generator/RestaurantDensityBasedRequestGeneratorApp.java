package cz.agents.agentpolis.darptestbed.siminfrastructure.request.generator;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Guice;
import com.google.inject.Injector;

import cz.agents.agentpolis.darptestbed.global.Utils;
import cz.agents.agentpolis.darptestbed.siminfrastructure.request.generator.support.FixedVehicleCapacityGenerator;
import cz.agents.agentpolis.darptestbed.siminfrastructure.request.generator.support.GPSPositionGeneratorWithRestaurantDensityBasedDistributionFactory;
import cz.agents.agentpolis.darptestbed.siminfrastructure.request.generator.support.PassengerGeneratorImpl;
import cz.agents.agentpolis.darptestbed.siminfrastructure.request.generator.support.PassengerGeneratorWithContinousRequestCallTimeDistribution;
import cz.agents.agentpolis.darptestbed.siminfrastructure.request.generator.support.RandomVehicleCapacityGenerator;
import cz.agents.agentpolis.darptestbed.simulator.initializator.osm.NodeExtendedFunction;

import org.apache.commons.io.FileUtils;
import org.apache.commons.math3.random.Well19937c;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.xml.DOMConfigurator;
import org.apache.xalan.xsltc.DOM;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class RestaurantDensityBasedRequestGeneratorApp {

    private static final Logger LOGGER = Logger.getRootLogger();

    // defaults (not used when invoked with multiple commandline arguments)
    static final String SOURCE_DATA_FOLDER = "dataFolderForGenerator";
    static final String OSM_FILE_NAME = "hague-map.osm";
    static final int PASSENGER_REQUESTS_COUNT = 100;
    static final int DRIVERS_COUNT = 10;
    static final int VEHICLE_CAPACITY_INCLUDING_DRIVER = 5;
    static final int TIME_WINDOWS_IN_MINUTES = 30;
    static final int CALL_TIME_BEFORE_REQUESTS_IN_MINUTES = 15;
    static final int EPSG = 2157;
    public static final String LOG_LOG4J_XML = "log"+File.separator+"log4j.xml";

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
            URL url = RestaurantDensityBasedRequestGeneratorApp.class.getResource(File.separator+"log"+File.separator+"log4j.xml");
            DOMConfigurator.configure(url);
        }
        
        // use defaults
        if (args.length == 1 && args[0].equals("DEFAULT")) {
            args = new String[] {
            		SOURCE_DATA_FOLDER, 
            		OSM_FILE_NAME, 
            		Integer.toString(PASSENGER_REQUESTS_COUNT),
                    Integer.toString(DRIVERS_COUNT),
                    Integer.toString(VEHICLE_CAPACITY_INCLUDING_DRIVER),
                    Integer.toString(TIME_WINDOWS_IN_MINUTES),
                    Integer.toString(CALL_TIME_BEFORE_REQUESTS_IN_MINUTES),
                    Integer.toString(EPSG)
            };
            System.out.printf("Using default command-line parameters: %s %s %s %s %s %s %s %s \n", args);
        } else if (args.length != 8) {
        	System.out.println("Provided args: "+args.length);
        // specify values by arguments
        	System.out.println("Usage 1: <path-to-source-data-folder> <osm-file-name-within-source> <passenger-requests-count> " +
                    "<driver-requests-counter> <vehicle-capacity-including-driver> <time-windows-in-mins> <call-time-before-req-in-mins> <EPSG code>");
        	System.out.println("Usage 2: DEFAULT");

            return;
        } 
        
        // create a directory for the new benchmark
        String newBenchmarkLocation = "experiments" + File.separator + args[1].replace(".osm", "") + 
        		"_d" + args[3] + 
        		"_p" + args[2] + 
        		"_c" + args[4] +
        		"_w" + args[5] +
        		"_b" + args[6];
        LOGGER.info("Deleting old folder if it exists: "+newBenchmarkLocation);
        	FileUtils.deleteDirectory(new File(newBenchmarkLocation));
        LOGGER.info("Creating this folder for new benchmark: "+newBenchmarkLocation);
        new File(newBenchmarkLocation+File.separatorChar+"config").mkdirs();
        
        // copy required source files there
        LOGGER.info("Copying required files from "+args[0]+" to "+newBenchmarkLocation+File.separator+"data");
        FileUtils.copyDirectoryToDirectory(new File(args[0]), new File(newBenchmarkLocation));
        LOGGER.info("Moving "+Paths.get(newBenchmarkLocation+File.separatorChar+(newBenchmarkLocation+File.separator+args[0]).substring((newBenchmarkLocation+File.separator+args[0]).lastIndexOf(File.separator)+1))+" to "+Paths.get(newBenchmarkLocation+File.separator+"data"));
        Files.move(Paths.get(newBenchmarkLocation+File.separatorChar+(newBenchmarkLocation+File.separator+args[0]).substring((newBenchmarkLocation+File.separator+args[0]).lastIndexOf(File.separator)+1)),
        		Paths.get(newBenchmarkLocation+File.separator+"data")
        		);
        Files.delete(Paths.get(newBenchmarkLocation+File.separator+"data"+File.separator+"scenario.groovy"));
        
        // create a config file
        LOGGER.info("Creating a config file "+newBenchmarkLocation+File.separator+"config"+File.separator+"scenario.groovy");
        String defaultConfigGroovy = readFile(args[0]+File.separator+"scenario.groovy", StandardCharsets.UTF_8);
        defaultConfigGroovy = defaultConfigGroovy.replace("__newbenchmarkfolder__","../../"+newBenchmarkLocation.replace('\\', '/')).replace("__epsg__",args[7]).replace("__osmfilename__", args[1]);
        PrintWriter out = new PrintWriter(newBenchmarkLocation+File.separator+"config"+File.separator+"scenario.groovy");
        out.write(defaultConfigGroovy);
        out.close();

        LOGGER.info("Generating ...");
        final String osmFileName = newBenchmarkLocation + File.separator + "data" + File.separator + args[1];
        final String benchmarkDir = newBenchmarkLocation + File.separator;
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
                        random, generator.getUtils(), Integer.parseInt(args[5]), Integer.parseInt(args[6])),
                "passenger-with-requests.json", osmMap);
        LOGGER.debug("GeneratePassengers finished");

        generator.generateDrivers(Integer.parseInt(args[3]), new FixedVehicleCapacityGenerator(Integer.parseInt(args[4])),
                "driver.json", osmMap);

        LOGGER.debug("GenerateDrivers finished");
    }
    
    static String readFile(String path, Charset encoding) 
    		  throws IOException 
    		{
    		  byte[] encoded = Files.readAllBytes(Paths.get(path));
    		  return new String(encoded, encoding);
    		}
}

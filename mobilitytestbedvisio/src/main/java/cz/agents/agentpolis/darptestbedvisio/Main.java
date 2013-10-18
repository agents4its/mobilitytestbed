package cz.agents.agentpolis.darptestbedvisio;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.Set;

import org.apache.log4j.Logger;

import com.google.common.collect.Sets;
import com.google.inject.Injector;

import cz.agents.agentpolis.darptestbed.global.GlobalParams;
import cz.agents.agentpolis.darptestbed.siminfrastructure.logger.analyser.TestbedLogAnalyser;
import cz.agents.agentpolis.darptestbed.siminfrastructure.logger.analyser.init.TestbedAnalazerProcessorInit;
import cz.agents.agentpolis.darptestbed.siminfrastructure.logger.item.PassengerGetInVehicleLogItem;
import cz.agents.agentpolis.darptestbed.siminfrastructure.logger.item.PassengerGetOffVehicleLogItem;
import cz.agents.agentpolis.darptestbed.siminfrastructure.planner.init.TestbedPlannerModulFactory;
import cz.agents.agentpolis.darptestbed.simmodel.agent.TestbedEntityType;
import cz.agents.agentpolis.darptestbed.simmodel.environment.TestbedEnvironmentFactory;
import cz.agents.agentpolis.darptestbed.simulator.initializator.DispatchingAndTimersInitFactory;
import cz.agents.agentpolis.darptestbed.simulator.initializator.DriverForBenchmarkInitFactory;
import cz.agents.agentpolis.darptestbed.simulator.initializator.PassengerForBenchmarkInitFactory;
import cz.agents.agentpolis.darptestbed.simulator.initializator.osm.NearestNodeInitModulFactory;
import cz.agents.agentpolis.darptestbed.simulator.initializator.osm.init.TestbedMapInit;
import cz.agents.agentpolis.siminfrastructure.logger.LogItem;
import cz.agents.agentpolis.simulator.creator.SimulationCreator;
import cz.agents.agentpolis.simulator.vehiclemodel.init.VehicleDataModelModulFactory;
import cz.agents.agentpolis.tools.geovisio.database.connection.DatabaseConnectionSettings;
import cz.agents.agentpolis.tools.geovisio.spy.AgentPolisDataReaderFactory;
import cz.agents.agentpolis.tools.geovisio.spy.SpyAgentInitFactory;
import cz.agents.agentpolis.tools.geovisio.spy.agentpolis.AgentPolisDataReader;
import cz.agents.agentpolis.tools.geovisio.spy.darptestbed.DarpTestbedDataReader;
import cz.agents.agentpolis.tools.geovisio.spy.darptestbed.LogHandler;
import cz.agents.agentpolis.utils.config.ConfigReader;
import cz.agents.agentpolis.utils.config.ConfigReaderException;
import cz.agents.dbtokmlexporter.darptestbed.DarpTestbedKmlVisualisator;
import eu.superhub.wp4.initializator.simulator.delaymodel.InfinityDelayingSegmentCapacityDeterminer;

/**
 * The main class of Mobility Testbed.
 */
public class Main {

	// ====================== SETTINGS START =======================

	// choose if we should use centralized (DARP) or decentralized coordination algorithms
	static boolean CENTRALIZED = true;

	// specify the path to the directory containing the benchmark scenario
	// (these can be downloaded from http://github.com/agents4its/mobilitytestbed/wiki/Benchmarks)
	static String BENCHMARK_DIR = "experiments/hague_20_drivers"; 

	// ======================= SETTINGS END ========================

	private static final Logger LOGGER = Logger.getLogger(Main.class);

	/**
	 * @param args
	 * @throws ConfigReaderException
	 * @throws IOException
	 */
	public static void main(String[] args) throws ConfigReaderException, IOException {

		// select the benchmark directory
		File experiment = null;
		if (args.length > 0) {
			experiment = new File(args[0]);
		} else {
			experiment = new File(BENCHMARK_DIR);
		}

		ConfigReader scenario = ConfigReader.initConfigReader(new File(experiment, "config/scenario.groovy").toURL());
		int resultFolderId = 0;

		// extract the filenames from scenario.groovy config file
		String driverPopulationPath = scenario.getStringValueFromConfig("driverPopulationPath");
		String passengerPopulationPath = scenario.getStringValueFromConfig("passengerPopulationPath");
		String darpResultFileName = scenario.getStringValueFromConfig("darpResultFileName");

		int epsg = scenario.getIntegerValueFromConfig("epsg");
		String vehicledatamodelPath = scenario.getStringValueFromConfig("vehicledatamodelPath");

		args = new String[0];

		// visualization settings
		final DatabaseConnectionSettings settings = new DatabaseConnectionSettings("", 0, "jeto", "jedno", "testbed",
				"public");
		String visualizationName = darpResultFileName;
		int visInterval = 2 * 60 * 1000;
		File resultFolder = new File(experiment, "results" + File.separator + resultFolderId);
		// visualization settings end

		TestbedLogAnalyser testbedLogAnalyser = new TestbedLogAnalyser(new File(resultFolder, "result_"
				+ darpResultFileName + ".txt"), System.currentTimeMillis());

		final LogHandler logHandler = new LogHandler();

		SimulationCreator creator = new SimulationCreator(new TestbedEnvironmentFactory(
				new InfinityDelayingSegmentCapacityDeterminer()), experiment, resultFolderId);

		creator.addLogger(testbedLogAnalyser);
		creator.addLogger(logHandler);

		creator.addInitModulFactory(new VehicleDataModelModulFactory(new File(vehicledatamodelPath)));

		TestbedAnalazerProcessorInit processorInit = new TestbedAnalazerProcessorInit(testbedLogAnalyser);
		creator.addInitFactory(processorInit);

		Set<Class<? extends LogItem>> logItems = Sets.newHashSet();
		logItems.add(PassengerGetInVehicleLogItem.class);
		logItems.add(PassengerGetOffVehicleLogItem.class);

		creator.addAllowEventForEventViewer(logItems);

		args = new String[0];
		// generate a random seed for the whole application
		if (args.length > 0) {
			GlobalParams.setUseResultsFile(false);

			GlobalParams.setRandomSeed(Long.parseLong(args[0]));
			GlobalParams.setNumberOfPassengers(Integer.parseInt(args[1]));
			GlobalParams.setNumberOfFiveSeatVehicles(Integer.parseInt(args[2]));
			GlobalParams.setTimeWindowsUsed(Boolean.parseBoolean(args[3]));
			GlobalParams.setCityCenterRadius(Double.parseDouble(args[4]));

			// *algorithm type
			int algorithmTypeNum = Integer.parseInt(args[5]);
			int timerInterval = Integer.parseInt(args[6]);

			if (algorithmTypeNum < 3) {
				GlobalParams.setCentralized(true);
				GlobalParams.setCentralAlgType(algorithmTypeNum + 2);
				GlobalParams.setTimerDispatchingInterval(timerInterval);
			} else {
				GlobalParams.setCentralized(false);
				GlobalParams.setDecentrAlgType(algorithmTypeNum - 2);
				GlobalParams.setTimerDriverInterval(timerInterval);
				GlobalParams.setTimerPassengerInterval(timerInterval / 2);
			}

			// **time windows size
			int timeWinSize = Integer.parseInt(args[7]);
			GlobalParams.setMaxPassengerStartLifeTime(10 * timeWinSize);
			GlobalParams.setEarliestDepartureShift((int) (15 * timeWinSize));
			GlobalParams.setTimeWinRelSize(timeWinSize);

		} else {
			// default (in-source) settings of parameters
			GlobalParams.setUseResultsFile(true);

			GlobalParams.setRandomSeed(4);
			GlobalParams.setNumberOfPassengers(30);
			GlobalParams.setNumberOfFiveSeatVehicles(8);
			GlobalParams.setTimeWindowsUsed(true);
			GlobalParams.setCityCenterRadius(600.0);

			GlobalParams.setMaxPassengerStartLifeTime(30); // 30
			GlobalParams.setEarliestDepartureShift(45); // 45
			GlobalParams.setTimeWinRelSize(3); // 3

			GlobalParams.setCentralized(CENTRALIZED); // use centralized
														// coordination?

			// applies only for centralized == true
			GlobalParams.setCentralAlgType(100); // Parameter here can be any
													// arbitrary number. This
													// defaults to
													// DispatchingLogicExample
													// class (centralized
													// example alg.)
			GlobalParams.setTimerDispatchingInterval(10);

			// applies only for centralized == false
			GlobalParams.setDecentrAlgType(50); // Parameter here can be any
												// arbitrary number. This
												// defaults to
												// DriverDecentrLogicExample
												// class (decentr. alg. example)
			GlobalParams.setTimerDriverInterval(10); // 30
			GlobalParams.setTimerPassengerInterval(5); // 35
		}

		// following parameters are usually the same
		GlobalParams.setRequestGeneratorType(1);
		GlobalParams.setNumberOfSixSeatVehicles(0);
		GlobalParams.setNumberOfSevenSeatVehicles(0);
		GlobalParams.setVelocityInKmph(5);
		GlobalParams.setDriverReturnsBack(false);
		GlobalParams.setPricePerKilometer(1000);

		LOGGER.info("Seed = " + GlobalParams.getRandomSeed());

		creator.addInitModulFactory(new NearestNodeInitModulFactory(epsg));
		creator.addInitModulFactory(new TestbedPlannerModulFactory());

		// add agents into the environment
		creator.addAgentInit(new DriverForBenchmarkInitFactory(new File(driverPopulationPath)));
		// creator.addAgentInit(new PassengerInitFactory());
		creator.addAgentInit(new PassengerForBenchmarkInitFactory(new File(passengerPopulationPath)));

		// initialize the dispatching and timers
		creator.addInitModulFactory(new DispatchingAndTimersInitFactory());

		// set up visual appearance of agents
		creator.addEntityStyleVis(TestbedEntityType.TAXI_DRIVER, Color.BLUE, 9);
		creator.addEntityStyleVis(TestbedEntityType.PASSENGER, Color.GREEN, 8);

		// visualization init
		creator.addAgentInit(new SpyAgentInitFactory(visInterval, visualizationName, new AgentPolisDataReaderFactory() {

			public AgentPolisDataReader createAgentPolisReader(Injector injector, String visName, int interval)
					throws ReflectiveOperationException, InterruptedException {
				return new DarpTestbedDataReader(settings, injector, visName, interval, 4326, logHandler
						.getRequestStorage());
			}
		}, creator));

		// start it up
		creator.startSimulation(new TestbedMapInit(epsg));

		// after finishing the simulation, report statistics
		testbedLogAnalyser.processResult();

		String outputString = resultFolder.getAbsolutePath() + File.separator + "visualizations";

		// export visualization to kml
		if (scenario.getBooleanValueFromConfig("createVisualizations") == true) {
			DarpTestbedKmlVisualisator kmlVisualisator = new DarpTestbedKmlVisualisator(settings, visInterval,
					"public", outputString);
			kmlVisualisator.visualize();
		} else {
			LOGGER.info("Visualizations are turned off.");
		}

		LOGGER.info("FINISHED!");

	}

	private static boolean uni(int r) {
		return r != -1;
	}
}

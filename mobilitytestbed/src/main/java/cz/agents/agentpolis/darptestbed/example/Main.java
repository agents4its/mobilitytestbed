package cz.agents.agentpolis.darptestbed.example;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import cz.agents.agentpolis.darptestbed.global.GlobalParams;
import cz.agents.agentpolis.darptestbed.siminfrastructure.logger.analyser.TestbedLogAnalyser;
import cz.agents.agentpolis.darptestbed.siminfrastructure.logger.analyser.init.TestbedAnalazerProcessorInit;
import cz.agents.agentpolis.darptestbed.siminfrastructure.logger.key.EPassengerLogItemKey;
import cz.agents.agentpolis.darptestbed.siminfrastructure.logger.key.EPassengerLogItemType;
import cz.agents.agentpolis.darptestbed.siminfrastructure.logger.key.ERequestLogItemKey;
import cz.agents.agentpolis.darptestbed.siminfrastructure.logger.key.ERequestLogItemType;
import cz.agents.agentpolis.darptestbed.siminfrastructure.logger.key.EVehicleLogItemType;
import cz.agents.agentpolis.darptestbed.siminfrastructure.planner.init.TestbedPlannerModulFactory;
import cz.agents.agentpolis.darptestbed.simmodel.agent.TestbedEntityType;
import cz.agents.agentpolis.darptestbed.simmodel.environment.TestbedEnvironmentFactory;
import cz.agents.agentpolis.darptestbed.simulator.initializator.DispatchingAndTimersInitFactory;
import cz.agents.agentpolis.darptestbed.simulator.initializator.DriverForBenchmarkInitFactory;
import cz.agents.agentpolis.darptestbed.simulator.initializator.PassengerForBenchmarkInitFactory;
import cz.agents.agentpolis.darptestbed.simulator.initializator.osm.NearestNodeInitModulFactory;
import cz.agents.agentpolis.darptestbed.simulator.initializator.osm.init.TestbedMapInit;
import cz.agents.agentpolis.logger.LogItemKey;
import cz.agents.agentpolis.logger.LogItemType;
import cz.agents.agentpolis.siminfrastructure.logger.key.EPassengerPositionLogItemKey;
import cz.agents.agentpolis.simulator.creator.SimulationCreator;
import cz.agents.agentpolis.utils.config.ConfigReader;
import cz.agents.agentpolis.utils.config.ConfigReaderException;
import eu.superhub.wp4.model.simodel.environment.model.delaymodel.factory.InfinityDelayingSegmentCapacityDeterminer;
import eu.superhub.wp4.simulator.initializator.vehiclemodel.init.VehicleDataModelModulFactory;

/**
 * The main class of DARP Testbed.
 * 
 * @author Lukas Canda
 */
public class Main {

	private static final Logger LOGGER = Logger.getLogger(Main.class);

	/**
	 * @param args
	 * @throws MalformedURLException
	 * @throws ConfigReaderException
	 */
	public static void main(String[] args) throws MalformedURLException, ConfigReaderException {

		// the file config.groovy sets up the map

		long startTime = System.currentTimeMillis();

		File experiment = new File("experiments/exp-2");

		TestbedLogAnalyser testbedLogAnalyser = new TestbedLogAnalyser(
				new File(experiment, "results/0/sim_results.txt"));
		List<Object> subscribe = new ArrayList<Object>();
		subscribe.add(testbedLogAnalyser);

		SimulationCreator creator = new SimulationCreator(new TestbedEnvironmentFactory(subscribe,
				new InfinityDelayingSegmentCapacityDeterminer()), experiment, 0);

		ConfigReader scenario = ConfigReader.initConfigReader(new File(experiment, "config/scenario.groovy").toURL());

		String driverPopulationPath = scenario.getStringValueFromConfig("driverPopulationPath");
		String passengerPopulationPath = scenario.getStringValueFromConfig("passengerPopulationPath");
		String vehicledatamodelPath = scenario.getStringValueFromConfig("vehicledatamodelPath");
		int epsg = scenario.getIntegerValueFromConfig("epsg");

		creator.addInitModulFactory(new VehicleDataModelModulFactory(new File(vehicledatamodelPath)));

		TestbedAnalazerProcessorInit processorInit = new TestbedAnalazerProcessorInit(testbedLogAnalyser);
		creator.addInitFactory(processorInit);

		// generate a random seed for the whole application
		/*
		 * Random random = new Random(); long seed = random.nextLong(); if(seed
		 * <= 0) { seed = seed * (-1); } else if (seed == 0) { seed = 1; }
		 */

		// set up all params either according to command line args or manually
		// args: 0: random_seed num_of_passen num_of_5_seat_taxis
		// 3: time_win_used city_center_radius algorithm_type_number*
		// 6: timer_interval time_win_size**
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
			// manual settings of parameters
			GlobalParams.setUseResultsFile(true);

			GlobalParams.setRandomSeed(4);
			GlobalParams.setNumberOfPassengers(30);
			GlobalParams.setNumberOfFiveSeatVehicles(8);
			GlobalParams.setTimeWindowsUsed(true);
			GlobalParams.setCityCenterRadius(600.0);

			GlobalParams.setMaxPassengerStartLifeTime(30);// 30
			GlobalParams.setEarliestDepartureShift(45);// 45
			GlobalParams.setTimeWinRelSize(3);// 3

			GlobalParams.setCentralized(true);

			// applies only for centralized == true
			GlobalParams.setCentralAlgType(1);
			GlobalParams.setTimerDispatchingInterval(10);

			// applies only for centralized == false
			GlobalParams.setDecentrAlgType(3);
			GlobalParams.setTimerDriverInterval(10);// 30
			GlobalParams.setTimerPassengerInterval(5);// 35
		}

		// following parameters are usually the same
		GlobalParams.setRequestGeneratorType(1);
		GlobalParams.setNumberOfSixSeatVehicles(0);
		GlobalParams.setNumberOfSevenSeatVehicles(0);
		GlobalParams.setVelocityInKmph(5);
		GlobalParams.setDriverReturnsBack(false);
		GlobalParams.setPricePerKilometer(1000);

		LOGGER.info("seed = " + GlobalParams.getRandomSeed());

		creator.replaceMapInitFactory(new TestbedMapInit(epsg));

		creator.addInitModulFactory(new NearestNodeInitModulFactory(epsg));
		creator.addInitModulFactory(new TestbedPlannerModulFactory());

		// add agents into the environment
		creator.addAgentInit(new DriverForBenchmarkInitFactory(new File(driverPopulationPath)));
		// creator.addAgentInit(new PassengerInitFactory());
		creator.addAgentInit(new PassengerForBenchmarkInitFactory(new File(passengerPopulationPath), 10000));

		// initialize the dispatching and timers
		creator.addInitModulFactory(new DispatchingAndTimersInitFactory());

		// set up visual appearance of agents
		creator.addEntityStyleVis(TestbedEntityType.TAXI_DRIVER, Color.BLUE, 9);
		creator.addEntityStyleVis(TestbedEntityType.PASSENGER, Color.GREEN, 8);

		// set up logger to CSV file (list all logged events and keys)
		List<LogItemType> allowEvents = new ArrayList<LogItemType>();
		allowEvents.addAll(Arrays.asList(EVehicleLogItemType.values()));
		allowEvents.addAll(Arrays.asList(EPassengerLogItemType.values()));
		allowEvents.addAll(Arrays.asList(ERequestLogItemType.values()));

		List<LogItemKey> keysToData = new ArrayList<LogItemKey>();
		keysToData.add(EPassengerPositionLogItemKey.PLACE);
		keysToData.add(EPassengerLogItemKey.VEHICLE);
		keysToData.add(ERequestLogItemKey.FROM_NODE);
		keysToData.add(ERequestLogItemKey.TO_NODE);
		keysToData.add(ERequestLogItemKey.TIME_WIN_OPEN);
		keysToData.add(ERequestLogItemKey.TIME_WIN_CLOSE);

		creator.addAllowEventsAndKeysForCSV(allowEvents, keysToData);

		// start it up
		creator.create();

		// after finishing the simulation, report statistics
		String path = experiment.getPath() + File.separator + "results" + File.separator + "0" + File.separator;
		// StatisticsLogger.getInstance().writeReport(
		// new File(path + "result" + GlobalParams.getNumberOfPassengers() +
		// ".csv"), new File(path + "taxi.csv"));

		// analazerProcessorInit.getReportBuilder().builtSimulationReport();

		try {
			FileUtils.writeStringToFile(new File(path + "sim_time.txt"),
					String.valueOf(System.currentTimeMillis() - startTime));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		testbedLogAnalyser.processResult();

	}
}

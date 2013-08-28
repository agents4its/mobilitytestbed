package cz.agents.agentpolis.darptestbedvisio;

import java.io.File;
import java.util.List;

import cz.agents.agentpolis.simulator.processing.batch.JavaProcess;
import cz.agents.agentpolis.utils.config.ConfigReader;

public class MainBatch {

	public static void main(String[] args) throws Exception {
		// the file config.groovy sets up the map
		File experiment = new File("experiments/exp-3");
		ConfigReader scenario = ConfigReader.initConfigReader(new File(experiment, "config/scenario.groovy").toURL());
		List<Integer> numOfPassengerRange = scenario.getArrayListFromConfig("numOfPassengerRange");
		List<Integer> numOfDriverRange = scenario.getArrayListFromConfig("numOfDriverRange");
		List<String> driverPopulationPaths = scenario.getArrayListFromConfig("driverPopulationPaths");
		List<String> passengerPopulationPaths = scenario.getArrayListFromConfig("passengerPopulationPaths");

		int resultFolderId = 0;

		for (String driverPopulationPath : driverPopulationPaths) {
			for (String passengerPopulationPath : passengerPopulationPaths) {

				for (Integer numOfPassenger : numOfPassengerRange) {

					Process process = null;
					for (Integer numOfDriver : numOfDriverRange) {
						resultFolderId++;

						args = new String[] { String.valueOf(resultFolderId), driverPopulationPath,
								passengerPopulationPath, String.valueOf(numOfPassenger), String.valueOf(numOfDriver) };

						Process currentProcess = JavaProcess.exec(MainOld.class, args);

						if (process == null) {
							process = currentProcess;
						} else {
							process.waitFor();
							currentProcess.waitFor();
							process = null;
						}

					}

					if (process != null) {
						process.waitFor();
					}

				}

			}
		}
	}

}

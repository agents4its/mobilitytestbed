package cz.agents.agentpolis.darptestbed.siminfrastructure.logger.analyser.processor;

import static com.google.common.base.Preconditions.checkState;

import java.util.Map;

import cz.agents.agentpolis.darptestbed.siminfrastructure.logger.analyser.init.TestbedProcessor;
import cz.agents.agentpolis.simmodel.environment.model.citymodel.transportnetwork.Graph;
import cz.agents.agentpolis.simmodel.environment.model.citymodel.transportnetwork.GraphType;
import eu.superhub.wp4.analyser.processor.AEmissionProcessor;
import eu.superhub.wp4.model.simodel.environment.model.vehiclemodel.VehicleDataModel;

public class TestbedEmissionProcessor extends AEmissionProcessor implements TestbedProcessor {

	public TestbedEmissionProcessor(Map<GraphType, Graph> transportNetworksByGraphType, long measureTimePeriodInms,
			VehicleDataModel vehicleDataModel) {
		super(transportNetworksByGraphType, measureTimePeriodInms, vehicleDataModel);
		// TODO Auto-generated constructor stub
	}

	@Override
	public String provideResult() {
		checkState(emissionDataMap.size() == 1 || emissionDataMap.size() == 0,
				"It contains emission data for more then 1 period");

		EmissionData emissionData = new EmissionData();
		if (emissionDataMap.size() == 1) {
			emissionData = emissionDataMap.values().iterator().next();
		}

		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("Total values of CO2 [gram]:");
		stringBuilder.append(emissionData.producedCO2InGram);
		stringBuilder.append(System.lineSeparator());

		stringBuilder.append("Total values of CO [gram]:");
		stringBuilder.append(emissionData.producedCOInGram);
		stringBuilder.append(System.lineSeparator());

		stringBuilder.append("Total values of NOx [gram]:");
		stringBuilder.append(emissionData.producedNOxInGram);
		stringBuilder.append(System.lineSeparator());

		stringBuilder.append("Total values of PM10 [gram]:");
		stringBuilder.append(emissionData.producedPM10InGram);
		stringBuilder.append(System.lineSeparator());

		stringBuilder.append("Total values of SOx [gram]:");
		stringBuilder.append(emissionData.producedSOxInGram);

		return stringBuilder.toString();

	}

}

package cz.agents.agentpolis.darptestbed.siminfrastructure.logger.analyser.processor;

import static com.google.common.base.Preconditions.checkState;

import java.util.Map;

import cz.agents.agentpolis.darptestbed.siminfrastructure.logger.analyser.init.TestbedProcessor;
import cz.agents.agentpolis.simmodel.environment.model.citymodel.transportnetwork.Graph;
import cz.agents.agentpolis.simmodel.environment.model.citymodel.transportnetwork.GraphType;
import cz.agents.agentpolis.simmodel.environment.model.vehiclemodel.VehicleDataModel;
import eu.superhub.wp4.simulator.analyser.processor.EnergyConsumptionProcessor;

public class TestbedEnergyConsumptionProcessor extends EnergyConsumptionProcessor implements TestbedProcessor {

	public TestbedEnergyConsumptionProcessor(Map<GraphType, Graph> transportNetworksByGraphType,
			long measureTimePeriodInms, VehicleDataModel vehicleDataModel) {
		super(transportNetworksByGraphType, measureTimePeriodInms, vehicleDataModel);
		// TODO Auto-generated constructor stub
	}

	@Override
	public String provideResult() {

		checkState(consumeFuel.size() == 1 || consumeFuel.size() == 0, "It containsfuel data for more then 1 period");

		Double fuel = 0.0;
		if (consumeFuel.size() == 1) {
			fuel = consumeFuel.values().iterator().next();
		}

		return String.format("Total values of fuel [Liter] :%s", fuel);

	}
}

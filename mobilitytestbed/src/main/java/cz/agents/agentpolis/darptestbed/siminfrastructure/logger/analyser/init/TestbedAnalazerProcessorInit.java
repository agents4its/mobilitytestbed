package cz.agents.agentpolis.darptestbed.siminfrastructure.logger.analyser.init;

import java.util.Map;

import com.google.inject.Injector;

import cz.agents.agentpolis.darptestbed.siminfrastructure.logger.analyser.TestbedLogAnalyser;
import cz.agents.agentpolis.darptestbed.siminfrastructure.logger.analyser.processor.TestbedEmissionProcessor;
import cz.agents.agentpolis.darptestbed.siminfrastructure.logger.analyser.processor.TestbedEnergyConsumptionProcessor;
import cz.agents.agentpolis.darptestbed.siminfrastructure.logger.analyser.processor.TotalVehicleDistanceProcessor;
import cz.agents.agentpolis.simmodel.environment.model.citymodel.transportnetwork.Graph;
import cz.agents.agentpolis.simmodel.environment.model.citymodel.transportnetwork.GraphType;
import cz.agents.agentpolis.simmodel.environment.model.citymodel.transportnetwork.TransportNetworks;
import cz.agents.agentpolis.simulator.creator.initializator.InitFactory;
import eu.superhub.wp4.model.simodel.environment.model.vehiclemodel.VehicleDataModel;

public class TestbedAnalazerProcessorInit implements InitFactory {

	private final TestbedLogAnalyser testbedLogAnalyser;

	public TestbedAnalazerProcessorInit(TestbedLogAnalyser testbedLogAnalyser) {
		super();
		this.testbedLogAnalyser = testbedLogAnalyser;
	}

	@Override
	public void initRestEnvironment(Injector injector) {

		final TransportNetworks transportNetworks = injector.getInstance(TransportNetworks.class);
		final Map<GraphType, Graph> transportNetworksByGraphType = transportNetworks.getGraphByType();
		final VehicleDataModel vehicleDataModel = injector.getInstance(VehicleDataModel.class);

		long dayInMillis = Long.MAX_VALUE;

		TestbedEmissionProcessor emissionProcessor = new TestbedEmissionProcessor(transportNetworksByGraphType,
				dayInMillis, vehicleDataModel);
		TotalVehicleDistanceProcessor totalVehicleDistanceProcessor = new TotalVehicleDistanceProcessor(
				transportNetworksByGraphType, dayInMillis);
		TestbedEnergyConsumptionProcessor testbedEnergyConsumptionProcessor = new TestbedEnergyConsumptionProcessor(
				transportNetworksByGraphType, dayInMillis, vehicleDataModel);

		testbedLogAnalyser.addVehiclePathProcessor(totalVehicleDistanceProcessor);
		testbedLogAnalyser.addVehiclePathProcessor(emissionProcessor);
		testbedLogAnalyser.addVehiclePathProcessor(testbedEnergyConsumptionProcessor);

	}

}

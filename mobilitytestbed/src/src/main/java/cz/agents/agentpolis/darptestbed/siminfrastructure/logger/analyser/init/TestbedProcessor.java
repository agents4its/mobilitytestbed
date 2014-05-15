package cz.agents.agentpolis.darptestbed.siminfrastructure.logger.analyser.init;

import eu.superhub.wp4.simulator.analyser.processor.VehiclePathProcessor;

public interface TestbedProcessor extends VehiclePathProcessor {

	public String provideResult();

}

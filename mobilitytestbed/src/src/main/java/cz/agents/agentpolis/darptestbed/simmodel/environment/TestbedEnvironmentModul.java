package cz.agents.agentpolis.darptestbed.simmodel.environment;

import java.util.HashMap;
import java.util.Map;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;

import cz.agents.agentpolis.darptestbed.simmodel.entity.vehicle.TestbedVehicle;
import cz.agents.alite.common.event.EventProcessor;

/**
 * The modul of basic simulation environment
 * 
 * @author Lukas Canda
 */
public class TestbedEnvironmentModul extends AbstractModule {

	private final EventProcessor eventProcessor;

	public TestbedEnvironmentModul(EventProcessor eventProcessor) {

		super();
		this.eventProcessor = eventProcessor;
	}

	@Override
	protected void configure() {
		// you can add some binding here
		// bind(DispatchingLogic.class).to(DispatchingLogicDummyParallel.class);
		bind(new TypeLiteral<Map<String, TestbedVehicle>>() {
		}).toInstance(new HashMap<String, TestbedVehicle>());

	}

}

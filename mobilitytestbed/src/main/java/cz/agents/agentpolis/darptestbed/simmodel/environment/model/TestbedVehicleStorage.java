package cz.agents.agentpolis.darptestbed.simmodel.environment.model;

import java.util.Map;
import java.util.Set;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import cz.agents.agentpolis.darptestbed.simmodel.entity.vehicle.TestbedVehicle;
import cz.agents.agentpolis.simmodel.entity.EntityType;
import cz.agents.agentpolis.simmodel.environment.model.EntityStorage;

@Singleton
public class TestbedVehicleStorage extends EntityStorage<TestbedVehicle> {

	@Inject
	public TestbedVehicleStorage(Map<String, TestbedVehicle> entities, Map<EntityType, Set<String>> entitiesByType) {
		super(entities, entitiesByType);
		// TODO Auto-generated constructor stub
	}

}

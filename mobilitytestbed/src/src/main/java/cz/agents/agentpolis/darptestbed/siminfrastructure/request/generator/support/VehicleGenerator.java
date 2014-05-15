package cz.agents.agentpolis.darptestbed.siminfrastructure.request.generator.support;

import java.util.Set;

public interface VehicleGenerator {

	public int generateVehicleCapacity();

	public Set<String> generateVehicleEquipments();

}

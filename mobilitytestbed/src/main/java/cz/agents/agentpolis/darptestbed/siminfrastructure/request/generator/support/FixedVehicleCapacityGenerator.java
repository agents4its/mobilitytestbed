package cz.agents.agentpolis.darptestbed.siminfrastructure.request.generator.support;

import java.util.Set;

import com.google.common.collect.Sets;

public class FixedVehicleCapacityGenerator implements VehicleGenerator {

	private final int fixVehicleCapacity;

	public FixedVehicleCapacityGenerator(int fixVehicleCapacity) {
		super();
		this.fixVehicleCapacity = fixVehicleCapacity;
	}

	@Override
	public int generateVehicleCapacity() {
		return fixVehicleCapacity;
	}

	@Override
	public Set<String> generateVehicleEquipments() {
		return Sets.newHashSet(AdditionalRequirementsVehicleEquipment.WHEELCHAIR_SUPPORT.vehicleEquipment);
	}

}

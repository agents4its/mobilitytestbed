package cz.agents.agentpolis.darptestbed.siminfrastructure.request.generator.support;

import java.util.Random;
import java.util.Set;

import com.google.common.collect.Sets;

public class RandomVehicleCapacityGenerator implements VehicleGenerator {

	private final static int MIN_NUMBER_OF_PASSENGER = 2;

	private final int maxVehicleCapacity;
	private final Random random;

	public RandomVehicleCapacityGenerator(int maxVehicleCapacity, Random random) {
		super();
		this.maxVehicleCapacity = maxVehicleCapacity;
		this.random = random;
	}

	@Override
	public int generateVehicleCapacity() {
		return MIN_NUMBER_OF_PASSENGER + random.nextInt(maxVehicleCapacity - MIN_NUMBER_OF_PASSENGER + 1);
	}

	@Override
	public Set<String> generateVehicleEquipments() {
		return Sets.newHashSet(AdditionalRequirementsVehicleEquipment.WHEELCHAIR_SUPPORT.vehicleEquipment);
	}
}

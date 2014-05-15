package cz.agents.agentpolis.darptestbed.simmodel.entity.vehicle;

import java.util.Set;

import cz.agents.agentpolis.simmodel.entity.EntityType;
import cz.agents.agentpolis.simmodel.entity.vehicle.Vehicle;
import cz.agents.agentpolis.simmodel.environment.model.citymodel.transportnetwork.GraphType;

/**
 * One such vehicle is generated for every taxi driver. This class can be
 * extended for arbitrary number of parameters, that can affect the planning
 * process.
 * 
 * @author Lukas Canda
 */
public class TestbedVehicle extends Vehicle {

	private final Set<String> vehicleEquipments;

	public TestbedVehicle(String vehicleId, EntityType type, double lengthInMeters, int vehiclePassengerCapacity,
			GraphType usingGraphTypeForMoving, Set<String> vehicleEquipments) {

		super(vehicleId, type, lengthInMeters, vehiclePassengerCapacity, usingGraphTypeForMoving);

		this.vehicleEquipments = vehicleEquipments;
	}

	public Set<String> getVehicleEquipments() {
		return vehicleEquipments;
	}
	
}

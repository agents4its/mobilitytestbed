package cz.agents.agentpolis.darptestbed.siminfrastructure.request;

import java.util.Set;

public class Driver {

	public final String driverId;
	public final GPS driverInitPosition;
	public final int vehicleCapacity;
	public final Set<String> vehicleEquipments;

	// It is just fo JSON serialization - don not use
	private Driver() {
		super();
		this.driverId = null;
		this.driverInitPosition = null;
		this.vehicleCapacity = 0;
		this.vehicleEquipments = null;
	}

	public Driver(String driverId, GPS driverInitPosition, int vehicleCapacity, Set<String> vehicleEquipments) {
		super();
		this.driverId = driverId;
		this.driverInitPosition = driverInitPosition;
		this.vehicleCapacity = vehicleCapacity;
		this.vehicleEquipments = vehicleEquipments;
	}

}

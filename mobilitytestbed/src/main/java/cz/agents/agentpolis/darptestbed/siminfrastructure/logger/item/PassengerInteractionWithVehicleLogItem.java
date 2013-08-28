package cz.agents.agentpolis.darptestbed.siminfrastructure.logger.item;

public abstract class PassengerInteractionWithVehicleLogItem {

	public final long simulationTime;
	public final String passengerId;
	public final String vehicleId;

	public PassengerInteractionWithVehicleLogItem(long simulationTime, String passengerId, String vehicleId) {
		super();
		this.simulationTime = simulationTime;
		this.passengerId = passengerId;
		this.vehicleId = vehicleId;
	}

}

package cz.agents.agentpolis.darptestbed.siminfrastructure.logger.item;

import cz.agents.agentpolis.siminfrastructure.logger.LogItem;

/**
 * 
 * @author Marek Cuchy
 * 
 */
public class RequestConfirmedLogItem implements LogItem {

	public final long simulationTime;
	public final String passengerId;
	public final String driverId;
	public final String vehicleId;

	public RequestConfirmedLogItem(long simulationTime, String passengerId, String driverId, String vehicleId) {
		super();
		this.simulationTime = simulationTime;
		this.passengerId = passengerId;
		this.driverId = driverId;
		this.vehicleId = vehicleId;
	}

	@Override
	public String toString() {
		return "RequestConfirmedLogItem [passengerId=" + passengerId + ", driverId=" + driverId + ", vehicleId="
				+ vehicleId + "]";
	}
}

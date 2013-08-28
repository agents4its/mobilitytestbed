package cz.agents.agentpolis.darptestbed.simmodel.agent.data;

/**
 * Information about the trip, that is usually sent to passengers.
 * It basically tells them to get ready to get in the vehicle quite soon.
 * 
 * @author Lukas Canda
 */
public class TripInfo {

	protected String driverId;
	/**
	 * The id of the vehicle that will pick me up
	 */
	protected String vehicleId;

	
	public TripInfo(String driverId, String vehicleId) {
		this.driverId = driverId;
		this.vehicleId = vehicleId;
	}


	public String getDriverId() {
		return driverId;
	}

	public String getVehicleId() {
		return vehicleId;
	}
}

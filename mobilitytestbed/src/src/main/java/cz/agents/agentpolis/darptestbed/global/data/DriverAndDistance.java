package cz.agents.agentpolis.darptestbed.global.data;

/**
 * Objects to be sorted in a table of distance times
 * 
 * @author Lukas Canda
 */
public class DriverAndDistance {
	
	protected String taxiDriverId;
	/**
	 * How long it takes to drive the distance between
	 * the taxi driver and the passenger
	 */
	protected long distanceTime;
	
	
	public DriverAndDistance(String taxiDriverId, long distanceTime) {
		this.taxiDriverId = taxiDriverId;
		this.distanceTime = distanceTime;
	}

	public String getTaxiDriverId() {
		return taxiDriverId;
	}

	/**
	 * How long it takes to drive the distance between
	 * the taxi driver and the passenger
	 * 
	 * @return travel time in milliseconds
	 */
	public long getDistanceTime() {
		return distanceTime;
	}
}

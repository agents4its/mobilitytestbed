package cz.agents.agentpolis.darptestbed.siminfrastructure.request;

public class GPS {

	public final double latitude;
	public final double longitude;

	private GPS() {
		super();
		this.latitude = 0;
		this.longitude = 0;
	}

	public GPS(double latitude, double longitude) {
		super();
		this.latitude = latitude;
		this.longitude = longitude;
	}

	@Override
	public String toString() {
		return "GPS [latitude=" + latitude + ", longitude=" + longitude + "]";
	}

}

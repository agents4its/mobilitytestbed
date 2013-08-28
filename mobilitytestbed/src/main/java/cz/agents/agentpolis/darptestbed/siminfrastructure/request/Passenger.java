package cz.agents.agentpolis.darptestbed.siminfrastructure.request;

import java.util.List;
import java.util.Set;

public class Passenger {

	public final String passsngerId;
	public final Set<String> additionalRequirements;
	public final List<PassengerRequest> requests;

	public Passenger() {
		super();
		this.passsngerId = null;
		this.additionalRequirements = null;
		this.requests = null;
	}

	public Passenger(String passsngerId, Set<String> additionalRequirements, List<PassengerRequest> requests) {
		super();
		this.passsngerId = passsngerId;
		this.additionalRequirements = additionalRequirements;
		this.requests = requests;
	}

}

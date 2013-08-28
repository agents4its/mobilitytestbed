package cz.agents.agentpolis.darptestbed.siminfrastructure.logger.key;

import cz.agents.agentpolis.logger.LogItemType;

public enum EPassengerLogItemType implements LogItemType {

	PASSENGER_GOT_IN_TAXI ("PASSENGER_GOT_IN_TAXI"),
	PASSENGER_GOT_OFF_TAXI ("PASSENGER_GOT_OFF_TAXI");
	
	
	private final String eventName;
	
	private EPassengerLogItemType(String eventName) {
		this.eventName = eventName;
	}
	
	@Override
	public String getLogItemType() {
		return eventName;
	}

}

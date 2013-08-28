package cz.agents.agentpolis.darptestbed.siminfrastructure.logger.key;

import cz.agents.agentpolis.logger.LogItemType;

public enum ERequestLogItemType implements LogItemType {

	PASSENGER_SENT_REQUEST ("PASSENGER_SENT_REQUEST");

	
	private final String eventName;
	
	private ERequestLogItemType(String eventName) {
		this.eventName = eventName;
	}
	
	@Override
	public String getLogItemType() {
		return eventName;
	}

}

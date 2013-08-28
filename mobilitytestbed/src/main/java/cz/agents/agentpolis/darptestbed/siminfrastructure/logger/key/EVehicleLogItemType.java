package cz.agents.agentpolis.darptestbed.siminfrastructure.logger.key;

import cz.agents.agentpolis.logger.LogItemType;

public enum EVehicleLogItemType implements LogItemType {

	VEHICLE_MOVEMENT ("VEHICLE_MOVEMENT");
	
	
	private final String eventName;	
	
	private EVehicleLogItemType(String eventName) {
		this.eventName = eventName;
	}
	
	@Override
	public String getLogItemType() {
		return eventName;
	}

}

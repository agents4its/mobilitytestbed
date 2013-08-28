package cz.agents.agentpolis.darptestbed.siminfrastructure.logger.key;

import cz.agents.agentpolis.logger.LogItemKey;

public enum EPassengerLogItemKey implements LogItemKey {
	
	VEHICLE ("VEHICLE");
	
	
	private final String keyName;
	
	private EPassengerLogItemKey(String keyName) {
		this.keyName = keyName;
	}

	@Override
	public String getLogItemKeyName() {
		return keyName;
	}
}

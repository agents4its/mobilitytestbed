package cz.agents.agentpolis.darptestbed.siminfrastructure.logger.key;

import cz.agents.agentpolis.logger.LogItemKey;

public enum ERequestLogItemKey implements LogItemKey {

	FROM_NODE ("FROM"),
	TO_NODE ("TO"),
	TIME_WIN_OPEN ("TIME_WIN_OPEN"),
	TIME_WIN_CLOSE ("TIME_WIN_CLOSE");
	
	
	private final String keyName;
	
	private ERequestLogItemKey(String keyName) {
		this.keyName = keyName;
	}

	@Override
	public String getLogItemKeyName() {
		return keyName;
	}
}

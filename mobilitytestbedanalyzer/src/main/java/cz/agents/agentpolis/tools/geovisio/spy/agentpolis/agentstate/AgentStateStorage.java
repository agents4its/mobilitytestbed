package cz.agents.agentpolis.tools.geovisio.spy.agentpolis.agentstate;

import java.util.HashMap;
import java.util.Map;

import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * 
 * @author Marek Cuchy
 * 
 */
@Singleton
public class AgentStateStorage {

	private final Map<String, AgentState> storage;

	@Inject
	public AgentStateStorage() {
		super();
		this.storage = new HashMap<>();
	}

	public void setState(String id, AgentState state) {
		storage.put(id, state);
	}

	public AgentState getState(String id) {
		return storage.get(id);
	}

}

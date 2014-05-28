package cz.agents.agentpolis.tools.geovisio.spy.agentpolis.agentstate;

import java.sql.Timestamp;

/**
 * 
 * @author Marek Cuchy
 * 
 */
public class AgentState {

	public enum AgentStateType {
		WAITING,
		TRAVEL,
		LOCATION
	}

	private final AgentStateType state;
	private final Timestamp startTime;
	private final Timestamp endTime;
	private final Timestamp duration;
	private final String description;
	
	public AgentState(AgentStateType state, Timestamp startTime, Timestamp endTime, Timestamp duration,
			String description) {
		super();
		this.state = state;
		this.startTime = startTime;
		this.endTime = endTime;
		this.duration = duration;
		this.description = description;
	}

	public AgentStateType getState() {
		return state;
	}

	public Timestamp getStarTime() {
		return startTime;
	}

	public Timestamp getEndTime() {
		return endTime;
	}

	public Timestamp getDuration() {
		return duration;
	}

	public String getDescription() {
		return description;
	}
}

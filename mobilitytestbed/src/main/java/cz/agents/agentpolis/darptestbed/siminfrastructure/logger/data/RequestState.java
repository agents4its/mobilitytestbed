package cz.agents.agentpolis.darptestbed.siminfrastructure.logger.data;

/**
 * Current states of requests in time (during reading the log file)
 * 
 * @author Lukas Canda
 */
public enum RequestState {
	
	SENT,
	SERVED_WITH_DELAY,
	SERVED_ON_TIME
}

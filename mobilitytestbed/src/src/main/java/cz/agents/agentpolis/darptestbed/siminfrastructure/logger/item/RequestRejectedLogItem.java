package cz.agents.agentpolis.darptestbed.siminfrastructure.logger.item;

import cz.agents.agentpolis.siminfrastructure.logger.LogItem;

/**
 * 
 * @author Marek Cuchy
 * 
 */
public class RequestRejectedLogItem implements LogItem {

	public final long simulationTime;
	public final String passengerId;

	public RequestRejectedLogItem(long simulationTime, String passengerId) {
		super();
		this.simulationTime = simulationTime;
		this.passengerId = passengerId;
	}

}

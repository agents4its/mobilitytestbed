package cz.agents.agentpolis.darptestbed.siminfrastructure.logger.item;

/**
 * 
 * @author Marek Cuchy
 * 
 */
public class RequestRejectedLogItem {

	public final long simulationTime;
	public final String passengerId;

	public RequestRejectedLogItem(long simulationTime, String passengerId) {
		super();
		this.simulationTime = simulationTime;
		this.passengerId = passengerId;
	}

}

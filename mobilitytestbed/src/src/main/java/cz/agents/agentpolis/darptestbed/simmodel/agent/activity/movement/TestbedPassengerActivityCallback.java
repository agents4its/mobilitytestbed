package cz.agents.agentpolis.darptestbed.simmodel.agent.activity.movement;

/**
 * The callback concerning a passenger's taxi trip.
 * 
 * @author Lukas Canda
 */
public interface TestbedPassengerActivityCallback {

	/**
	 * The method is usually called when the passenger reaches his target node.
	 * 
	 * @param targetNode the number of the target node
	 */
	public void tripFinished(long targetNode);
	
}

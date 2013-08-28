package cz.agents.agentpolis.darptestbed.simmodel.agent;

import cz.agents.agentpolis.darptestbed.global.Utils;
import cz.agents.agentpolis.darptestbed.simmodel.environment.model.TestbedModel;
import cz.agents.agentpolis.ondemandtransport.siminfrastructure.communication.protocol2.AMessageProtocol;
import cz.agents.agentpolis.simmodel.environment.model.query.AgentPositionQuery;

/**
 * The basic features of an Agent, especially his communication protocol that
 * enables him to contact other agents.
 * 
 * @author Lukas Canda
 */
public abstract class AgentLogic<TMessageProtocol extends AMessageProtocol<?>> {

	// protected final String agentId; // TODO: Agent takes this inforamtion too
	/**
	 * A message sender to communicate with other agents
	 */
	protected final TMessageProtocol sender;
	/**
	 * A storage to save all data concerning taxi drivers and passengers
	 */
	protected final TestbedModel taxiModel; // TODO: Not for all
	/**
	 * Returns an agent's current position.
	 */
	protected final AgentPositionQuery positionQuery;
	/**
	 * A set of useful methods for searching paths, distances etc.
	 */
	protected final Utils utils;

	public AgentLogic(TMessageProtocol sender, TestbedModel taxiModel, AgentPositionQuery positionQuery, Utils utils) {

		// this.agentId = agentId;
		this.sender = sender;
		this.taxiModel = taxiModel;
		this.positionQuery = positionQuery;
		this.utils = utils;
	}

	/**
	 * @return current simulation time as string in format HH:MM
	 */
	protected String getCurrentTimeStr() {
		return utils.toHoursAndMinutes(utils.getCurrentTime());
	}

}

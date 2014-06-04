package cz.agents.agentpolis.darptestbed.simmodel.agent;

import cz.agents.agentpolis.darptestbed.global.Utils;
import cz.agents.agentpolis.darptestbed.siminfrastructure.communication.protocol.GeneralMessageProtocol;
import cz.agents.agentpolis.darptestbed.siminfrastructure.communication.receiver.BaseReceiverVisitor;
import cz.agents.agentpolis.darptestbed.siminfrastructure.communication.receiver.StringMessage;
import cz.agents.agentpolis.darptestbed.simmodel.environment.model.TestbedModel;
import cz.agents.agentpolis.ondemandtransport.siminfrastructure.communication.protocol.AMessageProtocol;
import cz.agents.agentpolis.simmodel.environment.model.query.AgentPositionQuery;

import java.util.ArrayList;
import java.util.List;

/**
 * The basic features of an Agent, especially his communication protocol that
 * enables him to contact other agents.
 * 
 * @author Lukas Canda
 */
public abstract class AgentLogic<TMessageProtocol extends AMessageProtocol<? extends BaseReceiverVisitor>> {

	// protected final String agentId; // TODO: Agent takes this inforamtion too
	/**
	 * A message sender to communicate with other agents
	 */
	protected final TMessageProtocol sender;

    /**
     * A message sender for simple string communication
     */
    protected final GeneralMessageProtocol generalMessageProtocol;
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
    protected final String agentId;

    public AgentLogic(String agentId, TMessageProtocol sender, GeneralMessageProtocol generalMessageProtocol,
                      TestbedModel taxiModel,
                      AgentPositionQuery positionQuery, Utils utils) {

		this.sender = sender;
        this.generalMessageProtocol = generalMessageProtocol;
        this.taxiModel = taxiModel;
		this.positionQuery = positionQuery;
		this.utils = utils;
        this.agentId = agentId;
    }

	/**
	 * @return current simulation time as string in format HH:MM
	 */
	protected String getCurrentTimeStr() {
		return utils.toHoursAndMinutes(utils.getCurrentTime());
	}

    public void processTextMessage(StringMessage message) {}

    protected void sendTextMessage(String agentId, String message) {
        ArrayList<String> list = new ArrayList<>();
        list.add(agentId);
        sendTextMessage(list, message);
    }

    protected void sendTextMessage(List<String> list, String message) {
        StringMessage stringMessage = new StringMessage(getAgentId(), message);
        generalMessageProtocol.sendMessage(list, stringMessage);
    }

    /**
     * @return the agent's id
     */
    protected String getAgentId() {
        return this.agentId;
    }
}

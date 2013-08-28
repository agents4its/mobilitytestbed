package cz.agents.agentpolis.darptestbed.simmodel.agent.driver;

import org.apache.log4j.Logger;

import cz.agents.agentpolis.darptestbed.siminfrastructure.communication.requestconsumer.message.ProposalAccept;
import cz.agents.agentpolis.darptestbed.siminfrastructure.communication.requestconsumer.message.ProposalReject;
import cz.agents.agentpolis.darptestbed.siminfrastructure.communication.requestconsumer.receiver.RequestConsumerReceiverVisitor;
import cz.agents.agentpolis.darptestbed.simmodel.agent.data.Request;
import cz.agents.agentpolis.darptestbed.simmodel.agent.driver.logic.DriverDecentrLogic;
import cz.agents.agentpolis.darptestbed.simmodel.agent.timer.TimerCallback;
import cz.agents.agentpolis.simmodel.entity.EntityType;
import cz.agents.agentpolis.simmodel.environment.model.query.AgentPositionQuery;

public class DecentDriverAgent extends DriverAgent<DriverDecentrLogic> implements TimerCallback,
		RequestConsumerReceiverVisitor {

	private static final Logger LOGGER = Logger.getLogger(DecentDriverAgent.class);

	public DecentDriverAgent(String agentId, EntityType agentType, DriverDecentrLogic logic,
			AgentPositionQuery positionQuery) {
		super(agentId, agentType, logic, positionQuery);
		// TODO Auto-generated constructor stub
	}

	/**
	 * If the decentralized communication is used, this method is usually called
	 * by a timer at regular intervals.
	 */
	@Override
	public void timerCallback() {
		logic.processRequests();
		logic.processAcceptancesAndRejections();
	}

	// ---
	@Override
	public void visit(Request request) {
		//LOGGER.debug(getId() + ":" + request.getClass().getSimpleName());
		logic.processNewRequest(request);

	}

	@Override
	public void visit(ProposalReject proposalReject) {
		//LOGGER.debug(getId() + ":" + proposalReject.getClass().getSimpleName());

	}

	@Override
	public void visit(ProposalAccept proposalAccept) {
		//LOGGER.debug(getId() + ":" + proposalAccept.getClass().getSimpleName());
		logic.processNewAcceptance(proposalAccept.proposal);

	}

}

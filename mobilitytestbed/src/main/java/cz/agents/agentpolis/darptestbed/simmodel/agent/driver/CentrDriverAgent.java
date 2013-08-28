package cz.agents.agentpolis.darptestbed.simmodel.agent.driver;

import org.apache.log4j.Logger;

import cz.agents.agentpolis.darptestbed.siminfrastructure.communication.driver.message.MessageDispatcherSendsOutTaxi;
import cz.agents.agentpolis.darptestbed.siminfrastructure.communication.driver.receiver.DriverCentrReceiverVisitor;
import cz.agents.agentpolis.darptestbed.simmodel.agent.driver.logic.DriverCentrLogic;
import cz.agents.agentpolis.simmodel.entity.EntityType;
import cz.agents.agentpolis.simmodel.environment.model.query.AgentPositionQuery;

public class CentrDriverAgent extends DriverAgent<DriverCentrLogic> implements DriverCentrReceiverVisitor {

	private static final Logger LOGGER = Logger.getLogger(CentrDriverAgent.class);

	public CentrDriverAgent(String agentId, EntityType agentType, DriverCentrLogic logic,
			AgentPositionQuery positionQuery) {
		super(agentId, agentType, logic, positionQuery);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void visit(MessageDispatcherSendsOutTaxi messageDispatcherSendsOutTaxi) {
		//LOGGER.debug(getId() + ":" + messageDispatcherSendsOutTaxi.getClass().getSimpleName());
		logic.acceptTripPlan(messageDispatcherSendsOutTaxi.tripPlan);

	}

}

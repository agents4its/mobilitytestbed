package cz.agents.agentpolis.darptestbed.simmodel.agent.driver;

import cz.agents.agentpolis.darptestbed.siminfrastructure.communication.dispatching.message.DispatcherRequestsInsertionMessage;
import cz.agents.agentpolis.darptestbed.siminfrastructure.communication.requestconsumer.message.ProposalAccept;
import cz.agents.agentpolis.darptestbed.siminfrastructure.communication.requestconsumer.message.ProposalReject;
import cz.agents.agentpolis.darptestbed.siminfrastructure.communication.requestconsumer.receiver.RequestConsumerReceiverVisitor;
import cz.agents.agentpolis.darptestbed.simmodel.agent.data.Request;
import cz.agents.agentpolis.darptestbed.simmodel.agent.driver.logic.DriverDecentralizedLogic;
import cz.agents.agentpolis.darptestbed.simmodel.agent.timer.TimerCallback;
import cz.agents.agentpolis.simmodel.entity.EntityType;
import cz.agents.agentpolis.simmodel.environment.model.query.AgentPositionQuery;
import org.apache.log4j.Logger;

public class DriverDecentralizedAgent extends DriverAgent<DriverDecentralizedLogic> implements TimerCallback,
        RequestConsumerReceiverVisitor {

    private static final Logger LOGGER = Logger.getLogger(DriverDecentralizedAgent.class);

    public DriverDecentralizedAgent(String agentId, EntityType agentType, DriverDecentralizedLogic logic,
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

    @Override
    public void visit(DispatcherRequestsInsertionMessage dispatcherRequestsInsertionMessage) {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}

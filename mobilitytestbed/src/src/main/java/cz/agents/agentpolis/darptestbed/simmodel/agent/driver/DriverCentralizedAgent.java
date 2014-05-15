package cz.agents.agentpolis.darptestbed.simmodel.agent.driver;

import cz.agents.agentpolis.darptestbed.siminfrastructure.communication.dispatching.message.DispatcherRequestsInsertionMessage;
import cz.agents.agentpolis.darptestbed.siminfrastructure.communication.dispatching.message.DispatcherSendsOutTaxiMessage;
import cz.agents.agentpolis.darptestbed.siminfrastructure.communication.dispatching.message.FinalPlanConfirmationMessage;
import cz.agents.agentpolis.darptestbed.siminfrastructure.communication.dispatching.message.FinalPlanFailureMessage;
import cz.agents.agentpolis.darptestbed.siminfrastructure.communication.driver.receiver.DriverCentralizedReceiverVisitor;
import cz.agents.agentpolis.darptestbed.simmodel.agent.driver.logic.DriverCentralizedLogic;
import cz.agents.agentpolis.simmodel.entity.EntityType;
import cz.agents.agentpolis.simmodel.environment.model.query.AgentPositionQuery;
import org.apache.log4j.Logger;

public class DriverCentralizedAgent extends DriverAgent<DriverCentralizedLogic> implements DriverCentralizedReceiverVisitor {

    private static final Logger LOGGER = Logger.getLogger(DriverCentralizedAgent.class);

    public DriverCentralizedAgent(String agentId, EntityType agentType, DriverCentralizedLogic logic,
                                  AgentPositionQuery positionQuery) {
        super(agentId, agentType, logic, positionQuery);
        // TODO Auto-generated constructor stub
    }

    @Override
    public void visit(DispatcherSendsOutTaxiMessage dispatcherSendsOutTaxiMessage) {
//        if (getId().equals("DriverId19"))
//        LOGGER.debug("Received: " + dispatcherSendsOutTaxiMessage.hashCode() + " " + "DriverId: " + getId() + " " +
//                dispatcherSendsOutTaxiMessage);
        logic.setTripPlan(dispatcherSendsOutTaxiMessage.tripPlan);
    }

    @Override
    public void visit(DispatcherRequestsInsertionMessage dispatcherRequestsInsertionMessage) {
//        if (logic instanceof DriverCentralizedLogicSVExample)
//            ((DriverCentralizedLogicSVExample)logic).unexpectedPickUp(dispatcherRequestsInsertionMessage.request);
    }

    @Override
    public void visit(FinalPlanConfirmationMessage finalPlanConfirmationMessage) {
        logic.planConfirmed();
    }

    @Override
    public void visit(FinalPlanFailureMessage finalPlanFailureMessage) {
        logic.planFailed();
    }

}

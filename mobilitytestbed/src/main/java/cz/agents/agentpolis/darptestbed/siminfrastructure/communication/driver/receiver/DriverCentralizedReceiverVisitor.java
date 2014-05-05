package cz.agents.agentpolis.darptestbed.siminfrastructure.communication.driver.receiver;

import cz.agents.agentpolis.darptestbed.siminfrastructure.communication.dispatching.message.FinalPlanConfirmationMessage;
import cz.agents.agentpolis.darptestbed.siminfrastructure.communication.dispatching.message.FinalPlanFailureMessage;
import cz.agents.agentpolis.darptestbed.siminfrastructure.communication.dispatching.message.DispatcherRequestsInsertionMessage;
import cz.agents.agentpolis.darptestbed.siminfrastructure.communication.dispatching.message.DispatcherSendsOutTaxiMessage;

public interface DriverCentralizedReceiverVisitor {

    public void visit(DispatcherSendsOutTaxiMessage dispatcherSendsOutTaxiMessage);

    public void visit(DispatcherRequestsInsertionMessage dispatcherRequestsInsertionMessage);

    public void visit(FinalPlanConfirmationMessage finalPlanConfirmationMessage);

    public void visit(FinalPlanFailureMessage finalPlanFailureMessage);

}

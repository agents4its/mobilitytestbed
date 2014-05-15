package cz.agents.agentpolis.darptestbed.siminfrastructure.communication.dispatching.message;

import cz.agents.agentpolis.darptestbed.siminfrastructure.communication.driver.receiver.DriverCentralizedReceiverVisitor;
import cz.agents.agentpolis.darptestbed.simmodel.agent.data.Request;
import cz.agents.agentpolis.ondemandtransport.siminfrastructure.communication.protocol.MessageVisitor;

public class DispatcherRequestsInsertionMessage implements MessageVisitor<DriverCentralizedReceiverVisitor> {

    public final Request request;

    public DispatcherRequestsInsertionMessage(Request request) {
        super();
        this.request = request;
    }

    @Override
    public void accept(DriverCentralizedReceiverVisitor driverCentralizedReceiverVisitor) {
        driverCentralizedReceiverVisitor.visit(this);
    }
}

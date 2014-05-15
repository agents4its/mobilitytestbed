package cz.agents.agentpolis.darptestbed.siminfrastructure.communication.dispatching.message;

import cz.agents.agentpolis.darptestbed.siminfrastructure.communication.driver.receiver.DriverCentralizedReceiverVisitor;
import cz.agents.agentpolis.ondemandtransport.siminfrastructure.communication.protocol.MessageVisitor;

public class FinalPlanConfirmationMessage implements MessageVisitor<DriverCentralizedReceiverVisitor> {
    @Override
    public void accept(DriverCentralizedReceiverVisitor driverCentralizedReceiverVisitor) {
        driverCentralizedReceiverVisitor.visit(this);
    }
}

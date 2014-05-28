package cz.agents.agentpolis.darptestbed.siminfrastructure.communication.driver.message;

import cz.agents.agentpolis.darptestbed.siminfrastructure.communication.dispatching.receiver.DispatchingReceiverVisitor;
import cz.agents.agentpolis.darptestbed.simmodel.agent.data.Request;
import cz.agents.agentpolis.darptestbed.simmodel.agent.data.TripInfo;
import cz.agents.agentpolis.ondemandtransport.siminfrastructure.communication.protocol.MessageVisitor;

public class DriverNewPlanAcceptMessage implements MessageVisitor<DispatchingReceiverVisitor> {

    public final TripInfo tripInfo;

    public DriverNewPlanAcceptMessage(TripInfo tripInfo) {
        super();
        this.tripInfo = tripInfo;
    }

    @Override
    public void accept(DispatchingReceiverVisitor receiverVisitor) {
        receiverVisitor.visit(this);
    }

    public String toString() {
        return String.format("DriverNewPlanAcceptMessage - TripInfo: %s ", tripInfo.toString());
    }
}

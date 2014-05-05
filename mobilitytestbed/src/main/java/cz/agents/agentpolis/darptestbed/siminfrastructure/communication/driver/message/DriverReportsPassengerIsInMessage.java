package cz.agents.agentpolis.darptestbed.siminfrastructure.communication.driver.message;

import cz.agents.agentpolis.darptestbed.siminfrastructure.communication.dispatching.receiver.DispatchingReceiverVisitor;
import cz.agents.agentpolis.darptestbed.simmodel.agent.data.TripInfo;
import cz.agents.agentpolis.ondemandtransport.siminfrastructure.communication.protocol.MessageVisitor;

public class DriverReportsPassengerIsInMessage implements MessageVisitor<DispatchingReceiverVisitor> {

    public final String passengerId;
    public final TripInfo tripInfo;

    public DriverReportsPassengerIsInMessage(String passengerId, TripInfo tripInfo) {
        super();
        this.passengerId = passengerId;
        this.tripInfo = tripInfo;
    }

    @Override
    public void accept(DispatchingReceiverVisitor receiverVisitor) {
        receiverVisitor.visit(this);
    }

    public String toString() {
        return passengerId + " : " + tripInfo;
    }
}

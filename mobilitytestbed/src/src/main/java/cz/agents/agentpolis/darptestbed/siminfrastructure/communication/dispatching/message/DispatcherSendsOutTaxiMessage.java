package cz.agents.agentpolis.darptestbed.siminfrastructure.communication.dispatching.message;

import cz.agents.agentpolis.darptestbed.siminfrastructure.communication.driver.receiver.DriverCentralizedReceiverVisitor;
import cz.agents.agentpolis.darptestbed.simmodel.agent.data.TripPlan;
import cz.agents.agentpolis.ondemandtransport.siminfrastructure.communication.protocol.MessageVisitor;

public class DispatcherSendsOutTaxiMessage implements MessageVisitor<DriverCentralizedReceiverVisitor> {

    public final TripPlan tripPlan;

    public DispatcherSendsOutTaxiMessage(TripPlan tripPlan) {
        super();
        this.tripPlan = new TripPlan(tripPlan);
    }

    @Override
    public void accept(DriverCentralizedReceiverVisitor receiverVisitor) {
        receiverVisitor.visit(this);

    }

    public String toString() {
        return tripPlan.toString();
    }

}

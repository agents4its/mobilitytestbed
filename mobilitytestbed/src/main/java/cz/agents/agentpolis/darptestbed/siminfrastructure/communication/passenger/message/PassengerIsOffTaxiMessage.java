package cz.agents.agentpolis.darptestbed.siminfrastructure.communication.passenger.message;

import cz.agents.agentpolis.darptestbed.siminfrastructure.communication.driver.receiver.DriverReceiverVisitor;
import cz.agents.agentpolis.darptestbed.simmodel.agent.data.TripInfo;
import cz.agents.agentpolis.ondemandtransport.siminfrastructure.communication.protocol.MessageVisitor;

public class PassengerIsOffTaxiMessage implements MessageVisitor<DriverReceiverVisitor> {

    public final String passengerId;
    public final TripInfo tripInfo;

    public PassengerIsOffTaxiMessage(String passengerId, TripInfo tripInfo) {
        super();
        this.passengerId = passengerId;
        this.tripInfo = tripInfo;
    }

    @Override
    public void accept(DriverReceiverVisitor receiverVisitor) {
        receiverVisitor.visit(this);

    }

    public String toString() {
        return PassengerIsOffTaxiMessage.class.getSimpleName().toString() + ": " + passengerId + " " + tripInfo;
    }

}

package cz.agents.agentpolis.darptestbed.siminfrastructure.communication.passenger.message;

import cz.agents.agentpolis.darptestbed.siminfrastructure.communication.passenger.receiver.PassengerReceiverVisitor;
import cz.agents.agentpolis.darptestbed.simmodel.agent.data.TripInfo;
import cz.agents.agentpolis.ondemandtransport.siminfrastructure.communication.protocol.MessageVisitor;

public class OrderConfirmation implements MessageVisitor<PassengerReceiverVisitor> {

	public final TripInfo confirmation;

	public OrderConfirmation(TripInfo confirmation) {
		super();
		this.confirmation = confirmation;
	}

	@Override
	public void accept(PassengerReceiverVisitor receiverVisitor) {
		receiverVisitor.visit(this);
	}

    public String toString() {
        return confirmation.toString();
    }

}

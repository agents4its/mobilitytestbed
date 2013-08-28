package cz.agents.agentpolis.darptestbed.siminfrastructure.communication.passenger.message;

import cz.agents.agentpolis.darptestbed.siminfrastructure.communication.passenger.receiver.PassengerReceiverVisitor;
import cz.agents.agentpolis.darptestbed.simmodel.agent.data.TripInfo;
import cz.agents.agentpolis.ondemandtransport.siminfrastructure.communication.protocol2.MessageVisitor;

public class MessageDriverArrived implements MessageVisitor<PassengerReceiverVisitor> {

	public final String driverId;
	public final TripInfo confirmation;

	public MessageDriverArrived(String driverId, TripInfo confirmation) {
		super();
		this.driverId = driverId;
		this.confirmation = confirmation;
	}

	@Override
	public void accept(PassengerReceiverVisitor receiverVisitor) {
		receiverVisitor.visit(this);
	}

}

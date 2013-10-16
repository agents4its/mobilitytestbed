package cz.agents.agentpolis.darptestbed.siminfrastructure.communication.driver.message;

import cz.agents.agentpolis.darptestbed.siminfrastructure.communication.driver.receiver.DriverReceiverVisitor;
import cz.agents.agentpolis.darptestbed.simmodel.agent.data.TripInfo;
import cz.agents.agentpolis.ondemandtransport.siminfrastructure.communication.protocol.MessageVisitor;

public class PassengerIsInTaxiMessage implements MessageVisitor<DriverReceiverVisitor> {

	public final String passengerId;
	public final TripInfo tripInfo;

	public PassengerIsInTaxiMessage(String passengerId, TripInfo tripInfo) {
		super();
		this.passengerId = passengerId;
		this.tripInfo = tripInfo;
	}

	@Override
	public void accept(DriverReceiverVisitor receiverVisitor) {
		receiverVisitor.visit(this);

	}

}

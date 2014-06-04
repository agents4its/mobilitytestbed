package cz.agents.agentpolis.darptestbed.siminfrastructure.communication.passenger.receiver;

import cz.agents.agentpolis.darptestbed.siminfrastructure.communication.passenger.message.*;
import cz.agents.agentpolis.darptestbed.siminfrastructure.communication.receiver.BaseReceiverVisitor;
import cz.agents.agentpolis.darptestbed.siminfrastructure.communication.receiver.StringMessage;

public interface PassengerReceiverVisitor extends BaseReceiverVisitor {

	public void visit(OrderConfirmation taxiSendConfirmationToPassengerMessage);

	public void visit(Proposal proposal);

	public void visit(RequestReject requestReject);

	public void visit(DriverArrivedMessage driverArrivedMessage);
}

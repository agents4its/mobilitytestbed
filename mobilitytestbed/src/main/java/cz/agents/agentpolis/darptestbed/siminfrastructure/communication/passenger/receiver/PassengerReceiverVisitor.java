package cz.agents.agentpolis.darptestbed.siminfrastructure.communication.passenger.receiver;

import cz.agents.agentpolis.darptestbed.siminfrastructure.communication.passenger.message.MessageDriverArrived;
import cz.agents.agentpolis.darptestbed.siminfrastructure.communication.passenger.message.OrderConfirmation;
import cz.agents.agentpolis.darptestbed.siminfrastructure.communication.passenger.message.Proposal;
import cz.agents.agentpolis.darptestbed.siminfrastructure.communication.passenger.message.RequestReject;

public interface PassengerReceiverVisitor {

	public void visit(OrderConfirmation taxiSendConfirmationToPassengerMessage);

	public void visit(Proposal proposal);

	public void visit(RequestReject requestReject);

	public void visit(MessageDriverArrived messageDriverArrived);

}

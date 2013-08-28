package cz.agents.agentpolis.darptestbed.siminfrastructure.communication.requestconsumer.message;

import cz.agents.agentpolis.darptestbed.siminfrastructure.communication.passenger.message.Proposal;
import cz.agents.agentpolis.darptestbed.siminfrastructure.communication.requestconsumer.receiver.RequestConsumerReceiverVisitor;
import cz.agents.agentpolis.ondemandtransport.siminfrastructure.communication.protocol2.MessageVisitor;

public class ProposalReject implements MessageVisitor<RequestConsumerReceiverVisitor> {

	public final Proposal proposal;

	public ProposalReject(Proposal proposal) {
		super();
		this.proposal = proposal;
	}

	@Override
	public void accept(RequestConsumerReceiverVisitor receiverVisitor) {
		receiverVisitor.visit(this);

	}

}

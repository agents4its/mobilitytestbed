package cz.agents.agentpolis.darptestbed.siminfrastructure.communication.requestconsumer.message;

import cz.agents.agentpolis.darptestbed.siminfrastructure.communication.passenger.message.Proposal;
import cz.agents.agentpolis.darptestbed.siminfrastructure.communication.requestconsumer.receiver.RequestConsumerReceiverVisitor;
import cz.agents.agentpolis.ondemandtransport.siminfrastructure.communication.protocol.MessageVisitor;

public class ProposalAccept implements MessageVisitor<RequestConsumerReceiverVisitor> {

	public final Proposal proposal;

	public ProposalAccept(Proposal proposal) {
		super();
		this.proposal = proposal;
	}

	@Override
	public void accept(RequestConsumerReceiverVisitor receiverVisitor) {
		receiverVisitor.visit(this);

	}

    @Override
    public String toString() {
        return "ProposalAccept{" +
                "proposal=" + proposal +
                '}';
    }
}

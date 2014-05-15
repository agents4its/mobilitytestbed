package cz.agents.agentpolis.darptestbed.siminfrastructure.communication.requestconsumer.receiver;

import cz.agents.agentpolis.darptestbed.siminfrastructure.communication.requestconsumer.message.ProposalAccept;
import cz.agents.agentpolis.darptestbed.siminfrastructure.communication.requestconsumer.message.ProposalReject;
import cz.agents.agentpolis.darptestbed.simmodel.agent.data.Request;

public interface RequestConsumerReceiverVisitor {

    public void visit(Request request);

    public void visit(ProposalReject proposalReject);

    public void visit(ProposalAccept proposalAccept);

}

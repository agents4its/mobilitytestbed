package cz.agents.agentpolis.darptestbed.siminfrastructure.communication.protocol.data;

import cz.agents.agentpolis.darptestbed.siminfrastructure.communication.passenger.message.Proposal;
import cz.agents.agentpolis.ondemandtransport.siminfrastructure.communication.protocol.data.MessengerData;

@Deprecated
public class ProposalMessengerData implements MessengerData {

	public final Proposal proposal;

	public ProposalMessengerData(Proposal proposal) {
		this.proposal = proposal;
	}

}

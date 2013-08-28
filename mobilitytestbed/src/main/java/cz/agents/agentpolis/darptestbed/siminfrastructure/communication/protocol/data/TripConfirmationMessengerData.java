package cz.agents.agentpolis.darptestbed.siminfrastructure.communication.protocol.data;

import cz.agents.agentpolis.darptestbed.simmodel.agent.data.TripInfo;
import cz.agents.agentpolis.ondemandtransport.siminfrastructure.communication.protocol.data.MessengerData;

@Deprecated
public class TripConfirmationMessengerData implements MessengerData {

	public final TripInfo confirmation;

	public TripConfirmationMessengerData(TripInfo confirmation) {
		this.confirmation = confirmation;
	}

}

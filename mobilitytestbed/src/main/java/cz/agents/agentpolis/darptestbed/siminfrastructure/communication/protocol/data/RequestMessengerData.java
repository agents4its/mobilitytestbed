package cz.agents.agentpolis.darptestbed.siminfrastructure.communication.protocol.data;

import cz.agents.agentpolis.darptestbed.simmodel.agent.data.Request;
import cz.agents.agentpolis.ondemandtransport.siminfrastructure.communication.protocol.data.MessengerData;

@Deprecated
public class RequestMessengerData implements MessengerData {

	public final Request request;

	public RequestMessengerData(Request request) {
		this.request = request;
	}

}

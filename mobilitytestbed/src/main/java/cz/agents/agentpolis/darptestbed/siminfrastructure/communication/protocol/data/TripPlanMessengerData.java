package cz.agents.agentpolis.darptestbed.siminfrastructure.communication.protocol.data;

import cz.agents.agentpolis.darptestbed.simmodel.agent.data.TripPlan;
import cz.agents.agentpolis.ondemandtransport.siminfrastructure.communication.protocol.data.MessengerData;

@Deprecated
public class TripPlanMessengerData implements MessengerData {

	public final TripPlan tripPlan;

	public TripPlanMessengerData(TripPlan tripPlan) {
		this.tripPlan = tripPlan;
	}

}

package cz.agents.agentpolis.darptestbed.siminfrastructure.logger.item;

import cz.agents.agentpolis.darptestbed.simmodel.agent.data.TimeWindow;

public class PassengerRequestLogItem {

	public final String passengerId;
	public final TimeWindow timeWindow;
	public final long fromNodeId;
	public final long toNodeId;
	
	public PassengerRequestLogItem(String passengerId, TimeWindow timeWindow, long fromNodeId, long toNodeId) {
		super();
		this.passengerId = passengerId;
		this.timeWindow = timeWindow;
		this.fromNodeId = fromNodeId;
		this.toNodeId = toNodeId;
	}

	@Override
	public String toString() {
		return "PassengerRequestLogItem [passengerId=" + passengerId + ", timeWindow=" + timeWindow + ", fromNodeId="
				+ fromNodeId + ", toNodeId=" + toNodeId + "]";
	}
}

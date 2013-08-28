package cz.agents.agentpolis.darptestbed.siminfrastructure.communication.protocol.data;

import cz.agents.agentpolis.ondemandtransport.siminfrastructure.communication.protocol.data.MessengerData;

@Deprecated
public class DelayMessengerData implements MessengerData {

	/**
	 * Departure/arrival was delayed
	 */
	public final boolean departure;
	/**
	 * The delay in milliseconds (counted according to time window)
	 */
	public final long delay;

	public DelayMessengerData(boolean departure, long delay) {
		this.departure = departure;
		this.delay = delay;
	}

}

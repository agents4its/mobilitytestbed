package cz.agents.agentpolis.darptestbed.siminfrastructure.logger.item;

import cz.agents.agentpolis.siminfrastructure.logger.LogItem;

public class AlgRealTimeLogItem implements LogItem {

	public final long realTime;

	public AlgRealTimeLogItem(long realTime) {
		super();
		this.realTime = realTime;
	}

}

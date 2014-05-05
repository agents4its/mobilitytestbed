package cz.agents.agentpolis.darptestbed.siminfrastructure.logger.item;

import cz.agents.agentpolis.darptestbed.global.Utils;
import cz.agents.agentpolis.darptestbed.simmodel.agent.data.TimeWindow;
import cz.agents.agentpolis.siminfrastructure.logger.LogItem;

public class PassengerRequestLogItem implements LogItem {

    public final String passengerId;
    public final TimeWindow timeWindow;
    public final long fromNodeId;
    public final long toNodeId;
    public final Utils utils;

    public PassengerRequestLogItem(String passengerId, TimeWindow timeWindow, long fromNodeId, long toNodeId, Utils utils) {
        super();
        this.passengerId = passengerId;
        this.timeWindow = timeWindow;
        this.fromNodeId = fromNodeId;
        this.toNodeId = toNodeId;
        this.utils = utils;
    }

    @Override
    public String toString() {
        return "PassengerRequestLogItem [passengerId=" + passengerId + ", timeWindow=" + timeWindow + ", fromNodeId="
                + fromNodeId + ", toNodeId=" + toNodeId + "]";
    }
}

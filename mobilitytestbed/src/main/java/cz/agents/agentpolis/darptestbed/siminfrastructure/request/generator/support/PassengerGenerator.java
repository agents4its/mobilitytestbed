package cz.agents.agentpolis.darptestbed.siminfrastructure.request.generator.support;

import cz.agents.agentpolis.darptestbed.siminfrastructure.request.GPS;
import cz.agents.agentpolis.darptestbed.siminfrastructure.request.TimeWindow;

import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;

public interface PassengerGenerator {

    public RequestTimeInfo generateRequestTimeInfo(GPS fromGPS, GPS toGPS);

    public RequestTimeInfo generateRequestTimeInfo(long fromNode, long toNode);

    public Set<String> generateAdditionalRequirements();

    RequestTimeInfo generateRequestTimeInfo();

    public static class RequestTimeInfo {

        public final long callTimeInDayRange;
        public final TimeWindow fromTimeWindow;
        public final TimeWindow toTimeWindow;

        public RequestTimeInfo(long callTimeInDayRange, TimeWindow fromTimeWindow, TimeWindow toTimeWindow) {
            super();

            checkArgument(fromTimeWindow.fromTimeInDayRange >= callTimeInDayRange,
                    "The 'callTimeInDayRange' is called later then from time");

            checkArgument(fromTimeWindow.fromTimeInDayRange >= callTimeInDayRange,
                    "The 'callTimeInDayRange' is called later then from time");

            this.callTimeInDayRange = callTimeInDayRange;
            this.fromTimeWindow = fromTimeWindow;
            this.toTimeWindow = toTimeWindow;
        }

        @Override
        public String toString() {
            return "RequestTimeInfo [callTimeInDayRange=" + callTimeInDayRange + ", fromTimeWindow=" + fromTimeWindow
                    + ", toTimeWindow=" + toTimeWindow + "]";
        }

    }
}

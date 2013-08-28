package cz.agents.agentpolis.darptestbed.siminfrastructure.request.generator.support;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.Set;

import cz.agents.agentpolis.darptestbed.siminfrastructure.request.TimeWindow;

public interface PasssengerGenerator {

	public RequestTimeInfo generateRequestTimeInfo();

	public Set<String> generateAdditionalRequirements();

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

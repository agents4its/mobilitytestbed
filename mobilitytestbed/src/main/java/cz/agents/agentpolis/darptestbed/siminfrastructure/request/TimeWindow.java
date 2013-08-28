package cz.agents.agentpolis.darptestbed.siminfrastructure.request;

import static com.google.common.base.Preconditions.checkArgument;

public class TimeWindow {

	public final long fromTimeInDayRange;
	public final long toTimeInDayRange;

	private TimeWindow() {
		super();
		this.fromTimeInDayRange = -1;
		this.toTimeInDayRange = -1;
	}

	public TimeWindow(long fromTimeInDayRange, long toTimeInDayRange) {
		super();

		checkArgument(fromTimeInDayRange >= 0 && fromTimeInDayRange < 86400000,
				"The 'fromTimeInDayRange' is out of the day range");

		checkArgument(toTimeInDayRange >= 0 && toTimeInDayRange < 86400000,
				"The 'toTimeInDayRange' is out of the day range");

		checkArgument(fromTimeInDayRange <= toTimeInDayRange, "The 'toTimeInDayRange' has to be greater or equal");

		this.fromTimeInDayRange = fromTimeInDayRange;
		this.toTimeInDayRange = toTimeInDayRange;
	}

	@Override
	public String toString() {
		return "TimeWindow [fromTimeInDayRange=" + fromTimeInDayRange + ", toTimeInDayRange=" + toTimeInDayRange + "]";
	}

}

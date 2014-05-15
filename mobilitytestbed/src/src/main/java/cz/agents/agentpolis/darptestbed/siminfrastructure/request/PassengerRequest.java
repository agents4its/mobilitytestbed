package cz.agents.agentpolis.darptestbed.siminfrastructure.request;

import static com.google.common.base.Preconditions.checkArgument;

public class PassengerRequest {

	public final GPS originPosition;
	public final GPS destinationnPosition;
	public final long reqestCallTimeInDayRange;
	public final TimeWindow fromTimeWindow;
	public final TimeWindow toTimeWindow;

	private PassengerRequest() {
		this.originPosition = null;
		this.destinationnPosition = null;
		this.reqestCallTimeInDayRange = -1;
		this.fromTimeWindow = null;
		this.toTimeWindow = null;
	}

	public PassengerRequest(GPS originPosition, GPS destinationnPosition, long reqestCallTimeInDayRange,
			TimeWindow fromTimeWindow, TimeWindow toTimeWindow) {
		super();
		checkArgument(reqestCallTimeInDayRange <= fromTimeWindow.fromTimeInDayRange);
		this.originPosition = originPosition;
		this.destinationnPosition = destinationnPosition;
		this.reqestCallTimeInDayRange = reqestCallTimeInDayRange;
		this.fromTimeWindow = fromTimeWindow;
		this.toTimeWindow = toTimeWindow;
	}
}

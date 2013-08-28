package cz.agents.agentpolis.darptestbed.simmodel.agent.data.generator.support;

import java.util.Set;

import org.joda.time.Duration;

import cz.agents.agentpolis.darptestbed.siminfrastructure.request.PassengerRequest;
import cz.agents.agentpolis.darptestbed.simmodel.agent.data.TimeWindow;
import cz.agents.agentpolis.siminfrastructure.time.TimeProvider;

public class RequestBuilder {

	private final static long HOUR_24 = Duration.standardDays(1).getMillis();

	private final long originMappedToNodeId;
	private final long destinationMappedToNodeId;
	private final PassengerRequest request;
	private final long callTimeInDayRange;
	private final TimeProvider timeProvider;

	public RequestBuilder(long originMappedToNodeId, long destinationMappedToNodeId, PassengerRequest request,
			long callTimeInDayRange, TimeProvider timeProvider) {
		super();
		this.originMappedToNodeId = originMappedToNodeId;
		this.destinationMappedToNodeId = destinationMappedToNodeId;
		this.request = request;
		this.callTimeInDayRange = callTimeInDayRange;
		this.timeProvider = timeProvider;
	}

	public cz.agents.agentpolis.darptestbed.simmodel.agent.data.Request buildRequest(String passengerId,
			Set<String> additionalRequirements) {

		long addSimulationTimeAlignedDayBeginning = computeSimulationTime();

		long departFrom = request.fromTimeWindow.fromTimeInDayRange + addSimulationTimeAlignedDayBeginning;
		long departTo = request.fromTimeWindow.toTimeInDayRange + addSimulationTimeAlignedDayBeginning;
		long arriveFrom = request.toTimeWindow.fromTimeInDayRange + addSimulationTimeAlignedDayBeginning;
		long arriveTo = request.toTimeWindow.toTimeInDayRange + addSimulationTimeAlignedDayBeginning;

		TimeWindow timeWindow = new TimeWindow(departFrom, departTo, arriveFrom, arriveTo);

		return new cz.agents.agentpolis.darptestbed.simmodel.agent.data.Request(passengerId, callTimeInDayRange,
				originMappedToNodeId, destinationMappedToNodeId, timeWindow, additionalRequirements);

	}

	private long computeSimulationTime() {
		long nextDayFlag = 0;
		if (timeProvider.getSimTimeInDayRange() > getMinDepartureTimeInDayRange()) {
			nextDayFlag++;
		}

		return (timeProvider.getCurrentDayFlag() + nextDayFlag) * HOUR_24;
	}

	private long getMinDepartureTimeInDayRange() {
		return request.fromTimeWindow.fromTimeInDayRange;
	}
}

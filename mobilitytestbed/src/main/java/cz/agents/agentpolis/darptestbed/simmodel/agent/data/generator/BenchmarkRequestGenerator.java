package cz.agents.agentpolis.darptestbed.simmodel.agent.data.generator;

import java.util.LinkedList;
import java.util.Set;

import cz.agents.agentpolis.darptestbed.simmodel.agent.data.Request;
import cz.agents.agentpolis.darptestbed.simmodel.agent.data.generator.support.RequestBuilder;
import cz.agents.agentpolis.siminfrastructure.time.TimeProvider;
import cz.agents.agentpolis.simmodel.agent.activity.TimeSpendingActivity;
import cz.agents.agentpolis.simmodel.agent.activity.callback.TimeActivityCallback;
import cz.agents.agentpolis.simmodel.environment.model.action.AgentPositionAction;

public class BenchmarkRequestGenerator implements RequestGenerator {

	private final AgentPositionAction agentPositionAction;
	private final LinkedList<RequestBuilder> requests;

	private final TimeProvider timeProvider;
	private final TimeSpendingActivity timeSpendingActivity;

	public BenchmarkRequestGenerator(AgentPositionAction agentPositionAction, LinkedList<RequestBuilder> requests,
			TimeProvider timeProvider, TimeSpendingActivity timeSpendingActivity) {
		super();
		this.agentPositionAction = agentPositionAction;
		this.requests = requests;
		this.timeProvider = timeProvider;
		this.timeSpendingActivity = timeSpendingActivity;
	}

	@Override
	public void start(final String agentId, Set<String> additionalRequirements,
			final RequestGeneratorCall requestGeneratorCall) {

		RequestBuilder requestBuilder = requests.poll();
		final Request request = requestBuilder.buildRequest(agentId, additionalRequirements);

		timeSpendingActivity.spendingTime(new TimeActivityCallback() {

			@Override
			public void timeCallback() {

				agentPositionAction.actSetPosition(agentId, request.getFromNode());
				requestGeneratorCall.sendRequest(request);

			}
		}, computeWaitingTime(request.getCallTimeInDayRange()));

	}

	@Override
	public void timerSignal() {
		// TODO Auto-generated method stub

	}

	private long computeWaitingTime(long requestCallTimeInDayRange) {
		long waitingTime = timeProvider.computeDepartureTime(timeProvider.getCurrentDayFlag(),
				requestCallTimeInDayRange);

		if (waitingTime > 0) {
			return waitingTime;

		}

		return 1;

	}

}

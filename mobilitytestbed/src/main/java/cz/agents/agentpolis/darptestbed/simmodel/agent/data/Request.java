package cz.agents.agentpolis.darptestbed.simmodel.agent.data;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Set;
import cz.agents.agentpolis.darptestbed.siminfrastructure.communication.requestconsumer.receiver.RequestConsumerReceiverVisitor;
import cz.agents.agentpolis.ondemandtransport.siminfrastructure.communication.protocol.MessageVisitor;

/**
 * A request for a taxi to drive from point A to B.
 * 
 * @author Lukas Canda
 */
public final class Request implements MessageVisitor<RequestConsumerReceiverVisitor> {

	private final String passengerId;
	private final long callTimeInDayRange;
	private final long fromNode;
	private final long toNode;
	private final TimeWindow timeWindow;
	private final Set<String> additionalRequirements;

	// this class can be extended by adding max price, max group size etc.

	public Request(String passengerId, long fromNode, long toNode, Set<String> additionalRequirements) {
		this(passengerId, -1, fromNode, toNode, null, additionalRequirements);
	}

	public Request(String passengerId, long fromNode, long toNode, TimeWindow timeWindow,
			Set<String> additionalRequirements) {
		super();
		this.passengerId = passengerId;
		this.callTimeInDayRange = -1;
		this.fromNode = fromNode;
		this.toNode = toNode;
		this.timeWindow = timeWindow;
		this.additionalRequirements = additionalRequirements;
	}

	// including time window
	public Request(String passengerId, long callTimeInDayRange, long fromNode, long toNode, TimeWindow timeWindow,
			Set<String> additionalRequirements) {
		super();
		this.passengerId = checkNotNull(passengerId);
		this.callTimeInDayRange = callTimeInDayRange;
		this.fromNode = fromNode;
		this.toNode = toNode;
		this.timeWindow = timeWindow;
		this.additionalRequirements = checkNotNull(additionalRequirements);
	}

	public String getPassengerId() {
		return passengerId;
	}

	public long getFromNode() {
		return fromNode;
	}

	public long getToNode() {
		return toNode;
	}

	public TimeWindow getTimeWindow() {
		return timeWindow;
	}

	public long getCallTimeInDayRange() {
		return callTimeInDayRange;
	}

	public Set<String> getAdditionalRequirements() {
		return additionalRequirements;
	}

	@Override
	public void accept(RequestConsumerReceiverVisitor receiverVisitor) {
		receiverVisitor.visit(this);

	}
}

package cz.agents.agentpolis.darptestbed.siminfrastructure.communication.passenger.message;

import cz.agents.agentpolis.darptestbed.siminfrastructure.communication.passenger.receiver.PassengerReceiverVisitor;
import cz.agents.agentpolis.darptestbed.simmodel.agent.data.Request;
import cz.agents.agentpolis.ondemandtransport.siminfrastructure.communication.protocol.MessageVisitor;

/**
 * Request rejection - holding original request and identifier of the agent who rejected it
 */
public class RequestReject implements MessageVisitor<PassengerReceiverVisitor> {

	public Request request;
	public String rejectReceivedFrom;
	
	public RequestReject(Request originalRequest, String rejectFrom) {
		super();
		this.request = originalRequest;
		this.rejectReceivedFrom = rejectFrom;
	}
	
	@Override
	public void accept(PassengerReceiverVisitor receiverVisitor) {
		receiverVisitor.visit(this);
	}

}

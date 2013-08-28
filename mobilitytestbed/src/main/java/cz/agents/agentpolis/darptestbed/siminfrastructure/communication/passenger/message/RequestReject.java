package cz.agents.agentpolis.darptestbed.siminfrastructure.communication.passenger.message;

import cz.agents.agentpolis.darptestbed.siminfrastructure.communication.passenger.receiver.PassengerReceiverVisitor;
import cz.agents.agentpolis.darptestbed.simmodel.agent.data.Request;
import cz.agents.agentpolis.darptestbed.simmodel.agent.data.TripPlan;
import cz.agents.agentpolis.ondemandtransport.siminfrastructure.communication.protocol2.MessageVisitor;

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

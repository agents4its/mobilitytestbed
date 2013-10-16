package cz.agents.agentpolis.darptestbed.siminfrastructure.communication.driver.message;

import cz.agents.agentpolis.darptestbed.siminfrastructure.communication.driver.receiver.DriverCentrReceiverVisitor;
import cz.agents.agentpolis.darptestbed.simmodel.agent.data.TripPlan;
import cz.agents.agentpolis.ondemandtransport.siminfrastructure.communication.protocol.MessageVisitor;

public class MessageDispatcherSendsOutTaxi implements MessageVisitor<DriverCentrReceiverVisitor> {

	public final TripPlan tripPlan;

	public MessageDispatcherSendsOutTaxi(TripPlan tripPlan) {
		super();
		this.tripPlan = tripPlan;
	}

	@Override
	public void accept(DriverCentrReceiverVisitor receiverVisitor) {
		receiverVisitor.visit(this);

	}

}

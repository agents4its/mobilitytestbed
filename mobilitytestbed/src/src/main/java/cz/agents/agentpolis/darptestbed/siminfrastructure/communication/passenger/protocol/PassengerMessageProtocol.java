package cz.agents.agentpolis.darptestbed.siminfrastructure.communication.passenger.protocol;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import cz.agents.agentpolis.darptestbed.siminfrastructure.communication.passenger.receiver.PassengerReceiverVisitor;
import cz.agents.agentpolis.ondemandtransport.siminfrastructure.communication.protocol.AMessageProtocol;
import cz.agents.alite.common.event.EventProcessor;

@Singleton
public class PassengerMessageProtocol extends AMessageProtocol<PassengerReceiverVisitor> {

	@Inject
	public PassengerMessageProtocol(EventProcessor eventProcessor) {
		super(eventProcessor);
		// TODO Auto-generated constructor stub
	}

}

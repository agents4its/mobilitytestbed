package cz.agents.agentpolis.darptestbed.siminfrastructure.communication.requestconsumer.protocol;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import cz.agents.agentpolis.darptestbed.siminfrastructure.communication.requestconsumer.receiver.RequestConsumerReceiverVisitor;
import cz.agents.agentpolis.ondemandtransport.siminfrastructure.communication.protocol.AMessageProtocol;
import cz.agents.alite.common.event.EventProcessor;

@Singleton
public class RequestConsumerMessageProtocol extends AMessageProtocol<RequestConsumerReceiverVisitor> {

	@Inject
	public RequestConsumerMessageProtocol(EventProcessor eventProcessor) {
		super(eventProcessor);
		// TODO Auto-generated constructor stub
	}

}

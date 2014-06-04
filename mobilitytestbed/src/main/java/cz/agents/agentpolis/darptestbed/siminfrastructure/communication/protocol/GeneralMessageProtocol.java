package cz.agents.agentpolis.darptestbed.siminfrastructure.communication.protocol;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import cz.agents.agentpolis.darptestbed.siminfrastructure.communication.passenger.receiver.PassengerReceiverVisitor;
import cz.agents.agentpolis.darptestbed.siminfrastructure.communication.receiver.BaseReceiverVisitor;
import cz.agents.agentpolis.ondemandtransport.siminfrastructure.communication.protocol.AMessageProtocol;
import cz.agents.alite.common.event.EventProcessor;

@Singleton
public class GeneralMessageProtocol extends AMessageProtocol<BaseReceiverVisitor> {

	@Inject
	public GeneralMessageProtocol(EventProcessor eventProcessor) {
		super(eventProcessor);
		// TODO Auto-generated constructor stub
	}

}

package cz.agents.agentpolis.darptestbed.siminfrastructure.communication.driver.protocol;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import cz.agents.agentpolis.darptestbed.siminfrastructure.communication.driver.receiver.DriverCentrReceiverVisitor;
import cz.agents.agentpolis.ondemandtransport.siminfrastructure.communication.protocol.AMessageProtocol;
import cz.agents.alite.common.event.EventProcessor;

@Singleton
public class DriverCentrMessageProtocol extends AMessageProtocol<DriverCentrReceiverVisitor> {

	@Inject
	public DriverCentrMessageProtocol(EventProcessor eventProcessor) {
		super(eventProcessor);
		// TODO Auto-generated constructor stub
	}

}

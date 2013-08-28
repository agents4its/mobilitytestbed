package cz.agents.agentpolis.darptestbed.siminfrastructure.communication.driver.protocol;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import cz.agents.agentpolis.darptestbed.siminfrastructure.communication.driver.receiver.DriverReceiverVisitor;
import cz.agents.agentpolis.ondemandtransport.siminfrastructure.communication.protocol2.AMessageProtocol;
import cz.agents.alite.common.event.EventProcessor;

@Singleton
public class DriverMessageProtocol extends AMessageProtocol<DriverReceiverVisitor> {

	@Inject
	public DriverMessageProtocol(EventProcessor eventProcessor) {
		super(eventProcessor);
		// TODO Auto-generated constructor stub
	}

}

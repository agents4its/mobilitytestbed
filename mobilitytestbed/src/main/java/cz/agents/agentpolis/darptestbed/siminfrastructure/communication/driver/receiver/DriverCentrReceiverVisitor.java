package cz.agents.agentpolis.darptestbed.siminfrastructure.communication.driver.receiver;

import cz.agents.agentpolis.darptestbed.siminfrastructure.communication.driver.message.MessageDispatcherSendsOutTaxi;

public interface DriverCentrReceiverVisitor {

	public void visit(MessageDispatcherSendsOutTaxi messageDispatcherSendsOutTaxi);

}

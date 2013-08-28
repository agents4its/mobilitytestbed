package cz.agents.agentpolis.darptestbed.siminfrastructure.communication.driver.receiver;

import cz.agents.agentpolis.darptestbed.siminfrastructure.communication.driver.message.PassengerIsInTaxiMessage;
import cz.agents.agentpolis.darptestbed.siminfrastructure.communication.driver.message.PassengerSaysTaxiIsTooLateMessage;

public interface DriverReceiverVisitor {

	public void visit(PassengerIsInTaxiMessage passengerIsInTaxiMessage);

	public void visit(PassengerSaysTaxiIsTooLateMessage passengerSaysTaxiIsTooLate);

}

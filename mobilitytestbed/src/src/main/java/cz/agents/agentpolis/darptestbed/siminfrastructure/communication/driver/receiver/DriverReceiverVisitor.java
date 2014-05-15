package cz.agents.agentpolis.darptestbed.siminfrastructure.communication.driver.receiver;

import cz.agents.agentpolis.darptestbed.siminfrastructure.communication.dispatching.message.DispatcherRequestsInsertionMessage;
import cz.agents.agentpolis.darptestbed.siminfrastructure.communication.passenger.message.PassengerIsInTaxiMessage;
import cz.agents.agentpolis.darptestbed.siminfrastructure.communication.passenger.message.PassengerIsOffTaxiMessage;
import cz.agents.agentpolis.darptestbed.siminfrastructure.communication.passenger.message.PassengerSaysTaxiIsTooLateForPickupMessage;

public interface DriverReceiverVisitor {

    public void visit(PassengerIsInTaxiMessage passengerIsInTaxiMessage);

    public void visit(PassengerIsOffTaxiMessage passengerIsInTaxiMessage);

    public void visit(PassengerSaysTaxiIsTooLateForPickupMessage passengerSaysTaxiIsTooLate);

    public void visit(DispatcherRequestsInsertionMessage dispatcherRequestsInsertionMessage);

}

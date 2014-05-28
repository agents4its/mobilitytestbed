package cz.agents.agentpolis.darptestbed.siminfrastructure.communication.driver.receiver;

import cz.agents.agentpolis.darptestbed.siminfrastructure.communication.dispatching.message.DispatcherRequestsInsertionMessage;
import cz.agents.agentpolis.darptestbed.siminfrastructure.communication.passenger.message.PassengerIsInTaxiMessage;
import cz.agents.agentpolis.darptestbed.siminfrastructure.communication.passenger.message.PassengerIsOffTaxiMessage;
import cz.agents.agentpolis.darptestbed.siminfrastructure.communication.passenger.message.PassengerSaysTaxiIsTooLateForPickupMessage;
import cz.agents.agentpolis.darptestbed.siminfrastructure.communication.receiver.BaseReceiverVisitor;
import cz.agents.agentpolis.darptestbed.siminfrastructure.communication.receiver.StringMessage;

public interface DriverReceiverVisitor extends BaseReceiverVisitor {

    public void visit(PassengerIsInTaxiMessage passengerIsInTaxiMessage);

    public void visit(PassengerIsOffTaxiMessage passengerIsInTaxiMessage);

    public void visit(PassengerSaysTaxiIsTooLateForPickupMessage passengerSaysTaxiIsTooLate);

    public void visit(DispatcherRequestsInsertionMessage dispatcherRequestsInsertionMessage);

    public void visit(StringMessage stringMessage);
}

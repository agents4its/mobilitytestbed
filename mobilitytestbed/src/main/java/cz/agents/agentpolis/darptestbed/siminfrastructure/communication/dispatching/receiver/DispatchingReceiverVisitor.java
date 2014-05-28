package cz.agents.agentpolis.darptestbed.siminfrastructure.communication.dispatching.receiver;

import cz.agents.agentpolis.darptestbed.siminfrastructure.communication.driver.message.*;
import cz.agents.agentpolis.darptestbed.siminfrastructure.communication.receiver.BaseReceiverVisitor;

public interface DispatchingReceiverVisitor extends BaseReceiverVisitor {
    public void visit(DriverReportsPassengerIsInMessage passengerIsInTaxiMessage);

    public void visit(DriverReportsPassengerHasLeftMessage passengerHasLeftTaxiMessage);

    public void visit(DriverReportsLateForPassengerMessage driverReportsLateForPassengerMessage);

    public void visit(DriverNewPlanAcceptMessage driverNewPlanAcceptMessage);

    public void visit(DriverNewPlanRejectMessage driverNewPlanRejectMessage);
}

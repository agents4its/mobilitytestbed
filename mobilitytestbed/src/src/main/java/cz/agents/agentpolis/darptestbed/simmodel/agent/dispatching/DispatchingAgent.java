package cz.agents.agentpolis.darptestbed.simmodel.agent.dispatching;

import cz.agents.agentpolis.darptestbed.siminfrastructure.communication.dispatching.protocol.DispatchingMessageProtocolCombinedVisitor;
import cz.agents.agentpolis.darptestbed.siminfrastructure.communication.driver.message.*;
import cz.agents.agentpolis.darptestbed.siminfrastructure.communication.requestconsumer.message.ProposalAccept;
import cz.agents.agentpolis.darptestbed.siminfrastructure.communication.requestconsumer.message.ProposalReject;
import cz.agents.agentpolis.darptestbed.simmodel.agent.data.Request;
import cz.agents.agentpolis.darptestbed.simmodel.agent.dispatching.logic.DispatchingLogic;
import cz.agents.agentpolis.darptestbed.simmodel.agent.timer.TimerCallback;
import cz.agents.agentpolis.siminfrastructure.description.DescriptionImpl;
import cz.agents.agentpolis.simmodel.agent.Agent;
import cz.agents.agentpolis.simmodel.entity.EntityType;
import org.apache.log4j.Logger;

/**
 * The life cycle of the dispatching (there's usually only one instance in the
 * whole application).
 *
 * @author Lukas Canda
 */
public class DispatchingAgent extends Agent implements TimerCallback, DispatchingMessageProtocolCombinedVisitor {

    private static final Logger LOGGER = Logger.getLogger(DispatchingAgent.class);

    /**
     * My logic processes all operations with received requests including
     * planning. It also sends messages to whoever needs them.
     */
    private DispatchingLogic logic;

    public DispatchingAgent(String agentId, EntityType agentType, DispatchingLogic logic) {

        super(agentId, agentType);
        this.logic = logic;
    }

    @Override
    public void born() {
    }

    /**
     * This method is usually called by a timer at regular intervals.
     */
    @Override
    public void timerCallback() {
        logic.processRequests();
    }

    @Override
    public DescriptionImpl getDescription() {
        return new DescriptionImpl();
    }

    @Override
    public void visit(Request request) {
        //LOGGER.debug(getId() + ":" + request.getClass().getSimpleName());
        logic.processNewRequest(request);

    }

    @Override
    public void visit(ProposalReject proposalReject) {
        //LOGGER.debug(getId() + ":" + proposalReject.getClass().getSimpleName());
        logic.processRejectedProposal(proposalReject);
    }

    @Override
    public void visit(ProposalAccept proposalAccept) {
        //LOGGER.debug(getId() + ":" + proposalAccept.getClass().getSimpleName());
        logic.confirmOrder(proposalAccept);
    }

    @Override
    public void visit(DriverReportsPassengerIsInMessage passengerIsInTaxiMessage) {
        LOGGER.debug("---> Dispatching: Passenger entered " + passengerIsInTaxiMessage);
        logic.processPassengerInVehicle(passengerIsInTaxiMessage);
    }

    @Override
    public void visit(DriverReportsPassengerHasLeftMessage passengerHasLeftTaxiMessage) {
        LOGGER.debug("---> Dispatching: Passenger left " + passengerHasLeftTaxiMessage);
        logic.processPassengerOffVehicle(passengerHasLeftTaxiMessage);
    }

    @Override
    public void visit(DriverReportsLateForPassengerMessage driverReportsLateForPassengerMessage) {
        logic.processDriverArrivedLateForPassengerPickup(driverReportsLateForPassengerMessage);
    }

    @Override
    public void visit(DriverNewPlanAcceptMessage driverNewPlanAcceptMessage) {
        logic.processDriverAcceptsNewPlan(driverNewPlanAcceptMessage);
    }

    @Override
    public void visit(DriverNewPlanRejectMessage driverNewPlanRejectMessage) {
        logic.processDriverRejectsNewPlan(driverNewPlanRejectMessage);
    }
}

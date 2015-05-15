package cz.agents.agentpolis.darptestbed.simmodel.agent.passenger.logic;

import cz.agents.agentpolis.darptestbed.global.Utils;
import cz.agents.agentpolis.darptestbed.siminfrastructure.communication.driver.protocol.DriverMessageProtocol;
import cz.agents.agentpolis.darptestbed.siminfrastructure.communication.passenger.message.Proposal;
import cz.agents.agentpolis.darptestbed.siminfrastructure.communication.passenger.message.RequestReject;
import cz.agents.agentpolis.darptestbed.siminfrastructure.communication.protocol.GeneralMessageProtocol;
import cz.agents.agentpolis.darptestbed.siminfrastructure.communication.requestconsumer.message.ProposalAccept;
import cz.agents.agentpolis.darptestbed.siminfrastructure.communication.requestconsumer.protocol.RequestConsumerMessageProtocol;
import cz.agents.agentpolis.darptestbed.siminfrastructure.logger.RequestLogger;
import cz.agents.agentpolis.darptestbed.simmodel.agent.activity.movement.TestbedPassengerActivity;
import cz.agents.agentpolis.darptestbed.simmodel.agent.data.Request;
import cz.agents.agentpolis.darptestbed.simmodel.agent.passenger.PassengerProfile;
import cz.agents.agentpolis.darptestbed.simmodel.environment.model.TestbedModel;
import cz.agents.agentpolis.simmodel.agent.activity.TimeSpendingActivity;
import cz.agents.agentpolis.simmodel.environment.model.query.AgentPositionQuery;

/**
 * The basic features of a PassengerAgent, especially his communication protocol
 * that enables him to contact other agents.
 * <p>
 * The centralized communication means that the passenger communicates only with
 * the dispatching (control center).
 * </p>
 * <p>
 * The passenger using this logic accepts all the proposals received from dispatching. Request are usually initiated by
 * {@link cz.agents.agentpolis.darptestbed.simmodel.agent.data.generator.RequestGenerator} and just called through the passenger.
 * </p>
 *
 * @author Lukas Canda
 */
public class PassengerCentralizedLogic extends PassengerLogicWithRequestConsumerMessageProtocol {

    public PassengerCentralizedLogic(String agentId, RequestConsumerMessageProtocol sender,
                                     DriverMessageProtocol driverMessageProtocol,
                                     GeneralMessageProtocol generalMessageProtocol, TestbedModel taxiModel,
                                     AgentPositionQuery positionQuery, Utils utils, PassengerProfile passengerProfile,
                                     TestbedPassengerActivity passengerActivity,
                                     TimeSpendingActivity timeSpendingActivity, RequestLogger logger) {

        super(agentId, sender, driverMessageProtocol, generalMessageProtocol, taxiModel, positionQuery, utils,
                passengerProfile, passengerActivity, timeSpendingActivity, logger);

    }

    @Override
    public final boolean isDecentralized() {
        return false;
    }

    /**
     * Sends a request to the dispatching.
     * <p/>
     * This method is usually called only by a request generator through the
     * passenger.
     *
     * @param request request to be send
     */
    public void sendRequest(Request request) {
        super.sendRequest(request);
        this.sender.sendMessage(taxiModel.getDispatching().getId(), request);
    }


    @Override
    public void processProposal(Proposal proposal) {
        this.passengerAdditionalRequirements = proposal.getRequest().getAdditionalRequirements();
        this.currentDriverId = proposal.getDriverId();
        this.currentVehicleId = proposal.getVehicleId();
        this.currentRequestConfirmed = proposal.getRequest();
        sender.sendMessage(taxiModel.getDispatching().getId(), new ProposalAccept(proposal));
    }

    @Override
    public void processRejection(RequestReject rejection) {
        // log the request failure for statistical purposes
        logger.logRequestRejected(getAgentId());
        this.stopWaiting(rejection.rejectReceivedFrom);
    }

}

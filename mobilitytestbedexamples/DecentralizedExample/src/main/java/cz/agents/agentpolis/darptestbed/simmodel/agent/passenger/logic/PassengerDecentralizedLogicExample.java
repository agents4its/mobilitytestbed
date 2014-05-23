package cz.agents.agentpolis.darptestbed.simmodel.agent.passenger.logic;

import cz.agents.agentpolis.darptestbed.global.Utils;
import cz.agents.agentpolis.darptestbed.siminfrastructure.communication.driver.protocol.DriverMessageProtocol;
import cz.agents.agentpolis.darptestbed.siminfrastructure.communication.passenger.message.Proposal;
import cz.agents.agentpolis.darptestbed.siminfrastructure.communication.passenger.message.RequestReject;
import cz.agents.agentpolis.darptestbed.siminfrastructure.communication.requestconsumer.message.ProposalAccept;
import cz.agents.agentpolis.darptestbed.siminfrastructure.communication.requestconsumer.protocol.RequestConsumerMessageProtocol;
import cz.agents.agentpolis.darptestbed.siminfrastructure.logger.RequestLogger;
import cz.agents.agentpolis.darptestbed.simmodel.agent.activity.movement.TestbedPassengerActivity;
import cz.agents.agentpolis.darptestbed.simmodel.agent.data.Request;
import cz.agents.agentpolis.darptestbed.simmodel.agent.passenger.PassengerProfile;
import cz.agents.agentpolis.darptestbed.simmodel.environment.model.TestbedModel;
import cz.agents.agentpolis.simmodel.agent.activity.TimeSpendingActivity;
import cz.agents.agentpolis.simmodel.environment.model.query.AgentPositionQuery;

import java.util.ArrayList;

/**
 * Example of decentralized control mechanism. Related classes:
 * DriverDecentralizedLogicExample
 */
public class PassengerDecentralizedLogicExample extends PassengerDecentralizedLogic {

    public PassengerDecentralizedLogicExample(String agentId, RequestConsumerMessageProtocol sender,
                                              DriverMessageProtocol driverMessageProtocol, TestbedModel taxiModel, AgentPositionQuery positionQuery,
                                              Utils utils, PassengerProfile passengerProfile, TestbedPassengerActivity passengerActivity,
                                              TimeSpendingActivity timeSpendingActivity, RequestLogger logger) {
        super(agentId, sender, driverMessageProtocol, taxiModel, positionQuery, utils, passengerProfile,
                passengerActivity, timeSpendingActivity, logger);
    }

    // list in which this passenger remembers which drivers rejected him in the
    // past
    private ArrayList<String> driversThatRejectedMe = new ArrayList<String>();


    /**
     * Sends a request to closest free driver, that has never rejected us or
     * give up after too many rejections.
     *
     * @param request request to be sent
     */
    public void sendRequest(Request request) {

        // if too many drivers rejected us, don't even try
        if (driversThatRejectedMe.size() >= 3) {
            LOGGER.debug(request.getPassengerId() + " giving up after 3 rejections.");
            logger.logRequestRejected(passengerId);
            return;
        }

        // find the closest free taxi driver (while skipping those that rejected
        // us in the past)
        String closestDriver = null;
        Double closestDist = 0.0;
        LOGGER.debug("Count: " + taxiModel.getTaxiDriversFree());
        for (String driverId : taxiModel.getTaxiDriversFree()) {
            LOGGER.debug("Free: " + driverId);
            double thisDist = utils.computeDistance(this.getCurrentPositionNode(),
                    positionQuery.getCurrentPositionByNodeId(driverId));
            if ((!driversThatRejectedMe.contains(driverId)) && (closestDriver == null || thisDist < closestDist)) {
                closestDist = thisDist;
                closestDriver = driverId;
            }
        }

        // send a request
        if (closestDriver != null) {
            super.sendRequest(request);
            sender.sendMessage(closestDriver, request);
        } else {
            LOGGER.debug(request.getPassengerId() + " giving up failing to find possible driver.");
            logger.logRequestRejected(passengerId);
        }

    }

    // accept any received proposal
    @Override
    public void processProposal(Proposal proposal) {
        // send the "I accept" message
        sender.sendMessage(proposal.getDriverId(), new ProposalAccept(proposal));
        // and start waiting for a given driver/vehicle
        this.startWaiting(proposal.getDriverId(), proposal.getVehicleId(), proposal.getRequest()
                .getAdditionalRequirements());
    }

    // if our request is rejected, send it to someone else
    @Override
    public void processRejection(RequestReject rejection) {
        // remember that this driver rejected us
        if (!driversThatRejectedMe.contains(rejection.rejectReceivedFrom))
            driversThatRejectedMe.add(rejection.rejectReceivedFrom);
        // and send the request again (now to someone else)
        this.stopWaiting(rejection.rejectReceivedFrom);
        this.sendRequest(rejection.request);
    }

    // not used right now
    @Override
    public void processProposals() {
    }

}

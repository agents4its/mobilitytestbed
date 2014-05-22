package cz.agents.agentpolis.darptestbed.simmodel.agent.driver.logic;

import com.google.common.collect.Sets;
import cz.agents.agentpolis.darptestbed.global.Utils;
import cz.agents.agentpolis.darptestbed.siminfrastructure.communication.dispatching.protocol.DispatchingMessageProtocol;
import cz.agents.agentpolis.darptestbed.siminfrastructure.communication.driver.message.*;
import cz.agents.agentpolis.darptestbed.siminfrastructure.communication.passenger.message.DriverArrivedMessage;
import cz.agents.agentpolis.darptestbed.siminfrastructure.communication.passenger.protocol.PassengerMessageProtocol;
import cz.agents.agentpolis.darptestbed.simmodel.agent.data.TripInfo;
import cz.agents.agentpolis.darptestbed.simmodel.agent.data.TripPlan;
import cz.agents.agentpolis.darptestbed.simmodel.agent.data.generator.PassengersInAndOutPair;
import cz.agents.agentpolis.darptestbed.simmodel.entity.vehicle.TestbedVehicle;
import cz.agents.agentpolis.darptestbed.simmodel.environment.model.TestbedModel;
import cz.agents.agentpolis.simmodel.agent.activity.movement.DriveVehicleActivity;
import cz.agents.agentpolis.simmodel.environment.model.citymodel.transportnetwork.AllNetworkNodes;
import cz.agents.agentpolis.simmodel.environment.model.query.AgentPositionQuery;
import org.apache.log4j.Logger;

/**
 * The basic features of a DriverAgent, especially his communication protocol
 * that enables him to contact other agents.
 * <p/>
 * The centralized communication means that the taxi driver communicates only
 * with the dispatching (control center).
 *
 * @author Lukas Canda
 */
public class DriverCentralizedLogic extends DriverLogicWithPassengerMessageProtocol {

    private static final Logger LOGGER = Logger.getLogger(DriverCentralizedLogic.class);

    private final DispatchingMessageProtocol dispatchingMessageProtocol;
    private TripPlan candidateTripPlan;
    private TripPlan oldPlan;

    private enum DriverState {
        WAITING_FOR_PASSENGERS,
        ACCEPTING_NEW_PLANS,
        WAITING_FOR_PLAN_CONFIRMATION
    }

    private DriverState driverState = DriverState.ACCEPTING_NEW_PLANS;

    public DriverCentralizedLogic(String agentId, PassengerMessageProtocol sender, TestbedModel taxiModel,
                                  AgentPositionQuery positionQuery, AllNetworkNodes allNetworkNodes, Utils utils, TestbedVehicle vehicle,
                                  DriveVehicleActivity drivingActivity,
                                  DispatchingMessageProtocol dispatchingMessageProtocol) {

        super(agentId, sender, taxiModel, positionQuery, allNetworkNodes, utils, vehicle, drivingActivity);
        this.dispatchingMessageProtocol = dispatchingMessageProtocol;
    }

//    /**
//     * Accepts the message with the trip plan, which the driver should follow after he finishes the current one.
//     *
//     * @param tripPlan a list of nodes to visit, a map of passengers to get in
//     */
//    public void acceptTripPlan(TripPlan tripPlan) {
//        if (this.getTripPlan() != null) {
//            this.extendTripPlan(tripPlan);
//        } else {
//            this.setTripPlan(tripPlan);
//        }
//
//        this.driveNextPartOfTripPlan();
//    }

    /**
     * Accepts the message with the trip plan, which the driver should follow.
     *
     * @param tripPlan a list of nodes to visit, a map of passengers to get in
     */
    public void setTripPlan(TripPlan tripPlan) {
        if (isTripPlanAcceptable(tripPlan)) {
            LOGGER.info("Accepted plan - " + getDriverId() + "\nNEW: " + tripPlan + "\nOLD: " + this.getTripPlan());
            candidateTripPlan = tripPlan;
            oldPlan = this.getTripPlan();
            dispatchingMessageProtocol.driverDispatchingProtocol.sendMessage(taxiModel.getDispatching().getId(),
                new DriverNewPlanAcceptMessage(new TripInfo(getDriverId(), getVehicle().getId())));
            driverState = DriverState.WAITING_FOR_PLAN_CONFIRMATION;
        } else {
            LOGGER.info("Rejected plan - " + getDriverId() + ": " + tripPlan);
            dispatchingMessageProtocol.driverDispatchingProtocol.sendMessage(taxiModel.getDispatching().getId(),
                    new DriverNewPlanRejectMessage(new TripInfo(getDriverId(), getVehicle().getId())));
        }
    }

    private boolean isTripPlanAcceptable(TripPlan tripPlan) {
        return driverState == DriverState.ACCEPTING_NEW_PLANS &&
                Sets.symmetricDifference(getPassengersOnBoard(), tripPlan.getPassengersOnBoard()).isEmpty();
    }

    protected void driveNextPartOfTripPlan() {

        switch (driverState) {
            case ACCEPTING_NEW_PLANS:

                if (getTripPlan() == null)
                    return;


                long driverPosition = positionQuery.getCurrentPositionByNodeId(getDriverId());
                PassengersInAndOutPair nodeWithBoardingAndDisembarkingPassengers =
                        getTripPlan().getNodeWithBoardingAndDisembarkingPassengers(driverPosition);
                boolean hasList = nodeWithBoardingAndDisembarkingPassengers == null;
                boolean oldIsEverybodyOnBoard = hasList ||
                nodeWithBoardingAndDisembarkingPassengers.getIn() == null ||
                    nodeWithBoardingAndDisembarkingPassengers.getIn().isEmpty();
                boolean oldIsEverybodyOffBoard = hasList ||
                        nodeWithBoardingAndDisembarkingPassengers.getOff() == null ||
                        nodeWithBoardingAndDisembarkingPassengers.getOff().isEmpty();

                super.driveNextPartOfTripPlan();

                if (!oldIsEverybodyOnBoard || !oldIsEverybodyOffBoard) {
                    driverState = DriverState.WAITING_FOR_PASSENGERS;
                }

                break;
            case WAITING_FOR_PASSENGERS:

                oldIsEverybodyOnBoard = isEverybodyOnBoard();
                oldIsEverybodyOffBoard = isEverybodyOffBoard();

                super.driveNextPartOfTripPlan();

                if (oldIsEverybodyOnBoard && oldIsEverybodyOffBoard) {
                    driverState = DriverState.ACCEPTING_NEW_PLANS;
                }

                break;
            case WAITING_FOR_PLAN_CONFIRMATION:
                break;
        }
    }

    @Override
    protected void sendTaxiArrivedLateMessageToDispatching(String passengerId) {
        dispatchingMessageProtocol.driverDispatchingProtocol.sendMessage(taxiModel.getDispatching().getId(),
                new DriverReportsLateForPassengerMessage(passengerId,
                        new TripInfo(getDriverId(), getVehicle().getId())));
    }

    @Override
    protected void sendTaxiArrivedToPickup(String passengerId) {
        LOGGER.debug("Sending pickup: " + passengerId);
        sender.sendMessage(passengerId,
                new DriverArrivedMessage(getDriverId(), new TripInfo(getDriverId(), this.getVehicle().getId())));
        dispatchingMessageProtocol.driverDispatchingProtocol.sendMessage(taxiModel.getDispatching().getId(),
                new DriverReportsPassengerIsInMessage(passengerId,
                new TripInfo(this.getDriverId(), this.getVehicle().getId())));
    }

    @Override
    protected void sendTaxiArrivedToDropOff(String passengerId) {
        sender.sendMessage(passengerId,
                new DriverArrivedMessage(getDriverId(), new TripInfo(getDriverId(), this.getVehicle().getId())));
        dispatchingMessageProtocol.driverDispatchingProtocol.sendMessage(taxiModel.getDispatching().getId(),
                new DriverReportsPassengerHasLeftMessage(passengerId, new TripInfo(this.getDriverId(),
                        this.getVehicle().getId())));
    }

    @Override
    public final boolean isDecentralized() {
        return false;
    }

    public void planConfirmed() {
        if (driverState == DriverState.WAITING_FOR_PLAN_CONFIRMATION) {
            driverState =  DriverState.ACCEPTING_NEW_PLANS;
            super.setTripPlan(candidateTripPlan);
            candidateTripPlan = null;
            oldPlan = null;

            driveNextPartOfTripPlan();
        }
    }

    public void planFailed() {
        if (driverState == DriverState.WAITING_FOR_PLAN_CONFIRMATION) {
            driverState =  DriverState.ACCEPTING_NEW_PLANS;
            super.setTripPlan(oldPlan);
            candidateTripPlan = null;
            oldPlan = null;

            driveNextPartOfTripPlan();
        }
    }
}

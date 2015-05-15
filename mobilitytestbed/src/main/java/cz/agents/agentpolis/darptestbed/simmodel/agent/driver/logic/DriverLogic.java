package cz.agents.agentpolis.darptestbed.simmodel.agent.driver.logic;

import cz.agents.agentpolis.darptestbed.global.Utils;
import cz.agents.agentpolis.darptestbed.siminfrastructure.communication.protocol.GeneralMessageProtocol;
import cz.agents.agentpolis.darptestbed.siminfrastructure.communication.receiver.BaseReceiverVisitor;
import cz.agents.agentpolis.darptestbed.simmodel.agent.AgentLogic;
import cz.agents.agentpolis.darptestbed.simmodel.agent.data.FlexiblePlan;
import cz.agents.agentpolis.darptestbed.simmodel.agent.data.TripPlan;
import cz.agents.agentpolis.darptestbed.simmodel.agent.data.generator.PassengersInAndOutPair;
import cz.agents.agentpolis.darptestbed.simmodel.entity.vehicle.TestbedVehicle;
import cz.agents.agentpolis.darptestbed.simmodel.environment.model.TestbedModel;
import cz.agents.agentpolis.ondemandtransport.siminfrastructure.communication.protocol.AMessageProtocol;
import cz.agents.agentpolis.siminfrastructure.planner.trip.Trip;
import cz.agents.agentpolis.simmodel.agent.activity.movement.DriveVehicleActivity;
import cz.agents.agentpolis.simmodel.agent.activity.movement.callback.DrivingFinishedActivityCallback;
import cz.agents.agentpolis.simmodel.environment.model.citymodel.transportnetwork.AllNetworkNodes;
import cz.agents.agentpolis.simmodel.environment.model.query.AgentPositionQuery;
import org.apache.log4j.Logger;

import java.util.HashSet;
import java.util.Set;

/**
 * This class contains all methods, that are common for both centralized and
 * decentralized version. Mainly, this applies for communication concerning
 * notifying passenger of my arrival and also driving.
 *
 * @author Lukas Canda
 */
public abstract class DriverLogic<TMessageProtocol extends AMessageProtocol<? extends BaseReceiverVisitor>> extends
        AgentLogic<TMessageProtocol>
        implements DrivingFinishedActivityCallback {

    private static final Logger LOGGER = Logger.getLogger(DriverLogic.class);

    /**
     * The taxi I'm driving
     */
    private final TestbedVehicle vehicle;
    /**
     * Using this activity I can drive a vehicle
     */
    private final DriveVehicleActivity drivingActivity;
    /**
     * My trips to be gone on
     */
    private TripPlan tripPlan = null;
    // TODO: get rid of special values!!!!! Getters should never return special, obscure values!!!!
    /**
     * The number of boarding passengers we're waiting for (if there's nobody to
     * get in, lets set it to -1)
     */
    private int numOfPassenToGetIn = -1;
    /**
     * The number of boarding passengers we're waiting for to get off (if there's nobody to
     * get off, lets set it to -1)
     */
    private int numOfPassenToGetOff = -1;
    /**
     * The next trip to be driven (while we're waiting for boarding passengers)
     */
    private Trip tripToDrive = null;
    /**
     * Algorithms, that allow diversion, can use this property to re-plan the
     * plan of a taxi on the way
     */
    protected FlexiblePlan flexiblePlan = null;

    private final Set<String> informedBoardingPassengersAtCurrentNode = new HashSet<>();
    private final Set<String> informedDisembarkingPassengersAtCurrentNode = new HashSet<>();

    private final Set<String> passengersOnBoard = new HashSet<>();

    private long lastLoadingNode = -1;

    public DriverLogic(String agentId, TMessageProtocol sender, GeneralMessageProtocol generalMessageProtocol,
                       TestbedModel serviceModel,
                       AgentPositionQuery positionQuery, AllNetworkNodes allNetworkNodes, Utils utils, TestbedVehicle vehicle,
                       DriveVehicleActivity drivingActivity) {

        super(agentId, sender, generalMessageProtocol, serviceModel, positionQuery, utils);
        this.vehicle = vehicle;
        this.drivingActivity = drivingActivity;
    }

    /**
     * @return the driver's current position node
     */
    protected Long getCurrentPositionNode() {
        return this.positionQuery.getCurrentPositionByNodeId(this.getAgentId());
    }

    /**
     * @return the current number of passengers this driver has on board
     */
    protected int getCurrentPassengersOnBoard() {
        return taxiModel.getNumOfPassenOnBoard(this.getVehicle().getId());
    }

    /**
     * @return the driver's vehicle instance
     */
    protected TestbedVehicle getVehicle() {
        return this.vehicle;
    }

    /**
     * @return the driver's trip plan
     */
    protected TripPlan getTripPlan() {
        return this.tripPlan;
    }

    /**
     * Set the trip plan of this driver
     *
     * @param newTripPlan TripPlan object that the driver should take
     */
    protected void setTripPlan(TripPlan newTripPlan) {
        this.tripPlan = newTripPlan;


        if (lastLoadingNode != -1) {

            if (lastLoadingNode == getCurrentPositionNode()) {

                Long currentPos = null;

                if (!informedBoardingPassengersAtCurrentNode.isEmpty()) {
                    currentPos = getCurrentPositionNode();
                    for (String passengerId : informedBoardingPassengersAtCurrentNode) {
                        tripPlan.removePassengerFromBoardingPassengersAtNode(passengerId, currentPos);
//                        LOGGER.debug("Removed from boarding: " + passengerId);
                    }
                }

                if (!informedDisembarkingPassengersAtCurrentNode.isEmpty()) {
                    if (currentPos == null)
                        currentPos = getCurrentPositionNode();

                    for (String passengerId : informedBoardingPassengersAtCurrentNode) {
                        tripPlan.removePassengerFromDisembarkingPassengersAtNode(passengerId, currentPos);
//                        LOGGER.debug("Removed from disembarking: " + passengerId);
                    }
                }
            } else {
                informedBoardingPassengersAtCurrentNode.clear();
                informedDisembarkingPassengersAtCurrentNode.clear();
//                LOGGER.debug("Cleared informed - " + getVehicle().getId() + ": " +
//                        informedDisembarkingPassengersAtCurrentNode);
            }
        }

        LOGGER.debug("Assigned to " + getAgentId() + " " + tripPlan);
    }

    /**
     * Extend current trip plan of this driver by a new one
     *
     * @param newTripPlan TripPlan object that the driver should take
     */
    @Deprecated //seems to be invalid - recursive trap?
    protected void extendTripPlan(TripPlan newTripPlan) {
        if (this.tripPlan != null) {
            this.extendTripPlan(newTripPlan);
        } else {
            this.setTripPlan(newTripPlan);
        }
    }

    /**
     * @return the driver's vehicle drivingActivity
     */
    protected DriveVehicleActivity getDrivingActivity() {
        return this.drivingActivity;
    }

    /**
     * After all passengers from the current node get in, the taxi can drive
     * another part of the trip.
     *
     * @param passengerId id of the passenger that's just gotten in
     */
    public void processPassengerGotInVehicle(String passengerId) {
        numOfPassenToGetIn--;
        if (numOfPassenToGetIn == 0 && numOfPassenToGetOff == 0) {
            driveNextPartOfTripPlan();
        }
    }

    /**
     * After all passengers from the current node get off, the taxi can drive
     * another part of the trip.
     *
     * @param passengerId id of the passenger that's just gotten off
     */
    public void processPassengerGotOffVehicle(String passengerId) {
        numOfPassenToGetOff--;
        if (numOfPassenToGetOff == 0 && numOfPassenToGetIn == 0) {
            driveNextPartOfTripPlan();
        }
    }

    /**
     * The passenger notifies the taxi driver of delay
     *
     * @param passengerId message sender
     * @param departure   true, if the delay was during departure, false if during
     *                    arrival
     * @param delay       the delay in milliseconds
     */
    public void processVehicleIsTooLate(String passengerId, boolean departure, long delay) {
        // lets just skip the passenger

        numOfPassenToGetIn--;
        tripPlan.removeLatePickupPassengerFromTripPlan(passengerId, getVehicle().getId(), utils);
        sendTaxiArrivedLateMessageToDispatching(passengerId);
        if (numOfPassenToGetIn == 0) {
            driveNextPartOfTripPlan();
        }
    }

    protected abstract void sendTaxiArrivedLateMessageToDispatching(String passengerId);

    /**
     * This callback method is called after finishing driving a part of the trip
     */
    @Override
    public void finishedDriving() {
        driveNextPartOfTripPlan();
    }

    /**
     * After finishing a part of the trip, the driver gets on a node, where he
     * can pick up passengers according to their boarding positions.
     * <p/>
     * After all the passengers finish boarding, he will continue driving.
     */
    protected void driveNextPartOfTripPlan() {
        if (lastLoadingNode != -1) {
            if (lastLoadingNode != getCurrentPositionNode()) {

                informedBoardingPassengersAtCurrentNode.clear();
                informedDisembarkingPassengersAtCurrentNode.clear();
                LOGGER.debug("Cleared informed - " + getVehicle().getId() + ": " +
                        informedDisembarkingPassengersAtCurrentNode);
            }
        }

//        LOGGER.debug(getVehicle().getId() + ": " + "Location - " +  getCurrentPositionNode() + " Drive TripPlan: " + tripPlan);

        Long currentPos = this.getCurrentPositionNode();
        PassengersInAndOutPair passengersToGetInAndOut =
                tripPlan.getNodeWithBoardingAndDisembarkingPassengers(currentPos);
        // if there are passengers waiting to get in on this node
        if (passengersToGetInAndOut != null) {
            lastLoadingNode = currentPos;
            boolean waitOnBoarding = false;
            if (numOfPassenToGetIn == -1 || !isEverybodyOnBoard()) {
                processBoardingPassengers(currentPos, passengersToGetInAndOut);

                if (!isEverybodyOnBoard())
                    waitOnBoarding = true;
            }

            if (numOfPassenToGetOff == -1 || !isEverybodyOffBoard()) {
                if (processDisembarkingPassengers(currentPos, passengersToGetInAndOut))
                    return;
            }

            if (waitOnBoarding)
                return;

//                LOGGER.debug("Leaving with boarding: " + informedBoardingPassengersAtCurrentNode);
//                LOGGER.debug("Leaving with disembarking: " + informedDisembarkingPassengersAtCurrentNode);
//
//                if (tripPlan == null || !tripPlan.getTrips().hasTrip())
//                    tripFinished();
        }
        this.numOfPassenToGetIn = -1;
        this.numOfPassenToGetOff = -1;

//        LOGGER.debug(getVehicle().getId() + ": " + "Late = Location - " +  getCurrentPositionNode() + " Drive " +
//                "TripPlan: " + tripPlan);

        // skip null trips (e.g. if there are two passengers at the same place)
        do {
            tripToDrive = tripPlan.getTrips().getAndRemoveFirstTrip();
        } while (tripToDrive == null && tripPlan.getTrips().hasTrip());


        if (tripToDrive == null) {
            tripFinished();
        } else {
            taxiModel.setTaxiBusy(vehicle.getId());
            // only some algorithms use these properties (here, we need to
            // refresh them)
            if (this.flexiblePlan != null) {
                taxiModel.setEndOfTripPosition(vehicle.getId(), this.flexiblePlan.getLastNode());
                this.flexiblePlan.removeNodesBefore(tripToDrive.showLastTripItem().tripPositionByNodeId);
            }
            LOGGER.debug("Drive: " + vehicle.getId() + " " + tripToDrive);
            drivingActivity.drive(getAgentId(), vehicle, tripToDrive, this);
        }
    }

    private boolean processDisembarkingPassengers(Long currentPos, PassengersInAndOutPair passengersToGetInAndOut) {
        Set<String> passengersToGetOut = passengersToGetInAndOut.getOff();
        this.numOfPassenToGetOff = passengersToGetOut.size();
        LOGGER.debug("Processing off - driver: " + getVehicle().getId() + " " + tripPlan + " " +
                this.numOfPassenToGetOff);

        if (this.numOfPassenToGetOff > 0) {

            for (String passengerId : new HashSet<>(passengersToGetOut)) {
                if (!passengersOnBoard.contains(passengerId)) {
                    informedDisembarkingPassengersAtCurrentNode.add(passengerId);
                    --numOfPassenToGetOff;
                    continue;
                }

                LOGGER.debug("Driver: DISEMBARK of " + passengerId + ", used " + getVehicle().getId() +
                        ", driven by " + getAgentId() + " at " + currentPos + ". " +
                        passengersToGetOut);
                tripPlan.removePassengerFromDisembarkingPassengersAtNode(passengerId, currentPos);
                sendTaxiArrivedToDropOff(passengerId);

                informedDisembarkingPassengersAtCurrentNode.add(passengerId);
                passengersOnBoard.remove(passengerId);
            }

            return (this.numOfPassenToGetOff > 0);

        } else {
            return false;
        }
    }

    private void processBoardingPassengers(Long currentPos, PassengersInAndOutPair passengersToGetInAndOut) {
        Set<String> passengersToGetIn = passengersToGetInAndOut.getIn();
        this.numOfPassenToGetIn = passengersToGetIn.size();
        LOGGER.debug("Processing in - driver: " + getVehicle().getId() + " " + this.numOfPassenToGetIn);
        for (String passengerId : new HashSet<>(passengersToGetIn)) {
            LOGGER.debug("Driver: PICKUP of " + passengerId + ", used " + getVehicle().getId() +
                    ", driven by " + getAgentId() + " at " + currentPos + " - " +
                passengersToGetIn.toString());

            tripPlan.removePassengerFromBoardingPassengersAtNode(passengerId, currentPos);
            sendTaxiArrivedToPickup(passengerId);
//
//                        LOGGER.debug("After PICKUP: " + passengersToGetIn.toString() + " " + tripPlan + " " +
//                                this.numOfPassenToGetIn);

            informedBoardingPassengersAtCurrentNode.add(passengerId);
            passengersOnBoard.add(passengerId);
        }
    }

    protected Set<? extends String> getPassengersOnBoard() {
        return passengersOnBoard;
    }

    /**
     * Notify a passenger of arriving this taxi (so he can get in)
     *
     * @param passengerId the passenger to be notified
     */
    protected abstract void sendTaxiArrivedToPickup(String passengerId);

    /**
     * Notify a passenger of arriving this taxi (so he can get off)
     *
     * @param passengerId the passenger to be notified
     */
    protected abstract void sendTaxiArrivedToDropOff(String passengerId);

    // TaxiArrivedToPassengerMessage
    // sender.sendTaxiArrivedToPassenger(agentId, vehicle.getId(), passengerId);
    // }

    /**
     * This method should be called after finishing the whole trip and having
     * nothing else to do
     */
    private void tripFinished() {
        tripPlan = null;
        flexiblePlan = null;
        taxiModel.setTaxiFree(vehicle.getId());
        LOGGER.debug(this.agentId + ": I've finished my trip at " + utils.toHoursAndMinutes(utils.getCurrentTime()));
    }

    /**
     * @return true, if the driver currently has a non-empty trip plan
     */
    protected boolean isOnTheWay() {
        if (this.tripPlan == null || this.tripPlan.getTrips().numTrips() == 0) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * @return true, if the driver is currently busy for some reason (on a trip or communicating with passenger)
     */
    protected boolean isBusy() {
        if (!taxiModel.getTaxiDriversFree().contains(this.getAgentId())) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Moves this taxi from list of free taxis into the list
     * "busy". And also does the same with its driver.
     */
    protected void setBusy() {
        taxiModel.setTaxiBusy(this.getVehicle().getId());
    }

    /**
     * @return true, if the driver is currently NOT busy for any reason (not on a trip or communicating with passenger)
     */
    protected boolean isFree() {
        return !isBusy();
    }

    /**
     * Moves this taxi from list of busy taxis into the list
     * "free". And also does the same with its driver.
     */
    protected void setFree() {
        taxiModel.setTaxiFree(this.getVehicle().getId());
    }

    /**
     * @return true, if everybody who was supposed to, has gotten in
     */
    protected boolean isEverybodyOnBoard() {
        return this.numOfPassenToGetIn == 0 ? true : false;
    }

    /**
     * @return true, if everybody who was supposed to, has gotten in
     */
    protected boolean isEverybodyOffBoard() {
        return this.numOfPassenToGetOff == 0 ? true : false;
    }

    public String toString() {
        return getAgentId();
    }

}

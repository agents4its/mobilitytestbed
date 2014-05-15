package cz.agents.agentpolis.darptestbed.simmodel.agent.data;

import com.google.common.collect.Sets;
import cz.agents.agentpolis.darptestbed.global.Utils;
import cz.agents.agentpolis.darptestbed.simmodel.agent.data.generator.PassengersInAndOutPair;
import cz.agents.agentpolis.darptestbed.simmodel.agent.driver.logic.DriverLogic;
import cz.agents.agentpolis.siminfrastructure.planner.trip.Trip;
import cz.agents.agentpolis.siminfrastructure.planner.trip.Trips;
import org.apache.log4j.Logger;

import java.util.*;
import java.util.Map.Entry;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A trip plan to be driven by a taxi driver. It consists of the list of nodes
 * to visit, along with the lists of passengers that get in on those nodes.
 *
 * @author Lukas Canda
 */
public class TripPlan {

    private static final Logger LOGGER = Logger.getLogger(TripPlan.class);

    /**
     * A list of trips to serve a few passengers. Each trip ends at a
     * pickup/drop location.
     */
    protected Trips trips;
    /**
     * The map that lists passengers that get in on some nodes of the trips
     * (couples <node number, ids of passengers>)
     */
    protected final Map<Long, PassengersInAndOutPair> mapOfBoardingAndDisembarkingPassengers;
    /**
     * The plan, that can be re-planned by a planner (optional)
     */
    protected FlexiblePlan flexiblePlan = null;

    public TripPlan(Trips trips, Map<Long, PassengersInAndOutPair> mapOfBoardingAndDisembarkingPassengers) {

        this(trips, mapOfBoardingAndDisembarkingPassengers, null);
    }

    public TripPlan(Trips trips, Map<Long, PassengersInAndOutPair> mapOfBoardingAndDisembarkingPassengers, FlexiblePlan planForPlanner) {

        this.trips = trips;
        this.mapOfBoardingAndDisembarkingPassengers = mapOfBoardingAndDisembarkingPassengers;
        this.flexiblePlan = planForPlanner;
    }

    public TripPlan() {
        this.trips = new Trips();
        this.mapOfBoardingAndDisembarkingPassengers = new HashMap<>();
    }

    public TripPlan(TripPlan toCopy) {

        if (toCopy.mapOfBoardingAndDisembarkingPassengers != null) {
            mapOfBoardingAndDisembarkingPassengers = new HashMap(toCopy.mapOfBoardingAndDisembarkingPassengers);
            for (Entry<Long, PassengersInAndOutPair> passengersEntry : mapOfBoardingAndDisembarkingPassengers.entrySet()) {
                PassengersInAndOutPair pair = new PassengersInAndOutPair(passengersEntry.getValue());
                passengersEntry.setValue(pair);
            }
        } else {
            mapOfBoardingAndDisembarkingPassengers = null;
        }

        if (toCopy.trips != null) {
            trips = new Trips();
            for (Trip<?> tTrip : toCopy.trips) {
                if (tTrip != null) {
                    trips.addTrip(tTrip.clone());
                } else {
//                    trips.addTrip(null);
                }
            }
        }

        if (toCopy.flexiblePlan != null) {
            try {
                flexiblePlan = (FlexiblePlan) toCopy.flexiblePlan.clone();
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
    }

    public Trips getTrips() {
        return trips;
    }

    public void extend(TripPlan tripPlan) {
        tripPlan = checkNotNull(tripPlan);

        Trips clone = tripPlan.getTrips().clone();
        while (clone.hasTrip()) {
            trips.addEndCurrentTrips(clone.getAndRemoveFirstTrip());
        }

        for (Entry<Long, PassengersInAndOutPair> tripPlanEntry :
                tripPlan.getMapOfBoardingAndDisembarkingPassengers().entrySet()) {
            PassengersInAndOutPair set = mapOfBoardingAndDisembarkingPassengers.get(tripPlanEntry.getKey());
            if (set == null) {
                set = new PassengersInAndOutPair();
            }
            PassengersInAndOutPair setNew = tripPlan.getMapOfBoardingAndDisembarkingPassengers().get(tripPlanEntry.getKey());
            if (setNew == null) {
                setNew = new PassengersInAndOutPair();
            }
            set.addAll(setNew);

            mapOfBoardingAndDisembarkingPassengers.put(tripPlanEntry.getKey(), set);
        }

        if (this.flexiblePlan != null && tripPlan.flexiblePlan != null) {
            this.flexiblePlan.currentTime = tripPlan.flexiblePlan.currentTime;
            this.flexiblePlan.lastNode = tripPlan.flexiblePlan.lastNode;
            this.flexiblePlan.planItems.addAll(tripPlan.flexiblePlan.planItems);

            if (this.flexiblePlan.vehicle.getId() != tripPlan.flexiblePlan.vehicle.getId()) {
                throw new RuntimeException("Vehicle ids of flexible plans have to the same");
            }

        }

        if (this.flexiblePlan == null) {
            this.flexiblePlan = tripPlan.flexiblePlan;
        }

    }

    public void setTrips(Trips trips) {
        this.trips = trips;
    }

    protected Map<Long, PassengersInAndOutPair> getMapOfBoardingAndDisembarkingPassengers() {
        return mapOfBoardingAndDisembarkingPassengers;
    }

    public FlexiblePlan getFlexiblePlan(Utils utils) {
        return flexiblePlan;
    }

    @Override
    public String toString() {
        return TripPlan.class.getSimpleName() + " Passengers: " +
                mapOfBoardingAndDisembarkingPassengers + " " + this.getTrips().numTrips() + " Trips: " + trips;
    }

    public boolean removeRequestFromBoardingAndDisembarkingPassengers(Request request) {


        boolean success = true;
        try {
            PassengersInAndOutPair passengersAtNode =
                    getMapOfBoardingAndDisembarkingPassengers().get(request.getFromNode());

            if (passengersAtNode != null) {
                passengersAtNode.getIn().remove(request.getPassengerId());
                if (passengersAtNode.isEmpty()) {
                    getMapOfBoardingAndDisembarkingPassengers().remove(request.getFromNode());
                }
            } else {
                success = false;
            }

            passengersAtNode = getMapOfBoardingAndDisembarkingPassengers().get(request.getToNode());

            if (passengersAtNode == null) {
//                LOGGER.debug("NULL REMOVE: " + request + " " + this);
            }

            passengersAtNode.getOff().remove(request.getPassengerId());
            if (passengersAtNode.isEmpty()) {
                getMapOfBoardingAndDisembarkingPassengers().remove(request.getToNode());
            }
        } catch (NullPointerException e) {
//            LOGGER.debug("NULL POINTER");
        }

        return success;
    }

    public boolean removePassengerFromBoardingPassengersAtNode(String passengerId, long node) {
        PassengersInAndOutPair passengersAtNode =
                getMapOfBoardingAndDisembarkingPassengers().get(node);

        if (passengersAtNode != null) {
            passengersAtNode.getIn().remove(passengerId);
            if (passengersAtNode.isEmpty())
                getMapOfBoardingAndDisembarkingPassengers().remove(node);

            return true;
        } else {
            return false;
        }
    }

    public boolean removePassengerFromDisembarkingPassengersAtNode(String passengerId, long node) {
        PassengersInAndOutPair passengersAtNode =
                getMapOfBoardingAndDisembarkingPassengers().get(node);

        if (passengersAtNode != null) {
            passengersAtNode.getOff().remove(passengerId);
            if (passengersAtNode.isEmpty())
                getMapOfBoardingAndDisembarkingPassengers().remove(node);

            return true;
        } else {
            return false;
        }
    }

    public boolean removePassengerFromBoardingAndDisembarkingPassengersAtNode(String passengerId, long node) {
        return removePassengerFromBoardingPassengersAtNode(passengerId, node) &&
            removePassengerFromDisembarkingPassengersAtNode(passengerId, node);
    }

    public void addRequestToBoardingAndDisembarkingPassengers(Request request) {
        PassengersInAndOutPair passengersAtNode =
                getMapOfBoardingAndDisembarkingPassengers().get(request.getFromNode());
        if (passengersAtNode == null) {
            passengersAtNode = new PassengersInAndOutPair();
            getMapOfBoardingAndDisembarkingPassengers().put(request.getFromNode(), passengersAtNode);
        }
        passengersAtNode.getIn().add(request.getPassengerId());


        passengersAtNode =
                getMapOfBoardingAndDisembarkingPassengers().get(request.getToNode());
        if (passengersAtNode == null) {
            passengersAtNode = new PassengersInAndOutPair();
            getMapOfBoardingAndDisembarkingPassengers().put(request.getToNode(), passengersAtNode);
        }
        passengersAtNode.getOff().add(request.getPassengerId());
    }

    public PassengersInAndOutPair getNodeWithBoardingAndDisembarkingPassengers(long node) {
        return getMapOfBoardingAndDisembarkingPassengers().get(node);
    }

    public void clear() {
        trips = new Trips();
        flexiblePlan = null;
        getMapOfBoardingAndDisembarkingPassengers().clear();
    }

    public Set<String> getPassengersOnBoard() {
        Set<String> inPassengersRegistry = new HashSet<>();
        Set<String> allPassengersRegistry = new HashSet<>();
        for (PassengersInAndOutPair onOffPair : getMapOfBoardingAndDisembarkingPassengers().values()) {
            allPassengersRegistry = Sets.union(allPassengersRegistry, onOffPair.getIn());
            allPassengersRegistry = Sets.union(allPassengersRegistry, onOffPair.getOff());
            inPassengersRegistry = Sets.union(inPassengersRegistry, onOffPair.getIn());
        }

        return Sets.difference(allPassengersRegistry, inPassengersRegistry).immutableCopy();
    }

    public void removeLatePickupPassengerFromTripPlan(String passengerId, String vehicleId, Utils utils) {
        Trips trips = getTrips().clone();
        Trips constructed = new Trips();

        while (trips.hasTrip()) {

            Trip current = null;
            while (trips.hasTrip() && (current = trips.getAndRemoveFirstTrip()) == null) ;
            long lastNode = current.showLastTripItem().tripPositionByNodeId;

            PassengersInAndOutPair off = getNodeWithBoardingAndDisembarkingPassengers(lastNode);

            if (off != null && off.getOff().contains(passengerId)) {
                removePassengerFromDisembarkingPassengersAtNode(passengerId, lastNode);
                long start = current.showCurrentTripItem().tripPositionByNodeId;

                if (trips.hasTrip()) {
                    while (trips.hasTrip() && (current = trips.getAndRemoveFirstTrip()) == null);
                } else {
                    continue;
                }

                if (current != null) {
                    long end = current.showLastTripItem().tripPositionByNodeId;
                    current = utils.planTrip(vehicleId, start, end);
                } else {
                    continue;
                }
            }

            constructed.addTrip(current);
        }

        setTrips(constructed);
    }
}

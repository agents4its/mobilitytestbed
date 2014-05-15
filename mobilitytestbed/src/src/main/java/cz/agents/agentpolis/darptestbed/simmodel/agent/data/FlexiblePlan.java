package cz.agents.agentpolis.darptestbed.simmodel.agent.data;

import cz.agents.agentpolis.darptestbed.global.Utils;
import cz.agents.agentpolis.darptestbed.simmodel.entity.vehicle.TestbedVehicle;

import java.util.ArrayList;
import java.util.List;

/**
 * A list of start and target request nodes in order. This plan is easy to work
 * with during planning process.
 * <p/>
 * TODO the behavior when more passengers somehow share the same node (some get
 * in, some get off) hasn't been implemented yet
 *
 * @author Lukas Canda
 */
public class FlexiblePlan implements Cloneable {

    /**
     * A set of useful methods for searching paths, distances etc.
     */
    protected final Utils utils;
    /**
     * The vehicle that will drive according to this plan
     */
    public final TestbedVehicle vehicle;
    /**
     * The time at the beginning of the plan
     */
    public long currentTime;
    /**
     * The taxi driver's initial node
     */
    public long firstNode;
    /**
     * The node where the driver wants to go after finishing the plan
     */
    public long lastNode;
    /**
     * Nodes of the plan, where passengers either get in or get off
     */
    public List<PlanItem> planItems;

    public FlexiblePlan(Utils utils, TestbedVehicle vehicle, long currentTime, long firstNode) {

        this(utils, vehicle, currentTime, firstNode, -1);
    }

    public FlexiblePlan(Utils utils, TestbedVehicle vehicle, long currentTime, long firstNode, long lastNode) {

        this.utils = utils;
        this.vehicle = vehicle;
        this.currentTime = currentTime;
        this.planItems = new ArrayList<PlanItem>();
        this.firstNode = firstNode;
        this.lastNode = lastNode;
    }

    /**
     * Removes the item from the specified index (other items will automatically
     * move back)
     *
     * @param index
     */
    public void removeItem(int index) {
        if (index < 0 || getSize() == 0) {
            return;
        }
        PlanItem prevItem = null;
        PlanItem nextItem = null;
        long drivingTime = -1;
        long departTimeDiff = -1;

        // remove it physically
        this.planItems.remove(index);
        if (getSize() == 0 || getSize() == index) {
            return;
        }

        nextItem = this.planItems.get(index);

        // first item
        if (index == 0) {
            if (firstNode == -1) {
                departTimeDiff = nextItem.takeTime(nextItem.getArrivalTime() - currentTime);
            } else {
                drivingTime = utils.computeDrivingTime(firstNode, nextItem.getNode());
                departTimeDiff = nextItem.takeTime(nextItem.getArrivalTime() - (currentTime + drivingTime));
            }
        } else {
            prevItem = planItems.get(index - 1);
            drivingTime = utils.computeDrivingTime(prevItem.getNode(), nextItem.getNode());
            departTimeDiff = nextItem.takeTime(nextItem.getArrivalTime() - (prevItem.getDepartureTime() + drivingTime));
        }

        // recount arrival times in all following nodes
        for (int i = index + 1; i < this.getSize(); i++) {
            nextItem = this.planItems.get(i);
            departTimeDiff = nextItem.takeTime(departTimeDiff);
        }

    }

    /**
     * Get the passenger that gets in on the node from the specified index
     *
     * @param nodeIndex index in my list of plan items
     * @return id of the passenger who gets in (or null if nobody gets in)
     */
    public String getBoardingPassenger(int nodeIndex) {
        if (!this.planItems.get(nodeIndex).isBoarding) {
            return null;
        }
        return this.planItems.get(nodeIndex).getPassengerId();
    }

    /**
     * Get the passenger that gets in on the node from the specified index
     *
     * @param nodeIndex index in my list of plan items
     * @return id of the passenger who gets in (or null if nobody gets in)
     */
    public String getLeavingPassenger(int nodeIndex) {
        if (this.planItems.get(nodeIndex).isBoarding) {
            return null;
        }
        return this.planItems.get(nodeIndex).getPassengerId();
    }

    public long getNode(int nodeIndex) {
        return this.planItems.get(nodeIndex).getNode();
    }

    /**
     * Removes all such nodes from the plan, that are visited before the given
     * node (including starting node)
     *
     * @param node node that will become the first node of the plan
     * @return false, if the given node is not found in the plan
     */
    public boolean removeNodesBefore(long node) {
        // remove all nodes
        if (lastNode == node) {
            firstNode = -1;
            this.planItems = new ArrayList<PlanItem>();
            return true;
        }

        // first, search for the node
        boolean found = false;
        for (PlanItem item : this.planItems) {
            if (item.getNode() == node) {
                found = true;
            }
        }
        if (!found) {
            return false;
        }

        // remove all nodes before the given node
        firstNode = -1;
        long nodeToDelete = this.planItems.get(0).getNode();
        while (nodeToDelete != node) {
            removeItem(0);
            nodeToDelete = this.planItems.get(0).getNode();
        }
        return true;
    }

    // /**
    // * Finds and removes the passenger's target node from the plan
    // *
    // * @param passengerId
    // * the passenger, from whom we want to remove target node
    // * @return true, if the passenger's target node has been found and removed
    // */
    // public boolean removePassengerTarget(String passengerId) {
    // PlanItem item;
    // for (int i = 0; i < getSize(); i++) {
    // item = this.planItems.get(i);
    // if (!item.isBoarding && item.getPassengerId().equals(passengerId)) {
    // this.removeItem(i);
    // return true;
    // }
    // }
    // return false;
    // }

    /**
     * @return the number of nodes to visit by this plan (except by the special
     *         initial and ending node)
     */
    public int getSize() {
        return this.planItems.size();
    }

    /**
     * @return either starting node, or, if there's no such node, return the
     *         node represented by the first plan item
     */
    public long getFirstNode() {
        if (this.firstNode != -1) {
            return this.firstNode;
        }
        if (getSize() > 0) {
            return this.planItems.get(0).getNode();
        }
        return -1;
    }

    /**
     * @return the list of requests, that are fully contained in this plan (both
     *         beginning and the end of the request)
     */
    public List<Request> getRequests() {
        List<Request> beginningReqs = new ArrayList<Request>();
        List<Request> confirmedReqs = new ArrayList<Request>();

        for (PlanItem item : this.planItems) {
            if (item.isBoarding) {
                beginningReqs.add(item.request);
            } else if (beginningReqs.contains(item.request)) {
                confirmedReqs.add(item.request);
            }
        }
        return confirmedReqs;
    }

    /**
     * Finds out, when the passenger arrives to his target node according to the
     * plan
     *
     * @param passengerId the id of the passenger
     * @return the arrival time (in milliseconds), or -1, if the passenger
     *         hasn't been found in the plan
     */
    public long getArrivalTime(String passengerId) {
        for (PlanItem item : this.planItems) {
            if (!item.isBoarding && item.getPassengerId().equals(passengerId)) {
                return item.getArrivalTime();
            }
        }
        return -1;
    }

    /**
     * @return the number of the last node of this plan
     */
    public long getLastNode() {
        if (lastNode != -1) {
            return lastNode;
        }
        if (getSize() > 0) {
            return this.planItems.get(getSize() - 1).getNode();
        }
        return -1;
    }

    public List<PlanItem> getPlanItems() {
        return planItems;
    }

    /**
     * @return the departure time from the last node of the plan
     */
    public long getEndOfPlanTime() {
        if (getSize() == 0) {
            return currentTime;
        }
        PlanItem lastItem = this.planItems.get(getSize() - 1);
        if (lastNode != -1) {
            long drivingTime = utils.computeDrivingTime(lastItem.getNode(), lastNode);
            return lastItem.getDepartureTime() + drivingTime;
        }
        return lastItem.getDepartureTime();
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        FlexiblePlan planClone = new FlexiblePlan(utils, vehicle, currentTime, firstNode, lastNode);

        for (int i = 0; i < getSize(); i++) {
            planClone.planItems.add((PlanItem) this.planItems.get(i).clone());
        }
        return planClone;
    }

    // /**
    // * Remove both get in and get off nodes, that belong to the request given
    // *
    // * @param reqToRemove
    // * the request to be removed from the plan
    // * @return true, if the request has been found
    // */
    // public boolean removeRequest(Request reqToRemove) {
    // int removedItems = 0;
    // for (int i = getSize() - 1; i >= 0; i--) {
    // if (this.planItems.get(i).request == reqToRemove) {
    // removeItem(i);
    // removedItems++;
    // if (removedItems == 2) {
    // break;
    // }
    // }
    // }
    // return removedItems > 0 ? true : false;
    // }

}

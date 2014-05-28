package cz.agents.agentpolis.darptestbed.simmodel.agent.data;


/**
 * One item that belongs to the FlexiblePlan (it represents either a start or a
 * target request node)
 *
 * @author Lukas Canda
 */
public class PlanItem implements Cloneable {

    /**
     * This plan item represents one of two nodes of this request
     */
    protected Request request;
    /**
     * True, if it represents the start node, false, if it's the target node
     */
    public final boolean isBoarding;
    /**
     * The time when the taxi driver arrives to this node (according to the
     * plan)
     */
    protected long arrivalTime = -1;
    /**
     * How much time the driver needs to wait on this node (for the time window
     * to open)
     */
    protected long waitingTime = -1;

    public PlanItem(Request request, boolean isBoarding) {
        this.request = request;
        this.isBoarding = isBoarding;
        // these fields need to be set up later
        this.arrivalTime = -1;
        this.waitingTime = -1;
    }

    /**
     * Creates new instance of PlanItem
     *
     * @param request     this plan item represents one of two nodes of this request
     * @param getIn       true, if it represents the start node, false, if it's the
     *                    target node
     * @param arrivalTime the time when the taxi arrives into this node
     * @return new instance of PlanItem, or null, if the timeToArrive is too
     *         late
     */
    public static PlanItem createPlanItem(Request request, boolean getIn, long arrivalTime) {
        if (PlanItem.hasDelay(request, getIn, arrivalTime)) {
            return null;
        }
        return new PlanItem(request, getIn, arrivalTime);
    }

    private PlanItem(Request request, boolean isBoarding, long arrivalTime) {
        this.request = request;
        this.isBoarding = isBoarding;
        this.arrivalTime = arrivalTime;
        computeWaitingTime();
    }

    /**
     * Sets the arrival time to this node, counts waiting time
     *
     * @param arrivalTime
     * @return -1 if the time causes delay, else it returns the departure time
     *         (including waiting)
     */
    public long setArrivalTime(long arrivalTime) {
        if (hasDelay(arrivalTime)) {
            return -1;
        }
        this.arrivalTime = arrivalTime;
        computeWaitingTime();

        return getDepartureTime();
    }

    /**
     * Adds a time to the current arrival time
     *
     * @param timeToAdd time to add to the current arrival time
     * @return -1 if the time causes delay, else it returns the difference that
     *         it causes in departure time (including waiting)
     */
    public long addTime(long timeToAdd) {
        long returnDiff = computeDepartureDifference(timeToAdd);
        if (returnDiff >= 0) {
            // count savings in waiting
            if (waitingTime >= timeToAdd) {
                waitingTime -= timeToAdd;
            } else {
                waitingTime = 0;
            }
            arrivalTime += timeToAdd;
            return returnDiff;
        }
        return -1;
    }

    /**
     * Tries to add a time (but doesn't really add it)
     *
     * @param timeToAdd time to add to the current arrival time
     * @return -1 if the time causes delay, else it returns the difference that
     *         it causes in departure time (including waiting)
     */
    public long computeDepartureDifference(long timeToAdd) {
        if (canAddTime(timeToAdd)) {
            // no difference, only saves some waiting
            if (waitingTime >= timeToAdd) {
                return 0;
            }
            return timeToAdd - waitingTime;
        }
        return -1;
    }

    /**
     * The vehicle will arrive sooner, so lets adjust the arrival time
     *
     * @param timeToTake the amount of time we should subtract
     * @return the decrease that it caused in departure time
     */
    public long takeTime(long timeToTake) {
        long departTime = getDepartureTime();
        arrivalTime -= timeToTake;
        computeWaitingTime();
        return departTime - getDepartureTime();
    }

    /**
     * The time we try to add won't cause the delay in this node (that is, if
     * the driver doesn't come too late)
     *
     * @param timeToAdd time we're trying to add
     * @return true, if it won't cause the delay
     */
    public boolean canAddTime(long timeToAdd) {
        if (hasDelay(arrivalTime + timeToAdd)) {
            return false;
        }
        return true;
    }

    /**
     * Counts the waiting time according to the request and arrival time
     */
    private void computeWaitingTime() {
        this.waitingTime = 0;
        TimeWindow timeWin = request.getTimeWindow();
        if (timeWin == null) {
            return;
        }
        if (isBoarding) {
            long earliestDep = timeWin.getEarliestDeparture();
            if (arrivalTime < earliestDep) {
                this.waitingTime = earliestDep - arrivalTime;
            }
        } else if (!timeWin.isOneInterval()) {
            long earliestArr = timeWin.getEarliestArrival();
            if (arrivalTime < earliestArr) {
                this.waitingTime = earliestArr - arrivalTime;
            }
        }
    }

    /**
     * @param arrivalTime
     * @return true, if the arrival time is too late
     */
    public boolean hasDelay(long arrivalTime) {
        return PlanItem.hasDelay(request, isBoarding, arrivalTime);
    }

    private static boolean hasDelay(Request request, boolean getIn, long arrivalTime) {
        TimeWindow timeWin = request.getTimeWindow();
        if (timeWin == null) {
            return false;
        }
        if (getIn) {
            if ((timeWin.isOneInterval() && arrivalTime > timeWin.getLatestArrival())
                    || (!timeWin.isOneInterval() && arrivalTime > timeWin.getLatestDeparture())) {
                return true;
            }
        } else {
            if (arrivalTime > timeWin.getLatestArrival()) {
                return true;
            }
        }
        return false;
    }

    /**
     * @return the time when the driver departs from this node (according to the
     *         plan)
     */
    public long getDepartureTime() {
        return arrivalTime + waitingTime;
    }

    public long getArrivalTime() {
        return arrivalTime;
    }

    /**
     * Get the number of node this plan item represents
     *
     * @return the number of node
     */
    public long getNode() {
        if (isBoarding) {
            return request.getFromNode();
        }
        return request.getToNode();
    }

    public String getPassengerId() {
        return request.getPassengerId();
    }

    /**
     * Sets arrival time and waiting time back do default
     */
    public void resetArrivalTime() {
        this.arrivalTime = -1;
        this.waitingTime = -1;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return new PlanItem(request, isBoarding, arrivalTime);
    }
}

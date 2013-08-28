package cz.agents.agentpolis.darptestbed.simmodel.agent.data;

/**
 * This class supports at least the three most common types of time windows:
 * -two intervals (departure, arrival)
 * -one interval (earliest departure, latest arrival)
 * -latest arrival
 * 
 * @author Lukas Canda
 */
public class TimeWindow {
	/**
	 * Supports two intervals (departure and arrival) at most
	 */
	protected long departMin;
	protected long departMax;
	protected long arriveMin;
	protected long arriveMax;
	
	/**
	 * The typical time window called "earliest departure, latest arrival"
	 * (one interval)
	 * 
	 * @param from earliest departure time
	 * @param to latest arrival time
	 */
	public TimeWindow(long from, long to) {
		//fix sizes
		if(from > to) {
			long tmp = from;
			from = to;
			to = tmp;
		}
		long[] values = {from, to};
		makePositive(values);
		
		departMin = values[0];
		arriveMax = values[1];
	}
	
	/**
	 * Full time window (two intervals)
	 * 
	 * @param from earliest departure time
	 * @param to latest arrival time
	 */
	public TimeWindow(long departFrom, long departTo, long arriveFrom, long arriveTo) {
		// fix sizes
		if(departFrom > arriveTo) {
			long tmp = departFrom;
			departFrom = arriveTo;
			arriveTo = tmp;
		}
		if(departTo > 0 && departFrom > departTo) {
			long tmp = departFrom;
			departFrom = departTo;
			departTo = tmp;
		}
		if(arriveFrom > arriveTo) {
			long tmp = arriveFrom;
			arriveFrom = arriveTo;
			arriveTo = tmp;
		}
		long[] values = {departFrom, departTo, arriveFrom, arriveTo};
		makePositive(values);
		
		departMin = values[0];
		departMax = values[1];
		arriveMin = values[2];
		arriveMax = values[3];
	}
	
	/**
	 * Time window called "latest arrival"
	 * (it will behave like one interval starting from 0)
	 * 
	 * @param to latest arrival time
	 */
	public TimeWindow(long to) {
		if(to < 0) {
			to = 0;
		}
		arriveMax = to;
	}
	
	/**
	 * Changes negative numbers to zero
	 * 
	 * @param values long numbers to be checked out
	 */
	public void makePositive(long[] values) {
		for(int i=0; i<values.length; i++) {
			if(values[i] < 0) {
				values[i] = 0;
			}
		}
	}
	
	/**
	 * @return true, if the time window is the typical "one interval" type.
	 */
	public boolean isOneInterval() {
		if(departMax == 0 && arriveMin == 0) {
			return true;
		}
		return false;
	}
	
	/**
	 * @param departureTime the time when the taxi arrived to pick up the passenger
	 * @return the delay:
	 * negative, if he arrived too early
	 * positive, if he arrived too late
	 * 0, if he arrived on time
	 */
	public long getDepartureDelay(long departureTime) {
		// too early
		if(departureTime < departMin) {
			return - (departMin - departureTime);
		}
		// too late (if using full time windows)
		if(departMax > 0 && departureTime > departMax) {
			return departureTime - departMax;
		}
		// too late
		if(departureTime > arriveMax) {
			return departureTime - arriveMax;
		}
		// on time
		return 0;
	}
	
	/**
	 * @param arrivalTime the time when the taxi arrived to target node
	 * to drop the passenger
	 * @return the delay:
	 * negative, if he arrived too early
	 * positive, if he arrived too late
	 * 0, if he arrived on time
	 */
	public long getArrivalDelay(long arrivalTime) {
		// too early
		if(isOneInterval() && arrivalTime < departMin) {
			return - (departMin - arrivalTime);
		}
		// too early (if using full time windows)
		if(!isOneInterval() && arrivalTime < arriveMin) {
			return - (arriveMin - arrivalTime);
		}
		// too late
		if(arrivalTime > arriveMax) {
			return arrivalTime - arriveMax;
		}
		// on time
		return 0;
	}
	
	public long getEarliestDeparture() {
		return this.departMin;
	}
	
	public long getLatestDeparture() {
		if(departMax == 0) {
			return arriveMax;
		}
		return departMax;
	}
	
	public long getEarliestArrival() {
		if(arriveMin == 0) {
			return departMin;
		}
		return arriveMin;
	}
	
	public long getLatestArrival() {
		return this.arriveMax;
	}

	@Override
	public String toString() {
		return "TimeWindow [departMin=" + departMin + ", departMax=" + departMax + ", arriveMin=" + arriveMin
				+ ", arriveMax=" + arriveMax + "]";
	}
}

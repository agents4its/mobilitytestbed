package cz.agents.agentpolis.darptestbed.simmodel.agent.data.comparator;

import java.util.Comparator;

import cz.agents.agentpolis.darptestbed.simmodel.agent.data.Request;

/**
 * Compares requests according to the latest possible time of departure
 * (the most critical with the lowest time of departure are in front).
 * 
 * If they're the same, then it compares according to the earliest departure.
 * 
 * @author Lukas Canda
 */
public class RequestLatestDepComparator implements Comparator<Request> {

	@Override
	public int compare(Request o1, Request o2) {
		if(o1.getTimeWindow() == null || o2.getTimeWindow() == null) {
			return 0;
		}
		long lateDep1 = o1.getTimeWindow().getLatestDeparture();
		long lateDep2 = o2.getTimeWindow().getLatestDeparture();
		if(lateDep1 - lateDep2 < 0) {
			return -1;
		}
		if(lateDep1 - lateDep2 > 0) {
			return 1;
		}
		//if ==
		long earlDep1 = o1.getTimeWindow().getEarliestDeparture();
		long earlDep2 = o2.getTimeWindow().getEarliestDeparture();
		if(earlDep1 - earlDep2 < 0) {
			return -1;
		}
		if(earlDep1 - earlDep2 > 0) {
			return 1;
		}
		return 0;
	}

}

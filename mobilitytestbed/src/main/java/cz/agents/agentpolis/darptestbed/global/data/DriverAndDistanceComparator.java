package cz.agents.agentpolis.darptestbed.global.data;

import java.util.Comparator;

/**
 * Compares TaxiAndDistance objects according to the distance
 * (the closest taxis are in the front)
 * 
 * @author Lukas Canda
 */
public class DriverAndDistanceComparator implements Comparator<DriverAndDistance> {

	@Override
	public int compare(DriverAndDistance o1, DriverAndDistance o2) {
		long distTime1 = o1.getDistanceTime();
		long distTime2 = o2.getDistanceTime();
		if(distTime1 - distTime2 < 0) {
			return -1;
		}
		if(distTime1 - distTime2 > 0) {
			return 1;
		}
		return 0;
	}
}

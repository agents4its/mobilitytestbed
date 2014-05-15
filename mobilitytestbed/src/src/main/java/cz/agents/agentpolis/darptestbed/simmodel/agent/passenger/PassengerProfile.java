package cz.agents.agentpolis.darptestbed.simmodel.agent.passenger;

import java.util.List;

/**
 * This class encapsulates all personal information and needs of a passenger.
 * It is used mainly for generating requests from real passengers.
 * 
 * @author Lukas Canda
 */
public class PassengerProfile {
	
	protected int age;
	protected int maxWalkingDistance;
	protected List<String> discounts;
	// you can add any additional information, that will affect request generator
	
	
	public PassengerProfile() {
	}
	
	public PassengerProfile(int age, int maxWalkingDistance,
			List<String> discounts) {
		
		this.age = age;
		this.maxWalkingDistance = maxWalkingDistance;
		this.discounts = discounts;
	}

	public int getAge() {
		return age;
	}

	public int getMaxWalkingDistance() {
		return maxWalkingDistance;
	}

	public List<String> getDiscounts() {
		return discounts;
	}
}

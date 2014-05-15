package cz.agents.agentpolis.darptestbed.simmodel.agent;

import cz.agents.agentpolis.simmodel.entity.EntityType;

/**
 * Types of agents used for DARP Testbed
 * 
 * @author Lukas Canda
 */
public enum TestbedEntityType implements EntityType {
	
	DISPATCHING("DISPATCHING"),
	PASSENGER("PASSENGER"), 
	TAXI_DRIVER("TAXI_DRIVER");
	
	private final String entityType;
	

	private TestbedEntityType(String entityType) {
		this.entityType = entityType;
	}	

	public String getDescriptionEntityType() {
		return entityType;
	}
}

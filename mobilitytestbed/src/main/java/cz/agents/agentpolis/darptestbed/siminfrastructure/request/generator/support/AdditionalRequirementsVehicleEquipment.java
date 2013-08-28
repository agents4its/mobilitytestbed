package cz.agents.agentpolis.darptestbed.siminfrastructure.request.generator.support;

public enum AdditionalRequirementsVehicleEquipment {

	WHEELCHAIR_SUPPORT("WHEELCHAIR_SUPPORT", "WHEELCHAIR_SUPPORT");

	public final String additionalRequirements;
	public final String vehicleEquipment;

	private AdditionalRequirementsVehicleEquipment(String additionalRequirements, String vehicleEquipment) {
		this.additionalRequirements = additionalRequirements;
		this.vehicleEquipment = vehicleEquipment;
	}

}

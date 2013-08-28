package cz.agents.agentpolis.darptestbed.siminfrastructure.request.generator.support;

import java.util.Set;

import org.apache.commons.lang.NotImplementedException;

import com.google.common.collect.Sets;

public class PasssengerGeneratorJustRequirements implements PasssengerGenerator {

	@Override
	public RequestTimeInfo generateRequestTimeInfo() {
		throw new NotImplementedException();
	}

	@Override
	public Set<String> generateAdditionalRequirements() {
		return Sets.newHashSet(AdditionalRequirementsVehicleEquipment.WHEELCHAIR_SUPPORT.additionalRequirements);
	}

}

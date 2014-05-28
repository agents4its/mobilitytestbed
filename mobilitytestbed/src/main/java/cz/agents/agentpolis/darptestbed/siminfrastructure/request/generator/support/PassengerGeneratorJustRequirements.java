package cz.agents.agentpolis.darptestbed.siminfrastructure.request.generator.support;

import com.google.common.collect.Sets;
import cz.agents.agentpolis.darptestbed.global.Utils;
import cz.agents.agentpolis.darptestbed.siminfrastructure.request.GPS;
import org.apache.commons.lang.NotImplementedException;

import java.util.Set;

public class PassengerGeneratorJustRequirements implements PassengerGenerator {

    @Override
    public RequestTimeInfo generateRequestTimeInfo(GPS fromGPS, GPS toGPS) {
        throw new NotImplementedException();
    }

    @Override
    public RequestTimeInfo generateRequestTimeInfo(long fromNode, long toNode) {
        throw new NotImplementedException();
    }

    @Override
    public Set<String> generateAdditionalRequirements() {
        return Sets.newHashSet(AdditionalRequirementsVehicleEquipment.WHEELCHAIR_SUPPORT.additionalRequirements);
    }

    @Override
    public RequestTimeInfo generateRequestTimeInfo() {
        throw new NotImplementedException();
    }

}

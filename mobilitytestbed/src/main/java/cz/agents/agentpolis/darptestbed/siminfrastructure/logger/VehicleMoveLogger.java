package cz.agents.agentpolis.darptestbed.siminfrastructure.logger;

import java.util.HashMap;
import java.util.Map;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import cz.agents.agentpolis.darptestbed.siminfrastructure.logger.key.EVehicleLogItemType;
import cz.agents.agentpolis.logger.LogItemKey;
import cz.agents.agentpolis.siminfrastructure.logger.Logger;
import cz.agents.agentpolis.siminfrastructure.logger.LoggerProtocol;
import cz.agents.agentpolis.siminfrastructure.logger.key.EPassengerPositionLogItemKey;
import cz.agents.agentpolis.simmodel.environment.model.VehicleGroupModel;
import cz.agents.alite.common.event.EventProcessor;

/**
 * Logs taxi mevements
 * 
 * @author Lukas Canda
 */
@Singleton
public class VehicleMoveLogger extends Logger {

	@Inject
    public VehicleMoveLogger(LoggerProtocol eventProtocol,
    		EventProcessor eventProcessor, VehicleGroupModel vehicleGroupModel) {
        
		super(eventProtocol, eventProcessor);
	}
	
	
	/**
	 * Logs into file, when any taxi moves from one node to another
	 * (just a few meters)
	 * 
	 * @param vehicleId the taxi that has moved
	 * @param toByNodeId the node it's moved onto
	 */
	public void logVehicleMove(String vehicleId, long toByNodeId) {
       
		Map<LogItemKey,Object> eventData = new HashMap<LogItemKey,Object>();
        eventData.put(EPassengerPositionLogItemKey.PLACE, toByNodeId);
        logCommonEvent(vehicleId, EVehicleLogItemType.VEHICLE_MOVEMENT, eventData);
    }
	
}

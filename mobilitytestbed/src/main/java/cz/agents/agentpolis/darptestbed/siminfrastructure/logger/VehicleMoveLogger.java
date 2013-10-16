package cz.agents.agentpolis.darptestbed.siminfrastructure.logger;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import cz.agents.agentpolis.darptestbed.siminfrastructure.logger.item.VehilceMovementLogItem;
import cz.agents.agentpolis.siminfrastructure.logger.Logger;
import cz.agents.agentpolis.siminfrastructure.logger.PublishSubscribeLogger;
import cz.agents.alite.common.event.EventProcessor;

/**
 * Logs taxi mevements
 * 
 * @author Lukas Canda
 */
@Singleton
public class VehicleMoveLogger extends Logger {

	@Inject
	public VehicleMoveLogger(PublishSubscribeLogger publishSubscribeLogger, EventProcessor eventProcessor) {
		super(publishSubscribeLogger, eventProcessor);
	}

	/**
	 * Logs into file, when any taxi moves from one node to another (just a few
	 * meters)
	 * 
	 * @param vehicleId
	 *            the taxi that has moved
	 * @param toByNodeId
	 *            the node it's moved onto
	 */
	public void logVehicleMove(String vehicleId, long toByNodeId) {

		// Map<LogItemKey, Object> eventData = new HashMap<LogItemKey,
		// Object>();
		// eventData.put(EPassengerPositionLogItemKey.PLACE, toByNodeId);
		// logCommonEvent(vehicleId, EVehicleLogItemType.VEHICLE_MOVEMENT,
		// eventData);

		log(new VehilceMovementLogItem(vehicleId, getCurrentSimulationTime(), toByNodeId));
	}

}

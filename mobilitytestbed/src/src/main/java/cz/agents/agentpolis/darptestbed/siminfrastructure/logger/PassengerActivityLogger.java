package cz.agents.agentpolis.darptestbed.siminfrastructure.logger;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import cz.agents.agentpolis.darptestbed.siminfrastructure.logger.item.PassengerGetInVehicleLogItem;
import cz.agents.agentpolis.darptestbed.siminfrastructure.logger.item.PassengerGetOffVehicleLogItem;
import cz.agents.agentpolis.siminfrastructure.logger.Logger;
import cz.agents.agentpolis.siminfrastructure.logger.PublishSubscribeLogger;
import cz.agents.alite.common.event.EventProcessor;

/**
 * Logs the events of a passenger - when he gets in and gets out from a taxi
 * 
 * @author Lukas Canda
 */
@Singleton
public class PassengerActivityLogger extends Logger {

	@Inject
	public PassengerActivityLogger(PublishSubscribeLogger publishSubscribeLogger, EventProcessor eventProcessor) {
		super(publishSubscribeLogger, eventProcessor);
	}

	public void logPassengerGotInVehicle(String passengerId, long nodeId, String vehicleId) {

		// Map<LogItemKey, Object> eventData = new HashMap<LogItemKey,
		// Object>();
		// eventData.put(EPassengerPositionLogItemKey.PLACE, nodeId);
		// eventData.put(EPassengerLogItemKey.VEHICLE, vehicleId);
		// logCommonEvent(passengerId,
		// EPassengerLogItemType.PASSENGER_GOT_IN_TAXI, eventData);

		log(new PassengerGetInVehicleLogItem(getCurrentSimulationTime(), passengerId, vehicleId));
	}

	public void logPassengerGotOffVehicle(String passengerId, long nodeId, String vehicleId) {

		// Map<LogItemKey, Object> eventData = new HashMap<LogItemKey,
		// Object>();
		// eventData.put(EPassengerPositionLogItemKey.PLACE, nodeId);
		// eventData.put(EPassengerLogItemKey.VEHICLE, vehicleId);
		// logCommonEvent(passengerId,
		// EPassengerLogItemType.PASSENGER_GOT_OFF_TAXI, eventData);

		log(new PassengerGetOffVehicleLogItem(getCurrentSimulationTime(), passengerId, vehicleId));
	}

}

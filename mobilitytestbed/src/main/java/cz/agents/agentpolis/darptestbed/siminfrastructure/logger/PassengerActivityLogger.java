package cz.agents.agentpolis.darptestbed.siminfrastructure.logger;

import java.util.HashMap;
import java.util.Map;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import cz.agents.agentpolis.darptestbed.siminfrastructure.logger.item.PassengerGetInVehicleLogItem;
import cz.agents.agentpolis.darptestbed.siminfrastructure.logger.item.PassengerGetOffVehicleLogItem;
import cz.agents.agentpolis.darptestbed.siminfrastructure.logger.key.EPassengerLogItemKey;
import cz.agents.agentpolis.darptestbed.siminfrastructure.logger.key.EPassengerLogItemType;
import cz.agents.agentpolis.logger.LogItemKey;
import cz.agents.agentpolis.siminfrastructure.logger.Logger;
import cz.agents.agentpolis.siminfrastructure.logger.LoggerProtocol;
import cz.agents.agentpolis.siminfrastructure.logger.PublishSubscribeLogger;
import cz.agents.agentpolis.siminfrastructure.logger.key.EPassengerPositionLogItemKey;
import cz.agents.alite.common.event.EventProcessor;

/**
 * Logs the events of a passenger - when he gets in and gets out from a taxi
 * 
 * @author Lukas Canda
 */
@Singleton
public class PassengerActivityLogger extends Logger {

	private final PublishSubscribeLogger publishSubscribeLogger;
	private final EventProcessor eventProcessor;

	@Inject
	public PassengerActivityLogger(LoggerProtocol eventProtocol, EventProcessor eventProcessor,
			PublishSubscribeLogger publishSubscribeLogger) {
		super(eventProtocol, eventProcessor);
		this.publishSubscribeLogger = publishSubscribeLogger;
		this.eventProcessor = eventProcessor;
	}

	public void logPassengerGotInVehicle(String passengerId, long nodeId, String vehicleId) {

		Map<LogItemKey, Object> eventData = new HashMap<LogItemKey, Object>();
		eventData.put(EPassengerPositionLogItemKey.PLACE, nodeId);
		eventData.put(EPassengerLogItemKey.VEHICLE, vehicleId);
		logCommonEvent(passengerId, EPassengerLogItemType.PASSENGER_GOT_IN_TAXI, eventData);

		publishSubscribeLogger.log(new PassengerGetInVehicleLogItem(getSimulationTime(), passengerId, vehicleId));
	}

	public void logPassengerGotOffVehicle(String passengerId, long nodeId, String vehicleId) {

		Map<LogItemKey, Object> eventData = new HashMap<LogItemKey, Object>();
		eventData.put(EPassengerPositionLogItemKey.PLACE, nodeId);
		eventData.put(EPassengerLogItemKey.VEHICLE, vehicleId);
		logCommonEvent(passengerId, EPassengerLogItemType.PASSENGER_GOT_OFF_TAXI, eventData);

		publishSubscribeLogger.log(new PassengerGetOffVehicleLogItem(getSimulationTime(), passengerId, vehicleId));
	}

	public long getSimulationTime() {
		return eventProcessor.getCurrentTime();
	}
}

package cz.agents.agentpolis.darptestbed.siminfrastructure.logger;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import cz.agents.agentpolis.darptestbed.siminfrastructure.logger.item.PassengerRequestLogItem;
import cz.agents.agentpolis.darptestbed.siminfrastructure.logger.item.RequestConfirmedLogItem;
import cz.agents.agentpolis.darptestbed.siminfrastructure.logger.item.RequestRejectedLogItem;
import cz.agents.agentpolis.darptestbed.simmodel.agent.data.TimeWindow;
import cz.agents.agentpolis.siminfrastructure.logger.Logger;
import cz.agents.agentpolis.siminfrastructure.logger.PublishSubscribeLogger;
import cz.agents.alite.common.event.EventProcessor;

/**
 * Logs the events concerning requests - when a new request is generated
 * 
 * @author Lukas Canda
 */
@Singleton
public class RequestLogger extends Logger {

	@Inject
	public RequestLogger(PublishSubscribeLogger publishSubscribeLogger, EventProcessor eventProcessor) {
		super(publishSubscribeLogger, eventProcessor);

	}

	public void logPassengerSentRequest(String passengerId, long fromNode, long toNode, long departFrom, long departTo,
			long arrivalFrom, long arrivalTo) {

		// Map<LogItemKey, Object> eventData = new HashMap<LogItemKey,
		// Object>();
		// eventData.put(ERequestLogItemKey.FROM_NODE, fromNode);
		// eventData.put(ERequestLogItemKey.TO_NODE, toNode);
		// if (departFrom > 0 && arrivalTo > 0) {
		// eventData.put(ERequestLogItemKey.TIME_WIN_OPEN, departFrom);
		// eventData.put(ERequestLogItemKey.TIME_WIN_CLOSE, arrivalTo);
		// }
		// logCommonEvent(passengerId,
		// ERequestLogItemType.PASSENGER_SENT_REQUEST, eventData);

		log(new PassengerRequestLogItem(passengerId, new TimeWindow(departFrom, departTo, arrivalFrom, arrivalTo),
				fromNode, toNode));
	}

	public void logPassengerSentRequest(String passengerId, long fromNode, long toNode) {
		logPassengerSentRequest(passengerId, fromNode, toNode, -1, -1, -1, -1);
	}

	public void logRequestConfirmed(String passengerId, String driverId, String vehicleId) {
		log(new RequestConfirmedLogItem(getCurrentSimulationTime(), passengerId, driverId, vehicleId));
	}

	public void logRequestRejected(String passengerId) {

		log(new RequestRejectedLogItem(getCurrentSimulationTime(), passengerId));
	}

}

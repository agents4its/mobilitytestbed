package cz.agents.agentpolis.tools.geovisio.spy.darptestbed;

import com.google.common.eventbus.Subscribe;

import cz.agents.agentpolis.darptestbed.siminfrastructure.logger.item.PassengerGetInVehicleLogItem;
import cz.agents.agentpolis.darptestbed.siminfrastructure.logger.item.PassengerGetOffVehicleLogItem;
import cz.agents.agentpolis.darptestbed.siminfrastructure.logger.item.PassengerRequestLogItem;
import cz.agents.agentpolis.darptestbed.siminfrastructure.logger.item.RequestConfirmedLogItem;
import cz.agents.agentpolis.darptestbed.siminfrastructure.logger.item.RequestRejectedLogItem;

/**
 * 
 * @author Marek Cuchy
 * 
 */
public class LogHandler {

	private final RequestStorage requestStorage = new RequestStorage();

	public RequestStorage getRequestStorage() {
		return requestStorage;
	}

	@Subscribe
	public void processPassengerRequest(PassengerRequestLogItem passengerRequest) {
		requestStorage.addRequest(passengerRequest);
	}

	@Subscribe
	public void processPassengerGetInVehicle(PassengerGetInVehicleLogItem passengerGetInVehicle) {
		requestStorage.passengerGetInVehicle(passengerGetInVehicle.passengerId, passengerGetInVehicle.simulationTime);
	}

	@Subscribe
	public void processPassengerGetOffVehicle(PassengerGetOffVehicleLogItem passengerGetOfVehicle) {
		requestStorage
				.passengerGetOutOfVehicle(passengerGetOfVehicle.passengerId, passengerGetOfVehicle.simulationTime);
	}

	@Subscribe
	public void processRequestConfirmed(RequestConfirmedLogItem requestConfirmed) {
		requestStorage.confirmRequest(requestConfirmed.passengerId, requestConfirmed.simulationTime);
	}

	@Subscribe
	public void processRequestRejected(RequestRejectedLogItem requestRejected) {
		requestStorage.rejectRequest(requestRejected.passengerId, requestRejected.simulationTime);
	}

}

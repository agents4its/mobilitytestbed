package cz.agents.agentpolis.tools.geovisio.spy.darptestbed;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import cz.agents.agentpolis.darptestbed.siminfrastructure.logger.item.PassengerRequestLogItem;

/**
 * 
 * @author Marek Cuchy
 * 
 */
public class RequestStorage {

	public enum State {
		NONE,
		SENT,
		DELAYED_SENT,
		CONFIRMED,
		REJECTED,
		DELAYED_CONFIRMED,
		IN_VEHICLE,
		IN_VEHICLE_WITH_DELAYED_DEPARTURE,
		IN_VEHICLE_WITH_DELAYED_ARRIVAL,
		OUT_OF_VEHICLE,
		OUT_OF_VEHICLE_WITH_DELAYED_ARRIVAL
	}

	private static final Logger logger = Logger.getLogger(RequestStorage.class);

	private final Map<String, Record> storage = new HashMap<>();

	public void addRequest(PassengerRequestLogItem passengerRequest) {
		storage.put(passengerRequest.passengerId, new Record(passengerRequest));
	}

	public State getCurrentState(String passengerId, long currentSimTime) {
		Record record = storage.get(passengerId);
		if (record == null) return State.NONE;

		if (record.currentState == State.SENT && record.request.timeWindow.getLatestDeparture() < currentSimTime) {
			return State.DELAYED_SENT;
		}
		if (record.currentState == State.CONFIRMED && record.request.timeWindow.getLatestDeparture() < currentSimTime) {
			return State.DELAYED_CONFIRMED;
		}
		if ((record.currentState == State.IN_VEHICLE || record.currentState == State.IN_VEHICLE_WITH_DELAYED_DEPARTURE)
		        && record.request.timeWindow.getLatestArrival() < currentSimTime) {
			return State.IN_VEHICLE_WITH_DELAYED_ARRIVAL;
		}
		return record.currentState;
	}

	public int size() {
		return storage.size();
	}

	public void passengerGetInVehicle(String passengerId, long simulationTime) {
		Record record = storage.get(passengerId);
		if (record.request.timeWindow.getLatestDeparture() < simulationTime) {
			record.setCurrentState(State.IN_VEHICLE_WITH_DELAYED_DEPARTURE);
		} else {
			record.setCurrentState(State.IN_VEHICLE);
		}
	}

	public void passengerGetOutOfVehicle(String passengerId, long simulationTime) {
		Record record = storage.get(passengerId);
		if (record.request.timeWindow.getLatestArrival() < simulationTime) {
			record.setCurrentState(State.OUT_OF_VEHICLE_WITH_DELAYED_ARRIVAL);
		} else {
			record.setCurrentState(State.OUT_OF_VEHICLE);
		}
	}

	public void confirmRequest(String passengerId, long simulationTime) {
		Record record = storage.get(passengerId);
		record.setCurrentState(State.CONFIRMED);
	}

	public void rejectRequest(String passengerId, long simulationTime) {
		Record record = storage.get(passengerId);
		record.setCurrentState(State.REJECTED);
	}

	private static class Record {

		public final PassengerRequestLogItem request;
		private State currentState;

		public Record(PassengerRequestLogItem request) {
			super();
			this.request = request;
			this.currentState = State.SENT;
		}

		public void setCurrentState(State state) {
			currentState = state;
		}

		@Override
		public String toString() {
			return "Record [request=" + request + ", currentState=" + currentState + "]";
		}

	}
}

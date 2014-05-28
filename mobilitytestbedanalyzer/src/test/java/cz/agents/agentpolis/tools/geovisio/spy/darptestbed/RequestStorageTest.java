package cz.agents.agentpolis.tools.geovisio.spy.darptestbed;

import static org.junit.Assert.assertTrue;

import cz.agents.agentpolis.darptestbed.global.Utils;
import org.junit.BeforeClass;
import org.junit.Test;

import cz.agents.agentpolis.darptestbed.siminfrastructure.logger.item.PassengerRequestLogItem;
import cz.agents.agentpolis.darptestbed.simmodel.agent.data.TimeWindow;
import cz.agents.agentpolis.tools.geovisio.spy.darptestbed.RequestStorage.State;

/**
 * 
 * @author Marek Cuchy
 * 
 */
public class RequestStorageTest {

	public static RequestStorage storage;

// TODO: eh?????
//	@BeforeClass
//	public static void setUpBeforeClass() throws Exception {
//		storage = new RequestStorage();
//		storage.addRequest(new PassengerRequestLogItem("1", new TimeWindow(80, 180, 250, 350), 0, 0));
//		storage.addRequest(new PassengerRequestLogItem("2", new TimeWindow(80, 180, 250, 350), 0, 0));
//		storage.addRequest(new PassengerRequestLogItem("3", new TimeWindow(80, 180, 250, 350), 0, 0));
//	}
//
//	@Test
//	public void testAddRequest() {
//		storage.addRequest(new PassengerRequestLogItem("22", new TimeWindow(80, 180, 250, 350), 0, 0));
//		assertTrue(storage.size() == 4);
//	}
//
//	@Test
//	public void testConfirmRequest() {
//		storage.confirmRequest("2", 50);
//		assertTrue(storage.getCurrentState("2", 0) == State.CONFIRMED);
//		storage.confirmRequest("2", 200);
//		assertTrue(storage.getCurrentState("2", 200) == State.DELAYED_CONFIRMED);
//	}
//
//	@Test
//	public void testPassengerGetInVehicle() {
//		storage.passengerGetInVehicle("2", 100);
//		assertTrue(storage.getCurrentState("2", 0) == State.IN_VEHICLE);
//		storage.passengerGetInVehicle("2", 100);
//		assertTrue(storage.getCurrentState("2", 400) == State.IN_VEHICLE_WITH_DELAYED_ARRIVAL);
//		storage.passengerGetInVehicle("2", 200);
//		assertTrue(storage.getCurrentState("2", 0) == State.IN_VEHICLE_WITH_DELAYED_DEPARTURE);
//	}
//
//	@Test
//	public void testPassengerGetOutOfVehicle() {
//		storage.passengerGetOutOfVehicle("2", 300);
//		assertTrue(storage.getCurrentState("2", 0) == State.OUT_OF_VEHICLE);
//		storage.passengerGetOutOfVehicle("2", 400);
//		assertTrue(storage.getCurrentState("2", 0) == State.OUT_OF_VEHICLE_WITH_DELAYED_ARRIVAL);
//	}
//
//	@Test
//	public void testGetCurrentState() {
//		assertTrue(storage.getCurrentState("1", 0) == State.SENT);
//		assertTrue(storage.getCurrentState("33", 0) == State.NONE);
//		assertTrue(storage.getCurrentState("1", 200) == State.DELAYED_SENT);
//	}
}

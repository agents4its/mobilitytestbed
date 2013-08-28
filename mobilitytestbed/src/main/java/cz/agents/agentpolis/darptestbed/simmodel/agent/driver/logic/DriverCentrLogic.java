package cz.agents.agentpolis.darptestbed.simmodel.agent.driver.logic;

import cz.agents.agentpolis.darptestbed.global.Utils;
import cz.agents.agentpolis.darptestbed.siminfrastructure.communication.passenger.message.MessageDriverArrived;
import cz.agents.agentpolis.darptestbed.siminfrastructure.communication.passenger.protocol.PassengerMessageProtocol;
import cz.agents.agentpolis.darptestbed.simmodel.agent.data.TripInfo;
import cz.agents.agentpolis.darptestbed.simmodel.agent.data.TripPlan;
import cz.agents.agentpolis.darptestbed.simmodel.entity.vehicle.TestbedVehicle;
import cz.agents.agentpolis.darptestbed.simmodel.environment.model.TestbedModel;
import cz.agents.agentpolis.simmodel.agent.activity.movement.VehicleDrivingActivity;
import cz.agents.agentpolis.simmodel.environment.model.citymodel.transportnetwork.AllNetworkNodes;
import cz.agents.agentpolis.simmodel.environment.model.query.AgentPositionQuery;

/**
 * The basic features of a DriverAgent, especially his communication protocol
 * that enables him to contact other agents.
 * 
 * The centralized communication means that the taxi driver communicates only
 * with the dispatching (control center).
 * 
 * @author Lukas Canda
 */
public class DriverCentrLogic extends DriverLogic<PassengerMessageProtocol> {

	private TripPlan tripPlan;

	public DriverCentrLogic(String agentId, PassengerMessageProtocol sender, TestbedModel taxiModel,
			AgentPositionQuery positionQuery, AllNetworkNodes allNetworkNodes, Utils utils, TestbedVehicle vehicle,
			VehicleDrivingActivity drivingActivity) {

		super(agentId, sender, taxiModel, positionQuery, allNetworkNodes, utils, vehicle, drivingActivity);
	}

	/**
	 * Accepts the message with the trip plan, which the driver should follow.
	 * 
	 * @param tripPlan
	 *            a list of nodes to visit, a map of passengers to get in
	 */
	public void acceptTripPlan(TripPlan tripPlan) {
		if (this.getTripPlan() != null) {
			this.extendTripPlan(tripPlan);
		} else {
			this.setTripPlan(tripPlan);
		}

		this.driveNextPartOfTripPlan();
	}

	@Override
	protected void sendTaxiArrived(String passengerId) {
		sender.sendMessage(passengerId, new MessageDriverArrived(getDriverId(), new TripInfo(getDriverId(), this.getVehicle().getId())));
	}

}

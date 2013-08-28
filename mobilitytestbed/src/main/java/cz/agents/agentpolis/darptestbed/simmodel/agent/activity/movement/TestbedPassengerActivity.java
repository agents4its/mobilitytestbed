package cz.agents.agentpolis.darptestbed.simmodel.agent.activity.movement;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.inject.Inject;

import cz.agents.agentpolis.darptestbed.global.Utils;
import cz.agents.agentpolis.darptestbed.siminfrastructure.logger.PassengerActivityLogger;
import cz.agents.agentpolis.darptestbed.simmodel.environment.model.TestbedModel;
import cz.agents.agentpolis.simmodel.environment.model.action.UseVehicleWithNotifyAction;
import cz.agents.agentpolis.simmodel.environment.model.action.callback.PassengerVehiclePlanCallback;
import cz.agents.agentpolis.simmodel.environment.model.linkedentitymodel.sensor.LinkedEntitySensor;
import cz.agents.agentpolis.simmodel.environment.model.query.AgentPositionQuery;

/**
 * Through this activity, a passenger can get in a vehicle, that has arrived to
 * the passenger's initial position. After that, he gets off at his target
 * position.
 * 
 * @author Lukas Canda
 */
public class TestbedPassengerActivity implements LinkedEntitySensor {

	/**
	 * This action enables us to get in the vehicle
	 */
	protected final UseVehicleWithNotifyAction useVehicleAction;
	/**
	 * A query to find the current position of an agent
	 */
	protected final AgentPositionQuery positionQuery;
	/**
	 * A storage to save all data concerning taxi drivers and passengers
	 */
	protected final TestbedModel taxiModel;
	/**
	 * A set of useful methods for searching paths, distances etc.
	 */
	protected final Utils utils;
	/**
	 * Logs important events into the log file.
	 */
	protected final PassengerActivityLogger logger;
	/**
	 * We need this class just to prevent crash.
	 */
	private final EmptyPassengerVehiclePlanCallback emptyCallback;

	/**
	 * The id of the travelling passenger
	 */
	protected String passengerId;
	/**
	 * The id of the taxi he's travelling with
	 */
	protected String vehicleId;
	/**
	 * The number of the target node.
	 */
	protected long targetPosition;

	/**
	 * The Callback to notify of finishing the trip (can be equal to null)
	 */
	protected TestbedPassengerActivityCallback passengerActivityCallback;

	@Inject
	public TestbedPassengerActivity(UseVehicleWithNotifyAction useVehicleAction, AgentPositionQuery positionQuery,
			TestbedModel taxiModel, Utils utils, PassengerActivityLogger logger) {

		this.useVehicleAction = useVehicleAction;
		this.positionQuery = positionQuery;
		this.taxiModel = taxiModel;
		this.utils = utils;
		this.logger = logger;
		this.emptyCallback = new EmptyPassengerVehiclePlanCallback();
	}

	/**
	 * Get in the vehicle, that is just waiting on my position. Drive with it
	 * until it reaches my target node.
	 * 
	 * @param passengerId
	 *            the passenger who wants to travel
	 * @param vehicleId
	 *            the vehicle he's going to travel with
	 * @param targetNode
	 *            the number of his target node
	 */
	public void useArrivedVehicle(String passengerId, String vehicleId, long targetPosition) {
		this.passengerId = passengerId;
		this.vehicleId = vehicleId;
		this.targetPosition = targetPosition;
		taxiModel.addPassengerOnBoard(passengerId, vehicleId);
		checkArgument(useVehicleAction.getInVehicle(passengerId, vehicleId, this.emptyCallback, this),
				"The passenger is not able to get into the vehicle with id" + vehicleId
						+ ", because the vehicle has no free seats");
		// log the event
		Long currentPos = positionQuery.getCurrentPositionByNodeId(this.passengerId);
		logger.logPassengerGotInVehicle(passengerId, currentPos, vehicleId);
	}

	/**
	 * Get in the vehicle, that is just waiting on my position. Drive with it
	 * until it reaches my target node.
	 * 
	 * @param passengerId
	 *            the passenger who wants to travel
	 * @param vehicleId
	 *            the vehicle he's going to travel with
	 * @param targetNode
	 *            the number of his target node
	 * @param passengerActivityCallback
	 *            callback to be called after reaching my target node
	 */
	public void useArrivedVehicle(String passengerId, String vehicleId, long targetPosition,
			TestbedPassengerActivityCallback passengerActivityCallback) {

		this.passengerActivityCallback = passengerActivityCallback;
		this.useArrivedVehicle(passengerId, vehicleId, targetPosition);
	}

	/**
	 * The passenger always gets unlinked after arriving to arbitrary node
	 * (that's what drive method does automatically). If he hasn't arrived where
	 * he wanted, he needs to get back in.
	 * 
	 * @param unlinkedFromEntityId
	 *            the vehicle he's gotten unlinked from
	 */
	@Override
	public void entityWasUnlinked(String unlinkedFromEntityId) {

		Long currentPos = positionQuery.getCurrentPositionByNodeId(this.passengerId);
		// have we arrived to our target position?
		if (this.targetPosition == currentPos) {
			taxiModel.removePassengerOnBoard(passengerId, vehicleId);
			// log the event
			logger.logPassengerGotOffVehicle(passengerId, currentPos, vehicleId);
			if (passengerActivityCallback != null) {
				passengerActivityCallback.tripFinished(this.targetPosition);
			}
		} else {
			// if not, get in again and continue the journey
			useVehicleAction.getInVehicle(passengerId, vehicleId, this.emptyCallback, this);
		}
	}

	/**
	 * We need this class just to prevent crash.
	 * 
	 * @author Zbynek Moler
	 */
	private static class EmptyPassengerVehiclePlanCallback implements PassengerVehiclePlanCallback {

		@Override
		public void notifyPassengerAboutVehiclePlan(long fromNodeId, long toNodeId, String vehicleId) {
			// Do nothing
		}

		@Override
		public void notifyWaitingPassengerAboutVehiclePlan(long fromNodeId, long toNodeId, String vehicleId) {
			// Do nothing
		}
	}

}

package cz.agents.agentpolis.darptestbed.simmodel.agent.passenger;

import java.util.Set;

import org.apache.log4j.Logger;
import org.joda.time.Duration;

import cz.agents.agentpolis.darptestbed.global.GlobalParams;
import cz.agents.agentpolis.darptestbed.global.Utils;
import cz.agents.agentpolis.darptestbed.siminfrastructure.communication.passenger.message.DriverArrivedMessage;
import cz.agents.agentpolis.darptestbed.siminfrastructure.communication.passenger.message.OrderConfirmation;
import cz.agents.agentpolis.darptestbed.siminfrastructure.communication.passenger.message.Proposal;
import cz.agents.agentpolis.darptestbed.siminfrastructure.communication.passenger.message.RequestReject;
import cz.agents.agentpolis.darptestbed.siminfrastructure.communication.passenger.receiver.PassengerReceiverVisitor;
import cz.agents.agentpolis.darptestbed.simmodel.agent.data.generator.RequestGenerator;
import cz.agents.agentpolis.darptestbed.simmodel.agent.data.generator.RequestGeneratorCall;
import cz.agents.agentpolis.darptestbed.simmodel.agent.passenger.logic.PassengerLogic;
import cz.agents.agentpolis.siminfrastructure.description.DescriptionImpl;
import cz.agents.agentpolis.simmodel.agent.Agent;
import cz.agents.agentpolis.simmodel.agent.activity.TimeSpendingActivity;
import cz.agents.agentpolis.simmodel.agent.activity.callback.TimeActivityCallback;
import cz.agents.agentpolis.simmodel.entity.EntityType;

/**
 * The life cycle of a passenger
 * 
 * @author Lukas Canda
 */
public abstract class PassengerAgent<TPassengerLogic extends PassengerLogic> extends Agent implements
		RequestGeneratorCall, TimeActivityCallback, PassengerReceiverVisitor {

	private static final Logger LOGGER = Logger.getLogger(PassengerAgent.class);

	/**
	 * My logic processes all operations with received proposals. It also sends
	 * messages to whoever needs them.
	 */
	protected final TPassengerLogic logic;
	/**
	 * A smart generator, that makes up my requests for taxis according to my
	 * profile
	 */
	private RequestGenerator requestGenerator;
	/**
	 * A set of useful methods for searching paths, distances etc.
	 */
	protected final Utils utils;
	/**
	 * The delay before starting the first activity
	 */
	private final Duration startLife;
	/**
	 * This activity helps the passenger wait for some time
	 */
	private final TimeSpendingActivity timeSpendingActivity;

	private final Set<String> passengerRequirements;

	public PassengerAgent(String agentId, EntityType agentType, TPassengerLogic logic, Utils utils, Duration startLife,
			TimeSpendingActivity timeSpendingActivity, Set<String> passengerRequirements,
			RequestGenerator requestGenerator) {

		super(agentId, agentType);

		this.logic = logic;

		this.utils = utils;
		this.startLife = startLife;
		this.timeSpendingActivity = timeSpendingActivity;
		this.passengerRequirements = passengerRequirements;

		if (GlobalParams.getRequestGeneratorType() == 1) {
			this.requestGenerator = requestGenerator;
		}
	}

	@Override
	public void timeCallback() {
		// generate a request for a ride
		requestGenerator.start(getId(), passengerRequirements, this);
	}

	@Override
	public DescriptionImpl getDescription() {
		return new DescriptionImpl();
	}

	@Override
	public void born() {
		//LOGGER.info(getId() + " starts to live at " + utils.toHoursAndMinutes(this.startLife.getMillis()));

		if (startLife.getMillis() > 0) {
			timeSpendingActivity.spendingTime(this, startLife);
		} else {
			timeCallback();
		}
	}

	@Override
	public void visit(OrderConfirmation taxiSendConfirmationToPassengerMessage) {
		logic.acceptTripInfo(taxiSendConfirmationToPassengerMessage.confirmation, passengerRequirements);
		LOGGER.debug(getId() + ":" + taxiSendConfirmationToPassengerMessage.getClass().getSimpleName() + " " +
            taxiSendConfirmationToPassengerMessage);
	}

	@Override
	public void visit(DriverArrivedMessage driverArrivedMessage) {
		logic.processVehicleArrived(driverArrivedMessage.driverId, driverArrivedMessage.confirmation.getVehicleId());
		LOGGER.debug(getId() + ":" + driverArrivedMessage.getClass().getSimpleName());

	}

	@Override
	public void visit(RequestReject requestReject) {
		//LOGGER.debug(getId() + ":" + requestReject.getClass().getSimpleName());
		logic.processRejection(requestReject);
	}

	@Override
	public void visit(Proposal proposal) {
		LOGGER.debug(getId() + ":" + proposal.getClass().getSimpleName());
		logic.processProposal(proposal);
	}

}

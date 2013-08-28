package cz.agents.agentpolis.darptestbed.simmodel.agent.driver;

import org.apache.log4j.Logger;

import cz.agents.agentpolis.darptestbed.siminfrastructure.communication.driver.message.PassengerIsInTaxiMessage;
import cz.agents.agentpolis.darptestbed.siminfrastructure.communication.driver.message.PassengerSaysTaxiIsTooLateMessage;
import cz.agents.agentpolis.darptestbed.siminfrastructure.communication.driver.receiver.DriverReceiverVisitor;
import cz.agents.agentpolis.darptestbed.simmodel.agent.driver.logic.DriverLogic;
import cz.agents.agentpolis.siminfrastructure.description.DescriptionImpl;
import cz.agents.agentpolis.simmodel.agent.Agent;
import cz.agents.agentpolis.simmodel.entity.EntityType;
import cz.agents.agentpolis.simmodel.environment.model.query.AgentPositionQuery;

/**
 * The life cycle of a taxi driver
 * 
 * @author Lukas Canda
 */
public abstract class DriverAgent<TDriverLogic extends DriverLogic> extends Agent implements DriverReceiverVisitor {

	private static final Logger LOGGER = Logger.getLogger(DriverAgent.class);

	/**
	 * My logic processes all operations with received requests including
	 * planning (if it is set to the decentralized version). It also sends
	 * messages to whoever needs them.
	 */
	protected final TDriverLogic logic;
	// /**
	// * True, if the AgentLogic is used in the centralized version, false means
	// * decentralized version.
	// */
	// private final boolean centralized;
	// /**
	// * The vehicle I'm driving
	// */
	// private final TestbedVehicle vehicle;
	/**
	 * Returns an agent's current position.
	 */
	private final AgentPositionQuery positionQuery;

	public DriverAgent(String agentId, EntityType agentType, TDriverLogic logic, AgentPositionQuery positionQuery) {

		super(agentId, agentType);

		this.logic = logic;
		// this.centralized = centralized;
		// this.vehicle = vehicle;
		this.positionQuery = positionQuery;
	}

	@Override
	public DescriptionImpl getDescription() {
		return new DescriptionImpl();
	}

	@Override
	public void born() {
		//LOGGER.info(getId() + " initial position " + positionQuery.getCurrentPositionByNodeId(getId()));
	}

	@Override
	public void visit(PassengerIsInTaxiMessage passengerIsInTaxiMessage) {
		//LOGGER.debug(getId() + ":" + passengerIsInTaxiMessage.getClass().getSimpleName());
		logic.processPassengerGotInVehicle(passengerIsInTaxiMessage.passengerId);

	}

	@Override
	public void visit(PassengerSaysTaxiIsTooLateMessage passengerSaysTaxiIsTooLate) {
		//LOGGER.debug(getId() + ":" + passengerSaysTaxiIsTooLate.getClass().getSimpleName());
		logic.processVehicleIsTooLate(passengerSaysTaxiIsTooLate.passengerId, passengerSaysTaxiIsTooLate.departure,
				passengerSaysTaxiIsTooLate.delay);

	}

}

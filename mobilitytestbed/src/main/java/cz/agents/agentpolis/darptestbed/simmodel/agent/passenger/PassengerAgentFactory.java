package cz.agents.agentpolis.darptestbed.simmodel.agent.passenger;

import java.util.Set;

import org.joda.time.Duration;

import com.google.inject.Injector;

import cz.agents.agentpolis.darptestbed.global.Utils;
import cz.agents.agentpolis.darptestbed.siminfrastructure.communication.passenger.protocol.PassengerMessageProtocol;
import cz.agents.agentpolis.darptestbed.simmodel.agent.TestbedEntityType;
import cz.agents.agentpolis.darptestbed.simmodel.agent.data.generator.RequestGenerator;
import cz.agents.agentpolis.darptestbed.simmodel.agent.passenger.logic.PassengerCentralizedLogic;
import cz.agents.agentpolis.darptestbed.simmodel.agent.passenger.logic.PassengerDecentralizedLogic;
import cz.agents.agentpolis.simmodel.agent.activity.TimeSpendingActivity;

/**
 * The factory for creating a new passenger instance
 * 
 * @author Lukas Canda
 */
public class PassengerAgentFactory {

	public PassengerCentralizedAgent createCentrAgent(String passengerId, PassengerCentralizedLogic logic, Duration startLife,
			Set<String> passengerRequirements, Injector injector, RequestGenerator requestGenerator) {

		// WalkingActivity walkingActivity =
		// injector.getInstance(WalkingActivity.class);
		Utils utils = injector.getInstance(Utils.class);
		TimeSpendingActivity timeSpendingActivity = injector.getInstance(TimeSpendingActivity.class);

		PassengerCentralizedAgent passengerAgent = new PassengerCentralizedAgent(passengerId, TestbedEntityType.PASSENGER, logic,
				utils, startLife, timeSpendingActivity, passengerRequirements, requestGenerator);

		// add the agent into message receivers table
		injector.getInstance(PassengerMessageProtocol.class).addReceiverVisitor(passengerId, passengerAgent);

		return passengerAgent;
	}

	public PassengerDecentralizedAgent createDecentrAgent(String passengerId, PassengerDecentralizedLogic logic,
			Duration startLife, Set<String> passengerRequirements, Injector injector, RequestGenerator requestGenerator) {

		// WalkingActivity walkingActivity =
		// injector.getInstance(WalkingActivity.class);
		Utils utils = injector.getInstance(Utils.class);
		TimeSpendingActivity timeSpendingActivity = injector.getInstance(TimeSpendingActivity.class);

		PassengerDecentralizedAgent passengerAgent = new PassengerDecentralizedAgent(passengerId, TestbedEntityType.PASSENGER,
				logic, utils, startLife, timeSpendingActivity, passengerRequirements, requestGenerator);

		// add the agent into message receivers table
		injector.getInstance(PassengerMessageProtocol.class).addReceiverVisitor(passengerId, passengerAgent);

		return passengerAgent;
	}

}

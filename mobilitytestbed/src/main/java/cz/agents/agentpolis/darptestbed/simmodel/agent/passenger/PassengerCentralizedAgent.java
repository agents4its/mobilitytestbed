package cz.agents.agentpolis.darptestbed.simmodel.agent.passenger;

import java.util.Set;

import org.apache.log4j.Logger;
import org.joda.time.Duration;

import cz.agents.agentpolis.darptestbed.global.Utils;
import cz.agents.agentpolis.darptestbed.simmodel.agent.data.Request;
import cz.agents.agentpolis.darptestbed.simmodel.agent.data.generator.RequestGenerator;
import cz.agents.agentpolis.darptestbed.simmodel.agent.passenger.logic.PassengerCentralizedLogic;
import cz.agents.agentpolis.simmodel.agent.activity.TimeSpendingActivity;
import cz.agents.agentpolis.simmodel.entity.EntityType;

public class PassengerCentralizedAgent extends PassengerAgent<PassengerCentralizedLogic> {

	private static final Logger LOGGER = Logger.getLogger(PassengerCentralizedAgent.class);

	public PassengerCentralizedAgent(String agentId, EntityType agentType, PassengerCentralizedLogic logic, Utils utils,
                                     Duration startLife, TimeSpendingActivity timeSpendingActivity, Set<String> passengerRequirements,
                                     RequestGenerator requestGenerator) {
		super(agentId, agentType, logic, utils, startLife, timeSpendingActivity, passengerRequirements,
				requestGenerator);
		// TODO Auto-generated constructor stub
	}

	/**
	 * This method is usually called by a request generator.
	 */
	@Override
	public void sendRequest(Request request) {
		long startTime = System.currentTimeMillis();
		logic.sendRequest(request);
		utils.logAlgRealTime(System.currentTimeMillis() - startTime);
		// TODO: LOG REQUEST
	}

}

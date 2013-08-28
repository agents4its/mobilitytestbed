package cz.agents.agentpolis.darptestbed.simmodel.agent.passenger;

import java.util.Set;

import org.apache.log4j.Logger;
import org.joda.time.Duration;

import cz.agents.agentpolis.darptestbed.global.Utils;
import cz.agents.agentpolis.darptestbed.simmodel.agent.data.Request;
import cz.agents.agentpolis.darptestbed.simmodel.agent.data.generator.RequestGenerator;
import cz.agents.agentpolis.darptestbed.simmodel.agent.passenger.logic.PassengerCentrLogic;
import cz.agents.agentpolis.simmodel.agent.activity.TimeSpendingActivity;
import cz.agents.agentpolis.simmodel.entity.EntityType;

public class PassengerCentrAgent extends PassengerAgent<PassengerCentrLogic> {

	private static final Logger LOGGER = Logger.getLogger(PassengerCentrAgent.class);

	public PassengerCentrAgent(String agentId, EntityType agentType, PassengerCentrLogic logic, Utils utils,
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
		long statrTime = System.currentTimeMillis();
		logic.sendRequest(request);
		utils.logAlgRealTime(System.currentTimeMillis() - statrTime);
		// TODO: LOG REQEST
	}

}

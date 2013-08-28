package cz.agents.agentpolis.darptestbed.simmodel.agent.passenger;

import java.util.Set;

import org.apache.log4j.Logger;
import org.joda.time.Duration;

import cz.agents.agentpolis.darptestbed.global.Utils;
import cz.agents.agentpolis.darptestbed.simmodel.agent.data.Request;
import cz.agents.agentpolis.darptestbed.simmodel.agent.data.generator.RequestGenerator;
import cz.agents.agentpolis.darptestbed.simmodel.agent.passenger.logic.PassengerDecentrLogic;
import cz.agents.agentpolis.darptestbed.simmodel.agent.timer.TimerCallback;
import cz.agents.agentpolis.simmodel.agent.activity.TimeSpendingActivity;
import cz.agents.agentpolis.simmodel.entity.EntityType;

public class PassengerDecentrAgent extends PassengerAgent<PassengerDecentrLogic> implements TimerCallback {

	private static final Logger LOGGER = Logger.getLogger(PassengerDecentrAgent.class);

	public PassengerDecentrAgent(String agentId, EntityType agentType, PassengerDecentrLogic logic, Utils utils,
			Duration startLife, TimeSpendingActivity timeSpendingActivity, Set<String> passengerRequirements,
			RequestGenerator requestGenerator) {
		super(agentId, agentType, logic, utils, startLife, timeSpendingActivity, passengerRequirements,
				requestGenerator);
		// TODO Auto-generated constructor stub
	}

	/**
	 * If the decentralized communication is used, this method is usually called
	 * by a timer at regular intervals.
	 */
	@Override
	public void timerCallback() {

		logic.processProposals();
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

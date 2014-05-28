package cz.agents.agentpolis.darptestbed.simmodel.agent.data.generator;

import java.util.List;
import java.util.Random;
import java.util.Set;

import cz.agents.agentpolis.darptestbed.global.GeneratorParams;
import org.joda.time.Duration;

import cz.agents.agentpolis.darptestbed.global.GlobalParams;
import cz.agents.agentpolis.darptestbed.global.Utils;
import cz.agents.agentpolis.darptestbed.simmodel.agent.data.Request;
import cz.agents.agentpolis.darptestbed.simmodel.agent.data.TimeWindow;
import cz.agents.agentpolis.darptestbed.simmodel.agent.passenger.PassengerProfile;
import cz.agents.agentpolis.simmodel.environment.model.query.AgentPositionQuery;

/**
 * A simple generator, that doesn't care about the passenger's profile and just
 * generates requests to any place on the map.
 * 
 * @author Lukas Canda
 */
public class RandomRequestGenerator extends ARequestGenerator {

	/**
	 * A set of useful methods for searching paths, distances etc.
	 */
	protected final Utils utils;
	protected final Duration passengerStartLife;
	private final List<Long> allNetworkNodes;

	public RandomRequestGenerator(PassengerProfile passengerProfile, AgentPositionQuery positionQuery,
			List<Long> allNetworkNodes, Utils utils, Duration passengerStartLife) {

		super(passengerProfile, positionQuery);
		this.utils = utils;
		this.passengerStartLife = passengerStartLife;
		this.allNetworkNodes = allNetworkNodes;
	}

	@Override
	public void start(String passengerId, Set<String> additionalRequirements, RequestGeneratorCall requestGeneratorCall) {

		Random random = GlobalParams.getRandom();
		Long initialPosition = positionQuery.getCurrentPositionByNodeId(passengerId);
		Long targetPosition = null;

		// generate random target position
		do {
			targetPosition = allNetworkNodes.get(random.nextInt(allNetworkNodes.size()));
		} while (initialPosition == targetPosition);

		// prepare the request to be send
		Request reqNew = null;

		if (GeneratorParams.isTimeWindowsUsed()) {
			int intPassengerStartLife = (int) passengerStartLife.getMillis();
			long minDeparture = intPassengerStartLife + GeneratorParams.getEarliestDepartureShift() * 60000;
			// an alternative - random earliest departure
			// long minDeparture = intPassengerStartLife +
			// GlobalParams.getRandom()
			// .nextInt(GlobalParams.getEarliestDepartureShift() + 1);

			long timeToDrive = utils
					.computeDrivingTime(initialPosition, targetPosition, GlobalParams.getVelocityInKmph());
			long timeWindowLength = (long) (timeToDrive * GeneratorParams.getTimeWinRelSize());

			TimeWindow timeWindow = new TimeWindow(minDeparture, minDeparture + timeWindowLength - timeToDrive,
					minDeparture + timeToDrive, minDeparture + timeWindowLength);

			reqNew = new Request(passengerId, initialPosition, targetPosition, timeWindow, additionalRequirements);
		} else {
			reqNew = new Request(passengerId, initialPosition, targetPosition, additionalRequirements);
		}

		requestGeneratorCall.sendRequest(reqNew);
	}

}

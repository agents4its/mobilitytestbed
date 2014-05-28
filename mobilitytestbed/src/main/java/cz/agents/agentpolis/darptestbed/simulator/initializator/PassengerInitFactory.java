package cz.agents.agentpolis.darptestbed.simulator.initializator;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import cz.agents.agentpolis.darptestbed.global.GeneratorParams;
import cz.agents.agentpolis.darptestbed.siminfrastructure.communication.protocol.GeneralMessageProtocol;
import cz.agents.agentpolis.darptestbed.simmodel.agent.passenger.logic.PassengerLogicWithRequestConsumerMessageProtocol;
import org.apache.log4j.Logger;
import org.joda.time.Duration;
import org.openstreetmap.osm.data.coordinates.LatLon;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.inject.Injector;

import cz.agents.agentpolis.darptestbed.global.GlobalParams;
import cz.agents.agentpolis.darptestbed.global.Utils;
import cz.agents.agentpolis.darptestbed.siminfrastructure.communication.driver.protocol.DriverMessageProtocol;
import cz.agents.agentpolis.darptestbed.siminfrastructure.communication.requestconsumer.protocol.RequestConsumerMessageProtocol;
import cz.agents.agentpolis.darptestbed.siminfrastructure.logger.RequestLogger;
import cz.agents.agentpolis.darptestbed.simmodel.agent.activity.movement.TestbedPassengerActivity;
import cz.agents.agentpolis.darptestbed.simmodel.agent.data.generator.RandomRequestGenerator;
import cz.agents.agentpolis.darptestbed.simmodel.agent.passenger.PassengerAgent;
import cz.agents.agentpolis.darptestbed.simmodel.agent.passenger.PassengerAgentFactory;
import cz.agents.agentpolis.darptestbed.simmodel.agent.passenger.PassengerDecentralizedAgent;
import cz.agents.agentpolis.darptestbed.simmodel.agent.passenger.PassengerProfile;
import cz.agents.agentpolis.darptestbed.simmodel.agent.passenger.logic.PassengerCentralizedLogic;
import cz.agents.agentpolis.darptestbed.simmodel.agent.passenger.logic.PassengerDecentralizedLogic;
import cz.agents.agentpolis.darptestbed.simmodel.agent.passenger.logic.PassengerLogic;
import cz.agents.agentpolis.darptestbed.simmodel.agent.timer.Timer;
import cz.agents.agentpolis.darptestbed.simmodel.environment.model.TestbedModel;
import cz.agents.agentpolis.ondemandtransport.simulator.initializator.TaxiPassengerInit;
import cz.agents.agentpolis.simmodel.agent.Agent;
import cz.agents.agentpolis.simmodel.agent.activity.TimeSpendingActivity;
import cz.agents.agentpolis.simmodel.environment.model.AgentPositionModel;
import cz.agents.agentpolis.simmodel.environment.model.citymodel.transportnetwork.AllNetworkNodes;
import cz.agents.agentpolis.simmodel.environment.model.citymodel.transportnetwork.elemets.Node;
import cz.agents.agentpolis.simmodel.environment.model.citymodel.transportnetwork.elemets.highway.HighwayNetwork;
import cz.agents.agentpolis.simmodel.environment.model.citymodel.transportnetwork.elemets.highway.HighwayNode;
import cz.agents.agentpolis.simmodel.environment.model.query.AgentPositionQuery;
import cz.agents.agentpolis.simulator.creator.initializator.AgentInitFactory;

/**
 * Initiates all passengers in the simulation.
 * 
 * @author Lukas Canda
 */
@Deprecated
public class PassengerInitFactory implements AgentInitFactory {

	private static final Logger logger = Logger.getLogger(TaxiPassengerInit.class);

    private final LogicConstructor logicConstructor;

    public PassengerInitFactory(LogicConstructor logicConstructor) {
        super();
        this.logicConstructor = logicConstructor;
    }


	/**
	 * It is implementations method from IAgentInit.
	 */
	public List<Agent> initAllAgentLifeCycles(Injector injector) {

		List<Agent> agents = new ArrayList<Agent>();
		PassengerAgentFactory factory = new PassengerAgentFactory();
		Random random = GlobalParams.getRandom();
		// List<Long> possibleNodes = new
		// ArrayList<Long>(injector.getInstance(HighwayNetwork.class).getNetwork()).;
		List<Long> possibleNodes = Lists.transform(
				Lists.newArrayList(injector.getInstance(HighwayNetwork.class).getNetwork().getAllNodes()),
				new Function<HighwayNode, Long>() {

					@Override
					public Long apply(HighwayNode input) {
						return input.getId();
					}
				});

		Timer passTimer = injector.getInstance(TestbedModel.class).getPassengersTimer();
		int randomBound = possibleNodes.size();

		// get ready for creating a logic (I couldn't figure out any better
		// place to hide this code)
		TestbedModel taxiModel = injector.getInstance(TestbedModel.class);
		AgentPositionQuery positionQuery = injector.getInstance(AgentPositionQuery.class);
		Utils utils = injector.getInstance(Utils.class);

		int numberOfPassengers = GeneratorParams.getNumberOfPassengers();
		int maxStartLife = GeneratorParams.getMaxPassengerStartLifeTime();

  		randomBound = possibleNodes.size();

		// create passengers
		for (int i = 0; i < numberOfPassengers; i++) {
			Duration startLife = null;
			if (maxStartLife <= 0) {
				startLife = Duration.standardMinutes(0);
			} else {
				startLife = Duration.standardMinutes(random.nextInt(maxStartLife) + 1);
			}
			// create agents and their logics
			String agentId = "Passenger" + i;
			PassengerProfile profile = new PassengerProfile();
			TimeSpendingActivity timeActivity = injector.getInstance(TimeSpendingActivity.class);
			TestbedPassengerActivity passengerActivity = injector.getInstance(TestbedPassengerActivity.class);
			RequestLogger logger = injector.getInstance(RequestLogger.class);

			RandomRequestGenerator requestGenerator = new RandomRequestGenerator(profile, positionQuery, possibleNodes,
					utils, startLife);

			DriverMessageProtocol driverMessageProtocol = injector.getInstance(DriverMessageProtocol.class);

            GeneralMessageProtocol generalMessageProtocol = injector.getInstance(GeneralMessageProtocol.class);

			PassengerAgent<? extends PassengerLogic> passenger;
            RequestConsumerMessageProtocol sender = injector.getInstance(RequestConsumerMessageProtocol.class);
            PassengerLogicWithRequestConsumerMessageProtocol logic =
                    logicConstructor.constructPassengerLogic(
                            agentId, sender, driverMessageProtocol, generalMessageProtocol, taxiModel,
                            positionQuery, utils, profile, passengerActivity, timeActivity, logger);
            if (!logic.isDecentralized()) {
				passenger = factory.createCentrAgent(agentId, (PassengerCentralizedLogic) logic, startLife,
                        Sets.<String> newHashSet(), injector, requestGenerator);
			} else {
				PassengerDecentralizedAgent passengerDecentralizedAgent =
                        factory.createDecentrAgent(agentId, (PassengerDecentralizedLogic) logic, startLife,
                                Sets.<String>newHashSet(), injector, requestGenerator);
				passTimer.addCallback(passengerDecentralizedAgent);

				passenger = passengerDecentralizedAgent;
			}

			long initialLocation = new Long((Long) possibleNodes.get(random.nextInt(randomBound)));

			// chose home node
			injector.getInstance(AgentPositionModel.class).setNewEntityPosition(passenger.getId(), initialLocation);
			injector.getInstance(TestbedModel.class).addPassenger(passenger.getId());

			agents.add(passenger);

		}

		logger.info("Created passengers");
		return agents;

	}

}

package cz.agents.agentpolis.darptestbed.simulator.initializator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.log4j.Logger;

import com.google.common.collect.Lists;
import com.google.inject.Injector;

import cz.agents.agentpolis.darptestbed.global.GlobalParams;
import cz.agents.agentpolis.darptestbed.global.Utils;
import cz.agents.agentpolis.darptestbed.siminfrastructure.communication.passenger.protocol.PassengerMessageProtocol;
import cz.agents.agentpolis.darptestbed.siminfrastructure.logger.VehicleMoveLogger;
import cz.agents.agentpolis.darptestbed.simmodel.agent.driver.DecentDriverAgent;
import cz.agents.agentpolis.darptestbed.simmodel.agent.driver.DriverAgentFactory;
import cz.agents.agentpolis.darptestbed.simmodel.agent.driver.logic.DriverCentrLogic;
import cz.agents.agentpolis.darptestbed.simmodel.agent.driver.logic.DriverDecentrLogic;
import cz.agents.agentpolis.darptestbed.simmodel.agent.driver.logic.DriverDecentrLogicExample;
import cz.agents.agentpolis.darptestbed.simmodel.agent.exception.WrongSettingsException;
import cz.agents.agentpolis.darptestbed.simmodel.agent.timer.Timer;
import cz.agents.agentpolis.darptestbed.simmodel.entity.vehicle.TestbedVehicle;
import cz.agents.agentpolis.darptestbed.simmodel.environment.model.TestbedModel;
import cz.agents.agentpolis.darptestbed.simmodel.environment.model.TestbedVehicleStorage;
import cz.agents.agentpolis.simmodel.agent.Agent;
import cz.agents.agentpolis.simmodel.agent.activity.movement.DriveVehicleActivity;
import cz.agents.agentpolis.simmodel.entity.vehicle.VehicleType;
import cz.agents.agentpolis.simmodel.environment.model.AgentPositionModel;
import cz.agents.agentpolis.simmodel.environment.model.VehiclePositionModel;
import cz.agents.agentpolis.simmodel.environment.model.VehicleStorage;
import cz.agents.agentpolis.simmodel.environment.model.citymodel.transportnetwork.AllNetworkNodes;
import cz.agents.agentpolis.simmodel.environment.model.citymodel.transportnetwork.EGraphType;
import cz.agents.agentpolis.simmodel.environment.model.citymodel.transportnetwork.elemets.highway.HighwayNetwork;
import cz.agents.agentpolis.simmodel.environment.model.citymodel.transportnetwork.elemets.highway.HighwayNode;
import cz.agents.agentpolis.simmodel.environment.model.entityvelocitymodel.EntityVelocityModel;
import cz.agents.agentpolis.simmodel.environment.model.query.AgentPositionQuery;
import cz.agents.agentpolis.simulator.creator.initializator.AgentInitFactory;
import cz.agents.agentpolis.utils.convertor.VelocityConvertor;

/**
 * Initiates all taxi drivers in the simulation.
 * 
 * @author Lukas Canda
 */
@Deprecated
public class DriverInitFactory implements AgentInitFactory {

	private static final Logger logger = Logger.getLogger(DriverInitFactory.class);

	@Override
	public List<Agent> initAllAgentLifeCycles(Injector injector) {
		Random random = GlobalParams.getRandom();
		// all nodes in the map
		List<HighwayNode> possibleNodes = Lists.newArrayList(injector.getInstance(HighwayNetwork.class).getNetwork()
				.getAllNodes());
		Timer taxiDriversTimer = injector.getInstance(TestbedModel.class).getTaxiDriversTimer();
		int randomBound = possibleNodes.size();

		boolean centralized = GlobalParams.isCentralized();

		// get ready for creating a logic (I couldn't figure out any better
		// place to hide this code)
		TestbedModel taxiModel = injector.getInstance(TestbedModel.class);
		AgentPositionQuery positionQuery = injector.getInstance(AgentPositionQuery.class);
		AllNetworkNodes allNetworkNodes = injector.getInstance(AllNetworkNodes.class);
		Utils utils = injector.getInstance(Utils.class);

		// Integer startLifeMin = 2;
		// Integer startLifeMax = 8;

		Map<Integer, Integer> seatsDistribution = new HashMap<Integer, Integer>();
		seatsDistribution.put(5, GlobalParams.getNumberOfFiveSeatVehicles());
		seatsDistribution.put(6, GlobalParams.getNumberOfSixSeatVehicles());
		seatsDistribution.put(7, GlobalParams.getNumberOfSevenSeatVehicles());

		List<Agent> agents = new ArrayList<Agent>();
		DriverAgentFactory factory = new DriverAgentFactory();

		double velocityOfVehicle = VelocityConvertor.kmph2mps(GlobalParams.getVelocityInKmph());

		Iterator<Integer> iterator = seatsDistribution.keySet().iterator();

		int cntDrivers = 0;
		// place all taxi vehicles with given seat quantities
		while (iterator.hasNext()) {
			int seatsQuantity = iterator.next();

			for (int i = 0; i < seatsDistribution.get(seatsQuantity); i++) {
				TestbedVehicle vehicle = new TestbedVehicle("Taxi" + cntDrivers, VehicleType.CAR, 5.0, seatsQuantity,
						EGraphType.HIGHWAY, new HashSet<String>());
				long initialLocation = possibleNodes.get(random.nextInt(randomBound)).getId();

				injector.getInstance(VehicleStorage.class).addEntity(vehicle);
				injector.getInstance(TestbedVehicleStorage.class).addEntity(vehicle);
				injector.getInstance(VehiclePositionModel.class).setNewEntityPosition(vehicle.getId(), initialLocation);
				injector.getInstance(EntityVelocityModel.class)
						.addEntityMaxVelocity(vehicle.getId(), velocityOfVehicle);

				String agentId = "TaxiDriver" + cntDrivers;
				DriveVehicleActivity drivingActivity = injector.getInstance(DriveVehicleActivity.class);

				Agent driver = null;
				if (centralized) {
					// centralized algorithms
					DriverCentrLogic logic = null;
					PassengerMessageProtocol sender = injector.getInstance(PassengerMessageProtocol.class);

					switch (GlobalParams.getCentralAlgType()) {
					//case 1:
					//case 2:
					//case 3:
					//	logic = new DriverCentrLogic(agentId, sender, taxiModel, positionQuery, allNetworkNodes, utils,
					//			vehicle, drivingActivity);
					//	break;
					//case 4:
					//	logic = new DriverCentrLogicWithDiv(agentId, sender, taxiModel, positionQuery, allNetworkNodes,
					//			utils, vehicle, drivingActivity);
					//	break;
					default:
						logic = new DriverCentrLogic(agentId, sender, taxiModel, positionQuery, allNetworkNodes, utils,
								vehicle, drivingActivity);
						//throw new WrongSettingsException(
						//		"The parameter \"Central alg type\" has a wrong value assigned");
					}

					driver = factory.createCentrDriverAgent(agentId, logic, injector);

				} else {
					// decentralized algorithms
					DriverDecentrLogic logic = null;
					PassengerMessageProtocol sender = injector.getInstance(PassengerMessageProtocol.class);

					switch (GlobalParams.getDecentrAlgType()) {
					//case 1:
					//	logic = new DriverDecentrLogicAccAllNoDiv(agentId, sender, taxiModel, positionQuery,
					//			allNetworkNodes, utils, vehicle, drivingActivity);
					//	break;
					//case 2:
					//	logic = new DriverDecentrLogicAccAllWithDiv(agentId, sender, taxiModel, positionQuery,
					//			allNetworkNodes, utils, vehicle, drivingActivity);
					//	break;
					//case 3:
					//	logic = new DriverDecentrLogicPriceWithDiv(agentId, sender, taxiModel, positionQuery,
					//			allNetworkNodes, utils, vehicle, drivingActivity);
					//	break;
					//case 50:
					default:
						logic = new DriverDecentrLogicExample(agentId, sender, taxiModel, positionQuery,
									allNetworkNodes, utils, vehicle, drivingActivity);
						break;
						//throw new WrongSettingsException(
						//		"The parameter \"Decentr alg type\" has a wrong value assigned");
					}

					DecentDriverAgent decentDriverAgent = factory.createDecentDriverAgent(agentId, logic, injector);
					taxiDriversTimer.addCallback(decentDriverAgent);

					driver = decentDriverAgent;
				}

				injector.getInstance(AgentPositionModel.class).setNewEntityPosition(driver.getId(), initialLocation);
				injector.getInstance(TestbedModel.class).addFreeTaxi(vehicle.getId(), driver.getId());
				// injector.getInstance(StatisticsLogger.class).addVehicleForSensor(driver.getId());
				// init position log
				injector.getInstance(VehicleMoveLogger.class).logVehicleMove(vehicle.getId(), initialLocation);

				agents.add(driver);

				cntDrivers++;
			}
		}

		logger.info("Taxi drivers have been created");
		return agents;
	}

}

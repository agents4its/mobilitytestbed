package cz.agents.agentpolis.darptestbed.simulator.initializator;

import cz.agents.agentpolis.darptestbed.global.Utils;
import cz.agents.agentpolis.darptestbed.siminfrastructure.communication.dispatching.protocol.DispatchingMessageProtocol;
import cz.agents.agentpolis.darptestbed.siminfrastructure.communication.driver.protocol.DriverCentralizedMessageProtocol;
import cz.agents.agentpolis.darptestbed.siminfrastructure.communication.driver.protocol.DriverMessageProtocol;
import cz.agents.agentpolis.darptestbed.siminfrastructure.communication.passenger.protocol.PassengerMessageProtocol;
import cz.agents.agentpolis.darptestbed.siminfrastructure.communication.requestconsumer.protocol.RequestConsumerMessageProtocol;
import cz.agents.agentpolis.darptestbed.siminfrastructure.logger.RequestLogger;
import cz.agents.agentpolis.darptestbed.siminfrastructure.planner.TestbedPlanner;
import cz.agents.agentpolis.darptestbed.simmodel.agent.activity.movement.TestbedPassengerActivity;
import cz.agents.agentpolis.darptestbed.simmodel.agent.dispatching.logic.DispatchingLogic;
import cz.agents.agentpolis.darptestbed.simmodel.agent.driver.logic.DriverCentralizedLogic;
import cz.agents.agentpolis.darptestbed.simmodel.agent.driver.logic.DriverDecentralizedLogic;
import cz.agents.agentpolis.darptestbed.simmodel.agent.passenger.PassengerProfile;
import cz.agents.agentpolis.darptestbed.simmodel.agent.passenger.logic.PassengerCentralizedLogic;
import cz.agents.agentpolis.darptestbed.simmodel.agent.passenger.logic.PassengerDecentralizedLogic;
import cz.agents.agentpolis.darptestbed.simmodel.entity.vehicle.TestbedVehicle;
import cz.agents.agentpolis.darptestbed.simmodel.environment.model.TestbedModel;
import cz.agents.agentpolis.darptestbed.simmodel.environment.model.TestbedVehicleStorage;
import cz.agents.agentpolis.simmodel.agent.activity.TimeSpendingActivity;
import cz.agents.agentpolis.simmodel.agent.activity.movement.DriveVehicleActivity;
import cz.agents.agentpolis.simmodel.environment.model.citymodel.transportnetwork.AllNetworkNodes;
import cz.agents.agentpolis.simmodel.environment.model.query.AgentPositionQuery;

public interface LogicConstructor {
    PassengerDecentralizedLogic constructDecentralizedPassengerLogic(String agentId,
                                                                     RequestConsumerMessageProtocol sender, DriverMessageProtocol driverMessageProtocol,
                                                                     TestbedModel taxiModel, AgentPositionQuery positionQuery, Utils utils, PassengerProfile passengerProfile,
                                                                     TestbedPassengerActivity passengerActivity, TimeSpendingActivity timeSpendingActivity, RequestLogger logger);

    PassengerCentralizedLogic constructCentralizedPassengerLogic(String agentId,
                                                                 RequestConsumerMessageProtocol sender, DriverMessageProtocol driverMessageProtocol,
                                                                 TestbedModel taxiModel, AgentPositionQuery positionQuery, Utils utils, PassengerProfile passengerProfile,
                                                                 TestbedPassengerActivity passengerActivity, TimeSpendingActivity timeSpendingActivity, RequestLogger logger);

    DriverDecentralizedLogic constructDecentralizedDriverLogic(String agentId, PassengerMessageProtocol sender, TestbedModel taxiModel,
                                                               AgentPositionQuery positionQuery, AllNetworkNodes allNetworkNodes, Utils utils, TestbedVehicle vehicle,
                                                               DriveVehicleActivity drivingActivity);

    DriverCentralizedLogic constructCentralizedDriverLogic(String agentId, PassengerMessageProtocol sender, TestbedModel taxiModel,
                                                           AgentPositionQuery positionQuery, AllNetworkNodes allNetworkNodes, Utils utils, TestbedVehicle vehicle,
                                                           DriveVehicleActivity drivingActivity,
                                                           DispatchingMessageProtocol dispatchingMessageProtocol);

    DispatchingLogic constructDispatchingLogic(String dispatching, PassengerMessageProtocol sender, DriverCentralizedMessageProtocol driverCentralizedMessageProtocol, TestbedModel taxiModel, AgentPositionQuery positionQuery, AllNetworkNodes allNetworkNodes, Utils utils, TestbedPlanner pathPlanner, TestbedVehicleStorage vehicleStorage);
}

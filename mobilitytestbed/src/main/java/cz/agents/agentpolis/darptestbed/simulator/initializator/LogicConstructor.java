package cz.agents.agentpolis.darptestbed.simulator.initializator;

import com.google.inject.Injector;
import cz.agents.agentpolis.darptestbed.global.Utils;
import cz.agents.agentpolis.darptestbed.siminfrastructure.communication.driver.protocol.DriverCentralizedMessageProtocol;
import cz.agents.agentpolis.darptestbed.siminfrastructure.communication.driver.protocol.DriverMessageProtocol;
import cz.agents.agentpolis.darptestbed.siminfrastructure.communication.passenger.protocol.PassengerMessageProtocol;
import cz.agents.agentpolis.darptestbed.siminfrastructure.communication.protocol.GeneralMessageProtocol;
import cz.agents.agentpolis.darptestbed.siminfrastructure.communication.requestconsumer.protocol.RequestConsumerMessageProtocol;
import cz.agents.agentpolis.darptestbed.siminfrastructure.logger.RequestLogger;
import cz.agents.agentpolis.darptestbed.siminfrastructure.planner.TestbedPlanner;
import cz.agents.agentpolis.darptestbed.simmodel.agent.activity.movement.TestbedPassengerActivity;
import cz.agents.agentpolis.darptestbed.simmodel.agent.dispatching.logic.DispatchingLogic;
import cz.agents.agentpolis.darptestbed.simmodel.agent.driver.logic.DriverLogicWithPassengerMessageProtocol;
import cz.agents.agentpolis.darptestbed.simmodel.agent.passenger.PassengerProfile;
import cz.agents.agentpolis.darptestbed.simmodel.agent.passenger.logic.PassengerLogicWithRequestConsumerMessageProtocol;
import cz.agents.agentpolis.darptestbed.simmodel.entity.vehicle.TestbedVehicle;
import cz.agents.agentpolis.darptestbed.simmodel.environment.model.TestbedModel;
import cz.agents.agentpolis.darptestbed.simmodel.environment.model.TestbedVehicleStorage;
import cz.agents.agentpolis.simmodel.agent.activity.TimeSpendingActivity;
import cz.agents.agentpolis.simmodel.agent.activity.movement.DriveVehicleActivity;
import cz.agents.agentpolis.simmodel.environment.model.citymodel.transportnetwork.AllNetworkNodes;
import cz.agents.agentpolis.simmodel.environment.model.query.AgentPositionQuery;

/**
 * LogicConstructor interface prescribes the methods for constructing agents' logic
 * (i.e., passenger, driver, dispatching logic).
 *
 * <p>An implementation of this interface should define how the logic is constructed.
 * Possibly not all of the participants need a logic to be implemented, for example, in case of decentralized mechanism,
 * dispatcher's logic does not need to be implemented (it is recommended to implement by throwing an exception). </p>
 */
public interface LogicConstructor {
    /**
     * Method to construct passenger's logic.
     *
     *   <p>
     *      There are provide implementations:
     *      {@link cz.agents.agentpolis.darptestbed.simmodel.agent.passenger.logic.PassengerCentralizedLogic}
     *      and {@link cz.agents.agentpolis.darptestbed.simmodel.agent.passenger.logic.PassengerDecentralizedLogic}
     *      that can be extended for desired purposes.
     *   </p>
     *
     * @param agentId                        id of related agent for who the logic is implemented
     * @param sender
     * @param driverMessageProtocol
     * @param generalMessageProtocol
     * @param taxiModel
     * @param positionQuery
     * @param utils
     * @param passengerProfile
     * @param passengerActivity
     * @param timeSpendingActivity
     * @param logger
     * @return                               passenger logic implementation
     *
     */
    PassengerLogicWithRequestConsumerMessageProtocol
    constructPassengerLogic(String agentId,
                            RequestConsumerMessageProtocol sender,
                            DriverMessageProtocol driverMessageProtocol,
                            GeneralMessageProtocol
                                    generalMessageProtocol,
                            TestbedModel taxiModel,
                            AgentPositionQuery positionQuery, Utils utils,
                            PassengerProfile passengerProfile,
                            TestbedPassengerActivity passengerActivity,
                            TimeSpendingActivity timeSpendingActivity,
                            RequestLogger logger);

    /**
     *
     * @return whether to use dispatching (dispatching agent to be created)
     */
    boolean usesDispatching();

    /**
     *       Method to construct dispatching logic.
     *
     * @param dispatching
     * @param sender
     * @param driverCentralizedMessageProtocol
     * @param generalMessageProtocol
     * @param taxiModel
     * @param positionQuery
     * @param allNetworkNodes
     * @param utils
     * @param pathPlanner
     * @param vehicleStorage
     * @return  dispatching logic
     *
     * @see cz.agents.agentpolis.darptestbed.simmodel.agent.dispatching.logic.DispatchingLogic
     */
    DispatchingLogic constructDispatchingLogic(String dispatching, PassengerMessageProtocol sender,
                                               DriverCentralizedMessageProtocol driverCentralizedMessageProtocol,
                                               GeneralMessageProtocol generalMessageProtocol,
                                               TestbedModel taxiModel, AgentPositionQuery positionQuery,
                                               AllNetworkNodes allNetworkNodes, Utils utils, TestbedPlanner pathPlanner,
                                               TestbedVehicleStorage vehicleStorage);

    /**
     * Method to construct driver's logic.
     *
     *   <p>Driver logic can is implemented by
     *   {@link cz.agents.agentpolis.darptestbed.simmodel.agent.driver.logic.DriverDecentralizedLogic}
     *   and {@link cz.agents.agentpolis.darptestbed.simmodel.agent.driver.logic.DriverCentralizedLogic},
     *   these implementations can be extended for desired purposes.
     *   </p>
     *
     * @param agentId
     * @param sender
     * @param generalMessageProtocol
     * @param taxiModel
     * @param positionQuery
     * @param allNetworkNodes
     * @param utils
     * @param vehicle
     * @param drivingActivity
     * @param injector
     * @return driver's logic implementation
     */
    DriverLogicWithPassengerMessageProtocol
    constructDriverLogic(String agentId, PassengerMessageProtocol sender,
                         GeneralMessageProtocol
                                 generalMessageProtocol,
                         TestbedModel taxiModel,
                         AgentPositionQuery positionQuery,
                         AllNetworkNodes allNetworkNodes, Utils utils,
                         TestbedVehicle vehicle,
                         DriveVehicleActivity drivingActivity,
                         Injector injector);
}

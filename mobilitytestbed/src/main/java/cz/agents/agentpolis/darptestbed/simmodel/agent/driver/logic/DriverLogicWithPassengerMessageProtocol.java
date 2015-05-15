package cz.agents.agentpolis.darptestbed.simmodel.agent.driver.logic;

import cz.agents.agentpolis.darptestbed.global.Utils;
import cz.agents.agentpolis.darptestbed.siminfrastructure.communication.passenger.protocol.PassengerMessageProtocol;
import cz.agents.agentpolis.darptestbed.siminfrastructure.communication.protocol.GeneralMessageProtocol;
import cz.agents.agentpolis.darptestbed.simmodel.entity.vehicle.TestbedVehicle;
import cz.agents.agentpolis.darptestbed.simmodel.environment.model.TestbedModel;
import cz.agents.agentpolis.simmodel.agent.activity.movement.DriveVehicleActivity;
import cz.agents.agentpolis.simmodel.environment.model.citymodel.transportnetwork.AllNetworkNodes;
import cz.agents.agentpolis.simmodel.environment.model.query.AgentPositionQuery;


/**
 * Check {@link cz.agents.agentpolis.darptestbed.simmodel.agent.driver.logic.DriverLogic}
 * to see description of methods.
 *
 * For particular implementation see:
 * {@link cz.agents.agentpolis.darptestbed.simmodel.agent.driver.logic.DriverCentralizedLogic}
 * and
 * {@link cz.agents.agentpolis.darptestbed.simmodel.agent.driver.logic.DriverDecentralizedLogic}
 */
public abstract class DriverLogicWithPassengerMessageProtocol extends DriverLogic<PassengerMessageProtocol> {
    public DriverLogicWithPassengerMessageProtocol(String agentId, PassengerMessageProtocol sender,
                                                   GeneralMessageProtocol generalMessageProtocol,
                                                   TestbedModel serviceModel, AgentPositionQuery positionQuery,
                                                   AllNetworkNodes allNetworkNodes, Utils utils,
                                                   TestbedVehicle vehicle, DriveVehicleActivity drivingActivity) {
        super(agentId, sender, generalMessageProtocol, serviceModel, positionQuery, allNetworkNodes, utils, vehicle,
                drivingActivity);
    }

    /**
     * It is used to determine agent's implementation, whether to use {@link cz.agents.agentpolis.darptestbed.simmodel.agent.driver.DriverCentralizedAgent}
     * or {@link cz.agents.agentpolis.darptestbed.simmodel.agent.driver.DriverDecentralizedAgent}.
     *
     * For more details see {@link cz.agents.agentpolis.darptestbed.simmodel.agent.driver.DriverAgentFactory}.
     *
     * @return whether the logic is decentralized or centralized
     */
    public abstract boolean isDecentralized();
}

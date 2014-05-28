package cz.agents.agentpolis.darptestbed.simmodel.agent.driver.logic;

import cz.agents.agentpolis.darptestbed.global.Utils;
import cz.agents.agentpolis.darptestbed.siminfrastructure.communication.passenger.protocol.PassengerMessageProtocol;
import cz.agents.agentpolis.darptestbed.simmodel.entity.vehicle.TestbedVehicle;
import cz.agents.agentpolis.darptestbed.simmodel.environment.model.TestbedModel;
import cz.agents.agentpolis.simmodel.agent.activity.movement.DriveVehicleActivity;
import cz.agents.agentpolis.simmodel.environment.model.citymodel.transportnetwork.AllNetworkNodes;
import cz.agents.agentpolis.simmodel.environment.model.query.AgentPositionQuery;

public abstract class DriverLogicWithPassengerMessageProtocol extends DriverLogic<PassengerMessageProtocol> {
    public DriverLogicWithPassengerMessageProtocol(String agentId, PassengerMessageProtocol sender, TestbedModel serviceModel, AgentPositionQuery positionQuery, AllNetworkNodes allNetworkNodes, Utils utils, TestbedVehicle vehicle, DriveVehicleActivity drivingActivity) {
        super(agentId, sender, serviceModel, positionQuery, allNetworkNodes, utils, vehicle, drivingActivity);
    }

    public abstract boolean isDecentralized();
}

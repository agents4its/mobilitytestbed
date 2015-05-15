package cz.agents.agentpolis.darptestbed.simmodel.agent.passenger.logic;

import cz.agents.agentpolis.darptestbed.global.Utils;
import cz.agents.agentpolis.darptestbed.siminfrastructure.communication.driver.protocol.DriverMessageProtocol;
import cz.agents.agentpolis.darptestbed.siminfrastructure.communication.protocol.GeneralMessageProtocol;
import cz.agents.agentpolis.darptestbed.siminfrastructure.communication.requestconsumer.protocol.RequestConsumerMessageProtocol;
import cz.agents.agentpolis.darptestbed.siminfrastructure.logger.RequestLogger;
import cz.agents.agentpolis.darptestbed.simmodel.agent.activity.movement.TestbedPassengerActivity;
import cz.agents.agentpolis.darptestbed.simmodel.agent.passenger.PassengerProfile;
import cz.agents.agentpolis.darptestbed.simmodel.environment.model.TestbedModel;
import cz.agents.agentpolis.simmodel.agent.activity.TimeSpendingActivity;
import cz.agents.agentpolis.simmodel.environment.model.query.AgentPositionQuery;

/**
 * Check {@link cz.agents.agentpolis.darptestbed.simmodel.agent.passenger.logic.PassengerLogic}
 * to see description of methods.
 * <p/>
 * For particular implementation see:
 * {@link cz.agents.agentpolis.darptestbed.simmodel.agent.passenger.logic.PassengerCentralizedLogic}
 * and
 * {@link cz.agents.agentpolis.darptestbed.simmodel.agent.passenger.logic.PassengerDecentralizedLogic}
 */
public abstract class PassengerLogicWithRequestConsumerMessageProtocol extends
        PassengerLogic<RequestConsumerMessageProtocol> {

    public PassengerLogicWithRequestConsumerMessageProtocol(String agentId, RequestConsumerMessageProtocol sender,
                                                            DriverMessageProtocol driverCentralizedMessageProtocol,
                                                            GeneralMessageProtocol generalMessageProtocol,
                                                            TestbedModel taxiModel, AgentPositionQuery positionQuery,
                                                            Utils utils, PassengerProfile passengerProfile,
                                                            TestbedPassengerActivity passengerActivity,
                                                            TimeSpendingActivity timeSpendingActivity,
                                                            RequestLogger logger) {
        super(agentId, sender, driverCentralizedMessageProtocol, generalMessageProtocol, taxiModel, positionQuery, utils,
                passengerProfile, passengerActivity, timeSpendingActivity, logger);
    }

    /**
     * It is used to determine agent's implementation, whether to use {@link cz.agents.agentpolis.darptestbed.simmodel.agent.passenger.PassengerCentralizedAgent}
     * or {@link cz.agents.agentpolis.darptestbed.simmodel.agent.passenger.PassengerDecentralizedAgent}.
     * <p/>
     * For more details see {@link cz.agents.agentpolis.darptestbed.simmodel.agent.passenger.PassengerAgentFactory}.
     *
     * @return whether the logic is decentralized or centralized
     */
    public abstract boolean isDecentralized();
}

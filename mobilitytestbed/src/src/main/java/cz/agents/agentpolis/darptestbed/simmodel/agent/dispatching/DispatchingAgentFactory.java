package cz.agents.agentpolis.darptestbed.simmodel.agent.dispatching;

import com.google.inject.Injector;
import cz.agents.agentpolis.darptestbed.siminfrastructure.communication.requestconsumer.protocol.RequestConsumerMessageProtocol;
import cz.agents.agentpolis.darptestbed.simmodel.agent.TestbedEntityType;
import cz.agents.agentpolis.darptestbed.simmodel.agent.dispatching.logic.DispatchingLogic;
import cz.agents.agentpolis.darptestbed.siminfrastructure.communication.dispatching.protocol.DispatchingMessageProtocol;
import cz.agents.agentpolis.simmodel.agent.Agent;

/**
 * The factory for creating a new dispatching instance
 *
 * @author Lukas Canda
 */
public class DispatchingAgentFactory {

    public Agent createAgent(String dispatchingId, DispatchingLogic logic, Injector injector) {

        DispatchingAgent dispatchingAgent = new DispatchingAgent(dispatchingId, TestbedEntityType.DISPATCHING, logic);

        injector.getInstance(RequestConsumerMessageProtocol.class).addReceiverVisitor(dispatchingId, dispatchingAgent);
        injector.getInstance(DispatchingMessageProtocol.class).addReceiverVisitor(dispatchingId, dispatchingAgent);

        return dispatchingAgent;
    }
}

package cz.agents.agentpolis.darptestbed.siminfrastructure.communication.dispatching.protocol;

import cz.agents.agentpolis.darptestbed.siminfrastructure.communication.dispatching.receiver.DispatchingReceiverVisitor;
import cz.agents.agentpolis.darptestbed.siminfrastructure.communication.requestconsumer.receiver.RequestConsumerReceiverVisitor;

public interface DispatchingMessageProtocolCombinedVisitor extends RequestConsumerReceiverVisitor,
        DispatchingReceiverVisitor {
}

package cz.agents.agentpolis.darptestbed.siminfrastructure.communication.dispatching.protocol;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import cz.agents.agentpolis.darptestbed.siminfrastructure.communication.dispatching.receiver.DispatchingReceiverVisitor;
import cz.agents.agentpolis.darptestbed.siminfrastructure.communication.requestconsumer.receiver.RequestConsumerReceiverVisitor;
import cz.agents.agentpolis.ondemandtransport.siminfrastructure.communication.protocol.AMessageProtocol;
import cz.agents.alite.common.event.EventProcessor;

import static com.google.common.base.Preconditions.checkNotNull;

@Singleton
public class DispatchingMessageProtocol {

    public final AMessageProtocol<RequestConsumerReceiverVisitor> requestConsumerProtocol;
    public final AMessageProtocol<DispatchingReceiverVisitor> driverDispatchingProtocol;

    @Inject
    public DispatchingMessageProtocol(EventProcessor eventProcessor) {
        driverDispatchingProtocol = new AMessageProtocol<DispatchingReceiverVisitor>(eventProcessor) {};
        requestConsumerProtocol = new AMessageProtocol<RequestConsumerReceiverVisitor>(eventProcessor) {};
    }

    public void addReceiverVisitor(String agentId, DispatchingMessageProtocolCombinedVisitor receiverVisitor) {
        driverDispatchingProtocol.addReceiverVisitor(agentId, receiverVisitor);
        requestConsumerProtocol.addReceiverVisitor(agentId, receiverVisitor);
    }

}

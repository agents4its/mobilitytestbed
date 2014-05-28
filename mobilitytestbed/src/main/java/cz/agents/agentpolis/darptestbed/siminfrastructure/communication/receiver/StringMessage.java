package cz.agents.agentpolis.darptestbed.siminfrastructure.communication.receiver;

import cz.agents.agentpolis.ondemandtransport.siminfrastructure.communication.protocol.MessageVisitor;

public class StringMessage<TReceiverVisitor extends BaseReceiverVisitor> implements MessageVisitor<TReceiverVisitor> {

    public final String sender;
    public final String message;

    public StringMessage(String sender, String message) {
        super();
        this.sender = sender;
        this.message = message;
    }

    @Override
    public void accept(TReceiverVisitor receiverVisitor) {
        receiverVisitor.visit(this);
    }

    public String toString() {
        return String.format("String message from %s: %s", sender, message);
    }
}

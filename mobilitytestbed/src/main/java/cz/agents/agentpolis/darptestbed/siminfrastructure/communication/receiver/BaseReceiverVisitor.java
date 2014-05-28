package cz.agents.agentpolis.darptestbed.siminfrastructure.communication.receiver;

public interface BaseReceiverVisitor {
    public void visit(StringMessage stringMessage);
}

package cz.agents.agentpolis.darptestbed.simmodel.agent.exception;

/**
 * An agent throws this exception if someone set an unexpected type of communication
 * (e.g. uses centralized communication, but a decentralized method have been called).
 * 
 * @author Lukas Canda
 */
public class CommunicationTypeException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public CommunicationTypeException(String errorMessage) {
		super(errorMessage);
	}
}

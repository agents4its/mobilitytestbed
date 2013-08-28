package cz.agents.agentpolis.darptestbed.simmodel.agent.exception;

/**
 * This exception is thrown anywhere in the application, where the user's
 * settings are being read (the number of passengers, the size
 * of time windows etc.), but they are set to an incompatible value
 * (for example negative value).
 * 
 * @author Lukas Canda
 */
public class WrongSettingsException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public WrongSettingsException(String errorMessage) {
		super(errorMessage);
	}
}

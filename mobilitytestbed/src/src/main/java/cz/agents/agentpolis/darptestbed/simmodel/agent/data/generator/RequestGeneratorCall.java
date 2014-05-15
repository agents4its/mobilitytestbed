package cz.agents.agentpolis.darptestbed.simmodel.agent.data.generator;

import cz.agents.agentpolis.darptestbed.simmodel.agent.data.Request;


/**
 * An agent implementing this interface can be called directly by a request generator.
 * It enables the request generator to make a passenger send a request for taxi at any time.
 * 
 * @author Lukas Canda
 */
public interface RequestGeneratorCall {

	/**
     * This method is usually called by a request generator.
     */
    public void sendRequest(Request request);

}

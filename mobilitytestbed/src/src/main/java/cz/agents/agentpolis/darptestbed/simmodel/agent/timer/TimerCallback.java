package cz.agents.agentpolis.darptestbed.simmodel.agent.timer;

/**
 * Classes, that implements this interface, use a method, that is called regularly,
 * e.g. every 30 seconds. The time of calling depends on the timer,
 * which takes care of calling the callback method.
 * 
 * @author Lukas Canda
 */
public interface TimerCallback {
	
	/**
     * This method is called after the time of a timer has elapsed
     */
    public void timerCallback();
}

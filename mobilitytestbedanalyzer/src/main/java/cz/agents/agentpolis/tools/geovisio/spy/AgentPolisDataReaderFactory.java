package cz.agents.agentpolis.tools.geovisio.spy;

import com.google.inject.Injector;

import cz.agents.agentpolis.tools.geovisio.spy.agentpolis.AgentPolisDataReader;

/**
 * 
 * @author Marek Cuchy
 * 
 */
public interface AgentPolisDataReaderFactory {

	public AgentPolisDataReader createAgentPolisReader(Injector injector, String visName, int interval)
			throws ReflectiveOperationException, InterruptedException;

}

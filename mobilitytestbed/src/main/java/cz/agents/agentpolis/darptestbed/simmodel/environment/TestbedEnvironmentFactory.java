package cz.agents.agentpolis.darptestbed.simmodel.environment;

import java.util.List;
import java.util.Map;

import com.google.inject.Injector;

import cz.agents.agentpolis.darptestbed.global.GlobalParams;
import cz.agents.agentpolis.simmodel.environment.AgentPolisEnvironmentModule;
import cz.agents.agentpolis.simmodel.environment.EnvironmentFactory;
import cz.agents.agentpolis.simmodel.environment.model.citymodel.transportnetwork.Graph;
import cz.agents.agentpolis.simmodel.environment.model.citymodel.transportnetwork.GraphType;
import cz.agents.agentpolis.simmodel.environment.model.citymodel.transportnetwork.elemets.Node;
import cz.agents.agentpolis.simmodel.environment.model.delaymodel.factory.DelayingSegmentCapacityDeterminer;
import cz.agents.alite.simulation.Simulation;

/**
 * The factory to inject the modul using injector (framework Guice).
 * 
 * @author Lukas Canda
 */
public class TestbedEnvironmentFactory implements EnvironmentFactory {

	private final List<Object> subscribeLoggers;
	private final DelayingSegmentCapacityDeterminer delayingSegmentCapacityDeterminer;

	public TestbedEnvironmentFactory(List<Object> subscribeLoggers,
			DelayingSegmentCapacityDeterminer delayingSegmentCapacityDeterminer) {
		super();
		this.subscribeLoggers = subscribeLoggers;
		this.delayingSegmentCapacityDeterminer = delayingSegmentCapacityDeterminer;
	}

	@Override
	public Injector injectEnvironment(Injector injector, Simulation simulation, long seed,
			Map<GraphType, Graph> graphByGraphType, Map<Long, Node> nodesFromAllGraphs) {

		injector = injector.createChildInjector(new AgentPolisEnvironmentModule(simulation, GlobalParams.getRandom(),
				graphByGraphType, nodesFromAllGraphs, subscribeLoggers, delayingSegmentCapacityDeterminer));

		return injector.createChildInjector(new TestbedEnvironmentModul(simulation));
	}

}

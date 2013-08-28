package cz.agents.agentpolis.darptestbed.siminfrastructure.planner.init;

import javax.inject.Singleton;

import org.jgrapht.graph.DirectedWeightedMultigraph;

import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.Provides;

import cz.agents.agentpolis.darptestbed.siminfrastructure.planner.TestbedAStartPlanner;
import cz.agents.agentpolis.darptestbed.siminfrastructure.planner.TestbedPlanner;
import cz.agents.agentpolis.darptestbed.simulator.initializator.osm.NodeExtendedFunction;
import cz.agents.agentpolis.simmodel.environment.model.citymodel.transportnetwork.AllNetworkNodes;
import cz.agents.agentpolis.simmodel.environment.model.citymodel.transportnetwork.EGraphType;
import cz.agents.agentpolis.simmodel.environment.model.citymodel.transportnetwork.TransportNetworks;
import cz.agents.agentpolis.simulator.creator.initializator.InitModulFactory;
import eu.superhub.wp4.model.citizen.activityscheduler.planner.utils.PlannerEdge;
import eu.superhub.wp4.model.citizen.activityscheduler.planner.utils.PlannerGraphCreator;

public class TestbedPlannerModulFactory implements InitModulFactory {

	@Override
	public AbstractModule injectModule(Injector injector) {

		NodeExtendedFunction nodeExtendedFunction = injector.getInstance(NodeExtendedFunction.class);
		final TestbedPlanner pathPlanner = new TestbedAStartPlanner(nodeExtendedFunction, initPlannerGraph(injector),
				EGraphType.HIGHWAY);

		return new AbstractModule() {

			@Override
			protected void configure() {
				// TODO Auto-generated method stub

			}

			@Provides
			@Singleton
			public TestbedPlanner providePathPlanner() {
				return pathPlanner;
			}
		};
	}

	private DirectedWeightedMultigraph<Long, PlannerEdge> initPlannerGraph(Injector injector) {

		AllNetworkNodes allNetworkNodes = injector.getInstance(AllNetworkNodes.class);

		return PlannerGraphCreator.createGraph(
				injector.getInstance(TransportNetworks.class).getGraphByType(EGraphType.HIGHWAY),
				allNetworkNodes.getAllNetworkNodes());

	}

}

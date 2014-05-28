package cz.agents.agentpolis.darptestbed.siminfrastructure.planner.init;

import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.Provides;
import cz.agents.agentpolis.darptestbed.siminfrastructure.planner.TestbedAStarPlanner;
import cz.agents.agentpolis.darptestbed.siminfrastructure.planner.TestbedPlanner;
import cz.agents.agentpolis.darptestbed.simulator.initializator.osm.NodeExtendedFunction;
import cz.agents.agentpolis.siminfrastructure.planner.utils.PlannerEdge;
import cz.agents.agentpolis.siminfrastructure.planner.utils.PlannerGraphCreator;
import cz.agents.agentpolis.simmodel.environment.model.citymodel.transportnetwork.AllNetworkNodes;
import cz.agents.agentpolis.simmodel.environment.model.citymodel.transportnetwork.EGraphType;
import cz.agents.agentpolis.simmodel.environment.model.citymodel.transportnetwork.TransportNetworks;
import cz.agents.agentpolis.simulator.creator.initializator.InitModuleFactory;
import org.jgrapht.graph.DirectedWeightedMultigraph;

import javax.inject.Singleton;

public class TestbedPlannerModuleFactory implements InitModuleFactory {

    @Override
    public AbstractModule injectModule(Injector injector) {

        NodeExtendedFunction nodeExtendedFunction = injector.getInstance(NodeExtendedFunction.class);
        final TestbedPlanner pathPlanner = new TestbedAStarPlanner(nodeExtendedFunction, initPlannerGraph(injector),
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

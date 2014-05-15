package cz.agents.agentpolis.darptestbed.simulator.initializator;

import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import cz.agents.agentpolis.darptestbed.global.GlobalParams;
import cz.agents.agentpolis.darptestbed.global.Utils;
import cz.agents.agentpolis.darptestbed.siminfrastructure.communication.driver.protocol.DriverCentralizedMessageProtocol;
import cz.agents.agentpolis.darptestbed.siminfrastructure.communication.passenger.protocol.PassengerMessageProtocol;
import cz.agents.agentpolis.darptestbed.siminfrastructure.planner.TestbedPlanner;
import cz.agents.agentpolis.darptestbed.simmodel.agent.dispatching.DispatchingAgent;
import cz.agents.agentpolis.darptestbed.simmodel.agent.dispatching.DispatchingAgentFactory;
import cz.agents.agentpolis.darptestbed.simmodel.agent.dispatching.logic.DispatchingLogic;
import cz.agents.agentpolis.darptestbed.simmodel.agent.timer.Timer;
import cz.agents.agentpolis.darptestbed.simmodel.environment.TestbedPostprocessEnvironmentModul;
import cz.agents.agentpolis.darptestbed.simmodel.environment.model.TestbedModel;
import cz.agents.agentpolis.darptestbed.simmodel.environment.model.TestbedVehicleStorage;
import cz.agents.agentpolis.simmodel.environment.model.citymodel.transportnetwork.AllNetworkNodes;
import cz.agents.agentpolis.simmodel.environment.model.query.AgentPositionQuery;
import cz.agents.agentpolis.simulator.creator.initializator.InitModuleFactory;
import cz.agents.alite.common.event.EventProcessor;
import org.joda.time.Duration;

/**
 * Initiates the dispatching, timers and the statistics logger in the
 * simulation. This is not an AgentInitFactory, because it doesn't return any
 * agent in the decentralized case and also it saves the dispatching agent into
 * a special storage (TestbedModel).
 *
 * @author Lukas Canda
 */
public class DispatchingAndTimersInitFactory implements InitModuleFactory {

    private final LogicConstructor logicConstructor;

    public DispatchingAndTimersInitFactory(LogicConstructor logicConstructor) {
        this.logicConstructor = logicConstructor;
    }

    @Override
    public AbstractModule injectModule(Injector injector) {

        // init logger
        // StatisticsLogger.setInstance(injector.getInstance(StatisticsLogger.class));

        // get ready for creating timers
        TestbedModel taxiModel = injector.getInstance(TestbedModel.class);
        EventProcessor eventProcessor = injector.getInstance(EventProcessor.class);
        Utils utils = injector.getInstance(Utils.class);

        if (!GlobalParams.isCentralized()) {
            // create timers
            taxiModel.setTimers(
                    null,
                    new Timer("TaxiDriversTimer", eventProcessor, taxiModel, utils, 2, Duration
                            .standardMinutes(GlobalParams.getTimerDriverInterval())),
                    new Timer("PassengersTimer", eventProcessor, taxiModel, utils, 2, Duration
                            .standardMinutes(GlobalParams.getTimerPassengerInterval())));
        } else {
            // create a new dispatching agent
            DispatchingAgentFactory factory = new DispatchingAgentFactory();

            // get ready for creating the dispatching logic
            PassengerMessageProtocol sender = injector.getInstance(PassengerMessageProtocol.class);
            DriverCentralizedMessageProtocol driverCentralizedMessageProtocol = injector
                    .getInstance(DriverCentralizedMessageProtocol.class);
            AgentPositionQuery positionQuery = injector.getInstance(AgentPositionQuery.class);
            AllNetworkNodes allNetworkNodes = injector.getInstance(AllNetworkNodes.class);
            TestbedPlanner pathPlanner = injector.getInstance(TestbedPlanner.class);
            TestbedVehicleStorage vehicleStorage = injector.getInstance(TestbedVehicleStorage.class);

            DispatchingLogic logic = logicConstructor.constructDispatchingLogic("Dispatching", sender, driverCentralizedMessageProtocol, taxiModel,
                            positionQuery, allNetworkNodes, utils, pathPlanner, vehicleStorage);
//                    logic = new DispatchingTabuSearchLogic("Dispatching", sender, driverCentralizedMessageProtocol, taxiModel,
//                            positionQuery, allNetworkNodes, utils, pathPlanner, vehicleStorage);

            DispatchingAgent dispatchingAgent = (DispatchingAgent) factory.createAgent("Dispatching", logic, injector);

            // save the dispatching agent into his special storage
            injector.getInstance(TestbedModel.class).setDispatching(dispatchingAgent);

            // init dispatching timer
            Timer dispatchingTimer = new Timer("DispatchingTimer", eventProcessor, taxiModel, utils, 1,
                    Duration.standardMinutes(GlobalParams.getTimerDispatchingInterval()));
            dispatchingTimer.addCallback(dispatchingAgent);
            taxiModel.setTimers(dispatchingTimer, null, null);
        }

        taxiModel.startTimers();

        return new TestbedPostprocessEnvironmentModul();
    }

}

package cz.agents.agentpolis.darptestbed.simmodel.agent.driver;

import com.google.inject.Injector;

import cz.agents.agentpolis.darptestbed.siminfrastructure.communication.driver.protocol.DriverCentralizedMessageProtocol;
import cz.agents.agentpolis.darptestbed.siminfrastructure.communication.driver.protocol.DriverMessageProtocol;
import cz.agents.agentpolis.darptestbed.siminfrastructure.communication.requestconsumer.protocol.RequestConsumerMessageProtocol;
import cz.agents.agentpolis.darptestbed.simmodel.agent.TestbedEntityType;
import cz.agents.agentpolis.darptestbed.simmodel.agent.driver.logic.DriverCentralizedLogic;
import cz.agents.agentpolis.darptestbed.simmodel.agent.driver.logic.DriverDecentralizedLogic;
import cz.agents.agentpolis.simmodel.environment.model.query.AgentPositionQuery;

/**
 * The factory for creating a new taxi driver instance
 * 
 * @author Lukas Canda
 */
public class DriverAgentFactory {

	/**
	 * All parameters are required to create a DriverAgent instance. (We usually
	 * set the parameters in an AgentInitFactory when we know the details about
	 * the agents we want to create.)
	 */
	public DriverDecentralizedAgent createDecentDriverAgent(String driverId, DriverDecentralizedLogic logic, Injector injector) {

		AgentPositionQuery positionQuery = injector.getInstance(AgentPositionQuery.class);

		DriverDecentralizedAgent driverAgent = new DriverDecentralizedAgent(driverId, TestbedEntityType.TAXI_DRIVER, logic,
				positionQuery);

		injector.getInstance(RequestConsumerMessageProtocol.class).addReceiverVisitor(driverId, driverAgent);
		injector.getInstance(DriverMessageProtocol.class).addReceiverVisitor(driverId, driverAgent);

		return driverAgent;
	}

	/**
	 * All parameters are required to create a DriverAgent instance. (We usually
	 * set the parameters in an AgentInitFactory when we know the details about
	 * the agents we want to create.)
	 */
	public DriverCentralizedAgent createCentrDriverAgent(String driverId, DriverCentralizedLogic logic, Injector injector) {

		AgentPositionQuery positionQuery = injector.getInstance(AgentPositionQuery.class);

		DriverCentralizedAgent driverAgent = new DriverCentralizedAgent(driverId, TestbedEntityType.TAXI_DRIVER, logic,
				positionQuery);

		injector.getInstance(DriverCentralizedMessageProtocol.class).addReceiverVisitor(driverId, driverAgent);
		injector.getInstance(DriverMessageProtocol.class).addReceiverVisitor(driverId, driverAgent);

		return driverAgent;
	}
}

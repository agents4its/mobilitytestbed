package cz.agents.agentpolis.darptestbed.simmodel.agent.driver;

import com.google.inject.Injector;

import cz.agents.agentpolis.darptestbed.siminfrastructure.communication.driver.protocol.DriverCentrMessageProtocol;
import cz.agents.agentpolis.darptestbed.siminfrastructure.communication.driver.protocol.DriverMessageProtocol;
import cz.agents.agentpolis.darptestbed.siminfrastructure.communication.requestconsumer.protocol.RequestConsumerMessageProtocol;
import cz.agents.agentpolis.darptestbed.simmodel.agent.TestbedEntityType;
import cz.agents.agentpolis.darptestbed.simmodel.agent.driver.logic.DriverCentrLogic;
import cz.agents.agentpolis.darptestbed.simmodel.agent.driver.logic.DriverDecentrLogic;
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
	public DecentDriverAgent createDecentDriverAgent(String driverId, DriverDecentrLogic logic, Injector injector) {

		AgentPositionQuery positionQuery = injector.getInstance(AgentPositionQuery.class);

		DecentDriverAgent driverAgent = new DecentDriverAgent(driverId, TestbedEntityType.TAXI_DRIVER, logic,
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
	public CentrDriverAgent createCentrDriverAgent(String driverId, DriverCentrLogic logic, Injector injector) {

		AgentPositionQuery positionQuery = injector.getInstance(AgentPositionQuery.class);

		CentrDriverAgent driverAgent = new CentrDriverAgent(driverId, TestbedEntityType.TAXI_DRIVER, logic,
				positionQuery);

		injector.getInstance(DriverCentrMessageProtocol.class).addReceiverVisitor(driverId, driverAgent);
		injector.getInstance(DriverMessageProtocol.class).addReceiverVisitor(driverId, driverAgent);

		return driverAgent;
	}
}

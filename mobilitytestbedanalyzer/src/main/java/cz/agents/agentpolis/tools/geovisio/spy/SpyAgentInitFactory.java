package cz.agents.agentpolis.tools.geovisio.spy;

import java.sql.SQLException;
import java.util.List;

import org.apache.log4j.Logger;
import org.opengis.referencing.operation.TransformException;

import com.google.common.collect.Lists;
import com.google.inject.Injector;

import cz.agents.agentpolis.simmodel.agent.Agent;
import cz.agents.agentpolis.simulator.creator.SimulationCreator;
import cz.agents.agentpolis.simulator.creator.SimulationFinishedListener;
import cz.agents.agentpolis.simulator.creator.initializator.AgentInitFactory;
import cz.agents.agentpolis.tools.geovisio.spy.agentpolis.AgentPolisDataReader;

/**
 * 
 * @author Marek Cuchy
 * 
 */
public class SpyAgentInitFactory implements AgentInitFactory {

	private final static Logger logger = Logger.getLogger(SpyAgentInitFactory.class);

	private final int interval;
	private final String visName;
	private final SimulationCreator creator;
	private AgentPolisDataReader reader;
	private final AgentPolisDataReaderFactory readerFactory;

	public SpyAgentInitFactory(int interval, String visName,AgentPolisDataReaderFactory readerFactory, SimulationCreator creator) {
		super();
		this.interval = interval;
		this.visName = visName;
		this.creator = creator;
		this.readerFactory = readerFactory;
	}

	public AgentPolisDataReader getReader() {
		return reader;
	}

	public List<Agent> initAllAgentLifeCycles(Injector injector) {
		try {
			reader = readerFactory.createAgentPolisReader(injector,visName,interval);
			reader.initReadingAndRead();
			creator.addSimulationFinishedListener(new SimulationFinishedListener() {

				public void simulationFinished() {
					try {
						reader.onSimulationFinished();
						logger.info("Indexes for visualization table created.");
					} catch (SQLException | TransformException e) {
						logger.error("Indexes not created.");
					}
				}
			});
		} catch (ReflectiveOperationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return Lists.newArrayList();
	}

}

package cz.agents.agentpolis.tools.geovisio.spy.agentpolis;

import java.sql.SQLException;

import org.apache.log4j.Logger;
import org.opengis.referencing.operation.TransformException;

import cz.agents.alite.common.event.Event;
import cz.agents.alite.common.event.EventHandler;
import cz.agents.alite.common.event.EventProcessor;

/**
 * 
 * @author Marek Cuchy
 * 
 */
public class SpyEventHandler implements EventHandler {
	
	private final static Logger logger = Logger.getLogger(SpyEventHandler.class);

	private final EventProcessor eventProcessor;
	private final long interval;

	private final AgentPolisDataReader reader;

	public SpyEventHandler(EventProcessor eventProcessor, long interval, AgentPolisDataReader reader) {
		super();
		this.eventProcessor = eventProcessor;
		this.interval = interval;
		this.reader = reader;
	}

	public void startHandeling() {
		eventProcessor.addEvent(this, interval);
	}

	public EventProcessor getEventProcessor() {
		return eventProcessor;
	}

	public void handleEvent(Event event) {
		try {
			logger.debug("Start reading, sim time: "+event.getTime());
			reader.readAndSaveData();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (TransformException e) {
			e.printStackTrace();
		} catch (ReflectiveOperationException e) {
			e.printStackTrace();
		}
		eventProcessor.addEvent(this, interval);
	}

}

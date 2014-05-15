package cz.agents.agentpolis.darptestbed.siminfrastructure.logger;

import com.google.inject.Inject;

import cz.agents.agentpolis.darptestbed.siminfrastructure.logger.item.AlgRealTimeLogItem;
import cz.agents.agentpolis.siminfrastructure.logger.Logger;
import cz.agents.agentpolis.siminfrastructure.logger.PublishSubscribeLogger;
import cz.agents.alite.common.event.EventProcessor;

public class UtilsLogger extends Logger {

	@Inject
	public UtilsLogger(PublishSubscribeLogger publishSubscribeLogger, EventProcessor eventProcessor) {
		super(publishSubscribeLogger, eventProcessor);
		// TODO Auto-generated constructor stub
	}

	public void logAlgRealTime(long realTime) {
		log(new AlgRealTimeLogItem(realTime));

	}

}

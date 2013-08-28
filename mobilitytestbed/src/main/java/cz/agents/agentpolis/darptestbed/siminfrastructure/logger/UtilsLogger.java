package cz.agents.agentpolis.darptestbed.siminfrastructure.logger;

import com.google.inject.Inject;

import cz.agents.agentpolis.darptestbed.siminfrastructure.logger.item.AlgRealTimeLogItem;
import cz.agents.agentpolis.siminfrastructure.logger.PublishSubscribeLogger;

public class UtilsLogger {

	private final PublishSubscribeLogger publishSubscribeLogger;

	@Inject
	public UtilsLogger(PublishSubscribeLogger publishSubscribeLogger) {
		this.publishSubscribeLogger = publishSubscribeLogger;
	}

	public void logAlgRealTime(long realTime) {
		publishSubscribeLogger.log(new AlgRealTimeLogItem(realTime));

	}

}

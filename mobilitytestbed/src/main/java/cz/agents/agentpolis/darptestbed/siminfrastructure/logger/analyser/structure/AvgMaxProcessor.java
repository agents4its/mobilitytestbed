package cz.agents.agentpolis.darptestbed.siminfrastructure.logger.analyser.structure;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;

import eu.superhub.wp4.analyser.processor.data.AvgCounter;

public class AvgMaxProcessor {

	private final Map<String, Long> startedLogItems = new HashMap<String, Long>();
	private long maxTime = -1;
	private AvgCounter avgCounter = new AvgCounter();
	private List<Long> medianValues = Lists.newArrayList();

	public void addStartLogItem(String itemId, long simulationTime) {
		startedLogItems.put(checkNotNull(itemId), simulationTime);
	}

	public void addEndLogItem(String itemId, long simulationTime) {
		Long startSimulationTime = startedLogItems.remove(checkNotNull(itemId));
		if (startSimulationTime != null) {
			long diffSimulationTime = simulationTime - startSimulationTime;

			maxTime = Math.max(maxTime, diffSimulationTime);
			avgCounter.addValue(diffSimulationTime);
			medianValues.add(diffSimulationTime);

		}

	}

	public long getMax() {
		return maxTime;
	}

	public double getAvg() {
		return avgCounter.getCurrentAvgValue();
	}

	public long getMedian() {
		if (medianValues.isEmpty()) {
			return -1;
		}
		Collections.sort(medianValues);
		int middle = medianValues.size() / 2;
		if (medianValues.size() % 2 == 1) {
			return medianValues.get(middle);
		}
		return (long) ((medianValues.get(middle - 1) + medianValues.get(middle)) / 2.0);

	}
}

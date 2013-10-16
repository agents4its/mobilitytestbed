package cz.agents.agentpolis.darptestbed.simulator.initializator.osm.init;

import java.util.Map;

import com.google.common.collect.RangeMap;

import cz.agents.agentpolis.siminfrastructure.time.TimeProvider;
import cz.agents.agentpolis.simmodel.environment.model.SpeedInfluenceModel;
import cz.agents.agentpolis.simmodel.environment.model.citymodel.transportnetwork.GraphType;
import cz.agents.agentpolis.simmodel.environment.model.delaymodel.key.GraphTypeAndFromToNodeKey;

public class ExogenousSpeedLimitSegmentInfluence implements SpeedInfluenceModel {

	private final Map<GraphTypeAndFromToNodeKey, RangeMap<Long, Double>> exogenousSpeedLimits;
	private final TimeProvider timeProvider;

	public ExogenousSpeedLimitSegmentInfluence(
			Map<GraphTypeAndFromToNodeKey, RangeMap<Long, Double>> exogenousSpeedLimits, TimeProvider timeProvider) {
		super();
		this.exogenousSpeedLimits = exogenousSpeedLimits;
		this.timeProvider = timeProvider;
	}

	@Override
	public double computedInfluencedSpeed(GraphType graphType, long fromNodeByNodeId, long toNodeByNodeId,
			double originSpeedInmps, double influencedSpeedInmps) {

		GraphTypeAndFromToNodeKey graphTypeAndFromToNodeKey = new GraphTypeAndFromToNodeKey(graphType,
				fromNodeByNodeId, toNodeByNodeId);
		RangeMap<Long, Double> rangeMap = exogenousSpeedLimits.get(graphTypeAndFromToNodeKey);

		if (rangeMap != null && getSpeedLimit(rangeMap) != null) {
			return getSpeedLimit(rangeMap);
		}

		return influencedSpeedInmps;
	}

	private Double getSpeedLimit(RangeMap<Long, Double> rangeMap) {
		return rangeMap.get(timeProvider.getSimTimeInDayRange());
	}

}

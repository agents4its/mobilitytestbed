package cz.agents.agentpolis.darptestbed.siminfrastructure.request.generator.support;

import org.apache.commons.math3.distribution.UniformRealDistribution;
import org.apache.commons.math3.random.Well19937c;

import cz.agents.agentpolis.darptestbed.siminfrastructure.request.generator.support.GPSPositionGeneratorImpl.LatGeneratorItem;
import cz.agents.agentpolis.darptestbed.siminfrastructure.request.generator.support.GPSPositionGeneratorImpl.LonGeneratorItem;

public class GPSPositionGeneratorWithUniformDistributionFactory implements GPSPositionGeneratorFactory {

	private final int seed;

	public GPSPositionGeneratorWithUniformDistributionFactory(int seed) {
		super();
		this.seed = seed;
	}

	@Override
	public GPSPositionGenerator createGPSPositionGenerator(double minLon, double minLat, double maxLon, double maxLat) {
		LonGeneratorItem lonGeneratorData = new LonGeneratorItem(new UniformRealDistribution(new Well19937c(seed),
				minLon, maxLon, UniformRealDistribution.DEFAULT_INVERSE_ABSOLUTE_ACCURACY), minLon, maxLon);
		LatGeneratorItem latGeneratorData = new LatGeneratorItem(new UniformRealDistribution(new Well19937c(
				Integer.MAX_VALUE - seed), minLat, maxLat, UniformRealDistribution.DEFAULT_INVERSE_ABSOLUTE_ACCURACY),
				minLat, maxLat);

		return new GPSPositionGeneratorImpl(latGeneratorData, lonGeneratorData);

	}

}

package cz.agents.agentpolis.darptestbed.siminfrastructure.request.generator.support;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.random.Well19937c;

import cz.agents.agentpolis.darptestbed.siminfrastructure.request.generator.support.GPSPositionGeneratorImpl.LatGeneratorItem;
import cz.agents.agentpolis.darptestbed.siminfrastructure.request.generator.support.GPSPositionGeneratorImpl.LonGeneratorItem;

public class GPSPositionGeneratorWithNormalDistributionFactory implements GPSPositionGeneratorFactory {

	private static final double DEVIDE_TO_MIDDLE = 2.0;
	private static final double REDUCE_SD_TO_GET_MORE_NORL = 4.0;

	private final int seed;

	public GPSPositionGeneratorWithNormalDistributionFactory(int seed) {
		super();
		this.seed = seed;
	}

	@Override
	public GPSPositionGenerator createGPSPositionGenerator(double minLon, double minLat, double maxLon, double maxLat) {
		double exLon = minLon + (Math.abs(maxLon - minLon) / DEVIDE_TO_MIDDLE);
		double exLat = minLat + (Math.abs(maxLat - minLat) / DEVIDE_TO_MIDDLE);
		double sdLon = Math.abs(maxLon - exLon) / REDUCE_SD_TO_GET_MORE_NORL;
		double sdLat = Math.abs(maxLat - exLat) / REDUCE_SD_TO_GET_MORE_NORL;

		LonGeneratorItem lonGeneratorData = new LonGeneratorItem(new NormalDistribution(new Well19937c(seed), exLon,
				sdLon, NormalDistribution.DEFAULT_INVERSE_ABSOLUTE_ACCURACY), minLon, maxLon);
		LatGeneratorItem latGeneratorData = new LatGeneratorItem(new NormalDistribution(new Well19937c(
				Integer.MAX_VALUE - seed), exLat, sdLat, NormalDistribution.DEFAULT_INVERSE_ABSOLUTE_ACCURACY), minLat,
				maxLat);

		return new GPSPositionGeneratorImpl(latGeneratorData, lonGeneratorData);

	}

}

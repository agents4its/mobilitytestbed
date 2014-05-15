package cz.agents.agentpolis.darptestbed.siminfrastructure.request.generator.support;

import org.apache.commons.math3.distribution.AbstractRealDistribution;

import cz.agents.agentpolis.darptestbed.siminfrastructure.request.GPS;

public class GPSPositionGeneratorImpl implements GPSPositionGenerator {

	public final LatGeneratorItem latGeneratorData;
	public final LonGeneratorItem lonGeneratorData;

	public GPSPositionGeneratorImpl(LatGeneratorItem latGeneratorData, LonGeneratorItem lonGeneratorData) {
		super();
		this.latGeneratorData = latGeneratorData;
		this.lonGeneratorData = lonGeneratorData;
	}

	public GPS generateGPSPosition() {

		double lat = generateValue(latGeneratorData);
		double lon = generateValue(lonGeneratorData);

		return new GPS(lat, lon);
	}

	private double generateValue(GPSItem item) {
		return normalizeToBounds(item.getAbstractRealDistribution().sample(), item.getMin(), item.getMax());
	}

	private double normalizeToBounds(double sample, double min, double max) {
		return Math.min(Math.max(sample, min), max);
	}

	private abstract static class GPSItem {
		private final AbstractRealDistribution abstractRealDistribution;
		private final double min;
		private final double max;

		public GPSItem(AbstractRealDistribution abstractRealDistribution, double min, double max) {
			super();
			this.abstractRealDistribution = abstractRealDistribution;
			this.min = min;
			this.max = max;
		}

		public AbstractRealDistribution getAbstractRealDistribution() {
			return abstractRealDistribution;
		}

		public double getMin() {
			return min;
		}

		public double getMax() {
			return max;
		}

	}

	public static class LatGeneratorItem extends GPSItem {

		public LatGeneratorItem(AbstractRealDistribution abstractRealDistribution, double min, double max) {
			super(abstractRealDistribution, min, max);
			// TODO Auto-generated constructor stub
		}

	}

	public static class LonGeneratorItem extends GPSItem {

		public LonGeneratorItem(AbstractRealDistribution abstractRealDistribution, double min, double max) {
			super(abstractRealDistribution, min, max);
			// TODO Auto-generated constructor stub
		}

	}

}
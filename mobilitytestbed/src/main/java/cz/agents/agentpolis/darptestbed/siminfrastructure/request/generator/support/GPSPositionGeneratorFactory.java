package cz.agents.agentpolis.darptestbed.siminfrastructure.request.generator.support;

public interface GPSPositionGeneratorFactory {

	public GPSPositionGenerator createGPSPositionGenerator(double minLon, double minLat, double maxLon, double maxLat);

}
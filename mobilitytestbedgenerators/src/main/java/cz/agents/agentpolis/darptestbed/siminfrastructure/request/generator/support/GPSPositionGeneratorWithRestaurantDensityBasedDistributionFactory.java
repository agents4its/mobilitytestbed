package cz.agents.agentpolis.darptestbed.siminfrastructure.request.generator.support;

import com.google.inject.Injector;
import org.apache.commons.math3.random.RandomGenerator;

public class GPSPositionGeneratorWithRestaurantDensityBasedDistributionFactory implements GPSPositionGeneratorFactory {

    private final String osmFileName;
    private final String benchmarkDir;
    private final RandomGenerator rnd;
    private final Injector injector;

    public GPSPositionGeneratorWithRestaurantDensityBasedDistributionFactory(
            String osmFileName, String benchmarkDir, RandomGenerator rnd, Injector injector) {
        super();
        this.osmFileName = osmFileName;
        this.benchmarkDir = benchmarkDir;
        this.rnd = rnd;
        this.injector = injector;
    }

    @Override
    public GPSPositionGenerator createGPSPositionGenerator(double minLon, double minLat, double maxLon, double maxLat) {

        return new RestaurantDensityBasedDistribution(osmFileName, benchmarkDir, rnd, injector);

    }
}

package cz.agents.agentpolis.darptestbed.siminfrastructure.request.generator.support;

import cz.agents.agentpolis.darptestbed.siminfrastructure.request.GPS;
import cz.agents.agentpolis.simmodel.environment.model.citymodel.restaurantnetwork.elements.RestaurantNode;
import org.apache.commons.math3.random.RandomGenerator;
import org.openstreetmap.osm.data.coordinates.LatLon;

import java.util.Map;

public class TimeAwareRestaurantDensityBasedDistribution implements GPSPositionGenerator {

    private final RestaurantDensityCalculator calculator;
    private final RandomGenerator rnd;
    private final Map<RestaurantNode, Integer> histogram;
    private final long marginal;
    private LatLon latLon;

    public TimeAwareRestaurantDensityBasedDistribution(String osmFileName, String benchmarkDir, RandomGenerator rnd) {
        this.rnd = rnd;
        calculator = new RestaurantDensityCalculator(osmFileName, benchmarkDir, null);
        histogram = calculator.calculateFrequencies();
        marginal = calculateMarginal(histogram);
    }

    private long calculateMarginal(Map<RestaurantNode, Integer> histogram) {
        int marginal = 0;
        for (Map.Entry<RestaurantNode, Integer> node : histogram.entrySet()) {
            marginal += node.getValue();
        }

        return marginal;
    }

    public RestaurantNode sample() {
        int value = (int) (rnd.nextDouble() * marginal);

        for (Map.Entry<RestaurantNode, Integer> nodeEntry : histogram.entrySet()) {
            value -= nodeEntry.getValue();
             if (value < 0) {
                 return nodeEntry.getKey();
             }
        }

        return null;
    }

    @Override
    public GPS generateGPSPosition() {
        latLon = sample().getLatLon();
        return new GPS(latLon.lat(), latLon.lon());
    }
}

package cz.agents.agentpolis.darptestbed.siminfrastructure.request.generator;

import cz.agents.agentpolis.darptestbed.siminfrastructure.request.generator.support.RestaurantDensityCalculator;
import cz.agents.agentpolis.simmodel.environment.model.citymodel.restaurantnetwork.elements.RestaurantNode;
import cz.agents.dbtokmlexporter.darptestbed.kmlitembuilder.NodeDensityHeatMapKmlItemBuilder;
import cz.agents.dbtokmlexporter.kmlitem.builder.KmlItemBuilder;
import cz.agents.dbtokmlexporter.kmlitem.builder.SimpleKmlItemBuilder;

import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RestaurantDensityCalculatorApp {

    private static Logger LOGGER = Logger.getRootLogger();

    static String BENCHMARK_DIR = "prague";
    static String OSM_FILE_NAME = "prague";

//    static String BENCHMARK_DIR = "hague_20_drivers";
//    static String OSM_FILE_NAME = "haag";

    /**
     * @param args
     * @throws java.io.IOException
     * @throws org.codehaus.jackson.map.JsonMappingException
     *
     * @throws org.codehaus.jackson.JsonGenerationException
     *
     */
    public static void main(String[] args) throws IOException, SQLException {
//        PropertyConfigurator.configure("log4j.properties");

        if (args.length == 0) {
            args = new String[] {BENCHMARK_DIR, OSM_FILE_NAME};
        }

        String outputFolderParent = "../mobilitytestbedvisio/";
        final String osmFileName = outputFolderParent + "experiments/" + args[0] + "/data/" + args[1] + ".osm";
        final String benchmarkDir = outputFolderParent + "experiments/" + args[0] + "/";

        RestaurantDensityCalculator approximation = new RestaurantDensityCalculator(osmFileName, benchmarkDir, null);

        Map<RestaurantNode, Integer> frequencies = approximation.calculateFrequencies();



        NodeDensityHeatMapKmlItemBuilder nodeDensityHeatMap =
                NodeDensityHeatMapKmlItemBuilder.createNodeDensityMapKmlBuilder(
                    approximation.getRestaurantNodesCoordinates(), "node_density");

        List<SimpleKmlItemBuilder> itemBuilders = new ArrayList<>();
        itemBuilders.add(nodeDensityHeatMap);

        KmlItemBuilder.saveBuiltKmlItemsToSeparateFiles(itemBuilders, outputFolderParent + "experiments/" + args[0]
                + "/node_density/", BENCHMARK_DIR+File.separator+"data"+File.separator+"visualizations");



        LOGGER.debug(frequencies);

    }
}

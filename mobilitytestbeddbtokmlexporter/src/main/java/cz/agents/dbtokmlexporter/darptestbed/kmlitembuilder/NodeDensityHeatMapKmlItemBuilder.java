package cz.agents.dbtokmlexporter.darptestbed.kmlitembuilder;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import cz.agents.dbtokmlexporter.HeatMapKmlItem;
import cz.agents.dbtokmlexporter.kmlitem.builder.SimpleKmlItemBuilder;
import cz.agents.resultsvisio.kml.KmlItem;
import org.apache.log4j.Logger;

import java.sql.SQLException;
import java.util.List;

public class NodeDensityHeatMapKmlItemBuilder extends SimpleKmlItemBuilder {

	private static final Logger LOGGER = Logger.getLogger(NodeDensityHeatMapKmlItemBuilder.class);
    private final List<Coordinate> coordinates;

    public NodeDensityHeatMapKmlItemBuilder(List<Coordinate> coordinates, String schemaName,
                                            String fileName) {
		super(schemaName, fileName, true);
        this.coordinates = coordinates;
	}

    public static NodeDensityHeatMapKmlItemBuilder createNodeDensityMapKmlBuilder(List<Coordinate> coordinates,
                                                                                  String schemaName) {
		return new NodeDensityHeatMapKmlItemBuilder(coordinates, schemaName, "node_density.kmz");
	}

	@Override
	public KmlItem buildKmlItem() throws SQLException {
		LOGGER.info("Preparing visualizations: " + this.getClass().getSimpleName()
                + " (This may take a while. Please wait.)");

		HeatMapKmlItem kmlItem = new HeatMapKmlItem(schemaName);

        GeometryFactory geometryFactory = new GeometryFactory();
        for (Coordinate coordinate : coordinates) {
            kmlItem.addPoint(geometryFactory.createPoint(coordinate));
        }
		return kmlItem;
	}

}

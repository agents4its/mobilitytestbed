package cz.agents.dbtokmlexporter.jmk;

import java.awt.Color;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.operation.TransformException;

import cz.agents.agentpolis.tools.geovisio.database.connection.DatabaseConnection;
import cz.agents.agentpolis.tools.geovisio.database.connection.DatabaseConnectionSettings;
import cz.agents.agentpolis.tools.geovisio.database.postgres.postgis.PostgisConnection;
import cz.agents.agentpolis.tools.geovisio.database.postgres.postgis.PostgisGeometry;
import cz.agents.agentpolis.tools.geovisio.projection.ProjectionTransformer;
import cz.agents.dbtokmlexporter.factory.DescriptionFactory;
import cz.agents.dbtokmlexporter.factory.geometry.GeometryFactory;
import cz.agents.dbtokmlexporter.factory.geometry.LineGeometryFactory;
import cz.agents.dbtokmlexporter.factory.style.LineStyleFactory;
import cz.agents.dbtokmlexporter.factory.style.StyleFactory;
import cz.agents.dbtokmlexporter.kmlitem.NonInterpolatedTimeKmlItem;
import cz.agents.dbtokmlexporter.kmlitem.builder.KmlItemBuilder;
import cz.agents.resultsvisio.kml.KmlItem;
import de.micromata.opengis.kml.v_2_2_0.Kml;

public class JMKFromDbToNonInterpolatedKmlMain {
	private static final Logger logger = Logger.getLogger(JMKFromDbToNonInterpolatedKmlMain.class);

	public static void main(String[] args) throws NoSuchAuthorityCodeException, SQLException, FactoryException,
	        TransformException, ClassNotFoundException, IOException {

		// String schemaName = "testbed_dublin";
		String schemaName = "jmk_brno_exp";
		// String schemaName = "jmk_brno_without_tram_exp";
		// String schemaName = "testbed_sf";

		// new File(schemaName).mkdir();

		DOMConfigurator.configure("log4j.xml");

		DatabaseConnectionSettings set = new DatabaseConnectionSettings("mf.felk.cvut.cz", 5432, "visio", "geovisio", "visio");
		DatabaseConnection conn = new PostgisConnection(set);
		ProjectionTransformer transformer = new ProjectionTransformer(900913, 4326, true);
		conn.connect();

		ArrayList<KmlItemBuilder> builders = new ArrayList<>();
		builders.add(new AgentKmlItemBuilder(conn, schemaName, 2 * 60 * 1000, "agents.kml", Color.GREEN, transformer));

		KmlItemBuilder.saveBuiltKmlItemsToSeparateFiles(builders, schemaName);

		conn.close();
	}

	public static void saveToKml(KmlItem output, String path) throws FileNotFoundException {
		Kml kml = new Kml();
		kml.createAndSetDocument().addToFeature(output.initFeatureForKml(null));

		kml.marshal(new File(path));
	}

	public static NonInterpolatedTimeKmlItem createLineVis(String schemaName, String tableName,
	        DatabaseConnection conn, final Color color, DescriptionFactory descriptionFactory) throws SQLException,
	        NoSuchAuthorityCodeException, FactoryException, TransformException {
		String columns = "*";
		String sql = "SELECT " + columns + " FROM " + schemaName + "." + tableName + " ORDER BY from_time";

		ResultSet result = conn.executeQueryWithFetchSize(sql, 10000);
		ProjectionTransformer transformer = new ProjectionTransformer(900913, 4326, true);

		GeometryFactory geometryFactory = new LineGeometryFactory();
		StyleFactory styleFactory = new LineStyleFactory(color, 2);

		NonInterpolatedTimeKmlItem kmlItem = new NonInterpolatedTimeKmlItem(transformer, styleFactory, geometryFactory,
		        24 * 3600 * 1000);
		int counter = 0;
		while (result.next()) {
			if (counter++ % 100000 == 0) {
				System.out.println("Read records: " + counter);
			}
			String passenger = result.getString("passenger");
			String taxiDriver = result.getString("taxi_driver");
			PostgisGeometry geom = (PostgisGeometry) result.getObject("geom");
			Timestamp timestamp = result.getTimestamp("from_time");
			// kmlItem.addTimeGeometry(passenger + taxiDriver,
			// geom.getGeometry(), timestamp.getTime(),
			// descriptionFactory.createDescription(result));
		}

		return kmlItem;
	}

}

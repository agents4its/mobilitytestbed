package cz.agents.dbtokmlexporter.darptestbed;

import java.awt.Color;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import org.apache.log4j.xml.DOMConfigurator;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.operation.TransformException;

import com.vividsolutions.jts.geom.Point;

import cz.agents.agentpolis.tools.geovisio.database.connection.DatabaseConnection;
import cz.agents.agentpolis.tools.geovisio.database.postgres.postgis.PostgisConnection;
import cz.agents.agentpolis.tools.geovisio.database.postgres.postgis.PostgisGeometry;
import cz.agents.agentpolis.tools.geovisio.projection.ProjectionTransformer;
import cz.agents.agentpolis.tools.geovisio.visualisation.VisualisationSettings;
import cz.agents.dbtokmlexporter.kmlitem.InterpolatedTimeLayerKmlItem;
import cz.agents.resultsvisio.kml.KmlBuilder;

/**
 * Hello world!
 * 
 */
public class DarpTestbedFromDbToInterpolatedKmlMain {
	public static void main(String[] args) throws ClassNotFoundException, SQLException, NoSuchAuthorityCodeException,
			FactoryException, TransformException, IOException {
		DOMConfigurator.configure("log4j.xml");
		VisualisationSettings set = new VisualisationSettings("mf.felk.cvut.cz", 5432, "visio", "geovisio", "visio",
				"http://mf.felk.cvut.cz:8080/geoserver", "admin", "geovisio");
		DatabaseConnection conn = new PostgisConnection(set.getDatabaseServerHost(), set.getDatabaseServerPort(),
				set.getDatabaseUser(), set.getDatabasePassword(), set.getDatabaseName());

		conn.connect();

		KmlBuilder builder = new KmlBuilder();
		// builder.addKmlItem(createPTDriverVis("tram", conn, Color.RED));
		// createPTDriverVis("bus", conn,
		// Color.CYAN).saveToKml("bus_drivers.kml");
		// createPTDriverVis("tram", conn, Color.CYAN).saveToKml("pokus.kml");
		createAgentVis("man", conn, Color.RED).saveToKml("passengers.kml");
		createTaxiVis("cabs", conn, Color.GREEN).saveToKml("taxis.kml");

		// builder.writeDataToFileAndCleanBuilder(new File("bus_drivers.kmz"));
		conn.close();
	}

	public static InterpolatedTimeLayerKmlItem createTaxiVis(String driverType, DatabaseConnection conn, Color color)
			throws SQLException, NoSuchAuthorityCodeException, FactoryException, TransformException {
		String schema = "testbed_sf";
		String table = "taxi_drivers";
		String columns = "agentid,geom,from_time";
		String sql = "SELECT " + columns + " FROM " + schema + "." + table + " ORDER BY from_time";
		// String sql =
		// "select * from jmk_full_200k_population_model.tram_drivers where id=24081";

		ResultSet result = conn.executeQueryWithFetchSize(sql, 10000);
		ProjectionTransformer transformer = new ProjectionTransformer(900913, 4326, true);

		long duration = 1 * 60 * 1000 - 1;
		InterpolatedTimeLayerKmlItem kmlItem = new InterpolatedTimeLayerKmlItem(transformer, duration, driverType,
				color);
		int counter = 0;
		while (result.next()) {
			if (counter++ % 100000 == 0) {
				System.out.println("Read records: " + counter);
			}
			String id = result.getString("agentid");
			PostgisGeometry geom = (PostgisGeometry) result.getObject("geom");
			Point point = (Point) geom.getGeometry();
			Timestamp timestamp = result.getTimestamp("from_time");
			kmlItem.addTimePoint(id, point, timestamp.getTime());

		}

		return kmlItem;
	}

	public static InterpolatedTimeLayerKmlItem createAgentVis(String driverType, DatabaseConnection conn, Color color)
			throws SQLException, NoSuchAuthorityCodeException, FactoryException, TransformException {
		String schema = "testbed_sf";
		String table = "passengers";
		String columns = "agentid,geom,from_time";
		String sql = "SELECT " + columns + " FROM " + schema + "." + table + " ORDER BY from_time";
		// String sql =
		// "select * from jmk_full_200k_population_model.tram_drivers where id=24081";

		ResultSet result = conn.executeQueryWithFetchSize(sql, 10000);
		ProjectionTransformer transformer = new ProjectionTransformer(900913, 4326, true);

		long duration = 1 * 60 * 1000 - 1;
		InterpolatedTimeLayerKmlItem kmlItem = new InterpolatedTimeLayerKmlItem(transformer, duration, driverType,
				color);
		int counter = 0;
		while (result.next()) {
			if (counter++ % 100000 == 0) {
				System.out.println("Read records: " + counter);
			}
			String id = result.getString("agentid");
			PostgisGeometry geom = (PostgisGeometry) result.getObject("geom");
			Point point = (Point) geom.getGeometry();
			Timestamp timestamp = result.getTimestamp("from_time");
			kmlItem.addTimePoint(id, point, timestamp.getTime());

		}

		return kmlItem;
	}

	public static InterpolatedTimeLayerKmlItem createPTDriverVis(String driverType, DatabaseConnection conn, Color color)
			throws SQLException, NoSuchAuthorityCodeException, FactoryException, TransformException {
		String schema = "jmk_full_200k_population_model";
		String table = driverType + "_drivers";
		String columns = "agentid,geom,from_time";
		String sql = "SELECT " + columns + " FROM " + schema + "." + table + "";
		// String sql =
		// "select * from jmk_full_200k_population_model.tram_drivers where id=24081";

		ResultSet result = conn.executeQueryWithFetchSize(sql, 10000);
		ProjectionTransformer transformer = new ProjectionTransformer(900913, 4326, true);

		long duration = 1 * 60 * 1000 - 1;
		InterpolatedTimeLayerKmlItem kmlItem = new InterpolatedTimeLayerKmlItem(transformer, duration, driverType,
				color);
		while (result.next()) {
			String id = result.getString("agentid");
			PostgisGeometry geom = (PostgisGeometry) result.getObject("geom");
			Point point = (Point) geom.getGeometry();
			Timestamp timestamp = result.getTimestamp("from_time");
			kmlItem.addTimePoint(id, point, timestamp.getTime());

		}

		return kmlItem;
	}
}

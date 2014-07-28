package cz.agents.dbtokmlexporter.darptestbed;

import java.awt.Color;
import java.io.File;
import java.io.FileNotFoundException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

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
import cz.agents.dbtokmlexporter.factory.OneStringDescriptionFactory;
import cz.agents.dbtokmlexporter.factory.TableColumnsDescriptionFactory;
import cz.agents.dbtokmlexporter.factory.geometry.GeometryFactory;
import cz.agents.dbtokmlexporter.factory.geometry.LineGeometryFactory;
import cz.agents.dbtokmlexporter.factory.geometry.PointGeometryFactory;
import cz.agents.dbtokmlexporter.factory.style.IconStyleFactory;
import cz.agents.dbtokmlexporter.factory.style.LineStyleFactory;
import cz.agents.dbtokmlexporter.factory.style.StyleFactory;
import cz.agents.dbtokmlexporter.kmlitem.NonInterpolatedTimeKmlItem;
import cz.agents.resultsvisio.kml.KmlItem;
import de.micromata.opengis.kml.v_2_2_0.Kml;

/**
 * 
 * @author Marek Cuchy
 * 
 */
public class DarpTestbedFromDbToNonInterpolatedKmlMain {

	private static final Logger logger = Logger.getLogger(DarpTestbedFromDbToNonInterpolatedKmlMain.class);

	public static void main(String[] args) throws NoSuchAuthorityCodeException, SQLException, FactoryException,
			TransformException, FileNotFoundException, ClassNotFoundException {

		// String schemaName = "testbed_dublin";
		String schemaName = "testbed_san_francisco_3_r1000_d100_dutrue_putrue";
		// String schemaName = "testbed_sf";

		new File(schemaName).mkdir();

		DOMConfigurator.configure("log4j.xml");

		DatabaseConnectionSettings set = new DatabaseConnectionSettings("mf.felk.cvut.cz", 5432, "visio", "geovisio", "visio");
		DatabaseConnection conn = new PostgisConnection(set);
		conn.connect();

		OneStringDescriptionFactory descfac = new OneStringDescriptionFactory("agentid");
		saveToKml(createPointVis(schemaName, "taxi_drivers", "", conn, "data/car.png", descfac), schemaName
				+ "\\taxi_non_interpolated.kml");
		List<String> requestColumnNames = new ArrayList<>();
		requestColumnNames.add("agentid");
		requestColumnNames.add("request_status");
		requestColumnNames.add("request_departure_min");
		requestColumnNames.add("request_departure_max");
		requestColumnNames.add("request_arrival_min");
		requestColumnNames.add("request_arrival_max");
		DescriptionFactory requestDescription = new TableColumnsDescriptionFactory(requestColumnNames);

		saveToKml(
				createPointVis(schemaName, "passengers",
						"where from_time BETWEEN request_departure_min AND request_arrival_max", conn,
						"data/passenger.png", requestDescription), schemaName + "\\active_passengers_non_interpolated.kml");
		// saveToKml(
		// createPointVis(
		// schemaName,
		// "passengers",
		// "where request_status LIKE '%DELAY%'",
		// conn, Color.RED, "man", requestDescription), schemaName
		// + "\\delayed_passengers_non_interpolated.kml");
		saveToKml(
				createPointVis(schemaName, "passengers",
						"where from_time NOT BETWEEN request_departure_min AND request_arrival_max", conn, 
						"data/passenger.png", requestDescription), schemaName + "\\inactive_passengers_non_interpolated.kml");
		// System.out.println(Color.CYAN.getRGB());

		List<String> pairColumnNames = new ArrayList<>();
		pairColumnNames.add("passenger");
		pairColumnNames.add("taxi_driver");
		DescriptionFactory pairDescription = new TableColumnsDescriptionFactory(pairColumnNames);
		saveToKml(createLineVis(schemaName, "taxi_to_passenger_pair", conn, Color.CYAN, pairDescription), schemaName
				+ "\\passengers_taxi_pair_non_interpolated.kml");

//		saveToKml(createDarpScreenOverlayVis(schemaName, conn), schemaName + "/screenoverlay.kml");

		conn.close();
	}

	public static void saveToKml(KmlItem output, String path) throws FileNotFoundException {
		Kml kml = new Kml();
		kml.createAndSetDocument().addToFeature(output.initFeatureForKml(null));

		kml.marshal(new File(path));

	}

	public static NonInterpolatedTimeKmlItem createPointVis(String schemaName, String tableName, String whereSql,
			DatabaseConnection conn, final String iconUrl, DescriptionFactory descriptionFactory)
			throws SQLException, NoSuchAuthorityCodeException, FactoryException, TransformException {
		String columns = "*";
		String sql = "SELECT " + columns + " FROM " + schemaName + "." + tableName + " " + whereSql
				+ " ORDER BY from_time";

		ResultSet result = conn.executeQueryWithFetchSize(sql, 10000);
		ProjectionTransformer transformer = new ProjectionTransformer(900913, 4326, true);

		GeometryFactory geometryFactory = new PointGeometryFactory();
		StyleFactory styleFactory = new IconStyleFactory(iconUrl, 0.75);

		NonInterpolatedTimeKmlItem kmlItem = new NonInterpolatedTimeKmlItem(transformer, styleFactory,
				geometryFactory, 24 * 3600 * 1000, false, 2 * 60 * 1000);
//		int counter = 0;
		while (result.next()) {
//			if (counter++ % 100000 == 0) {
//				System.out.println("Read records: " + counter);
//			}
			String id = result.getString("agentid");
			PostgisGeometry geom = (PostgisGeometry) result.getObject("geom");
			Timestamp timestamp = result.getTimestamp("from_time");
//			kmlItem.addTimeGeometry(id, geom.getGeometry(), timestamp.getTime(),
//					descriptionFactory.createDescription(result));
		}

		return kmlItem;
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

		NonInterpolatedTimeKmlItem kmlItem = new NonInterpolatedTimeKmlItem(transformer, styleFactory,
				geometryFactory, 24 * 3600 * 1000);
		int counter = 0;
		while (result.next()) {
			if (counter++ % 100000 == 0) {
				System.out.println("Read records: " + counter);
			}
			String passenger = result.getString("passenger");
			String taxiDriver = result.getString("taxi_driver");
			PostgisGeometry geom = (PostgisGeometry) result.getObject("geom");
			Timestamp timestamp = result.getTimestamp("from_time");
//			kmlItem.addTimeGeometry(passenger + taxiDriver, geom.getGeometry(), timestamp.getTime(),
//					descriptionFactory.createDescription(result));
		}

		return kmlItem;
	}

//	public static ScreenOverlayTimeKmlItem createDarpScreenOverlayVis(String schemaName, PostgisConnection conn)
//			throws SQLException, NoSuchAuthorityCodeException, FactoryException, TransformException {
//
//		String sql = "SELECT time.from_time AS time, succ.count AS success, failed.count AS failed, active.count AS active "
//				+ "FROM "
//				+ "(SELECT DISTINCT from_time FROM "
//				+ schemaName
//				+ ".passengers ORDER BY from_time) AS time "
//				+ "FULL JOIN "
//				+ "(SELECT from_time,count(*) FROM "
//				+ schemaName
//				+ ".passengers WHERE request_status = 'OUT_OF_VEHICLE' GROUP BY from_time ORDER BY from_time) AS succ "
//				+ "ON time.from_time = succ.from_time "
//				+ "FULL JOIN "
//				+ "(SELECT from_time,count(*) FROM "
//				+ schemaName
//				+ ".passengers WHERE request_status = 'OUT_OF_VEHICLE_WITH_DELAYED_ARRIVAL' GROUP BY from_time ORDER BY from_time) AS failed "
//				+ "ON time.from_time = failed.from_time "
//				+ "FULL JOIN "
//				+ "(SELECT from_time, count(*) FROM "
//				+ schemaName
//				+ ".passengers WHERE from_time BETWEEN request_departure_min AND request_arrival_max GROUP BY from_time ORDER BY from_time) AS active "
//				+ "ON " + "time.from_time = active.from_time";
//
//		logger.debug(sql);
//
//		ResultSet result = conn.executeQueryWithFetchSize(sql, 10000);
//
//		// StyleFactory styleFactory = new IconStyleFactory(iconName, color,
//		// 0.75);
//
//		ScreenOverlayTimeKmlItem kmlItem = new ScreenOverlayTimeKmlItem(2 * 60 * 1000);
//
//		kmlItem.addTextOverlay(0, "Active passengers: " + 0 + "\nSuccesfull/failed requests: " + 0 + "/" + 0);
//
//		while (result.next()) {
//			Timestamp timestamp = result.getTimestamp("time");
//			int succes = result.getInt("success");
//			int failed = result.getInt("failed");
//			int active = result.getInt("active");
//			// int count = result.getInt("count");
//			kmlItem.addTextOverlay(timestamp.getTime(), "Active passengers: " + active
//					+ "\nSuccesfull/failed requests: " + succes + "/" + failed);
//		}
//		return kmlItem;
//	}

}

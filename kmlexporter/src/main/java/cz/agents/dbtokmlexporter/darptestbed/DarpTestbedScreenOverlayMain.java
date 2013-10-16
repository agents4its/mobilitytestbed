package cz.agents.dbtokmlexporter.darptestbed;

import java.awt.Color;
import java.io.File;
import java.io.FileNotFoundException;
import java.net.URLEncoder;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.operation.TransformException;

import cz.agents.agentpolis.tools.geovisio.database.connection.DatabaseConnection;
import cz.agents.agentpolis.tools.geovisio.database.connection.DatabaseConnectionSettings;
import cz.agents.agentpolis.tools.geovisio.database.postgres.postgis.PostgisConnection;
import cz.agents.agentpolis.tools.geovisio.projection.ProjectionTransformer;
import cz.agents.agentpolis.tools.geovisio.visualisation.VisualisationSettings;
import cz.agents.dbtokmlexporter.factory.DescriptionFactory;
import cz.agents.dbtokmlexporter.factory.style.IconStyleFactory;
import cz.agents.dbtokmlexporter.factory.style.StyleFactory;
import cz.agents.dbtokmlexporter.kmlitem.ScreenOverlayTimeKmlItem;
import cz.agents.resultsvisio.kml.KmlItem;
import de.micromata.opengis.kml.v_2_2_0.Kml;

/**
 * 
 * @author Marek Cuchy
 * 
 */
public class DarpTestbedScreenOverlayMain {

	private static final Logger logger = Logger.getLogger(DarpTestbedScreenOverlayMain.class);

	public static void main(String[] args) throws ClassNotFoundException, SQLException, FileNotFoundException,
			NoSuchAuthorityCodeException, FactoryException, TransformException {

		//
		String schemaName = "testbed_dublin";

		// new File(schemaName).mkdir();

		DOMConfigurator.configure("log4j.xml");

		DatabaseConnectionSettings set = new DatabaseConnectionSettings("mf.felk.cvut.cz", 5432, "visio", "geovisio", "visio");
		DatabaseConnection conn = new PostgisConnection(set);
		conn.connect();

		saveToKml(createDarpScreenOverlayVis(schemaName, conn), schemaName + "/screenoverlay.kml");

		conn.close();

	}

	public static void saveToKml(KmlItem output, String path) throws FileNotFoundException {
		Kml kml = new Kml();
		kml.createAndSetDocument().addToFeature(output.initFeatureForKml(null));

		kml.marshal(new File(path));
	}

	public static ScreenOverlayTimeKmlItem createScreenOverlayVis(String schemaName, DatabaseConnection conn)
			throws SQLException, NoSuchAuthorityCodeException, FactoryException, TransformException {
		String sql = "SELECT from_time, count(*) FROM " + schemaName
				+ ".passengers WHERE request_status = 'NOT CONFIRMED'" + " GROUP BY from_time " + " ORDER BY from_time";

		logger.debug(sql);

		ResultSet result = conn.executeQueryWithFetchSize(sql, 10000);

		// StyleFactory styleFactory = new IconStyleFactory(iconName, color,
		// 0.75);

		ScreenOverlayTimeKmlItem kmlItem = new ScreenOverlayTimeKmlItem(2 * 60 * 1000);

		while (result.next()) {
			Timestamp timestamp = result.getTimestamp("from_time");
			int count = result.getInt("count");
			kmlItem.addTextOverlay(timestamp.getTime(), "Current passengers with not\n confirmed request: " + count);
		}
		return kmlItem;
	}

	public static ScreenOverlayTimeKmlItem createDarpScreenOverlayVis(String schemaName, DatabaseConnection conn)
			throws SQLException, NoSuchAuthorityCodeException, FactoryException, TransformException {
//		String sql = "SELECT succ.from_time AS succ_time,failed.from_time AS failed_time, succ.count AS success, failed.count AS failed "
//				+ "FROM "
//				+ "(SELECT from_time,count(*) FROM "
//				+ schemaName
//				+ ".passengers WHERE request_status = 'OUT_OF_VEHICLE' GROUP BY from_time ORDER BY from_time) AS succ "
//				+ "FULL JOIN"
//				+ "(SELECT from_time,count(*) FROM "
//				+ schemaName
//				+ ".passengers WHERE request_status = 'OUT_OF_VEHICLE_WITH_DELAYED_ARRIVAL' GROUP BY from_time ORDER BY from_time) as failed "
//				+ "on failed.from_time = succ.from_time";

		String sql = "SELECT time.from_time AS time, succ.count AS success, failed.count AS failed, active.count AS active "
				+ "FROM "
				+ "(SELECT DISTINCT from_time FROM "
				+ schemaName
				+ ".passengers ORDER BY from_time) AS time "
				+ "FULL JOIN "
				+ "(SELECT from_time,count(*) FROM "
				+ schemaName
				+ ".passengers WHERE request_status = 'OUT_OF_VEHICLE' GROUP BY from_time ORDER BY from_time) AS succ "
				+ "ON time.from_time = succ.from_time "
				+ "FULL JOIN "
				+ "(SELECT from_time,count(*) FROM "
				+ schemaName
				+ ".passengers WHERE request_status = 'OUT_OF_VEHICLE_WITH_DELAYED_ARRIVAL' GROUP BY from_time ORDER BY from_time) AS failed "
				+ "ON time.from_time = failed.from_time "
				+ "FULL JOIN "
				+ "(SELECT from_time, count(*) FROM "
				+ schemaName
				+ ".passengers WHERE from_time BETWEEN request_departure_min AND request_arrival_max GROUP BY from_time ORDER BY from_time) AS active "
				+ "ON " + "time.from_time = active.from_time";

		logger.debug(sql);

		ResultSet result = conn.executeQueryWithFetchSize(sql, 10000);

		// StyleFactory styleFactory = new IconStyleFactory(iconName, color,
		// 0.75);

		ScreenOverlayTimeKmlItem kmlItem = new ScreenOverlayTimeKmlItem(2 * 60 * 1000);

		kmlItem.addTextOverlay(0, "Active passengers: " + 0 + "\nSuccesfull/failed requests: " + 0+"/"+0);

		while (result.next()) {
			Timestamp timestamp = result.getTimestamp("time");
			int succes = result.getInt("success");
			int failed = result.getInt("failed");
			int active = result.getInt("active");
//			int count = result.getInt("count");
			kmlItem.addTextOverlay(timestamp.getTime(), "Active passengers: " + active + "\nSuccesfull/failed requests: " + succes+"/"+failed);
		}
		return kmlItem;
	}
}

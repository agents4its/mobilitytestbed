package cz.agents.dbtokmlexporter.darptestbed;

import java.awt.Color;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.xml.DOMConfigurator;

import com.vividsolutions.jts.geom.Point;

import cz.agents.agentpolis.tools.geovisio.database.connection.DatabaseConnection;
import cz.agents.agentpolis.tools.geovisio.database.connection.DatabaseConnectionSettings;
import cz.agents.agentpolis.tools.geovisio.database.postgres.postgis.PostgisConnection;
import cz.agents.agentpolis.tools.geovisio.database.postgres.postgis.PostgisGeometry;
import cz.agents.agentpolis.tools.geovisio.layer.BoundingBox;
import cz.agents.alite.googleearth.updates.Kmz;
import cz.agents.dbtokmlexporter.HeatMapKmlItem;
import cz.agents.dbtokmlexporter.factory.DescriptionFactory;
import cz.agents.dbtokmlexporter.factory.TableColumnsDescriptionFactory;
import cz.agents.dbtokmlexporter.factory.geometry.PointGeometryFactory;
import cz.agents.dbtokmlexporter.factory.style.IconStyleFactory;
import cz.agents.dbtokmlexporter.kmlitem.NotificationKmlItem;
import cz.agents.dbtokmlexporter.utils.TimeUtils;
import cz.agents.resultsvisio.kml.KmlItem;
import de.micromata.opengis.kml.v_2_2_0.Document;
import de.micromata.opengis.kml.v_2_2_0.Kml;

/**
 * 
 * @author Marek Cuchy
 * 
 */
public class RequestSuccessFailDensityMapMain {

	private final static BoundingBox DUBLIN_BOUNDING_BOX = new BoundingBox(-6.439877418513285, 53.26100248399494,
			-6.061446946007517, 53.40390930491094, 4326);
	private final static BoundingBox SAN_FRANCISCO_BOUNDING_BOX = new BoundingBox(-122.63928717885337,
			37.69060923316812, -122.2283977678971, 37.94719910401152, 4326);

	public static void main(String[] args) throws ClassNotFoundException, SQLException, IOException {
		String schemaName = "testbed_san_francisco_3_r1000_d100_dutrue_putrue";
		// String schemaName = "testbed_sf";

		new File(schemaName).mkdir();

		DOMConfigurator.configure("log4j.xml");

		DatabaseConnectionSettings sett = new DatabaseConnectionSettings("mf.felk.cvut.cz", 5432, "cuchy", "geovisio",
				"visio", schemaName);
		DatabaseConnection conn = new PostgisConnection(sett);
		conn.connect();

		// List<String> schemas = new ArrayList<>();
		// ResultSet resultSet =
		// conn.executeQuery("select * from information_schema.schemata where schema_name like 'testbed_%'");
		// while (resultSet.next()) {
		// schemas.add(resultSet.getString("schema_name"));
		// }

		// for (String string : schemas) {
		// saveToKml(createRequestSuccessHeatMaps(conn, string,2*60*1000),
		// schemaName + "/heatmaps.kml");
		// saveToKml(createRequestFailHeatMaps(conn, string,2*60*1000),
		// schemaName + "/heatmaps.kml");
		// }

		// kmz.
		// kmz.
//		 Kmz kmz = saveToKml(createRequestSuccessHeatMaps(conn, schemaName, 2
//		 * 60 * 1000), schemaName + "/heatmaps.kml");
//		 kmz.writeToStream(new FileOutputStream(schemaName +
//		 "/heatmap_success.kmz"));

//		saveToKml(createRequestSuccessNotification(conn, schemaName, 2 * 60 * 1000), "request_success_notification.kml");
		long interval = 2 * 60 * 1000;
		long notificationDuration = 10 * interval;

//		RequestNotificationKmlItemBuilder.createSuccessNotificationKmlItemBuilder(conn, schemaName, interval,
//				notificationDuration);
		conn.close();
	}

//	private static KmlItem createRequestSuccessHeatMaps(PostgisConnection conn, String schemaName, long interval)
//			throws SQLException {
//		HeatMapKmlItem kmlItem = new HeatMapKmlItem(SAN_FRANCISCO_BOUNDING_BOX, schemaName);
//
//		String sql = "SELECT st_transform(p1.geom,4326) AS geom FROM " + schemaName + ".passengers as p1," + schemaName
//				+ ".passengers as p2 " + "WHERE p1.agentid = p2.agentid AND p1.from_time = (p2.from_time + "
//				+ "interval '" + getInterval(interval) + "' ) "
//				+ "AND p1.request_status = 'OUT_OF_VEHICLE' AND p2.request_status != p1.request_status;";
//
//		ResultSet resultSet = conn.executeQuery(sql);
//		int count = 0;
//		while (resultSet.next()) {
//			PostgisGeometry geometryClassRepresentation = (PostgisGeometry) resultSet.getObject("geom");
//			Point point = (Point) geometryClassRepresentation.getGeometry();
//			kmlItem.addPoint(point);
//			count++;
//		}
//
//		System.out.println(schemaName + "- succesfull:" + count);
//
//		return kmlItem;
//	}

//	private static KmlItem createRequestFailHeatMaps(PostgisConnection conn, String schemaName, long interval)
//			throws SQLException {
//		HeatMapKmlItem kmlItem = new HeatMapKmlItem(SAN_FRANCISCO_BOUNDING_BOX, schemaName);
//
//		String sql = "SELECT st_transform(p1.geom,4326) AS geom FROM "
//				+ schemaName
//				+ ".passengers as p1,"
//				+ schemaName
//				+ ".passengers as p2 "
//				+ "WHERE p1.agentid = p2.agentid AND p1.from_time = (p2.from_time + "
//				+ "interval '"
//				+ getInterval(interval)
//				+ "' ) "
//				+ "AND p1.request_status = 'OUT_OF_VEHICLE_WITH_DELAYED_ARRIVAL' AND p2.request_status != p1.request_status;";
//
//		ResultSet resultSet = conn.executeQuery(sql);
//		int count = 0;
//		while (resultSet.next()) {
//			PostgisGeometry geometryClassRepresentation = (PostgisGeometry) resultSet.getObject("geom");
//			Point point = (Point) geometryClassRepresentation.getGeometry();
//			kmlItem.addPoint(point);
//			count++;
//		}
//
//		System.out.println(schemaName + "- failed:" + count);
//
//		return kmlItem;
//	}

	public static String getInterval(long millis) {
		return TimeUtils.formatMillisToString(millis, "HH:mm:ss.SSS");
	}

	public static Kmz saveToKml(KmlItem output, String path) throws FileNotFoundException {
		Kml kml = new Kml();
		Kmz kmz = new Kmz(kml);
		Document doc = kml.createAndSetDocument().addToFeature(output.initFeatureForKml(kmz));
		doc.setName(path);

		kml.marshal(new File(path));

		return kmz;
	}
}

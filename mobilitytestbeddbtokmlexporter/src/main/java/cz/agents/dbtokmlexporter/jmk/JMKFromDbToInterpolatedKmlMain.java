package cz.agents.dbtokmlexporter.jmk;

import java.awt.Color;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

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

public class JMKFromDbToInterpolatedKmlMain {
	public static void main(String[] args) throws ClassNotFoundException, SQLException, NoSuchAuthorityCodeException,
			FactoryException, TransformException, IOException {
		DOMConfigurator.configure("log4j.xml");
		VisualisationSettings set = new VisualisationSettings("mf.felk.cvut.cz", 5432, "visio", "geovisio", "visio",
				"http://mf.felk.cvut.cz:8080/geoserver", "admin", "geovisio");
		DatabaseConnection conn = new PostgisConnection(set.getDatabaseServerHost(), set.getDatabaseServerPort(),
				set.getDatabaseUser(), set.getDatabasePassword(), set.getDatabaseName());

		conn.connect();

		createAgentVis("placemark_circle", conn, Color.RED).saveToKml("jmk_agents_20000.kml");

		conn.close();
	}

	public static InterpolatedTimeLayerKmlItem createAgentVis(String driverType, DatabaseConnection conn, Color color)
			throws SQLException, NoSuchAuthorityCodeException, FactoryException, TransformException {
		String schema = "jmk_brno_exp";
		String table = "agents";
		String columns = "agentid,geom,from_time";
		String sql = "SELECT " + columns + " FROM " + schema + "." + table + " ORDER BY from_time";
		// String sql =
		// "select * from jmk_full_200k_population_model.tram_drivers where id=24081";

		Map<String, String> skipper = new HashMap<>();
		Map<String, Timestamp> timeSkipper = new HashMap<>();

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
			Timestamp timestamp = result.getTimestamp("from_time");

			Timestamp timestampTmp = timeSkipper.put(id, timestamp);
			if (timestampTmp != null
					&& (Math.abs(timestampTmp.getMinutes() - timestamp.getMinutes()) + (timestampTmp.getHours() - timestamp
							.getHours()) * 60) < 10) {
				continue;
			} else {
				timeSkipper.put(id, timestamp);
			}

			String geomString = skipper.get(id);
			if (geomString != null && geomString.equals(geom.toString())) {
				continue;
			} else {
				skipper.put(id, geom.toString());
			}

			Point point = (Point) geom.getGeometry();
			kmlItem.addTimePoint(id, point, timestamp.getTime());

		}

		return kmlItem;
	}

}

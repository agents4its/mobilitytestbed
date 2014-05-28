package cz.agents.dbtokmlexporter.darptestbed.kmlitembuilder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import org.apache.log4j.Logger;

import cz.agents.agentpolis.tools.geovisio.database.connection.DatabaseConnection;
import cz.agents.dbtokmlexporter.kmlitem.ScreenOverlayTimeKmlItem;
import cz.agents.dbtokmlexporter.kmlitem.builder.KmlItemBuilder;
import cz.agents.resultsvisio.kml.KmlItem;

/**
 *
 *@author Marek Cuchy
 *
 */
public class RequestNumbersScreenOverlayKmlItemBuilder extends KmlItemBuilder {
	
	private static final Logger logger = Logger.getLogger(RequestNumbersScreenOverlayKmlItemBuilder.class);
	
	public RequestNumbersScreenOverlayKmlItemBuilder(DatabaseConnection connection, String schemaName, long interval,
			String fileName) {
		super(connection, schemaName, interval, fileName);
	}
	public RequestNumbersScreenOverlayKmlItemBuilder(DatabaseConnection connection, String schemaName, long interval) {
		super(connection, schemaName, interval, "request_screenoverlay.kml");
	}

	@Override
	public KmlItem buildKmlItem() throws SQLException {
		logger.info("Preparing visualizations: "+this.getClass().getSimpleName());
		
		ScreenOverlayTimeKmlItem kmlItem = new ScreenOverlayTimeKmlItem(interval);
		kmlItem.addTextOverlay(0, "Active passengers: " + 0 + "\nSuccessful/failed requests: " + 0 + "/" + 0);
		
		String sql = "SELECT time.from_time AS time, succ.c AS success, failed.c AS failed, active.c AS active " 
				+ "FROM "
				+ "(SELECT DISTINCT from_time FROM "
				+ schemaName
				+ ".passengers ORDER BY from_time) AS time "
				+ "LEFT JOIN "
				+ "(SELECT from_time,count(*) as c FROM "
				+ schemaName
				+ ".passengers WHERE request_status = 'OUT_OF_VEHICLE' GROUP BY from_time ORDER BY from_time) AS succ "
				+ "ON time.from_time = succ.from_time "
				+ " LEFT JOIN "
				+ "(SELECT from_time,count(*) as c FROM "
				+ schemaName
				+ ".passengers WHERE request_status = 'OUT_OF_VEHICLE_WITH_DELAYED_ARRIVAL' OR request_status = 'REJECTED' GROUP BY from_time ORDER BY from_time) AS failed "
				+ "ON time.from_time = failed.from_time "
				+ " LEFT JOIN "
				+ "(SELECT from_time, count(*) as c FROM "
				+ schemaName
				+ ".passengers WHERE from_time BETWEEN request_departure_min AND request_arrival_max GROUP BY from_time ORDER BY from_time) AS active "
				+ "ON " + "time.from_time = active.from_time";
		
		ResultSet resultSet = connection.executeQuery(sql);
		while (resultSet.next()) {
			Timestamp timestamp = resultSet.getTimestamp("time");
			int success = resultSet.getInt("success");
			int failed = resultSet.getInt("failed");
			int active = resultSet.getInt("active");
			
			kmlItem.addTextOverlay(timestamp.getTime(), "Active passengers: " + active
					+ "\nSuccessful/failed requests: " + success + "/" + failed);
		}
		//logger.info(this.getClass().getSimpleName()+" finishes building.");
		return kmlItem;
	}

}

package cz.agents.dbtokmlexporter.darptestbed.kmlitembuilder;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.lang.ArrayUtils;
import org.apache.log4j.Logger;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

import cz.agents.agentpolis.tools.geovisio.database.connection.DatabaseConnection;
import cz.agents.dbtokmlexporter.HeatMapKmlItem;
import cz.agents.dbtokmlexporter.kmlitem.builder.KmlItemBuilder;
import cz.agents.resultsvisio.kml.KmlItem;

/**
 * 
 * @author Marek Cuchy
 * 
 */
public class RequestHeatMapKmlItemBuilder extends KmlItemBuilder {

	private static final Logger logger = Logger.getLogger(RequestHeatMapKmlItemBuilder.class);

	private final String[] heatmapRequestStatuses;

	public RequestHeatMapKmlItemBuilder(DatabaseConnection connection, String schemaName, long interval,
	        String fileName, String... heatmapRequestStatuses) {
		super(connection, schemaName, interval, fileName, true);
		this.heatmapRequestStatuses = heatmapRequestStatuses;
	}

	public static RequestHeatMapKmlItemBuilder createSuccessRequestHeatMapKmlItemBuilder(DatabaseConnection connection,
	        String schemaName, long interval) {
		return new RequestHeatMapKmlItemBuilder(connection, schemaName, interval, "success_heatmap.kmz",
		        "OUT_OF_VEHICLE");
	}

	public static RequestHeatMapKmlItemBuilder createFailRequestHeatMapKmlItemBuilder(DatabaseConnection connection,
	        String schemaName, long interval) {
		return new RequestHeatMapKmlItemBuilder(connection, schemaName, interval, "fail_heatmap.kmz",
		        "OUT_OF_VEHICLE_WITH_DELAYED_ARRIVAL", "REJECTED");
	}

	@Override
	public KmlItem buildKmlItem() throws SQLException {
		logger.info("Preparing visualizations: " + this.getClass().getSimpleName()
		        + " (This may take a while. Please wait.)");
		HeatMapKmlItem kmlItem = new HeatMapKmlItem(schemaName);

		String sql = "SELECT * FROM " + schemaName + ".passengers ORDER BY agentid, from_time";

		ResultSet resultSet = connection.executeQuery(sql);

		Record previousRecord;

		if (resultSet.next()) {
			previousRecord = new Record(resultSet);
			if (ArrayUtils.contains(heatmapRequestStatuses, previousRecord.status)) {
				kmlItem.addPoint(previousRecord.point);
			}
		} else {
			logger.warn("Request heatmap can't be built. Passenger logs are empty.");
			return kmlItem;
		}

		while (resultSet.next()) {
			Record record = new Record(resultSet);
			if (ArrayUtils.contains(heatmapRequestStatuses, record.status)
			        && !previousRecord.status.equals(record.status) && record.agentId.equals(previousRecord.agentId)) {
				kmlItem.addPoint(record.point);
			}
			if (ArrayUtils.contains(heatmapRequestStatuses, record.status)
			        && !record.agentId.equals(previousRecord.agentId)) {
				kmlItem.addPoint(record.point);
			}

			previousRecord = record;
		}
		return kmlItem;
	}

	private static class Record {
		public final Point point;
		public final String status;
		public final long time;
		public final String agentId;

		public Record(ResultSet resultSet) throws SQLException {
			super();
			Geometry geometry = (Geometry) resultSet.getObject("geom");
			this.point = (Point) geometry;
			this.status = resultSet.getString("request_status");
			this.time = resultSet.getTimestamp("from_time").getTime();
			this.agentId = resultSet.getString("agentid");

		}

		@Override
		public String toString() {
			return "Record [point=" + point + ", status=" + status + ", time=" + time + ", agentId=" + agentId + "]";
		}

	}

}

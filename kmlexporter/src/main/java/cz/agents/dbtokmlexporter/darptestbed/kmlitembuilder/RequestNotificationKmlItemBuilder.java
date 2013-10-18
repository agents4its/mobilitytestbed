package cz.agents.dbtokmlexporter.darptestbed.kmlitembuilder;

import java.awt.Color;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.apache.log4j.Logger;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

import cz.agents.agentpolis.tools.geovisio.database.connection.DatabaseConnection;
import cz.agents.dbtokmlexporter.factory.DescriptionFactory;
import cz.agents.dbtokmlexporter.factory.TableColumnsDescriptionFactory;
import cz.agents.dbtokmlexporter.factory.geometry.PointGeometryFactory;
import cz.agents.dbtokmlexporter.factory.style.IconStyleFactory;
import cz.agents.dbtokmlexporter.factory.style.StyleFactory;
import cz.agents.dbtokmlexporter.kmlitem.NotificationKmlItem;
import cz.agents.dbtokmlexporter.kmlitem.builder.KmlItemBuilder;
import cz.agents.resultsvisio.kml.KmlItem;

/**
 * 
 * @author Marek Cuchy
 * 
 */
public class RequestNotificationKmlItemBuilder extends KmlItemBuilder {

	private static final Logger logger = Logger.getLogger(RequestNotificationKmlItemBuilder.class);

	private static final String ICON_URL = "http://maps.google.com/mapfiles/kml/shapes/shaded_dot.png";

	private final long notificationDuration;
	private final String[] notifiedRequestStatuses;
	private final Color notificationColor;

	public RequestNotificationKmlItemBuilder(DatabaseConnection connection, String schemaName, long interval,
	        String fileName, long notificationDuration, Color notificationColor, String... notifiedRequestStatuses) {
		super(connection, schemaName, interval, fileName);
		this.notificationDuration = notificationDuration;
		this.notifiedRequestStatuses = notifiedRequestStatuses;
		this.notificationColor = notificationColor;
	}

	public static RequestNotificationKmlItemBuilder createSuccessNotificationKmlItemBuilder(
	        DatabaseConnection connection, String schemaName, long interval, long notificationDuration) {
		return new RequestNotificationKmlItemBuilder(connection, schemaName, interval,
		        "succes_request_notification.kml", notificationDuration, Color.GREEN, "OUT_OF_VEHICLE");
	}

	public static RequestNotificationKmlItemBuilder createFailNotificationKmlItemBuilder(DatabaseConnection connection,
	        String schemaName, long interval, long notificationDuration) {
		return new RequestNotificationKmlItemBuilder(connection, schemaName, interval, "fail_request_notification.kml",
		        notificationDuration, Color.RED, "OUT_OF_VEHICLE_WITH_DELAYED_ARRIVAL", "REJECTED");
	}

	@Override
	public KmlItem buildKmlItem() throws SQLException {

		logger.info("Preparing visualizations: " + this.getClass().getSimpleName()
		        + " (This may take a while. Please wait.)");
		List<String> additionalColumnNames = getAdditionalColumnNames();

		DescriptionFactory descriptionFactory = new TableColumnsDescriptionFactory(additionalColumnNames);
		StyleFactory styleFactory = new IconStyleFactory(ICON_URL, notificationColor, 1.0);

		NotificationKmlItem kmlItem = new NotificationKmlItem(styleFactory, new PointGeometryFactory(),
		        notificationDuration);

		String sql = "SELECT * FROM " + schemaName + ".passengers ORDER BY agentid, from_time";

		ResultSet resultSet = connection.executeQuery(sql);

		Record previousRecord;
		String description;

		if (resultSet.next()) {
			previousRecord = new Record(resultSet);
			description = descriptionFactory.createDescription(resultSet);
			if (ArrayUtils.contains(notifiedRequestStatuses, previousRecord.status)) {
				kmlItem.add(previousRecord.agentId,
				        convertJTSCoordinatesToKmlCoordinates(previousRecord.point.getCoordinates()),
				        previousRecord.time, description);
			}
		} else {
			logger.warn("Request heatmap can't be built. Passenger logs are empty.");
			return kmlItem;
		}

		while (resultSet.next()) {
			Record record = new Record(resultSet);

			if (ArrayUtils.contains(notifiedRequestStatuses, record.status)
			        && !previousRecord.status.equals(record.status) && record.agentId.equals(previousRecord.agentId)) {
				kmlItem.add(record.agentId, convertJTSCoordinatesToKmlCoordinates(record.point.getCoordinates()),
				        record.time, description);
			}
			if (ArrayUtils.contains(notifiedRequestStatuses, record.status)
			        && !record.agentId.equals(previousRecord.agentId)) {
				kmlItem.add(record.agentId, convertJTSCoordinatesToKmlCoordinates(record.point.getCoordinates()),
				        record.time, description);
			}
			previousRecord = record;
			description = descriptionFactory.createDescription(resultSet);
		}

		return kmlItem;
	}

	private static List<String> getAdditionalColumnNames() {
		List<String> additionalColumnNames = new ArrayList<>();
		additionalColumnNames.add("agentid");
		additionalColumnNames.add("request_status");
		additionalColumnNames.add("request_departure_min");
		additionalColumnNames.add("request_departure_max");
		additionalColumnNames.add("request_arrival_min");
		additionalColumnNames.add("request_arrival_max");
		return additionalColumnNames;
	}

	private static class Record {

		public final Point point;
		public final String status;
		public final String agentId;
		public final long time;

		public Record(ResultSet resultSet) throws SQLException {
			super();
			Geometry geometry = (Geometry) resultSet.getObject("geom");
			this.point = (Point) geometry;
			this.status = resultSet.getString("request_status");
			this.agentId = resultSet.getString("agentid");
			this.time = resultSet.getTimestamp("from_time").getTime();
		}

		@Override
		public String toString() {
			return "Record [point=" + point + ", status=" + status + ", agentId=" + agentId + ", time=" + time + "]";
		}
	}
}

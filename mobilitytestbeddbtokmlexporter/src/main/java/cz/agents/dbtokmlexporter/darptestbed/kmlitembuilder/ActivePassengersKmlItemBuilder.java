package cz.agents.dbtokmlexporter.darptestbed.kmlitembuilder;

import java.awt.Color;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import cz.agents.agentpolis.tools.geovisio.database.connection.DatabaseConnection;
import cz.agents.dbtokmlexporter.factory.geometry.PointGeometryFactory;
import cz.agents.dbtokmlexporter.factory.style.IconStyleFactory;
import cz.agents.dbtokmlexporter.kmlitem.builder.InterpolableTimeKmlItemBuilder;

/**
 * 
 * @author Marek Cuchy
 * 
 */
public class ActivePassengersKmlItemBuilder extends InterpolableTimeKmlItemBuilder {

	private static final String TABLE_NAME = "passengers";
//    private static final String WHERE_CLAUSE = " WHERE from_time BETWEEN request_departure_min AND " +
//            "request_arrival_max";
	private static final String WHERE_CLAUSE = " WHERE from_time BETWEEN " +
        "request_call_time AND request_arrival_max AND from_time <= successful_arrival_time" +
        " AND request_status != 'REJECTED'";
//        " AND request_status != 'CONFIRMED'";

	public ActivePassengersKmlItemBuilder(DatabaseConnection connection, String schemaName, long interval,
			String fileName, Color passengerColor, boolean interpolate) {
		super(connection, schemaName, interval, fileName, TABLE_NAME, WHERE_CLAUSE, 
				new IconStyleFactory("data/passenger.png", 0.75), new PointGeometryFactory(), interpolate, true);
	}
	public ActivePassengersKmlItemBuilder(DatabaseConnection connection, String schemaName, long interval,
			Color passengerColor, boolean interpolate) {
		this(connection, schemaName, interval, "active_passengers.kmz", passengerColor, interpolate);
	}

	protected List<String> getDescriptionColumnNames() {
		List<String> descriptionColumnNames = new ArrayList<>();
		descriptionColumnNames.add("agentid");
		descriptionColumnNames.add("request_status");
		descriptionColumnNames.add("request_departure_min");
		descriptionColumnNames.add("request_departure_max");
		descriptionColumnNames.add("request_arrival_min");
		descriptionColumnNames.add("request_arrival_max");
        descriptionColumnNames.add("successful_arrival_time");
        descriptionColumnNames.add("from_time");
        descriptionColumnNames.add("request_call_time");

		return descriptionColumnNames;
	}

	@Override
	protected String getRecordId(ResultSet resultSet) throws SQLException {
		return resultSet.getString("agentid");
	}
}

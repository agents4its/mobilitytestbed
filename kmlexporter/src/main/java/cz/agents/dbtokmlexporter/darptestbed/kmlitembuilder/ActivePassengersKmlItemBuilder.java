package cz.agents.dbtokmlexporter.darptestbed.kmlitembuilder;

import java.awt.Color;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import cz.agents.agentpolis.tools.geovisio.database.connection.DatabaseConnection;
import cz.agents.agentpolis.tools.geovisio.database.postgres.postgis.PostgisGeometry;
import cz.agents.dbtokmlexporter.factory.DescriptionFactory;
import cz.agents.dbtokmlexporter.factory.TableColumnsDescriptionFactory;
import cz.agents.dbtokmlexporter.factory.geometry.GeometryFactory;
import cz.agents.dbtokmlexporter.factory.geometry.PointGeometryFactory;
import cz.agents.dbtokmlexporter.factory.style.IconStyleFactory;
import cz.agents.dbtokmlexporter.factory.style.StyleFactory;
import cz.agents.dbtokmlexporter.kmlitem.NonInterpolatedTimeKmlItem;
import cz.agents.dbtokmlexporter.kmlitem.builder.KmlItemBuilder;
import cz.agents.dbtokmlexporter.kmlitem.builder.TimeKmlItemBuilder;
import cz.agents.resultsvisio.kml.KmlItem;

/**
 * 
 * @author Marek Cuchy
 * 
 */
public class ActivePassengersKmlItemBuilder extends TimeKmlItemBuilder {

	private static final String TABLE_NAME = "passengers";
	private static final String WHERE_CLAUSE = " WHERE from_time BETWEEN request_departure_min AND request_arrival_max ";

	private static final String ICON_NAME = "man";

	public ActivePassengersKmlItemBuilder(DatabaseConnection connection, String schemaName, long interval,
			String fileName, Color passengerColor) {
		super(connection, schemaName, interval, fileName, TABLE_NAME, WHERE_CLAUSE, new IconStyleFactory(ICON_NAME,
				passengerColor, 0.75), new PointGeometryFactory(), true);
	}
	public ActivePassengersKmlItemBuilder(DatabaseConnection connection, String schemaName, long interval,
			Color passengerColor) {
		this(connection, schemaName, interval, "active_passengers.kml", passengerColor);
	}

	protected List<String> getDescriptionColumnNames() {
		List<String> descriptionColumnNames = new ArrayList<>();
		descriptionColumnNames.add("agentid");
		descriptionColumnNames.add("request_status");
		descriptionColumnNames.add("request_departure_min");
		descriptionColumnNames.add("request_departure_max");
		descriptionColumnNames.add("request_arrival_min");
		descriptionColumnNames.add("request_arrival_max");
		return descriptionColumnNames;
	}

	@Override
	protected String getRecordId(ResultSet resultSet) throws SQLException {
		return resultSet.getString("agentid");
	}
}

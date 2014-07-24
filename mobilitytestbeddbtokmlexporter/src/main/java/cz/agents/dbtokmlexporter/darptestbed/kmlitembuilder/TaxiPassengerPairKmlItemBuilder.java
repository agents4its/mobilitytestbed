package cz.agents.dbtokmlexporter.darptestbed.kmlitembuilder;

import java.awt.Color;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import cz.agents.agentpolis.tools.geovisio.database.connection.DatabaseConnection;
import cz.agents.dbtokmlexporter.factory.geometry.LineGeometryFactory;
import cz.agents.dbtokmlexporter.factory.style.LineStyleFactory;
import cz.agents.dbtokmlexporter.kmlitem.builder.InterpolableTimeKmlItemBuilder;

/**
 * 
 * @author Marek Cuchy
 * 
 */
public class TaxiPassengerPairKmlItemBuilder extends InterpolableTimeKmlItemBuilder {

	private static final String TABLE_NAME = "taxi_to_passenger_pair";

	public TaxiPassengerPairKmlItemBuilder(DatabaseConnection connection, String schemaName, long interval,
			String fileName, Color pairColor) {
		super(connection, schemaName, interval, fileName, TABLE_NAME, "", new LineStyleFactory(pairColor, 2),
				new LineGeometryFactory(), false, false);
	}

	public TaxiPassengerPairKmlItemBuilder(DatabaseConnection connection, String schemaName, long interval,
			Color pairColor) {
		this(connection, schemaName, interval, "taxi_passenger_pair.kml", Color.CYAN);
	}

	protected List<String> getDescriptionColumnNames() {
		List<String> descriptionColumnNames = new ArrayList<>();
		descriptionColumnNames.add("passenger");
		descriptionColumnNames.add("taxi_driver");
		return descriptionColumnNames;
	}

	@Override
	protected String getRecordId(ResultSet resultSet) throws SQLException {
		String passenger = resultSet.getString("passenger");
		String taxiDriver = resultSet.getString("taxi_driver");
		return passenger + taxiDriver;
	}

}

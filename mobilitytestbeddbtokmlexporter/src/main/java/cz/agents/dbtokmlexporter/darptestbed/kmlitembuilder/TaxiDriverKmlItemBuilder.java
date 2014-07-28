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
public class TaxiDriverKmlItemBuilder extends InterpolableTimeKmlItemBuilder {

	private static final String TABLE_NAME = "taxi_drivers";

	public TaxiDriverKmlItemBuilder(DatabaseConnection connection, String schemaName, long interval, String fileName,
			Color driverColor, boolean interpolate) {
		super(connection, schemaName, interval, fileName, TABLE_NAME, " ", new IconStyleFactory("data/car.png", 
				0.75), new PointGeometryFactory(), interpolate, true);
	}

	public TaxiDriverKmlItemBuilder(DatabaseConnection connection, String schemaName, long interval, Color driverColor,
                                    boolean interpolate) {
		this(connection, schemaName, interval, "taxi_driver.kmz", driverColor, interpolate);
	}

	@Override
	protected List<String> getDescriptionColumnNames() {
		List<String> descriptionColumnNames = new ArrayList<>();
		descriptionColumnNames.add("agentid");
		return descriptionColumnNames;
	}

	@Override
	protected String getRecordId(ResultSet resultSet) throws SQLException {
		return resultSet.getString("agentid");
	}

}

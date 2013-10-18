package cz.agents.dbtokmlexporter.darptestbed.kmlitembuilder;

import java.awt.Color;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import cz.agents.agentpolis.tools.geovisio.database.connection.DatabaseConnection;
import cz.agents.dbtokmlexporter.factory.geometry.PointGeometryFactory;
import cz.agents.dbtokmlexporter.factory.style.IconStyleFactory;
import cz.agents.dbtokmlexporter.kmlitem.builder.TimeKmlItemBuilder;

/**
 * 
 * @author Marek Cuchy
 * 
 */
public class TaxiDriverKmlItemBuilder extends TimeKmlItemBuilder {

	private static final String TABLE_NAME = "taxi_drivers";
	private static final String ICON_URL = "http://earth.google.com/images/kml-icons/track-directional/track-0.png";
	private static final boolean INTERPOLATE = true;

	public TaxiDriverKmlItemBuilder(DatabaseConnection connection, String schemaName, long interval, String fileName,
			Color driverColor) {
		super(connection, schemaName, interval, fileName, TABLE_NAME, " ", new IconStyleFactory(ICON_URL, driverColor,
				0.75), new PointGeometryFactory(), INTERPOLATE);
	}

	public TaxiDriverKmlItemBuilder(DatabaseConnection connection, String schemaName, long interval, Color driverColor) {
		this(connection, schemaName, interval, "taxi_driver.kml", driverColor);
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

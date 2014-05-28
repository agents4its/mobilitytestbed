package cz.agents.dbtokmlexporter.darptestbed;

import java.awt.Color;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import cz.agents.dbtokmlexporter.kmlitem.builder.SimpleKmlItemBuilder;
import org.apache.log4j.Logger;

import cz.agents.agentpolis.tools.geovisio.database.connection.DatabaseConnection;
import cz.agents.agentpolis.tools.geovisio.database.connection.DatabaseConnectionSettings;
import cz.agents.agentpolis.tools.geovisio.database.h2.H2InMemoryConnection;
import cz.agents.agentpolis.tools.geovisio.database.postgres.postgis.PostgisConnection;
import cz.agents.dbtokmlexporter.darptestbed.kmlitembuilder.ActivePassengersKmlItemBuilder;
import cz.agents.dbtokmlexporter.darptestbed.kmlitembuilder.PassengerTaxiDriverPairKmlItemBuilder;
import cz.agents.dbtokmlexporter.darptestbed.kmlitembuilder.RequestHeatMapKmlItemBuilder;
import cz.agents.dbtokmlexporter.darptestbed.kmlitembuilder.RequestNotificationKmlItemBuilder;
import cz.agents.dbtokmlexporter.darptestbed.kmlitembuilder.RequestNumbersScreenOverlayKmlItemBuilder;
import cz.agents.dbtokmlexporter.darptestbed.kmlitembuilder.TaxiDriverKmlItemBuilder;
import cz.agents.dbtokmlexporter.darptestbed.kmlitembuilder.TaxiPassengerPairKmlItemBuilder;
import cz.agents.dbtokmlexporter.kmlitem.builder.KmlItemBuilder;

/**
 * 
 * @author Marek Cuchy
 * 
 */
public class DarpTestbedKmlVisualisator {
	
	private static final Logger logger = Logger.getLogger(DarpTestbedKmlVisualisator.class);

	private final DatabaseConnectionSettings settings;
	private final int interval;
	private final String schemaName;
	private final String outputFolder;
    private final boolean interpolate;

    public DarpTestbedKmlVisualisator(DatabaseConnectionSettings settings, int interval, String schemaName,
			String outputFolder, boolean interpolate) {
		super();
		this.settings = settings;
		this.interval = interval;
		this.schemaName = schemaName;
		this.outputFolder = outputFolder;
        this.interpolate = interpolate;
	}

	public void visualize() {
		DatabaseConnection conn = new H2InMemoryConnection(settings);
		try {
			conn.connect();
			logger.info("Preparing KML visualization...");
			
			List<SimpleKmlItemBuilder> itemBuilders = new ArrayList<>();
			itemBuilders.add(new RequestNumbersScreenOverlayKmlItemBuilder(conn, schemaName, interval));
			itemBuilders.add(new ActivePassengersKmlItemBuilder(conn, schemaName, interval, Color.CYAN, interpolate));
			itemBuilders.add(new TaxiDriverKmlItemBuilder(conn, schemaName, interval, Color.YELLOW, interpolate));
//			itemBuilders.add(new TaxiPassengerPairKmlItemBuilder(conn, schemaName, interval, Color.MAGENTA));
			itemBuilders.add(new PassengerTaxiDriverPairKmlItemBuilder(conn, schemaName, interval));
			
			itemBuilders.add(RequestNotificationKmlItemBuilder.createSuccessNotificationKmlItemBuilder(conn,
					schemaName, interval, interval * 10));
			itemBuilders.add(RequestNotificationKmlItemBuilder.createFailNotificationKmlItemBuilder(conn, schemaName,
					interval, interval * 10));
			itemBuilders.add(RequestHeatMapKmlItemBuilder.createSuccessRequestHeatMapKmlItemBuilder(conn, schemaName,
					interval));
			itemBuilders.add(RequestHeatMapKmlItemBuilder.createFailRequestHeatMapKmlItemBuilder(conn, schemaName,
					interval));
			KmlItemBuilder.saveBuiltKmlItemsToSeparateFiles(itemBuilders, outputFolder);
			
			conn.close();
		} catch (ClassNotFoundException | SQLException | IOException e) {
			logger.error("Export to kml cant't be done.",e);
		}
	}
}

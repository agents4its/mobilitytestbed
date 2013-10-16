package cz.agents.dbtokmlexporter.darptestbed;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

import cz.agents.agentpolis.tools.geovisio.database.connection.DatabaseConnection;
import cz.agents.agentpolis.tools.geovisio.database.connection.DatabaseConnectionSettings;
import cz.agents.agentpolis.tools.geovisio.database.postgres.postgis.PostgisConnection;
import cz.agents.dbtokmlexporter.darptestbed.kmlitembuilder.ActivePassengersKmlItemBuilder;
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
public class Main {

	private static final Logger logger = Logger.getLogger(Main.class);
//
	public static void main(String[] args) throws ClassNotFoundException, SQLException, IOException {
		DOMConfigurator.configure("log4j.xml");

		String schemaName = "testbed_san_francisco_0_r1000_d500_dufalse_pufalse";
		File file = new File(schemaName);
		logger.debug("Folder deleted: " + file.delete());
		logger.debug("Folder created: " + file.mkdir());

		DatabaseConnectionSettings set = new DatabaseConnectionSettings("mf.felk.cvut.cz", 5432, "opengeo", "opengeo",
				"visio", schemaName);
		long interval = 2 * 60 * 1000;

		DatabaseConnection conn = new PostgisConnection(set);
		conn.connect();

		List<KmlItemBuilder> itemBuilders = new ArrayList<>();
//		
		itemBuilders.add(new RequestNumbersScreenOverlayKmlItemBuilder(conn, schemaName, interval));
		itemBuilders.add(new ActivePassengersKmlItemBuilder(conn, schemaName, interval, Color.CYAN));
		itemBuilders.add(new TaxiDriverKmlItemBuilder(conn, schemaName, interval, Color.YELLOW));
		itemBuilders.add(new TaxiPassengerPairKmlItemBuilder(conn, schemaName, interval, Color.MAGENTA));
		itemBuilders.add(RequestNotificationKmlItemBuilder.createSuccessNotificationKmlItemBuilder(conn, schemaName,
				interval, interval * 10));
		itemBuilders.add(RequestNotificationKmlItemBuilder.createFailNotificationKmlItemBuilder(conn, schemaName,
				interval, interval * 10));
		itemBuilders.add(RequestHeatMapKmlItemBuilder.createSuccessRequestHeatMapKmlItemBuilder(conn, schemaName,
				interval));
		itemBuilders.add(RequestHeatMapKmlItemBuilder
				.createFailRequestHeatMapKmlItemBuilder(conn, schemaName, interval));

		KmlItemBuilder.saveBuiltKmlItemsToSeparateFiles(itemBuilders, schemaName);

		conn.close();

		// save
	}

}

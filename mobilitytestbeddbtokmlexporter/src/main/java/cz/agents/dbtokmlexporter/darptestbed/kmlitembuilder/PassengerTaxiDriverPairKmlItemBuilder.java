package cz.agents.dbtokmlexporter.darptestbed.kmlitembuilder;

import java.awt.Color;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import org.apache.log4j.Logger;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

import cz.agents.agentpolis.tools.geovisio.database.connection.DatabaseConnection;
import cz.agents.dbtokmlexporter.factory.DescriptionFactory;
import cz.agents.dbtokmlexporter.factory.TableColumnsDescriptionFactory;
import cz.agents.dbtokmlexporter.factory.geometry.GeometryFactory;
import cz.agents.dbtokmlexporter.factory.geometry.LineGeometryFactory;
import cz.agents.dbtokmlexporter.factory.style.LineStyleFactory;
import cz.agents.dbtokmlexporter.factory.style.StyleFactory;
import cz.agents.dbtokmlexporter.kmlitem.NonInterpolatedTimeKmlItem;
import cz.agents.dbtokmlexporter.kmlitem.builder.KmlItemBuilder;
import cz.agents.resultsvisio.kml.KmlItem;

/**
 * 
 * @author Marek Cuchy
 * 
 */
public class PassengerTaxiDriverPairKmlItemBuilder extends KmlItemBuilder {

	private static final Logger logger = Logger.getLogger(PassengerTaxiDriverPairKmlItemBuilder.class);

	private final StyleFactory styleFactory = new LineStyleFactory(Color.CYAN, 2);
	private final GeometryFactory geometryFactory = new LineGeometryFactory();
	private final DescriptionFactory descriptionFactory;

	public PassengerTaxiDriverPairKmlItemBuilder(DatabaseConnection connection, String schemaName, long interval) {
		super(connection, schemaName, interval, "taxi_passenger_pair.kml");
		this.descriptionFactory = new TableColumnsDescriptionFactory("PASSENGER", "TAXI_DRIVER");
	}

	@Override
	public KmlItem buildKmlItem() throws SQLException {
		logger.info("Preparing visualizations: " + this.getClass().getSimpleName());
		NonInterpolatedTimeKmlItem kmlItem = new NonInterpolatedTimeKmlItem(styleFactory, geometryFactory, 0, false,
		        interval);

		String sql = " SELECT " + "pas.agentid," + "pas.geom AS p1," + " taxi.geom AS p2," + "pas.from_time, "
		        + "pas.agentid AS passenger, " + "taxi.agentid AS taxi_driver, "
                + "pas.request_status AS status"
                + " FROM "
		        + "passengers AS pas INNER JOIN "
		        + "taxi_drivers AS taxi ON pas.current_driver_id = taxi.agentid AND pas.from_time = taxi.from_time "
                + "AND pas.request_status != 'OUT_OF_VEHICLE' "
                + "AND pas.request_status != 'OUT_OF_VEHICLE_WITH_DELAYED_ARRIVAL'"
                + "AND pas.request_status != 'REJECTED'";

		ResultSet resultSet = connection.executeQueryWithFetchSize(sql, 10000);
		while (resultSet.next()) {
			String id = getRecordId(resultSet);
			Geometry geom = getGeometry(resultSet, "geom");
			Timestamp timestamp = resultSet.getTimestamp("from_time");

			kmlItem.addTimeGeometry(id, convertJTSCoordinatesToKmlCoordinates(geom.getCoordinates()),
			        timestamp.getTime(), descriptionFactory.createDescription(resultSet));
		}
		return kmlItem;
	}

	@Override
	public Geometry getGeometry(ResultSet resultSet, String columnName) throws SQLException {
		Point p1 = (Point) resultSet.getObject("p1");
		Point p2 = (Point) resultSet.getObject("p2");

		Coordinate[] c = new Coordinate[]
			{ p1.getCoordinate(), p2.getCoordinate() };

		return new com.vividsolutions.jts.geom.GeometryFactory().createLineString(c);
	}

	private String getRecordId(ResultSet resultSet) throws SQLException {
		return resultSet.getString("agentid");
	}

}

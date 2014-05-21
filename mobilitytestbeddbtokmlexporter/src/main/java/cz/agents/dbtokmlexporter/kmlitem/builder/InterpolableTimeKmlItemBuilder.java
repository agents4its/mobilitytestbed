package cz.agents.dbtokmlexporter.kmlitem.builder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

import cz.agents.dbtokmlexporter.kmlitem.InterpolatedTimeKmlItem;
import cz.agents.resultsvisio.kml.TimeKmlItem;
import org.apache.log4j.Logger;

import com.vividsolutions.jts.geom.Geometry;

import cz.agents.agentpolis.tools.geovisio.database.connection.DatabaseConnection;
import cz.agents.dbtokmlexporter.factory.DescriptionFactory;
import cz.agents.dbtokmlexporter.factory.TableColumnsDescriptionFactory;
import cz.agents.dbtokmlexporter.factory.geometry.GeometryFactory;
import cz.agents.dbtokmlexporter.factory.style.StyleFactory;
import cz.agents.dbtokmlexporter.kmlitem.NonInterpolatedTimeKmlItem;
import cz.agents.resultsvisio.kml.KmlItem;

/**
 * 
 * @author Marek Cuchy
 * 
 */
public abstract class InterpolableTimeKmlItemBuilder extends KmlItemBuilder {
	
	private static final Logger logger = Logger.getLogger(InterpolableTimeKmlItemBuilder.class);
	
	private final String tableName;
	private final String whereClause;
	
	private final StyleFactory styleFactory;
	private final GeometryFactory geometryFactory;
	private final DescriptionFactory descriptionFactory;

    private final boolean interpolated;

	public InterpolableTimeKmlItemBuilder(DatabaseConnection connection, String schemaName, long interval,
                                          String fileName, String tableName, String whereClause, StyleFactory styleFactory,
                                          GeometryFactory geometryFactory, boolean interpolated) {
		super(connection, schemaName, interval, fileName);
		this.tableName = tableName;
		this.whereClause = whereClause;
		this.styleFactory = styleFactory;
		this.geometryFactory = geometryFactory;
		this.descriptionFactory = getDescriptionFactory();
        this.interpolated = interpolated;
	}

	@Override
	public KmlItem buildKmlItem() throws SQLException {
		logger.info("Preparing visualizations: "+this.getClass().getSimpleName());
        TimeKmlItem kmlItem;
        if (interpolated)
            kmlItem = getInterpolatedTimeKmlItem();
        else
            kmlItem = getNonInterpolatedTimeKmlItem();

        String sql = "SELECT " +  " * FROM " + schemaName + "." + tableName + " "
				+ whereClause + " ORDER BY from_time";
		
		ResultSet resultSet = connection.executeQueryWithFetchSize(sql, 10000);
		while (resultSet.next()) {
			String id = getRecordId(resultSet);
			Geometry geom = getGeometry(resultSet, "geom");
			Timestamp timestamp = resultSet.getTimestamp("from_time");

			kmlItem.addTimeGeometry(id, convertJTSCoordinatesToKmlCoordinates(geom.getCoordinates()),
					timestamp.getTime(), descriptionFactory.createDescription(resultSet));
		}
		//logger.info(this.getClass().getSimpleName()+" finishes building.");
		return kmlItem;
	}

    private InterpolatedTimeKmlItem getInterpolatedTimeKmlItem() {
        return new InterpolatedTimeKmlItem(styleFactory, geometryFactory, 0, false, interval);
    }


    private NonInterpolatedTimeKmlItem getNonInterpolatedTimeKmlItem() {
        return new NonInterpolatedTimeKmlItem(styleFactory, geometryFactory, 0, false, interval);
    }

    private DescriptionFactory getDescriptionFactory(){
		return new TableColumnsDescriptionFactory(getDescriptionColumnNames());
	}
	
	protected abstract List<String> getDescriptionColumnNames();

	protected abstract String getRecordId(ResultSet resultSet) throws SQLException;

}

package cz.agents.dbtokmlexporter.kmlitem.builder;

import java.awt.Color;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

import org.apache.log4j.Logger;
import org.opengis.referencing.operation.TransformException;

import com.vividsolutions.jts.geom.Geometry;

import cz.agents.agentpolis.tools.geovisio.database.connection.DatabaseConnection;
import cz.agents.agentpolis.tools.geovisio.database.postgres.postgis.PostgisGeometry;
import cz.agents.dbtokmlexporter.factory.DescriptionFactory;
import cz.agents.dbtokmlexporter.factory.TableColumnsDescriptionFactory;
import cz.agents.dbtokmlexporter.factory.geometry.GeometryFactory;
import cz.agents.dbtokmlexporter.factory.style.StyleFactory;
import cz.agents.dbtokmlexporter.kmlitem.InterpolatedTimeLayerKmlItem;
import cz.agents.dbtokmlexporter.kmlitem.NonInterpolatedTimeKmlItem;
import cz.agents.resultsvisio.kml.KmlItem;
import de.micromata.opengis.kml.v_2_2_0.Style;

/**
 * 
 * @author Marek Cuchy
 * 
 */
public abstract class TimeKmlItemBuilder extends KmlItemBuilder {
	
	private static final Logger logger = Logger.getLogger(TimeKmlItemBuilder.class);
	private boolean interpolate;
	
	private final String tableName;
	private final String whereClause;
	
	private final StyleFactory styleFactory;
	private final GeometryFactory geometryFactory;
	private final DescriptionFactory descriptionFactory;

	public TimeKmlItemBuilder(DatabaseConnection connection, String schemaName, long interval,
			String fileName, String tableName, String whereClause, StyleFactory styleFactory,
			GeometryFactory geometryFactory, boolean interpolate) {
		super(connection, schemaName, interval, fileName);
		this.interpolate = interpolate;
		this.tableName = tableName;
		this.whereClause = whereClause;
		this.styleFactory = styleFactory;
		this.geometryFactory = geometryFactory;
		this.descriptionFactory = getDescriptionFactory();
		
	}

	@Override
	public KmlItem buildKmlItem() throws SQLException {
		logger.info("Preparing visualizations: "+this.getClass().getSimpleName());
		
		if (this.interpolate) {
			// interpolate the item movement
			Style s = styleFactory.createStyle();
			
			InterpolatedTimeLayerKmlItem kmlItem =new InterpolatedTimeLayerKmlItem(interval, s.getIconStyle().getIcon().getHref(), s.getIconStyle().getColor() ); 
			String sql = "SELECT " +  " * FROM " + schemaName + "." + tableName + " "
					+ whereClause + " ORDER BY agentid, from_time";
			
			ResultSet resultSet = connection.executeQueryWithFetchSize(sql, 10000);
			while (resultSet.next()) {
				String id = getRecordId(resultSet);
				Geometry geom = getGeometry(resultSet, "geom");
				Timestamp timestamp = resultSet.getTimestamp("from_time");
				try {
					kmlItem.addTimeCoordinate(id, geom.getCoordinate(), timestamp.getTime());
				} catch (TransformException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			return kmlItem;
		} else {
			// don't interpolate the item movement
			NonInterpolatedTimeKmlItem kmlItem = new NonInterpolatedTimeKmlItem(styleFactory, geometryFactory, 0, false, interval);
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
			return kmlItem;
		}
		
		//logger.info(this.getClass().getSimpleName()+" finishes building.");
	}
	
	private DescriptionFactory getDescriptionFactory(){
		return new TableColumnsDescriptionFactory(getDescriptionColumnNames());
	}
	
	protected abstract List<String> getDescriptionColumnNames();

	protected abstract String getRecordId(ResultSet resultSet) throws SQLException;

}

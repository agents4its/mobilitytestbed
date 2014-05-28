package cz.agents.dbtokmlexporter.jmk;

import java.awt.Color;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.opengis.referencing.operation.TransformException;

import com.vividsolutions.jts.geom.Geometry;

import cz.agents.agentpolis.tools.geovisio.database.connection.DatabaseConnection;
import cz.agents.agentpolis.tools.geovisio.database.postgres.postgis.PostgisGeometry;
import cz.agents.agentpolis.tools.geovisio.projection.ProjectionTransformer;
import cz.agents.dbtokmlexporter.factory.geometry.PointGeometryFactory;
import cz.agents.dbtokmlexporter.factory.style.IconStyleFactory;
import cz.agents.dbtokmlexporter.kmlitem.builder.InterpolableTimeKmlItemBuilder;

/**
 * 
 * @author Marek Cuchy
 * 
 */
public class AgentKmlItemBuilder extends InterpolableTimeKmlItemBuilder {

	private static final String TABLE_NAME = "agents";
	private static final String WHERE_CLAUSE = " ";

	private static final String ICON_NAME = "man";
	
	private final ProjectionTransformer transformer;

	public AgentKmlItemBuilder(DatabaseConnection connection, String schemaName, long interval, String fileName,
                               Color agentColor, ProjectionTransformer transformer, boolean interpolate) {
		super(connection, schemaName, interval, fileName, TABLE_NAME, WHERE_CLAUSE, new IconStyleFactory(ICON_NAME,
		        agentColor, 0.75), new PointGeometryFactory(), interpolate);
		this.transformer= transformer;
	}

	@Override
	public Geometry getGeometry(ResultSet resultSet, String columnName) throws SQLException {
		PostgisGeometry geom = (PostgisGeometry) resultSet.getObject(columnName);
		try {
	        return transformer.transform(geom.getGeometry());
        } catch (TransformException e) {
        	throw new IllegalArgumentException("Read geometry can't be transformed");
        }
	}

	@Override
	protected List<String> getDescriptionColumnNames() {
		List<String> descriptionColumnNames = new ArrayList<>();
//		descriptionColumnNames.add("agentid");
//		descriptionColumnNames.add("type");
//		descriptionColumnNames.add("state");
//		descriptionColumnNames.add("duration");
//		descriptionColumnNames.add("start_time");
//		descriptionColumnNames.add("end_time");
//		descriptionColumnNames.add("description");
//		descriptionColumnNames.add("remaining_activities");
		return descriptionColumnNames;
	}

	@Override
	protected String getRecordId(ResultSet resultSet) throws SQLException {
		return resultSet.getString("agentid");
	}

}

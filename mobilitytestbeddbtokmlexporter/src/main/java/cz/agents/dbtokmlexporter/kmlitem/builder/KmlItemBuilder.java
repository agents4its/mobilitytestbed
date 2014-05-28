package cz.agents.dbtokmlexporter.kmlitem.builder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.opengis.referencing.operation.TransformException;

import com.vividsolutions.jts.geom.Geometry;

import cz.agents.agentpolis.tools.geovisio.database.connection.DatabaseConnection;
import cz.agents.alite.googleearth.updates.Kmz;
import cz.agents.dbtokmlexporter.utils.TimeUtils;
import cz.agents.resultsvisio.kml.KmlItem;
import de.micromata.opengis.kml.v_2_2_0.Coordinate;
import de.micromata.opengis.kml.v_2_2_0.Document;
import de.micromata.opengis.kml.v_2_2_0.Folder;
import de.micromata.opengis.kml.v_2_2_0.Kml;

/**
 * 
 * @author Marek Cuchy
 * 
 */
public abstract class KmlItemBuilder extends SimpleKmlItemBuilder {

	protected final DatabaseConnection connection;
	protected final long interval;


	public KmlItemBuilder(DatabaseConnection connection, String schemaName, long interval, String fileName) {
		this(connection, schemaName, interval, fileName, false);
	}

	public KmlItemBuilder(DatabaseConnection connection, String schemaName, long interval, String fileName,
			boolean hasToBeSavedToKmz) {
		super(schemaName, fileName, hasToBeSavedToKmz);
		this.connection = connection;
		this.interval = interval;
	}
	
	public Geometry getGeometry(ResultSet resultSet, String columnName) throws SQLException{
		return (Geometry) resultSet.getObject(columnName);
	}

	public abstract KmlItem buildKmlItem() throws SQLException;

	public String getFileName() {
		return fileName;
	}

	protected String getTransformedGeomSql() {
		return "st_transform(geom,4326) as " + getTransformedGeomColumnName();
	}

	protected String getTransformedGeomColumnName() {
		return "tranformed_geom";
	}

}

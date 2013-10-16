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
public abstract class KmlItemBuilder {

	protected final DatabaseConnection connection;
	protected final String schemaName;
	protected final long interval;

	protected final String fileName;

	protected final boolean hasToBeSavedToKmz;

	public KmlItemBuilder(DatabaseConnection connection, String schemaName, long interval, String fileName) {
		this(connection, schemaName, interval, fileName, false);
	}

	public KmlItemBuilder(DatabaseConnection connection, String schemaName, long interval, String fileName,
			boolean hasToBeSavedToKmz) {
		super();
		this.connection = connection;
		this.schemaName = schemaName;
		this.interval = interval;
		this.fileName = fileName;
		this.hasToBeSavedToKmz = hasToBeSavedToKmz;
	}
	
	public Geometry getGeometry(ResultSet resultSet, String columnName) throws SQLException{
		return (Geometry) resultSet.getObject(columnName);
	}

	public abstract KmlItem buildKmlItem() throws SQLException;

	/**
	 * Save {@link KmlItem}s created by {@code builders} to one {@code kmz} file
	 * named {@code fileName}.
	 * 
	 * @param builders
	 *            that creates the {@code KmlItem}s to be saved.
	 * @param fileName
	 *            name of file to which the results are saved.
	 * @throws SQLException
	 * @throws IOException
	 */
	public static void saveBuiltKmlItemsToOneKmz(List<KmlItemBuilder> builders, String fileName) throws SQLException,
			IOException {
		Kml mainKml = new Kml();
		Kmz kmz = new Kmz(mainKml);
		Document doc = mainKml.createAndSetDocument();
		doc.setOpen(true);

		for (KmlItemBuilder builder : builders) {

			Folder folder = builder.buildKmlItem().initFeatureForKml(kmz);
			if (folder == null) {
				return;
			}
			folder.setName(builder.getFileName());
			doc.addToFeature(folder);

		}
		kmz.writeToStream(new FileOutputStream(fileName + ".kmz"));
	}

	/**
	 * Save {@link KmlItem}s created by {@code builders} each in separate file
	 * to folder named {@code folderName}.
	 * 
	 * @param builders
	 *            that creates the {@code KmlItem}s to be saved.
	 * @param folderName
	 *            name of folder to which the files are saved. Has to be without
	 *            ending {@code '/'}
	 * @throws SQLException
	 * @throws IOException
	 */
	public static void saveBuiltKmlItemsToSeparateFiles(List<KmlItemBuilder> builders, String folderName)
			throws SQLException, IOException {
		File folder = new File(folderName);
		folder.mkdir();

		if (!folder.exists()) {
			throw new IllegalArgumentException("Folder " + folderName
					+ " doesn't exist and the attempt to create it failed.");
		}

		for (KmlItemBuilder builder : builders) {

			String fileName = folderName + "/" + builder.fileName;
			if (builder.hasToBeSavedToKmz) {
				saveToKmz(builder.buildKmlItem(), fileName);
			} else {
				saveToKml(builder.buildKmlItem(), fileName);
			}

		}
	}

	public static void saveToKmz(KmlItem output, String path) throws FileNotFoundException, IOException {

		Kml kml = new Kml();
		Kmz kmz = new Kmz(kml);

		Folder folder = output.initFeatureForKml(kmz);
		if (folder == null) {
			return;
		}

		kml.createAndSetDocument().addToFeature(folder);

		kmz.writeToStream(new FileOutputStream(path));
	}

	public static void saveToKml(KmlItem output, String path) throws FileNotFoundException {
		Kml kml = new Kml();
		kml.createAndSetDocument().addToFeature(output.initFeatureForKml(null));

		kml.marshal(new File(path));

	}

	protected static String formatMillisToIntervalString(long millis) {
		return TimeUtils.formatMillisToString(millis, "HH:mm:ss.SSS");
	}

	protected static Coordinate[] convertJTSCoordinatesToKmlCoordinates(
			com.vividsolutions.jts.geom.Coordinate[] coordinates) {
		Coordinate[] coords = new Coordinate[coordinates.length];
		for (int i = 0; i < coordinates.length; i++) {
			coords[i] = new Coordinate(coordinates[i].x, coordinates[i].y);
		}
		return coords;
	}

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

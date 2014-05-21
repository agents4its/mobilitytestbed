package cz.agents.dbtokmlexporter.kmlitem;

import java.util.ArrayList;
import java.util.List;

import cz.agents.alite.googleearth.updates.Kmz;
import cz.agents.dbtokmlexporter.factory.geometry.GeometryFactory;
import cz.agents.dbtokmlexporter.factory.style.StyleFactory;
import cz.agents.resultsvisio.kml.KmlItem;
import cz.agents.resultsvisio.kml.util.TimeKmlFormater;
import de.micromata.opengis.kml.v_2_2_0.Coordinate;
import de.micromata.opengis.kml.v_2_2_0.Folder;
import de.micromata.opengis.kml.v_2_2_0.Geometry;
import de.micromata.opengis.kml.v_2_2_0.Placemark;
import de.micromata.opengis.kml.v_2_2_0.Style;
import de.micromata.opengis.kml.v_2_2_0.TimeSpan;

/**
 * Builds geometries that stay displayed only for duration set in constructor.
 * 
 * @author Marek Cuchy
 * 
 */
public class NotificationKmlItem implements KmlItem {

	private final StyleFactory styleFactory;
	private final GeometryFactory geometryFactory;
	private final long notificationDuration;

	private final List<Record> records = new ArrayList<>();

	/**
	 * 
	 * @param styleFactory
	 *            factory that creates style which is used for every geometryClassRepresentation
	 *            created by this {@link KmlItem}
	 * @param geometryFactory
	 *            factory that creates kml {@link Geometry} from coordinates
	 * @param notificationDuration
	 *            set for how long the geometryClassRepresentation will be displayed
	 */
	public NotificationKmlItem(StyleFactory styleFactory, GeometryFactory geometryFactory, long notificationDuration) {
		super();
		this.styleFactory = styleFactory;
		this.geometryFactory = geometryFactory;
		this.notificationDuration = notificationDuration;
	}

	/**
	 * Add new notification
	 * 
	 * @param id
	 * @param coords
	 *            position in GPS coordinates
	 * @param fromTime
	 *            time where the notification started (in millis)
	 * @param description
	 *            {@code String} that is used as a placemark description. HTML
	 *            can be used to format the description.
	 */
	public void add(String id, Coordinate[] coords, long fromTime, String description) {
		records.add(new Record(id, coords, fromTime, description));
	}

	@Override
	public Folder initFeatureForKml(Kmz kmz) {
		Folder folder = new Folder();
		Style style = styleFactory.createStyle();
		folder.addToStyleSelector(style);

		for (Record record : records) {
			Placemark p = new Placemark();
			p.setId(record.id);
			p.setDescription(record.description);
			p.setGeometry(geometryFactory.createGeometry(record.coords));
			p.withStyleUrl("#" + style.getId());

			TimeSpan timeSpan = new TimeSpan();
			timeSpan.setBegin(TimeKmlFormater.getTimeForKML(record.time));
			timeSpan.setEnd(TimeKmlFormater.getTimeForKML(record.time + notificationDuration - 1));

			p.withTimePrimitive(timeSpan);
			folder.addToFeature(p);
		}

		return folder;
	}

	private static class Record {
		public final String id;
		public final Coordinate[] coords;
		public final long time;
		public final String description;

		public Record(String id, Coordinate[] coords, long time, String description) {
			super();
			this.id = id;
			this.coords = coords;
			this.time = time;
			this.description = description;
		}
	}
}

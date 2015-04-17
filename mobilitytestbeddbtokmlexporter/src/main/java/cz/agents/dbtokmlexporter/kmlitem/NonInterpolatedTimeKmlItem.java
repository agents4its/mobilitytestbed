package cz.agents.dbtokmlexporter.kmlitem;

import cz.agents.agentpolis.tools.geovisio.projection.ProjectionTransformer;
import cz.agents.alite.googleearth.updates.Kmz;
import cz.agents.dbtokmlexporter.factory.geometry.GeometryFactory;
import cz.agents.dbtokmlexporter.factory.style.StyleFactory;
import cz.agents.dbtokmlexporter.kmlitem.InterpolatedTimeKmlItem.TimeRecords;
import cz.agents.resultsvisio.kml.TimeKmlItem;
import cz.agents.resultsvisio.kml.util.TimeKmlFormater;
import de.micromata.opengis.kml.v_2_2_0.*;

import org.apache.log4j.Logger;

import java.util.*;

/**
 * 
 * @author Marek Cuchy
 * 
 */
public class NonInterpolatedTimeKmlItem implements TimeKmlItem {

	private static final Logger logger = Logger.getLogger(NonInterpolatedTimeKmlItem.class);

	private final StyleFactory styleFactory;
	private final GeometryFactory geometryFactory;

	private final long endTime;
	/**
	 * Define if as end time of the last interval for some id is used
	 * {@code endTime} argument or {@code intervalDuration} argument.
	 */
	private final boolean useEndTime;
	private final long intervalDuration;

	public NonInterpolatedTimeKmlItem(ProjectionTransformer transformer, StyleFactory styleFactory,
			GeometryFactory geometryFactory, long endTime, boolean useEndTime, long intervalDuration) {
		super();
		this.styleFactory = styleFactory;
		this.geometryFactory = geometryFactory;
		this.endTime = endTime;
		this.useEndTime = useEndTime;
		this.intervalDuration = intervalDuration;
	}

	public NonInterpolatedTimeKmlItem(StyleFactory styleFactory, GeometryFactory geometryFactory, long endTime,
			boolean useEndTime, long intervalDuration) {
		super();
		this.styleFactory = styleFactory;
		this.geometryFactory = geometryFactory;
		this.endTime = endTime;
		this.useEndTime = useEndTime;
		this.intervalDuration = intervalDuration;
	}

	public NonInterpolatedTimeKmlItem(ProjectionTransformer transformer, StyleFactory styleFactory,
			GeometryFactory geometryFactory, int endTime) {
		this(transformer, styleFactory, geometryFactory, endTime, true, 0);
	}

	public NonInterpolatedTimeKmlItem(StyleFactory styleFactory, GeometryFactory geometryFactory, int endTime) {
		this(styleFactory, geometryFactory, endTime, true, 0);
	}

	private Map<String, TimeRecords> recordMap = new HashMap<>();

	@Override
	public Folder initFeatureForKml(Kmz kmz) {

		Folder folder = new Folder();
		Style style = styleFactory.createStyle();
		folder.addToStyleSelector(style);

		for (TimeRecords timeRecords : recordMap.values()) {

			List<Record> records = timeRecords.getFinalRecords();
			for (int i = 0; i < records.size(); i++) {
				Record record = records.get(i);
				Placemark p = new Placemark();
				p.setId(record.id);
				p.setDescription(record.description);
				p.setGeometry(geometryFactory.createGeometry(record.coords));
				p.withStyleUrl("#" + style.getId());

				TimeSpan timeSpan = new TimeSpan();
				timeSpan.setBegin(TimeKmlFormater.getTimeForKML(record.time));
				long timeSpanEnd;
				if (i >= records.size() - 1) {
					if (useEndTime) {
						timeSpanEnd = endTime;
					} else {
						timeSpanEnd = record.time + intervalDuration - 1000;
					}

				} else {
					Record r = records.get(i + 1);
					if (r == null) {
						records.remove(i + 1);
						timeSpanEnd = record.time + intervalDuration - 1000;
					} else {
						timeSpanEnd = records.get(i + 1).time - 1000;
					}

				}
				timeSpan.setEnd(TimeKmlFormater.getTimeForKML(timeSpanEnd));

				p.withTimePrimitive(timeSpan);
				folder.addToFeature(p);
			}
		}
		
		// add LookAt KML tag to folder

		if (recordMap.entrySet().iterator().hasNext()) {
			Map.Entry<String, TimeRecords> firstRecord = recordMap.entrySet().iterator().next();
			LookAt lookat = new LookAt();
			lookat.setLatitude(firstRecord.getValue().getFinalRecords().iterator().next().coords[0].getLatitude());
			lookat.setLongitude(firstRecord.getValue().getFinalRecords().iterator().next().coords[0].getLongitude());
			lookat.setAltitude(3000);
			lookat.setAltitudeMode(AltitudeMode.RELATIVE_TO_GROUND);
			lookat.setRange(500);
			lookat.setTilt(15);
			lookat.setHeading(0);
			folder.setAbstractView(lookat);
		}
		
		return folder;
	}

	public void addTimeGeometry(String id, Coordinate[] coords, long time, String description) {
		addToRecordMap(id, coords, time, description);
	}

	private void addToRecordMap(String id, Coordinate[] coords, long time, String description) {
		TimeRecords timeRecords = recordMap.get(id);
		if (timeRecords == null) {
			timeRecords = new TimeRecords(id);
			recordMap.put(id, timeRecords);
		}
		timeRecords.add(coords, time, description);
	}

	static int skipCounter = 0;
	static int totalCounter = 0;

	private class TimeRecords {
		String id;

		List<Record> records = new ArrayList<>();
		Record lastRecord;

		public TimeRecords(String id) {
			this.id = id;
		}

		public List<Record> getFinalRecords() {
			if (lastRecord == null) {
//				logger.debug(records);
			}
			records.add(lastRecord);
			return records;
		}

		public void add(Coordinate[] coords, long time, String description) {
			int lastIndex = records.size() - 1;
			Record record = new Record(id, coords, time, description);
			if (lastIndex >= 0 && records.get(lastIndex).equalsIgnoringTime(record)) {
				lastRecord = record;
				skipCounter++;
			} else {
				records.add(record);
			}
			totalCounter++;
		}
	}

	private class Record {

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

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + Arrays.hashCode(coords);
			result = prime * result + ((description == null) ? 0 : description.hashCode());
			result = prime * result + ((id == null) ? 0 : id.hashCode());
			return result;
		}

		public boolean equalsIgnoringTime(Object obj) {
            return false;
//			if (this == obj) return true;
//			if (obj == null) return false;
//			if (getClass() != obj.getClass()) return false;
//			Record other = (Record) obj;
//			if (!Arrays.equals(coords, other.coords)) return false;
//			if (description == null) {
//				if (other.description != null) return false;
//			} else if (!description.equals(other.description)) return false;
//			if (id == null) {
//				if (other.id != null) return false;
//			} else if (!id.equals(other.id)) return false;
//			return true;
		}
	}

}

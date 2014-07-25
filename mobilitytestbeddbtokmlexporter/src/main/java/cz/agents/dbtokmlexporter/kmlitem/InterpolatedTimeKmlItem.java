package cz.agents.dbtokmlexporter.kmlitem;

import com.vividsolutions.jts.geom.*;

import cz.agents.agentpolis.tools.geovisio.projection.ProjectionTransformer;
import cz.agents.alite.googleearth.updates.Kmz;
import cz.agents.dbtokmlexporter.factory.geometry.GeometryFactory;
import cz.agents.dbtokmlexporter.factory.style.StyleFactory;
import cz.agents.resultsvisio.kml.TimeKmlItem;
import cz.agents.resultsvisio.kml.util.TimeKmlFormater;
import de.micromata.opengis.kml.v_2_2_0.*;
import de.micromata.opengis.kml.v_2_2_0.Coordinate;
import de.micromata.opengis.kml.v_2_2_0.gx.Coord;
import de.micromata.opengis.kml.v_2_2_0.gx.Track;
import de.micromata.opengis.kml.v_2_2_0.gx.custom.TrackPoint;

import org.apache.log4j.Logger;

import java.util.*;

/**
 * 
 * @author Marek Cuchy
 * 
 */
public class InterpolatedTimeKmlItem implements TimeKmlItem {

	private static final Logger logger = Logger.getLogger(InterpolatedTimeKmlItem.class);

	private final StyleFactory styleFactory;
    private final ProjectionTransformer transformer;

    private final long endTime;
	/**
	 * Define if as end time of the last interval for some id is used
	 * {@code endTime} argument or {@code intervalDuration} argument.
	 */
	private final boolean useEndTime;
	private final long intervalDuration;

	public InterpolatedTimeKmlItem(ProjectionTransformer transformer, StyleFactory styleFactory,
                                   GeometryFactory geometryFactory, long endTime,
                                   boolean useEndTime, long intervalDuration) {
		super();
		this.styleFactory = styleFactory;
        this.endTime = endTime;
		this.useEndTime = useEndTime;
		this.intervalDuration = intervalDuration;
        this.transformer = transformer;
	}

	public InterpolatedTimeKmlItem(ProjectionTransformer transformer, StyleFactory styleFactory,
                                   GeometryFactory geometryFactory, int endTime) {
		this(transformer, styleFactory, geometryFactory, endTime, true, 0);
	}

    public InterpolatedTimeKmlItem(StyleFactory styleFactory,
                                   GeometryFactory geometryFactory, int endTime) {
        this(null, styleFactory, geometryFactory, endTime, true, 0);
    }

    public InterpolatedTimeKmlItem(StyleFactory styleFactory,
                                   GeometryFactory geometryFactory, long endTime,
                                   boolean useEndTime, long intervalDuration) {
        this(null, styleFactory, geometryFactory, endTime, useEndTime, intervalDuration);
    }

	private Map<String, TimeRecords> recordMap = new HashMap<>();

	@Override
	public Folder initFeatureForKml(Kmz kmz) {

		Folder folder = new Folder();
		Style style = styleFactory.createStyle();
		folder.addToStyleSelector(style);

		// add LookAt KML tag to folder
		Map.Entry<String, TimeRecords> firstRecord = recordMap.entrySet().iterator().next(); 
		if (firstRecord != null) {
			LookAt lookat = new LookAt();
			lookat.setLatitude(firstRecord.getValue().getFinalRecords().get(0).coord.getLatitude());
			lookat.setLongitude(firstRecord.getValue().getFinalRecords().get(0).coord.getLongitude());
			lookat.setAltitude(3000);
			lookat.setAltitudeMode(AltitudeMode.RELATIVE_TO_GROUND);
			lookat.setRange(500);
			lookat.setTilt(15);
			lookat.setHeading(0);
			folder.setAbstractView(lookat);
		}
		
		for (TimeRecords timeRecords : recordMap.values()) {

            Placemark p = new Placemark();
            p.setId(timeRecords.id);
            p.setDescription(timeRecords.description);
            p.withStyleUrl("#" + style.getId());


            Track track = new Track();

			List<Record> records = timeRecords.getFinalRecords();
			for (int i = 0; i < records.size(); i++) {
				Record record = records.get(i);


//                long time = useEndTime ? Math.min(record.time, endTime) : record.time;

                long time;
                if (useEndTime) {
                    time = Math.min(record.time, endTime);
                } else {
                    time = record.time;
                }

                TrackPoint trackPoint = new TrackPoint(TimeKmlFormater.getTimeForKML(time));
                Coordinate tmpCoord = record.coord;
                com.vividsolutions.jts.geom.Coordinate coordinate =
                        new com.vividsolutions.jts.geom.Coordinate(tmpCoord.getLongitude(), tmpCoord.getLatitude());
                Coord kmlCoordinate = new Coord(coordinate.x, coordinate.y);


                trackPoint.setCoord(kmlCoordinate);
                track.withTrackPoint(trackPoint);

			}
            p.setGeometry(track);
            folder.addToFeature(p);
        }
		return folder;
	}

	public void addTimeGeometry(String id, Coordinate[] coords, long time, String description) {
		addToRecordMap(id, coords[0], time, description);
	}

	private void addToRecordMap(String id, Coordinate coord, long time, String description) {
		TimeRecords timeRecords = recordMap.get(id);
		if (timeRecords == null) {
			timeRecords = new TimeRecords(id, description);
			recordMap.put(id, timeRecords);
		}
		timeRecords.add(coord, time);
	}

	static int skipCounter = 0;
	static int totalCounter = 0;

	public class TimeRecords {
		String id;

		List<Record> records = new ArrayList<>();
		Record lastRecord;
        String description;

		public TimeRecords(String id, String description) {
			this.id = id;
            this.description = description;
		}

		public List<Record> getFinalRecords() {
			if (lastRecord == null) {
//                logger.debug(id + " wut?");
//				logger.debug(records);
            } else {
//                if (id.endsWith("Id244"))
//                    logger.debug(id + " Time - finished repeated: " + TimeKmlFormater.getTimeForKML(lastRecord
//                            .time) + " " + lastRecord.coord);
                records.add(lastRecord);
            }

            return records;
		}

		public void add(Coordinate coord, long time) {
			int lastIndex = records.size() - 1;
			Record record = new Record(coord, time);
			if (lastIndex >= 0 && records.get(lastIndex).equalsIgnoringTime(record)) {
//                if (id.endsWith("Id244"))
//                    logger.debug(id + " Time - repeated: " + TimeKmlFormater.getTimeForKML(time) + " " + record.coord);
				lastRecord = record;
				skipCounter++;
			} else {
                if (lastRecord != null) {
//                    if (id.endsWith("Id244"))
//                        logger.debug(id + " Time - finished repeated: " + TimeKmlFormater.getTimeForKML(lastRecord
//                                .time) + " " + lastRecord.coord);
                    records.add(lastRecord);
                }
//                if (id.endsWith("Id244"))
//                    logger.debug(id + " Time: " + TimeKmlFormater.getTimeForKML(time) + " " + record.coord);
				records.add(record);
                lastRecord = null;
			}
			totalCounter++;
		}
	}

	private class Record {

		public final Coordinate coord;
		public final long time;

		public Record(Coordinate coord, long time) {
			super();
			this.coord = coord;
			this.time = time;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + coord.hashCode();
			return result;
		}

		public boolean equalsIgnoringTime(Object obj) {
			if (this == obj) return true;
			if (obj == null) return false;
			if (getClass() != obj.getClass()) return false;
			Record other = (Record) obj;

			if (coord != null && !coord.equals(other.coord)) return false;
            if (other.coord != null && !other.coord.equals(coord)) return false;

			return true;
		}
	}

}

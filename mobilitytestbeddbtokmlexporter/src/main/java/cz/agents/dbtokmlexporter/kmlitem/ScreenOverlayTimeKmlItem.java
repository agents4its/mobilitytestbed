package cz.agents.dbtokmlexporter.kmlitem;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

import cz.agents.alite.googleearth.updates.Kmz;
import cz.agents.resultsvisio.kml.KmlItem;
import cz.agents.resultsvisio.kml.util.TimeKmlFormater;
import de.micromata.opengis.kml.v_2_2_0.Folder;
import de.micromata.opengis.kml.v_2_2_0.ScreenOverlay;
import de.micromata.opengis.kml.v_2_2_0.TimeSpan;
import de.micromata.opengis.kml.v_2_2_0.Units;
import de.micromata.opengis.kml.v_2_2_0.Vec2;

/**
 * 
 * @author Marek Cuchy
 * 
 */
public class ScreenOverlayTimeKmlItem implements KmlItem {

	private static final Logger logger = Logger.getLogger(ScreenOverlayTimeKmlItem.class);

	private final long intervalLength;

	private final List<Record> records = new LinkedList<>();

	public ScreenOverlayTimeKmlItem(long intervalLength) {
		super();
		this.intervalLength = intervalLength;
	}

	public void addTextOverlay(long time, String text) {
		records.add(new Record(time, text));
	}

	public Folder initFeatureForKml(Kmz kmz) {
		Folder folder = new Folder();

		for (Record record : records) {
			try {
				folder.addToFeature(createScreenOverlay(record));
			} catch (UnsupportedEncodingException e) {
				logger.warn("Record not exported to kml: " + record, e);
			}
		}
		return folder;
	}

	private ScreenOverlay createScreenOverlay(Record record) throws UnsupportedEncodingException {
		ScreenOverlay overlay = new ScreenOverlay();

		TimeSpan timeSpan = new TimeSpan();
		timeSpan.setBegin(TimeKmlFormater.getTimeForKML(record.time));
		timeSpan.setEnd(TimeKmlFormater.getTimeForKML(record.time + intervalLength - 1000));

		overlay.withTimePrimitive(timeSpan);
		overlay.createAndSetIcon().withHref(getHref(record.text));

		Vec2 overlayXY = overlay.createAndSetOverlayXY();
		overlayXY.withX(0).withY(-1).withXunits(Units.FRACTION).withYunits(Units.FRACTION);
		
		Vec2 screenXY = overlay.createAndSetScreenXY();
		screenXY.withX(0.1).withY(0.1).withXunits(Units.FRACTION).withYunits(Units.FRACTION);
		
		Vec2 rotationXY = overlay.createAndSetRotationXY();
		rotationXY.withX(0).withY(0).withXunits(Units.FRACTION).withYunits(Units.FRACTION);
		
		Vec2 sizeXY = overlay.createAndSetSize();
		sizeXY.withX(0).withY(0).withXunits(Units.FRACTION).withYunits(Units.FRACTION);

		return overlay;
	}

	private String getHref(String text) throws UnsupportedEncodingException {
		StringBuilder sb = new StringBuilder("http://chart.apis.google.com/chart?chst=d_text_outline&chld=FFBBBB|16|l|BB0000|b");
		
		String[] split = text.split("\n");
		
		for (String s : split) {
			sb.append("|" + URLEncoder.encode(s, "UTF-8"));
		}
		return sb.toString();
				
	}

	private class Record {
		public final long time;
		public final String text;

		public Record(long time, String text) {
			super();
			this.time = time;
			this.text = text;
		}

		@Override
		public String toString() {
			return "Record [time=" + time + ", text=" + text + "]";
		}
	}
}

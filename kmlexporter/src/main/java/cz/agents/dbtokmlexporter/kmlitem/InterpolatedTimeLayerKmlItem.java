package cz.agents.dbtokmlexporter.kmlitem;

import java.awt.Color;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.opengis.referencing.operation.TransformException;

import cz.agents.agentpolis.tools.geovisio.projection.ProjectionTransformer;
import cz.agents.alite.googleearth.updates.KmlUtils;
import cz.agents.alite.googleearth.updates.Kmz;
import cz.agents.resultsvisio.kml.KmlItem;
import cz.agents.resultsvisio.kml.util.TimeKmlFormater;
import de.micromata.opengis.kml.v_2_2_0.AltitudeMode;
import de.micromata.opengis.kml.v_2_2_0.Document;
import de.micromata.opengis.kml.v_2_2_0.Folder;
import de.micromata.opengis.kml.v_2_2_0.Kml;
import de.micromata.opengis.kml.v_2_2_0.Placemark;
import de.micromata.opengis.kml.v_2_2_0.gx.Coord;
import de.micromata.opengis.kml.v_2_2_0.gx.Track;
import de.micromata.opengis.kml.v_2_2_0.gx.custom.TrackPoint;

/**
 * 
 * @author Marek Cuchy
 * 
 */
public class InterpolatedTimeLayerKmlItem implements KmlItem {
	
	
	private static final Logger logger = Logger.getLogger(InterpolatedTimeLayerKmlItem.class);

	private ProjectionTransformer transformer;
	private String iconName;
	private Color color;

	private Map<String, Record> recordMap = new HashMap<String, Record>();

	public InterpolatedTimeLayerKmlItem(ProjectionTransformer transformer, long duration, String iconName, Color color) {
		super();
		this.transformer = transformer;
		this.iconName = iconName;
		this.color = color;
	}

	public void saveToKml(final String path) throws IOException{
		Kml kml = new Kml();
		kml.createAndSetDocument().addToFeature(initFeatureForKml(null));
		
		
		OutputStream os = new OutputStream() {
			int i=0;
			
			FileOutputStream fos = new FileOutputStream(path);
//			ArrayList<>
			
			@Override
			public void write(int b) throws IOException {
				System.out.println(i++ +": \t"+(char)b);
				System.out.println("Write(int b)");
			}
			
			public void write(byte b[], int off, int len) throws IOException {
				i++;
				String s = new String(b,off,len);
				s=s.replaceAll("<gx:angles></gx:angles>\n", "");
				fos.write(s.getBytes());
//				System.out.println("write(byte b[], int off, int len) "+ i++ +" = "+ b.length);
//				System.out.println(new String(b,off,len));
//		        writeBytes(b, off, len);
		    }

			@Override
			public void write(byte[] b) throws IOException {
				this.write(b,0,b.length);
			}

			@Override
			public void flush() throws IOException {
				fos.flush();
			}

			@Override
			public void close() throws IOException {
				fos.close();
			}
			
			
		};
		
//		kml.
		kml.marshal(os);
		os.close();
		
	}

	public Folder initFeatureForKml(Kmz kmz) {

		Folder folder = new Folder();
		String styleId = "style_" + iconName;

		folder.createAndAddStyle().withId(styleId).createAndSetIconStyle().withScale(0.75).withHeading((double) 0)
		 .withColor(KmlUtils.colorToKmlColor(color))
				.createAndSetIcon().withHref("http://maps.google.com/mapfiles/kml/shapes/" + iconName + ".png");

		for (Record record : recordMap.values()) {
			// System.out.println(record);
			Placemark p = new Placemark();
			 p.setId(record.id);
//			 p.setName(record.id);
			 p.setDescription(record.id);
			p.withStyleUrl("#" + styleId);

			Track track = new Track();
			for (int i = 0; i < record.coordinates.size(); i++) {
				TrackPoint trackPoint = new TrackPoint(TimeKmlFormater.getTimeForKML(record.times.get(i)));
				trackPoint.setCoord(record.coordinates.get(i));
				track.withTrackPoint(trackPoint);
			}

			p.setGeometry(track);

			folder.addToFeature(p);

		}
		
		logger.info("Skipped: " + skipCounter+"/" +totalCounter);
		skipCounter =0;
		totalCounter = 0;
		return folder;
	}

	public void addTimePoint(String id, com.vividsolutions.jts.geom.Point point, long time) throws TransformException {
		com.vividsolutions.jts.geom.Coordinate coordinate = transformer.transform(point.getCoordinate());
		Coord kmlCoordinate = new Coord(coordinate.x, coordinate.y);

		addToRecordMap(id, kmlCoordinate, time);
	}

	private void addToRecordMap(String id, Coord kmlCoordinate, long time) {
		Record record = recordMap.get(id);
		if (record == null) {
			record = new Record(id);
			recordMap.put(id, record);
		}
		record.add(kmlCoordinate, time);
	}
	static int skipCounter = 0;
	static int totalCounter = 0;
	
	private class Record {
		String id;
		List<Coord> coordinates = new ArrayList<Coord>();
		List<Long> times = new ArrayList<Long>();
		boolean lastTwoEquals= false; 

		public Record(String id) {
			this.id = id;
		}

		
		public void add(Coord coord, long time) {
			int lastIndex = coordinates.size()-1;
			if(lastIndex>=0 && coordinates.get(lastIndex).equals(coord)){
				if(lastTwoEquals){
					coordinates.set(lastIndex, coord);
					times.set(lastIndex, time);
//					System.out.println(skipCounter +"/"+totalCounter + " skipped: "+time +" = " + coords);
					skipCounter++;
				}else{
					lastTwoEquals=true;
					coordinates.add(coord);
					times.add(time);
				}
			}else{
				lastTwoEquals = false;
				coordinates.add(coord);
				times.add(time);
			}				
			totalCounter++;
		}
	}

}

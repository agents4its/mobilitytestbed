package cz.agents.dbtokmlexporter.factory.geometry;

import java.util.Arrays;

import de.micromata.opengis.kml.v_2_2_0.Coordinate;
import de.micromata.opengis.kml.v_2_2_0.Geometry;
import de.micromata.opengis.kml.v_2_2_0.LineString;

/**
 *
 *@author Marek Cuchy
 *
 */
public class LineGeometryFactory implements GeometryFactory {

	@Override
	public Geometry createGeometry(Coordinate[] coords) {
		LineString line = new LineString();
		line.setCoordinates(Arrays.asList(coords));
		return line;
	}

}

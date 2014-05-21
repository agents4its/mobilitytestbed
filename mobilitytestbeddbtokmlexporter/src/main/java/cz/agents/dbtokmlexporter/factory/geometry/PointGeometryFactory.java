package cz.agents.dbtokmlexporter.factory.geometry;

import java.util.ArrayList;
import java.util.List;

import de.micromata.opengis.kml.v_2_2_0.Coordinate;
import de.micromata.opengis.kml.v_2_2_0.Geometry;
import de.micromata.opengis.kml.v_2_2_0.Point;

/**
 *
 *@author Marek Cuchy
 *
 */
public class PointGeometryFactory implements GeometryFactory{
	
	@Override
	public Geometry createGeometry(Coordinate[] coords) {
		Point p = new Point();
		List<Coordinate> coordiantes = new ArrayList<>(1);
		coordiantes.add(coords[0]);
		p.setCoordinates(coordiantes);
		return p;
	}

}

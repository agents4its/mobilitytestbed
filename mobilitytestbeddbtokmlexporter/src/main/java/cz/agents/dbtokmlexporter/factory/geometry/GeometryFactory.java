package cz.agents.dbtokmlexporter.factory.geometry;

import de.micromata.opengis.kml.v_2_2_0.Coordinate;
import de.micromata.opengis.kml.v_2_2_0.Geometry;

/**
 *
 *@author Marek Cuchy
 *
 */
public interface GeometryFactory {

	public Geometry createGeometry(Coordinate[] coords);
}

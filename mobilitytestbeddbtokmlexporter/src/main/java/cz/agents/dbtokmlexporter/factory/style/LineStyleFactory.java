package cz.agents.dbtokmlexporter.factory.style;

import java.awt.Color;

import cz.agents.alite.googleearth.updates.KmlUtils;
import de.micromata.opengis.kml.v_2_2_0.Style;

/**
 * 
 * @author Marek Cuchy
 * 
 */
public class LineStyleFactory implements StyleFactory {

	private final Color color;
	private final int width;

	private static int counter = 0;

	public LineStyleFactory(Color color, int width) {
		super();
		this.color = color;
		this.width = width;
	}

	@Override
	public Style createStyle() {
		Style style = new Style();
		style.withId("linestyle" + counter++).createAndSetLineStyle().withColor(KmlUtils.colorToKmlColor(color))
				.withWidth(width);
		return style;
	}
}

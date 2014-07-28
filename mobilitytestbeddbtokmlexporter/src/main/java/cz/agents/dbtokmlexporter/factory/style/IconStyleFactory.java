package cz.agents.dbtokmlexporter.factory.style;

import java.awt.Color;

import cz.agents.alite.googleearth.updates.KmlUtils;
import de.micromata.opengis.kml.v_2_2_0.Style;

/**
 * 
 * @author Marek Cuchy
 * 
 */
public class IconStyleFactory implements StyleFactory {

	private final String iconUrl;
	private final double scale;

	private static int counter = 0;

	public IconStyleFactory(String iconUrl, double scale) {
		super();
		this.iconUrl = iconUrl;
		this.scale = scale; 
	}

	@Override
	public Style createStyle() {
		Style style = new Style();
		style.withId("iconstyle" + counter++).createAndSetIconStyle().withScale(scale)
				.createAndSetIcon()
				.withHref(this.iconUrl);
		return style;
	}

}

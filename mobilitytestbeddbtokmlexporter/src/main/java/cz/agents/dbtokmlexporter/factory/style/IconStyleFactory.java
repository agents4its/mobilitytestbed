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

	private final String iconName;
	private final Color color;
	private final double scale;

	private static int counter = 0;

	public IconStyleFactory(String iconName, Color color, double scale) {
		super();
		this.iconName = iconName;
		this.color = color;
		this.scale = scale;
	}

	@Override
	public Style createStyle() {
		Style style = new Style();
		style.withId("iconstyle" + counter++).createAndSetIconStyle().withScale(scale)
				.withColor(KmlUtils.colorToKmlColor(color)).createAndSetIcon()
				.withHref("http://maps.google.com/mapfiles/kml/shapes/" + iconName + ".png");
		return style;
	}

}

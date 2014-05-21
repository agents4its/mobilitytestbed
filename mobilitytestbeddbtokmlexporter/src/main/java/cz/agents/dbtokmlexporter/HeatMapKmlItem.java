package cz.agents.dbtokmlexporter;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.TransformException;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Point;

import cz.agents.agentpolis.tools.geovisio.util.ColorMap;
import cz.agents.alite.googleearth.updates.Kmz;
import cz.agents.resultsvisio.kml.KmlItem;
import de.micromata.opengis.kml.v_2_2_0.Folder;
import de.micromata.opengis.kml.v_2_2_0.GroundOverlay;
import de.micromata.opengis.kml.v_2_2_0.LatLonBox;

/**
 * 
 * @author Marek Cuchy
 * 
 */
public class HeatMapKmlItem implements KmlItem {
	
	private static int heatMapCounter = 0;
	
	
	private static final Logger logger = Logger.getLogger(HeatMapKmlItem.class);

	/**
	 * It doesn't have dependence on result heatmap
	 */
	private static final Color ORIGINAL_POINT_COLOR = Color.RED;

    private static final int FIRST_BLUR_SIZE=10;
    private static final int SECOND_BLUR_SIZE=30;

    private static final double IMAGE_SCALE = 0.05;
    private static final int POINT_DIAMETER = 50;

	private final List<Point> points = new ArrayList<>();

	private Dimension imageDimension;

	private final String schemaName;

	/**
	 * GPS bounding box
	 */
	private double minx = Double.MAX_VALUE, maxx = Double.NEGATIVE_INFINITY, miny = Double.MAX_VALUE, maxy = Double.NEGATIVE_INFINITY;

	private int pointAlpha;

	public HeatMapKmlItem(String schemaName) {
		super();
		this.schemaName = schemaName;

	}

	public void addPoint(Point point) {
		points.add(point);

		updateBoundingBox(point);
	}

	private void updateBoundingBox(Point point) {
		double x = point.getX();
		double y = point.getY();
		

		maxx = Math.max(maxx, x);
		minx = Math.min(minx, x);

		maxy = Math.max(maxy, y);
		miny = Math.min(miny, y);
	}

	@Override
	public Folder initFeatureForKml(Kmz kmz) {
		if(points.isEmpty()){
			return new Folder();
		}
		try {
			imageDimension = getImageDimension();
		} catch (FactoryException | TransformException e) {
			e.printStackTrace();
		}
		pointAlpha = getPointAlpha();

		Folder folder = new Folder();

		BufferedImage image = new BufferedImage(imageDimension.width, imageDimension.height,
				BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = image.createGraphics();
		g.setColor(createTransparentColor(ORIGINAL_POINT_COLOR, pointAlpha));

		for (Point point : points) {
			int centerX = getCenterX(point);
			int centerY = getCenterY(point);

			g.fillOval(centerX - POINT_DIAMETER / 2, centerY - POINT_DIAMETER / 2, POINT_DIAMETER, POINT_DIAMETER);
		}

		image = filterImage(image);
		image.flush();
		new File(schemaName).mkdir();
		String imagePathInKml = null;
		try {
			File imageFile = File.createTempFile("heatmap", ".png");
			ImageIO.write(image, "png", imageFile);
			imagePathInKml = kmz.loadFile("heatmap"+(heatMapCounter++)+".png", imageFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
		GroundOverlay groundOverlay = new GroundOverlay().withAltitude(0);
		groundOverlay.createAndSetIcon().withHref(imagePathInKml);

		LatLonBox ll = groundOverlay.createAndSetLatLonBox();
		ll.withNorth(maxy).withSouth(miny).withEast(maxx).withWest(minx);

		folder.addToFeature(groundOverlay);
		return folder;
	}

	private int getPointAlpha() {
//		int imageArea =imageDimension.width*imageDimension.height;
//		double pointArea = ((POINT_DIAMETER+FIRST_BLUR_SIZE-1)* Math.PI)*points.size();
//		double pointDensity = pointArea/imageArea;
//
//		int alpha = (int) Math.max(1,Math.min(150, 2*1/pointDensity));
//
//		logger.debug("Used alpha: " + alpha);
//        return alpha;
		return 1;
	}
	


	private BufferedImage filterImage(BufferedImage image) {
		double maxAlpha = getMaxAlpha(image);

//		logger.debug(maxAlpha);
		
		ColorMap colorMap = new ColorMap(pointAlpha-1, maxAlpha*0.9, ColorMap.HUE_BLUE_TO_RED);
		BufferedImage im = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());

		// blur image
		int side = FIRST_BLUR_SIZE;
		float[] blurMatrix = createBlurMatrix(side, side);
		Kernel kernel = new Kernel(side, side, blurMatrix);
		BufferedImageOp op = new ConvolveOp(kernel);
		im = op.filter(image, null);

		// change colors
		for (int w = 0; w < im.getWidth(); w++) {
			for (int h = 0; h < im.getHeight(); h++) {
				int rgb = im.getRGB(w, h);
				if (rgb == 0) {
					im.setRGB(w, h, rgb);
				} else {
					int alpha = new Color(rgb, true).getAlpha();
					if(alpha>0){
//						logger.debug(alpha);
					}
					im.setRGB(w, h, colorMap.getColor(alpha).getRGB());
				}
			}
		}

		// blur again
		side = SECOND_BLUR_SIZE;
		blurMatrix = createBlurMatrix(side, side);
		kernel = new Kernel(side, side, blurMatrix);
		op = new ConvolveOp(kernel);
		im = op.filter(im, null);

		// reverse vertically
		AffineTransform tx = AffineTransform.getScaleInstance(1, -1);
		tx.translate(0, -im.getHeight(null));
		AffineTransformOp opp = new AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
		im = opp.filter(im, null);

		return im;
	}

	private double getMaxAlpha(BufferedImage im) {
		int max = Integer.MIN_VALUE;
		for (int w = 0; w < im.getWidth(); w++) {
			for (int h = 0; h < im.getHeight(); h++) {
				int rgb = im.getRGB(w, h);
				if (rgb == 0) {
					im.setRGB(w, h, rgb);
				} else {
					int alpha = new Color(rgb, true).getAlpha();
					max = Math.max(alpha, max);
				}
			}
		}
		return max;
	}

	private float[] createBlurMatrix(int height, int width) {
		float[] matrix = new float[height * width];
		for (int i = 0; i < matrix.length; i++) {
			matrix[i] = 1f / (float) (height * width);
		}
		return matrix;
	}

	private int getCenterY(Point point) {
		double bbYSize = maxy - miny;
		double yy = point.getCentroid().getY() - miny;
		return (int) (yy / bbYSize * imageDimension.height);
	}

	private int getCenterX(Point point) {
		double bbXSize = maxx - minx;
		double xx = point.getCentroid().getX() - minx;
		return (int) (xx / bbXSize * imageDimension.width);
	}

	private Dimension getImageDimension() throws FactoryException, TransformException {
		minx-=0.01;
		miny-=0.01;
		maxx+=0.01;
		maxy+=0.01;
		
		CoordinateReferenceSystem crs = CRS.decode("EPSG:4326", true);

		Coordinate c1 = new Coordinate(minx, miny);
		Coordinate c2 = new Coordinate(maxx, miny);
		double widthDist = JTS.orthodromicDistance(c1, c2, crs);

		c1 = new Coordinate(minx, miny);
		c2 = new Coordinate(minx, maxy);
		double heightDist = JTS.orthodromicDistance(c1, c2, crs);
		
		return new Dimension((int) (widthDist * IMAGE_SCALE), (int) (heightDist * IMAGE_SCALE));
	}

	public static Color createTransparentColor(Color c, int alpha) {
		return new Color(c.getRed(), c.getGreen(), c.getBlue(), alpha);
	}
}

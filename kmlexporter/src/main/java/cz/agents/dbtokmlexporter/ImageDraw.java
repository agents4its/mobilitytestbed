package cz.agents.dbtokmlexporter;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;

import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 *
 *@author Marek Cuchy
 *
 */
public class ImageDraw {

	public static void main(String[] args) {
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(1000, 1000);
		
		
		BufferedImage image = new BufferedImage(1000, 1000, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = image.createGraphics();
//		System.out.println(Color.BLACK.getRGB());
		Color c = createTransparentColor(Color.CYAN,50);
		g.setColor(c);
//		image.
		Raster data = image.getData();
		
		
//		data.
//		new Color
		int size = 300;
		
		for (int i = 0; i < 155; i++) {
			g.fillOval(0, 0,size, size);
		}
		
		int max = Integer.MIN_VALUE;
		for (int i = 0; i < image.getHeight(); i++) {
			for (int j = 0; j < image.getWidth(); j++) {
				int rgb = image.getRGB(i, j);
				
					Color color = new Color(rgb,true);
					max=Math.max(color.getAlpha(),max);
				
			}
		}
		System.out.println("alpha max " +max);
//		
		JPanel panel = new ImagePanel(image);
//		panel.add(new JLabel(new ImageIcon(image)));
//		panel.drawImage(image, 0, 0, null);
	
		frame.add(panel);
		frame.setVisible(true);
		
	}
	
	public static Color createTransparentColor(Color c, int alpha){
		return new Color(c.getRed(),c.getGreen(),c.getBlue(),alpha);
	}
}

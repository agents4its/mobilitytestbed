package cz.agents.dbtokmlexporter;

import java.awt.Graphics;
import java.awt.Image;

import javax.swing.JPanel;

/**
 *
 *@author Marek Cuchy
 *
 */
public class ImagePanel extends JPanel{

	/**
	 * 
	 */
	private static final long serialVersionUID = 229552311491882208L;
	private final Image image;

	public ImagePanel(Image image) {
		super();
		this.image = image;
	}

	public void paintComponent(Graphics g){
        super.paintComponent(g);
        if(image != null){
            g.drawImage(image, 0, 0, this);
        }
    }
	
}
	
	

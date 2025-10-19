package org.obrienscience.fractal;


import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Use Swing for desktop apps
 */
public class MandelbrotStream extends JFrame {
	
	private static final long serialVersionUID = 1L;

	public MandelbrotStream () {
		try {
            JFrame frame = new JFrame("Mandelbrot");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(640, 480);

            JPanel panel = new JPanel(); // Create a panel
            //JLabel label = new JLabel("Hello, Swing!"); // Create a label
			BufferedImage image = ImageIO.read(new File("/Users/michaelobrien/wse_github/ObrienlabsDev/mandelbrot/corei7_920_zoom_time_1_to_512_threads_graph2.JPG"));
			JLabel label = new JLabel(new ImageIcon(image));
            panel.add(label); 
            frame.add(panel); 
            frame.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		MandelbrotStream mandel = new MandelbrotStream();	

	}
}

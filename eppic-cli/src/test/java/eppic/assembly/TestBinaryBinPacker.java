package eppic.assembly;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.geom.Dimension2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.biojava.nbio.structure.gui.util.color.ColorUtils;


/*
 * Not a unit test, but a visualizer
 */
public class TestBinaryBinPacker {
	public static void main(String[] args) {
		// Pack some colored rectangles

		Color color = new Color(1,0,0,.5f);
		float rot = (float) (2./(1+Math.sqrt(5))); // golden ratio
		List<Entry<Dimension2D, Color>> boxes = new ArrayList<Entry<Dimension2D, Color>>();
		boxes.add(new SimpleEntry<Dimension2D, Color>(new Dimension(500,250), color));
		color = ColorUtils.rotateHue(color, rot);
		boxes.add(new SimpleEntry<Dimension2D, Color>(new Dimension(250,250), color));
		color = ColorUtils.rotateHue(color, rot);
		boxes.add(new SimpleEntry<Dimension2D, Color>(new Dimension(50,50), color));
		color = ColorUtils.rotateHue(color, rot);
		boxes.add(new SimpleEntry<Dimension2D, Color>(new Dimension(250,50), color));
		color = ColorUtils.rotateHue(color, rot);
		boxes.add(new SimpleEntry<Dimension2D, Color>(new Dimension(250,50), color));
		color = ColorUtils.rotateHue(color, rot);
		boxes.add(new SimpleEntry<Dimension2D, Color>(new Dimension(250,50), color));
		//		color = ColorUtils.rotateHue(color, rot);
		//		boxes.add(new SimpleEntry<Dimension2D, Color>(new Dimension(50,600), color));
		//		color = ColorUtils.rotateHue(color, rot);
		//		boxes.add(new SimpleEntry<Dimension2D, Color>(new Dimension(650,100), color));
		//		color = ColorUtils.rotateHue(color, rot);
		//		boxes.add(new SimpleEntry<Dimension2D, Color>(new Dimension(150,50), color));


		BinaryBinPacker<Color> packer = new BinaryBinPacker<Color>(boxes);

		// Add some entries out of order to test bidirectional resizing
		color = ColorUtils.rotateHue(color, rot);
		packer.add(new Dimension(50,600), color);
		color = ColorUtils.rotateHue(color, rot);
		packer.add(new Dimension(650,100), color);
		color = ColorUtils.rotateHue(color, rot);
		packer.add(new Dimension(150,50), color);

		List<Entry<Color, Rectangle2D>> placementMap = packer.getPlacements();

		JPanel panel = new JPanel();
		panel.setPreferredSize(new Dimension((int)packer.getBounds().getWidth(),(int)packer.getBounds().getHeight()));
		panel.setLayout(null);

		Insets insets = panel.getInsets();
		for(Entry<Color, Rectangle2D> entry : placementMap) {
			Color c = entry.getKey();
			Rectangle2D r = entry.getValue();

			JPanel rect = new JPanel();
			rect.setBackground(c);
			rect.setBorder(BorderFactory.createLineBorder(Color.BLACK));
			rect.setBounds((int)r.getX()+insets.left, (int)r.getY()+insets.right, (int)r.getWidth(), (int)r.getHeight());
			panel.add(rect);
		}

		JFrame frame = new JFrame(BinaryBinPacker.class.getSimpleName());
		frame.getContentPane().add(panel);
		frame.pack();
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
}

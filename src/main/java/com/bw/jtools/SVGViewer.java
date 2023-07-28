package com.bw.jtools;

import com.bw.jtools.shape.ShapePane;
import com.bw.jtools.svg.SVGConverter;
import com.bw.jtools.svg.SVGException;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.WindowConstants;
import java.awt.BorderLayout;
import java.awt.event.InputEvent;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class SVGViewer
{
	/**
	 * Test main. Shows a SVG file.
	 *
	 * @param args name
	 */
	public static void main(String[] args) throws FileNotFoundException, SVGException
	{
		FileInputStream is = new FileInputStream(args[0]);
		SVGConverter svg = new SVGConverter(is);

		ShapePane pane = new ShapePane();
		pane.setShapes(svg.getShapes());
		JScrollPane sp = new JScrollPane(pane);

		pane.addMouseWheelListener(we ->
		{
			if (we.getWheelRotation() != 0)
			{
				int mod = we.getModifiersEx();
				if ((mod & InputEvent.META_DOWN_MASK) != 0 || (mod & InputEvent.CTRL_DOWN_MASK) != 0)
				{
					double scale = -0.1 * we.getWheelRotation();
					double x = pane.getXScale() + scale;
					double y = pane.getYScale() + scale;
					if (x >= 0.1 && y >= 0.1)
					{
						pane.setScale(x, y);
						sp.revalidate();
					}
				}
			}
		});

		JFrame frame = new JFrame("SVG: " + args[0]);

		frame.setLayout(new BorderLayout());
		frame.add(sp);
		frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		frame.pack();
		frame.setVisible(true);
	}
}

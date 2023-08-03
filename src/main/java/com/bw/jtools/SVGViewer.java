package com.bw.jtools;

import com.bw.jtools.shape.ShapePainter;
import com.bw.jtools.svg.SVGConverter;
import com.bw.jtools.svg.SVGException;
import com.bw.jtools.ui.ShapeMultiResolutionImage;
import com.bw.jtools.ui.ShapePane;

import javax.swing.BorderFactory;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.file.Files;

/**
 * Demonstration- and Test-Utility to load and draw SVG files via jSVG.
 */
public class SVGViewer extends SVGAppBase
{
	protected ShapePane pane_;
	protected File svgFile_;


	protected void loadSVG2Pane(java.nio.file.Path svgFile)
	{
		try
		{
			InputStream ips = new BufferedInputStream(Files.newInputStream(svgFile));
			SVGConverter nsvg = new SVGConverter(ips);
			pane_.setShapes(nsvg.getShapes());
		}
		catch (Exception err)
		{
			err.printStackTrace();
		}

	}

	/**
	 * Shows a SVG file.
	 *
	 * @param args File name
	 */
	public static void main(String[] args) throws FileNotFoundException, SVGException
	{
		SVGViewer frame = new SVGViewer(args.length > 0 ? args[0] : null);
		frame.pack();
		frame.setVisible(true);
	}

	/**
	 * Create a new SVGViewer.
	 * Caller has to call "pack" and "setVisible".
	 *
	 * @param file The file to show or null.
	 */
	public SVGViewer(String file)
	{
		pane_ = new ShapePane();
		// ScrollPane viewport will clear on paint. No need to do it twice.
		pane_.setOpaque(false);

		// Activate time measurement in painter for the status bar.
		pane_.getPainter()
			 .setTimeMeasurementEnabled(true);

		if (file != null)
		{
			loadSVG2Pane(new File(file).toPath());
		}
		else
		{
			setTitle("jSVG Demonstration");
		}
		// Let us zoom by mouse-wheel...
		pane_.setZoomByMouseWheelEnabled(true);

		// Create layout (menu, painter pane and status bar).
		setLayout(new BorderLayout());
		JScrollPane sp = new JScrollPane(pane_);
		add(BorderLayout.CENTER, sp);

		JTextField status = new JTextField();
		status.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
		status.setEditable(false);
		add(BorderLayout.SOUTH, status);

		// Start timer to cyclic update status bar (but not for every paint!)
		startMeasurementTimer(status, pane_.getPainter());

		// Create menus
		JMenuItem loadMenuItem = new JMenuItem("Open...", KeyEvent.VK_O);
		loadMenuItem.setToolTipText("<html>Loads an other SVG.</html>");
		loadMenuItem.addActionListener(e ->
		{
			JFileChooser fs = getSVGFileChooser();
			if (svgFile_ != null)
			{
				fs.setCurrentDirectory(svgFile_.getParentFile());
			}
			int returnVal = fs.showOpenDialog(this);
			if (returnVal == JFileChooser.APPROVE_OPTION)
			{
				loadSVG2Pane(fs.getSelectedFile()
							   .toPath());
			}
		});

		JMenuItem saveAsImageMenuItem = new JMenuItem("Save as PNG...", KeyEvent.VK_P);
		saveAsImageMenuItem.setToolTipText("<html>Saves the SVG with the<br>current scale to a PNG image.</html>");
		saveAsImageMenuItem.addActionListener(e ->
		{
			JFileChooser fs = getPNGFileChooser();
			int returnVal = fs.showSaveDialog(this);
			if (returnVal == JFileChooser.APPROVE_OPTION)
			{
				saveImage(fs.getSelectedFile());
			}
		});


		JMenuBar menuBar = new JMenuBar();
		JMenu fileMenu = new JMenu("File");
		fileMenu.add(loadMenuItem);
		fileMenu.add(saveAsImageMenuItem);
		menuBar.add(fileMenu);
		setJMenuBar(menuBar);

		setLocationByPlatform(true);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setMinimumSize(new Dimension(400, 300));

		try
		{
			ShapePainter svgIconPainter = new ShapePainter(
					SVGConverter.convert(SVGViewer.class.getResourceAsStream("jSVGIcon.svg")));
			setIconImage(new ShapeMultiResolutionImage(svgIconPainter));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Save the SVG as PNG bitmap with the current scale.
	 *
	 * @param pngFile The File to store to. If extension is missing or empty, ".png" is added.
	 */
	public void saveImage(File pngFile)
	{
		saveImage(pngFile, pane_.getPainter());
	}

}

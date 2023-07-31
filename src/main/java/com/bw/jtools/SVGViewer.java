package com.bw.jtools;

import com.bw.jtools.shape.ShapePane;
import com.bw.jtools.svg.SVGConverter;
import com.bw.jtools.svg.SVGException;

import javax.swing.BorderFactory;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.Timer;
import javax.swing.WindowConstants;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * Demonstration and Test utility to load and draw SVG files via jSVG.
 */
public class SVGViewer extends JFrame
{
	protected long timeMS = 0;
	protected ShapePane pane;

	protected JFileChooser fileChooser;

	protected File svgFile;

	protected JFileChooser getFileChooser()
	{
		if (fileChooser == null)
		{
			fileChooser = new JFileChooser();
			fileChooser.setFileFilter(new FileNameExtensionFilter("SVG files", "svg"));
			fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		}
		return fileChooser;
	}

	protected void loadSVG(File svgFile)
	{
		try
		{
			InputStream ips = new BufferedInputStream(new FileInputStream(svgFile));
			SVGConverter nsvg = new SVGConverter(ips);
			setTitle("SVG: " + svgFile);
			this.svgFile = svgFile;
			pane.setShapes(nsvg.getShapes());
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
		pane = new ShapePane();
		// ScrollPane viewport will clear on paint. No need to do it twice.
		pane.setOpaque(false);

		// Activate time measurement in painter for the status bar.
		pane.getPainter()
			.setTimeMeasurementEnabled(true);

		if (file != null)
		{
			loadSVG(new File(file));
		}
		// Let us zoom by mouse-wheel...
		pane.setZoomByMouseWheelEnabled(true);

		// Create layout (menu, painter pane and status bar).
		setLayout(new BorderLayout());
		JScrollPane sp = new JScrollPane(pane);
		add(BorderLayout.CENTER, sp);

		JTextField status = new JTextField();
		status.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
		status.setEditable(false);
		add(BorderLayout.SOUTH, status);

		// Start timer to cyclic update status bar (but not for every paint!)
		startMeasurementTimer(status);

		// Create menus
		JMenuItem loadMenuItem = new JMenuItem("Open...", KeyEvent.VK_O);
		loadMenuItem.addActionListener(e ->
		{
			JFileChooser fs = getFileChooser();
			if (svgFile != null)
			{
				fs.setCurrentDirectory(svgFile.getParentFile());
			}
			int returnVal = fs.showOpenDialog(this);
			if (returnVal == JFileChooser.APPROVE_OPTION)
			{
				loadSVG(fs.getSelectedFile());
			}
		});

		JMenuBar menuBar = new JMenuBar();
		JMenu fileMenu = new JMenu("File");
		fileMenu.add(loadMenuItem);
		menuBar.add(fileMenu);
		setJMenuBar(menuBar);

		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setMinimumSize(new Dimension(400, 300));
	}

	protected void startMeasurementTimer(JTextField status)
	{
		Timer timer = new Timer(1000, e ->
		{
			timeMS = pane.getPainter()
						 .getMeasuredTimeMS();
			Dimension r = pane.getPreferredSize();
			status.setText(
					String.format("Size: %d x %d, Scale %.1f x %.1f%s",
							r.width, r.height, pane.getXScale(), pane.getYScale(),
							((timeMS > 0) ? ", Rendered in " + Double.toString(timeMS / 1000d) + "s" : "")));
		});
		timer.start();

		addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowClosed(WindowEvent e)
			{
				timer.stop();
			}
		});

	}
}

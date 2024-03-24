package com.bw.jtools.examples;

import com.bw.jtools.shape.AbstractPainterBase;
import com.bw.jtools.shape.AbstractShape;
import com.bw.jtools.svg.SVGException;
import com.bw.jtools.ui.ShapePane;

import javax.swing.BorderFactory;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import javax.swing.text.JTextComponent;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;

/**
 * Demonstration- and Test-Utility to load and draw SVG files via jSVG.<br>
 * You can start the class from command-line with the file as argument or select a file via menu.
 */
public class SVGViewer extends SVGAppBase
{
	/**
	 * The pane that shows the SVG shape.
	 */
	protected ShapePane pane_;

	/**
	 * The converted shape to show.
	 */
	protected AbstractShape shape_;

	/**
	 * The file to show.
	 */
	protected File svgFile_;

	/**
	 * Lazy created Dialog to show the paint-along-viewer.
	 * Contains {@link #paintAlongViewer_}
	 */
	protected JDialog paintAlongViewer_;

	/**
	 * Lazy created panel to show the paint-along-viewer.
	 */
	protected PaintAlongViewerPanel paintAlongPane_;

	@Override
	protected void setShapes(List<ShapeFile> shapes)
	{
		if (!shapes.isEmpty())
		{
			try
			{
				ShapeFile f = shapes.get(0);
				if (f.path_ != null)
					setTitle(f.path_.toString());
				else
					setTitle("jSVG Demonstration");

				shape_ = f.shape_;
				AbstractPainterBase painter = pane_.getPainter();

				pane_.setShape(shape_);
				pane_.setScale(1, 1);
				Rectangle2D.Double area = painter.getArea();

				Dimension s = getSize();
				if (s.width == 0)
				{
					s.width = 400;
				}
				if (s.height == 0)
				{
					s.height = 400;
				}

				double scale = Math.min(s.width / area.width, s.height / area.height);
				pane_.setScale(scale, scale);
			}
			catch (Exception err)
			{
				err.printStackTrace();
			}
		}
	}

	/**
	 * Shows a SVG file.
	 *
	 * @param args File name
	 * @throws SVGException          In case the file could not be parsed.
	 * @throws FileNotFoundException In case the files could not be found.
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
			loadSVGsFromPathOrUri(file);
		}
		else
		{
			setTitle("jSVG Demonstration");
		}
		// Let us zoom by ctrl-mouse-wheel...
		pane_.setZoomByMetaMouseWheelEnabled(true);
		// Let us drag by mouse...
		pane_.setMouseDragEnabled(true);
		// Let us rotate by shift-mouse-wheel...
		pane_.setRotateByShiftMouseWheelEnabled(true);

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
				loadSVGsFromPath(fs.getSelectedFile()
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

		JMenuItem paintAlongMenuItem = new JMenuItem("Paint along a Path");
		paintAlongMenuItem.setToolTipText("<html>Paints the image along some other path.</html>");
		paintAlongMenuItem.addActionListener(e ->
		{
			showPaintAlong();
		});

		JCheckBoxMenuItem clip = new JCheckBoxMenuItem("Clip viewBox");
		clip.setToolTipText("<html>Enables clipping of the outer viewBox attribute.</html>");
		clip.setSelected(true);
		clip.addActionListener(e ->
		{
			pane_.getPainter()
				 .setClippingEnabled(clip.isSelected());
			repaint();
		});


		JMenuBar menuBar = new JMenuBar();
		JMenu fileMenu = new JMenu("File");
		fileMenu.add(loadMenuItem);
		fileMenu.add(saveAsImageMenuItem);
		JMenu viewMenu = new JMenu("View");
		viewMenu.add(paintAlongMenuItem);
		viewMenu.add(clip);
		menuBar.add(fileMenu);
		menuBar.add(viewMenu);
		setJMenuBar(menuBar);

		setLocationByPlatform(true);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setMinimumSize(new Dimension(400, 300));
	}

	/**
	 * Shows the paint-along-dialog for the current shape.
	 */
	public void showPaintAlong()
	{
		if (paintAlongViewer_ == null)
		{
			paintAlongViewer_ = new JDialog(this);
			paintAlongViewer_.setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
			paintAlongViewer_.setLayout(new BorderLayout());

			paintAlongPane_ = new PaintAlongViewerPanel();
			paintAlongPane_.setTilePainter(pane_.getPainter());
			paintAlongPane_.addPath(new Rectangle2D.Double(0, 0, 200, 200));
			paintAlongPane_.addPath(new Ellipse2D.Double(50, 50, 100, 100));

			Path2D weave = new Path2D.Double();
			weave.moveTo(0, 280);
			weave.curveTo(66, 210, 133, 350, 200, 280);


			paintAlongPane_.addPath(weave);

			paintAlongViewer_.setContentPane(paintAlongPane_);
			paintAlongViewer_.pack();
			paintAlongViewer_.setLocationRelativeTo(this);

			addWindowListener(new WindowAdapter()
			{
				@Override
				public void windowClosed(WindowEvent e)
				{
					paintAlongViewer_.setVisible(false);
					paintAlongViewer_.dispose();
				}
			});
			paintAlongViewer_.setTitle("Paint Along");

		}
		paintAlongPane_.setTileShape(shape_);
		paintAlongViewer_.setVisible(true);
	}

	/**
	 * Save the SVG as PNG bitmap with the current scale.
	 *
	 * @param pngFile The File to store to. If extension is missing or empty, ".png" is added.
	 */
	public void saveImage(File pngFile)
	{
		pane_.getPainter()
			 .saveAsImage(pngFile);
	}


	private double lastScaleX_;
	private double lastScaleY_;
	private double lastRotation_;
	private boolean lastClippingEnabled_;


	@Override
	protected void statusUpdate(JTextComponent status, AbstractPainterBase painter)
	{
		super.statusUpdate(status, painter);
		if (paintAlongViewer_ != null && paintAlongViewer_.isVisible())
		{

			double scaleX = painter.getXScale();
			double scaleY = painter.getYScale();
			double rotation = painter.getRotationAngleDegree();
			boolean clippingEnabled = painter.isClippingEnabled();

			if (scaleY != lastScaleY_ || scaleX != lastScaleX_ || rotation != lastRotation_ || clippingEnabled != lastClippingEnabled_)
			{
				lastScaleY_ = scaleY;
				lastScaleX_ = scaleX;
				lastRotation_ = rotation;
				lastClippingEnabled_ = clippingEnabled;
				paintAlongPane_.updateTilePainter();
			}
		}
	}

}

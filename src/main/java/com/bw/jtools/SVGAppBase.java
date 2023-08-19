package com.bw.jtools;

import com.bw.jtools.shape.AbstractShape;
import com.bw.jtools.shape.ShapePainter;
import com.bw.jtools.svg.SVGConverter;
import com.bw.jtools.ui.SVGFilePreview;
import com.bw.jtools.ui.ShapeMultiResolutionImage;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JTextField;
import javax.swing.Timer;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Base class for the Demonstration- and Test-Utilities in jSVG.
 */
public class SVGAppBase extends JFrame
{
	protected long timeMS = 0;

	protected JFileChooser svgFileChooser;
	protected JFileChooser pngFileChooser;


	protected List<AbstractShape> loadSVG(java.nio.file.Path svgFile)
	{
		try
		{
			InputStream ips = new BufferedInputStream(Files.newInputStream(svgFile));
			SVGConverter nsvg = new SVGConverter(ips);
			return nsvg.getShapes();
		}
		catch (Exception err)
		{
			err.printStackTrace();
			return Collections.emptyList();
		}
	}


	protected JFileChooser getSVGFileChooser()
	{
		if (svgFileChooser == null)
		{
			svgFileChooser = new JFileChooser();
			svgFileChooser.setFileFilter(new FileNameExtensionFilter("SVG files", "svg"));
			svgFileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			// Install SVG preview.
			new SVGFilePreview(svgFileChooser);
		}
		return svgFileChooser;
	}

	protected JFileChooser getPNGFileChooser()
	{
		if (pngFileChooser == null)
		{
			pngFileChooser = new JFileChooser();
			pngFileChooser.setFileFilter(new FileNameExtensionFilter("Portable Network Graphics (PNG) files", "png"));
			pngFileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		}
		return pngFileChooser;
	}


	/**
	 * Create a new SVGViewer.
	 * Caller has to call "pack" and "setVisible".
	 */
	public SVGAppBase()
	{
		setAppIcon();
	}

	/**
	 * Save the SVG as PNG bitmap with the current scale.
	 *
	 * @param pngFile The File to store to. If extension is missing or empty, ".png" is added.
	 */
	public void saveImage(File pngFile, ShapePainter painter)
	{
		if (pngFile != null)
		{
			BufferedImage image = painter
					.paintShapedToBufferTransparent(null);
			if (image != null)
			{
				try
				{
					String fileName = pngFile.getName();
					int i = fileName.lastIndexOf('.');
					if (i < 0 || i == fileName.length() - 1)
					{
						if (i < 0)
						{
							fileName += ".";
						}
						fileName += "png";
						String dir = pngFile.getParent();
						if (dir == null)
							dir = "";
						if (!dir.isEmpty())
							dir += File.separator;
						pngFile = new File(dir + fileName);
					}
					ImageIO.write(image, "png", pngFile);
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		}
	}

	protected Timer measurementTimer_;

	private String lastStatus_;
	private int lastStatusCount_ = 0;

	protected void startMeasurementTimer(JTextField status, final ShapePainter painter)
	{
		if (measurementTimer_ == null)
		{
			addWindowListener(new WindowAdapter()
			{
				@Override
				public void windowClosed(WindowEvent e)
				{
					SVGAppBase.this.measurementTimer_.stop();
				}
			});
		}
		else
		{
			measurementTimer_.stop();
		}
		measurementTimer_ = new Timer(1000, e ->
		{
			timeMS = painter.getMeasuredTimeMS();
			Rectangle2D r = painter.getArea();

			String statusText = String.format("Size: %d x %d, Scale %.1f x %.1f, Rotation %.1f\u00B0%s",
					(int) r.getWidth(), (int) r.getHeight(), painter.getXScale(), painter.getYScale(), painter.getRotationAngleDegree(),
					((timeMS > 0) ? ", Rendered in " + Double.toString(timeMS / 1000d) + "s" : ""));

			if (Objects.equals(lastStatus_, statusText))
			{
				++lastStatusCount_;
				if (lastStatusCount_ == 5)
				{
					status.setText("Use the Mouse-Wheel +Meta/Ctrl to scale, +Shift to rotate.");
				}
			}
			else
			{
				lastStatusCount_ = 0;
				lastStatus_ = statusText;
				status.setText(statusText);
			}
		});
		measurementTimer_.start();
	}

	protected void setAppIcon()
	{
		if ( false)
		try
		{
			ShapePainter svgIconPainter = new ShapePainter(
					SVGConverter.convert(SVGIconTester.class.getResourceAsStream("SVGIcon.svg")));
			setIconImage(new ShapeMultiResolutionImage(svgIconPainter));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}

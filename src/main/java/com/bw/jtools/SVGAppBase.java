package com.bw.jtools;

import com.bw.jtools.shape.AbstractPainterBase;
import com.bw.jtools.shape.AbstractShape;
import com.bw.jtools.shape.ShapePainter;
import com.bw.jtools.svg.SVGConverter;
import com.bw.jtools.ui.SVGFilePreview;
import com.bw.jtools.ui.ShapeMultiResolutionImage;
import com.bw.jtools.ui.ShapePane;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.Timer;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.JTextComponent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Rectangle2D;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Objects;

/**
 * Base class for the Demonstration- and Test-Utilities in jSVG.
 */
public class SVGAppBase extends JFrame
{
	protected long timeMS = 0;

	protected JFileChooser svgFileChooser;

	protected AbstractShape loadSVG(java.nio.file.Path svgFile)
	{
		try
		{
			InputStream ips = new BufferedInputStream(Files.newInputStream(svgFile));
			SVGConverter nsvg = new SVGConverter(ips);
			return nsvg.getShape();
		}
		catch (Exception err)
		{
			err.printStackTrace();
			return null;
		}
	}

	protected FileNameExtensionFilter svgFileFilter = new FileNameExtensionFilter("SVG files", "svg");


	protected JFileChooser getSVGFileChooser()
	{
		if (svgFileChooser == null)
		{
			svgFileChooser = new JFileChooser();
			svgFileChooser.setFileFilter(svgFileFilter);
			svgFileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			// Install SVG preview.
			new SVGFilePreview(svgFileChooser);
		}
		return svgFileChooser;
	}

	protected JFileChooser getPNGFileChooser()
	{
		return ShapePane.getPNGFileChooser();
	}


	/**
	 * Create a new SVGViewer.
	 * Caller has to call "pack" and "setVisible".
	 */
	public SVGAppBase()
	{
		setAppIcon();
	}

	protected Timer measurementTimer_;

	private String lastStatus_;
	private int lastStatusCount_ = 0;

	protected void startMeasurementTimer(final JTextComponent status, final AbstractPainterBase painter)
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
			statusUpdate(status, painter);
		});
		measurementTimer_.start();
	}

	protected void setAppIcon()
	{
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

	protected void statusUpdate(JTextComponent status, AbstractPainterBase painter)
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
	}
}

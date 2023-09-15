package com.bw.jtools.examples;

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
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.text.JTextComponent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Rectangle2D;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Base class for the Demonstration- and Test-Utilities in jSVG.
 */
public abstract class SVGAppBase extends JFrame
{

	/**
	 * File Chooser to select SVG files.
	 */
	protected JFileChooser svgFileChooser;

	/**
	 * Loads a single SVG-file.
	 *
	 * @param svgFile The path to the file.
	 * @return The converted shape.
	 */
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

	/**
	 * File name filter for SVG files.
	 */
	protected FileNameExtensionFilter svgFileFilter = new FileNameExtensionFilter("SVG files", "svg");


	/**
	 * Gets the SVG file chooser.
	 *
	 * @return The file-chooser to select SVG files.
	 */
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

	/**
	 * Returns the PNG file chooser instance.
	 *
	 * @return The file chooser to select PNG files.
	 */
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

		System.out.println("Current Metal Theme: " + MetalLookAndFeel.getCurrentTheme()
																	 .getName());
	}

	/**
	 * The timer to update measurement status.
	 */
	protected Timer measurementTimer_;

	private String lastStatus_;
	private int lastStatusCount_ = 0;

	/**
	 * Starts measurement status timer.
	 *
	 * @param status  The text component to write to.
	 * @param painter The painter to monitor.
	 */
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

	/**
	 * Sets the application icon.
	 */
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

	/**
	 * Updates status
	 *
	 * @param status  The text component to write to.
	 * @param painter Painter to get status for.
	 */
	protected void statusUpdate(JTextComponent status, AbstractPainterBase painter)
	{
		long timeMS = painter.getMeasuredTimeMS();
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

	static private Pattern urlPattern = Pattern.compile("^([^:/?#]+):(//[^/?#]*)?([^?#]*)(?:\\?([^#]*))?(#.*)?");

	/**
	 * Checks via reg-exp if a string is a URI.
	 *
	 * @param value The value to check.
	 * @return true if the value is most likely a URI.
	 */
	public static boolean isUri(String value)
	{
		Matcher m = urlPattern.matcher(value);
		// Reject any schema < 1 character, it's most likely a window path.
		return m.matches() && (m.group(1)
								.length() > 1);
	}


	/**
	 * Loads SVG from a path or URI.
	 *
	 * @param path Path to a single file or to a directory.
	 */
	protected void loadSVGsFromPathOrUri(String path)
	{
		try
		{
			Path p = null;
			if (isUri(path))
			{
				URI uri = URI.create(path);
				final String scheme = uri.getScheme();
				if (scheme != null)
				{
					if (scheme.equalsIgnoreCase("jar"))
					{
						FileSystem fs = null;
						int si = path.indexOf("!/");
						String arc = path.substring(0, si);
						try
						{
							URI fsuri = new URI(arc);
							try
							{
								fs = FileSystems.getFileSystem(fsuri);
							}
							catch (FileSystemNotFoundException fsnf)
							{
								fs = FileSystems.newFileSystem(fsuri, Collections.emptyMap());
							}
							p = fs.getPath(path.substring(si + 1));
						}
						catch (Exception ex2)
						{
							ex2.printStackTrace();
						}
					}
					else
					{
						p = java.nio.file.Paths.get(uri);
					}
				}
				else
				{
					System.err.println("Path Schema not correct: " + path);
				}
			}
			else
			{
				p = Paths.get(path);
			}
			if (p != null)
				loadSVGsFromPath(p);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Loads SVG files from a path.
	 *
	 * @param path Path to a single file or to a directory.
	 */
	protected void loadSVGsFromPath(Path path)
	{
		try
		{
			if (Files.isDirectory(path))
			{
				try (Stream<Path> f = Files.list(path))
				{
					loadSVGs(f);
				}
			}
			else
			{
				loadSVGs(Collections.singletonList(path)
									.stream());
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}


	/**
	 * Load svg files. Will call {@link #loadSVGs(Stream)}.
	 *
	 * @param files Array with files.
	 */
	protected void loadSVGsFromFiles(File[] files)
	{
		loadSVGs(
				Arrays.asList(files)
					  .stream()
					  .map(f -> f.toPath()));
	}


	/**
	 * Regular expression to match svg-file paths.
	 */
	protected Pattern svgFileNameRegEx = Pattern.compile(".*\\.svg", Pattern.CASE_INSENSITIVE);


	/**
	 * Helper class to hold a shape with the source-file from where it was loaded.
	 */
	/**
	 * Helper class to hold information about a shape and its source file.
	 */
	protected final static class ShapeFile
	{
		/**
		 * The path the file was loaded from.
		 */
		public final Path path_;

		/**
		 * The shape.
		 */
		public final AbstractShape shape_;

		/**
		 * Creates a new instance.
		 *
		 * @param p The path.
		 * @param s The shape.
		 */
		public ShapeFile(Path p, AbstractShape s)
		{
			path_ = p;
			shape_ = s;
		}
	}

	/**
	 * Loads recursively all svg files from a stream of paths.
	 *
	 * @param paths Stream with file and/or directories.
	 */
	protected void loadSVGs(Stream<Path> paths)
	{
		List<ShapeFile> shapes = new ArrayList<>();
		paths.filter(p -> Files.isDirectory(p) || svgFileNameRegEx.matcher(p.getFileName()
																			.toString())
																  .matches())
			 .sorted((p1, p2) -> p1.getFileName()
								   .toString()
								   .compareTo(p1.getFileName()
												.toString()))
			 .forEach(path ->
			 {
				 if (Files.isDirectory(path))
				 {
					 try (Stream<Path> f = Files.list(path))
					 {
						 loadSVGs(f);
					 }
					 catch (IOException e)
					 {
						 e.printStackTrace();
					 }
				 }
				 else
				 {
					 System.out.println("Loading shape from " + path);
					 final AbstractShape shape = loadSVG(path);
					 shapes.add(new ShapeFile(path, shape));
				 }
			 });
		setShapes(shapes);
	}

	/**
	 * Sets loaded shapes.
	 * Needs to be implemented by inheritances.
	 *
	 * @param shapes The loaded shapes.
	 */
	protected abstract void setShapes(List<ShapeFile> shapes);

}

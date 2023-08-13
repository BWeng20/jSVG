package com.bw.jtools;

import com.bw.jtools.shape.AbstractShape;
import com.bw.jtools.shape.ShapePainter;
import com.bw.jtools.svg.SVGConverter;
import com.bw.jtools.svg.SVGException;
import com.bw.jtools.ui.ShapeIcon;
import com.bw.jtools.ui.ShapeMultiResolutionImage;
import com.bw.jtools.ui.ShapePane;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import javax.swing.border.Border;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Demonstration- and Test-Utility to test SVGs on buttons.
 */
public class SVGIconTester extends SVGAppBase
{

	protected Path svgPath_;
	protected JPanel pane_;

	protected int iconSize = 32;

	/**
	 * Shows a SVG file.
	 *
	 * @param args File name
	 */
	public static void main(String[] args) throws FileNotFoundException, SVGException
	{
		SVGIconTester frame = new SVGIconTester(args.length > 0 ? args[0] : null);
		frame.pack();
		frame.setVisible(true);
	}

	/**
	 * Create a new SVGViewer.
	 * Caller has to call "pack" and "setVisible".
	 *
	 * @param file The file to show or null.
	 */
	public SVGIconTester(String path)
	{
		pane_ = new JPanel(new FlowLayout());
		// pane_.setDoubleBuffered(false);
		if (path != null)
		{
			loadSVGs(path);
		}
		else
		{
			setTitle("jSVG Icon Demonstration");
		}

		// Create layout (menu, painter pane and status bar).
		setLayout(new BorderLayout());
		// JScrollPane sp = new JScrollPane(pane_);
		add(BorderLayout.CENTER, pane_);

		JTextField status = new JTextField();
		status.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
		status.setEditable(false);
		add(BorderLayout.SOUTH, status);

		// Create menus
		JMenuItem loadMenuItem = new JMenuItem("Open...", KeyEvent.VK_O);
		loadMenuItem.setToolTipText("<html>Loads other SVGs.</html>");
		loadMenuItem.addActionListener(e ->
		{
			JFileChooser fs = getSVGFileChooser();
			fs.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
			if (svgPath_ != null && "file".equals(svgPath_.getFileSystem()
														  .provider()
														  .getScheme()))
			{
				fs.setCurrentDirectory(svgPath_.toFile()
											   .getParentFile());
			}
			int returnVal = fs.showOpenDialog(this);
			if (returnVal == JFileChooser.APPROVE_OPTION)
			{
				loadSVGs(fs.getSelectedFiles());
			}
		});

		JMenuBar menuBar = new JMenuBar();
		JMenu fileMenu = new JMenu("File");
		fileMenu.add(loadMenuItem);
		menuBar.add(fileMenu);

		JMenu viewMenu = new JMenu("View");
		javax.swing.ButtonGroup sizeGroup = new ButtonGroup();

		for (int vsize : Arrays.asList(16, 32, 64, 128))
		{
			JRadioButtonMenuItem menuSizeX = new JRadioButtonMenuItem(String.format("%1$dx%1$d", vsize));
			sizeGroup.add(menuSizeX);
			viewMenu.add(menuSizeX);
			if (vsize == iconSize)
				menuSizeX.setSelected(true);
			final int vsizeFinal = vsize;
			menuSizeX.addActionListener(e -> setIconSize(vsizeFinal, vsizeFinal));
		}

		menuBar.add(viewMenu);
		setJMenuBar(menuBar);

		setLocationByPlatform(true);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setMinimumSize(new Dimension(400, 300));

		try
		{
			ShapePainter svgIconPainter = new ShapePainter(
					SVGConverter.convert(SVGIconTester.class.getResourceAsStream("jSVGIcon.svg")));
			setIconImage(new ShapeMultiResolutionImage(svgIconPainter));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

	}

	static private Pattern urlPattern = Pattern.compile("^([^:/?#]+):(//[^/?#]*)?([^?#]*)(?:\\?([^#]*))?(#.*)?");

	public static boolean isUri(String value)
	{
		Matcher m = urlPattern.matcher(value);
		// Reject any schema < 1 character, it's most likely a window path.
		return m.matches() && (m.group(1)
								.length() > 1);
	}

	protected void loadSVGs(String path)
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
				loadSVGs(p);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	protected void loadSVGs(Path path)
	{
		try
		{
			if (Files.isDirectory(path))
			{
				loadSVGs(Files.list(path));
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


	protected void loadSVGs(File[] files)
	{
		loadSVGs(
				Arrays.asList(files)
					  .stream()
					  .map(f -> f.toPath()));
	}

	final static Border br = BorderFactory.createLineBorder(Color.BLACK, 1);
	final static Insets is = new Insets(1, 1, 1, 1);

	JDialog contentViewer;
	ShapePane drawPane;

	protected void showShapes(String name, List<AbstractShape> shapes)
	{
		if (contentViewer == null)
		{
			contentViewer = new JDialog(this);
			contentViewer.setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);

			drawPane = new ShapePane();
			drawPane.setZoomByMouseWheelEnabled(true);
			drawPane.setInlineBorder(true);

			contentViewer.setLayout(new BorderLayout());
			contentViewer.add(BorderLayout.CENTER, new JScrollPane(drawPane));
			contentViewer.setPreferredSize(new Dimension(400, 400));

			contentViewer.pack();
			contentViewer.setLocationRelativeTo(this);

			addWindowListener(new WindowAdapter()
			{
				@Override
				public void windowClosed(WindowEvent e)
				{
					contentViewer.setVisible(false);
					contentViewer.dispose();
				}
			});
		}
		drawPane.setShapes(shapes);
		drawPane.setScale(1, 1);
		contentViewer.setTitle(name);
		contentViewer.setVisible(true);
	}

	protected void loadSVGs(Stream<Path> paths)
	{
		paths.forEach(path ->
		{
			final List<AbstractShape> shapes = loadSVG(path);
			System.out.println("Loaded " + shapes.size() + " shapes from " + path);

			ShapeIcon sicon = new ShapeIcon(shapes);
			int w = sicon.getIconWidth();
			int h = sicon.getIconHeight();
			// Keep Aspect ratio
			double scale = Math.min(32.0 / w, 32.0 / h);
			sicon.setScale(scale, scale);

			JButton b = new JButton(sicon);
			b.addActionListener(e ->
			{
				showShapes(path.toString(), shapes);
			});
			b.setOpaque(true);
			b.setBackground(Color.WHITE);
			b.setPreferredSize(new Dimension(34, 34));
			// b.setMaximumSize(new Dimension(34,34));
			pane_.add(b);
		});
	}

	void setIconSize(int w, int h)
	{
		iconSize = w;
		for (int ci = 0; ci < pane_.getComponentCount(); ++ci)
		{
			JComponent c = (JComponent) pane_.getComponent(ci);
			if (c instanceof JButton)
			{
				ShapeIcon si = (ShapeIcon) ((JButton) c).getIcon();
				si.setScale(1, 1);
				double iw = si.getIconWidth();
				double ih = si.getIconHeight();
				// Keep Aspect ratio
				double scale = Math.min(w / iw, h / ih);
				si.setScale(scale, scale);
				c.setPreferredSize(new Dimension(w + 2, h + 2));
			}
		}
		revalidate();
		pack();
	}
}

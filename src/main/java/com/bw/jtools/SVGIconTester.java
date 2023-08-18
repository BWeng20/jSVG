package com.bw.jtools;

import com.bw.jtools.shape.AbstractShape;
import com.bw.jtools.svg.SVGException;
import com.bw.jtools.ui.ShapeIcon;
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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
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
	protected JScrollPane scrollPane_;

	protected javax.swing.ButtonGroup sizeGroup_;

	protected int iconSize_ = 32;
	private int[] iconSizes_ = { 16, 32, 64, 128 };

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
	 * @param path The file to show or null.
	 */
	public SVGIconTester(String path)
	{
		gc.anchor = GridBagConstraints.NORTHWEST;
		gc.fill = GridBagConstraints.NONE;

		pane_ = new JPanel(new GridBagLayout());
		if (path != null)
		{
			loadSVGsFromPathOrUri(path);
		}
		else
		{
			setTitle("jSVG Icon Demonstration");
		}

		// Create layout (menu, painter pane and status bar).
		setLayout(new BorderLayout());
		JScrollPane sp = new JScrollPane(pane_);
		add(BorderLayout.CENTER, sp);

		JTextField status = new JTextField();
		status.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
		status.setEditable(false);
		add(BorderLayout.SOUTH, status);

		// Create menus
		JMenuItem loadMenuItem = new JMenuItem("Open...", KeyEvent.VK_O);
		loadMenuItem.setToolTipText("<html>Loads SVGs. You can select multiple SVGs or directories.</html>");
		loadMenuItem.addActionListener(e ->
		{
			JFileChooser fs = getSVGFileChooser();
			fs.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
			fs.setMultiSelectionEnabled(true);
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
				gc.gridy = 0;
				gc.gridx = 0;

				pane_.removeAll();
				loadSVGsFromFiles(fs.getSelectedFiles());
			}
		});

		JMenuBar menuBar = new JMenuBar();
		JMenu fileMenu = new JMenu("File");
		fileMenu.add(loadMenuItem);
		menuBar.add(fileMenu);

		JMenu viewMenu = new JMenu("View");
		sizeGroup_ = new ButtonGroup();

		for (int vsize : iconSizes_)
		{
			JRadioButtonMenuItem menuSizeX = new JRadioButtonMenuItem(String.format("%1$dx%1$d", vsize));
			sizeGroup_.add(menuSizeX);
			viewMenu.add(menuSizeX);
			if (vsize == iconSize_)
				menuSizeX.setSelected(true);
			final int vsizeFinal = vsize;
			menuSizeX.addActionListener(e -> setIconSize(vsizeFinal));
		}

		menuBar.add(viewMenu);
		setJMenuBar(menuBar);

		setLocationByPlatform(true);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setMinimumSize(new Dimension(400, 300));
	}


	public int getIconSize() {
		return iconSize_;
	}

	static private Pattern urlPattern = Pattern.compile("^([^:/?#]+):(//[^/?#]*)?([^?#]*)(?:\\?([^#]*))?(#.*)?");

	public static boolean isUri(String value)
	{
		Matcher m = urlPattern.matcher(value);
		// Reject any schema < 1 character, it's most likely a window path.
		return m.matches() && (m.group(1)
								.length() > 1);
	}

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

	protected void loadSVGsFromPath(Path path)
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


	protected void loadSVGsFromFiles(File[] files)
	{
		loadSVGs(
				Arrays.asList(files)
					  .stream()
					  .map(f -> f.toPath()));
	}

	final static Border br = BorderFactory.createLineBorder(Color.BLACK, 1);
	final static Insets is = new Insets(1, 1, 1, 1);

	JDialog contentViewer_;
	ShapePane drawPane_;

	final GridBagConstraints gc = new GridBagConstraints();


	protected void showShapes(String name, List<AbstractShape> shapes)
	{
		if (contentViewer_ == null)
		{
			contentViewer_ = new JDialog(this);
			contentViewer_.setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);

			drawPane_ = new ShapePane();
			drawPane_.setZoomByMetaMouseWheelEnabled(true);
			drawPane_.setMouseDragEnabled(true);
			drawPane_.setRotateByShiftMouseWheelEnabled(true);
			drawPane_.setInlineBorder(true);

			scrollPane_ = new JScrollPane(drawPane_);

			contentViewer_.setLayout(new BorderLayout());
			contentViewer_.add(BorderLayout.CENTER, scrollPane_);
			contentViewer_.setPreferredSize(new Dimension(400, 400));

			contentViewer_.pack();
			contentViewer_.setLocationRelativeTo(this);

			addWindowListener(new WindowAdapter()
			{
				@Override
				public void windowClosed(WindowEvent e)
				{
					contentViewer_.setVisible(false);
					contentViewer_.dispose();
				}
			});
		}
		Dimension s = scrollPane_.getSize(null);
		Rectangle2D.Double targetArea = new Rectangle2D.Double(0,0,s.width-4, s.height-4);

		drawPane_.setShapes(shapes);
		drawPane_.setScale(1,1);
		Rectangle2D.Double area = drawPane_.getPainter().getArea();

		double scale = Math.min( targetArea.width/area.width, targetArea.height/area.height );
		drawPane_.setScale( scale, scale );

		contentViewer_.setTitle(name);
		contentViewer_.setVisible(true);
	}

	protected void loadSVGs(Stream<Path> paths)
	{

		paths.forEach(path ->
		{
			if (Files.isDirectory(path))
			{
				try
				{
					loadSVGs(Files.list(path));
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}
			else
			{
				if (gc.gridx >= 20)
				{
					gc.gridx = 0;
					gc.gridy++;
				}

				final List<AbstractShape> shapes = loadSVG(path);
				System.out.println("Loaded " + shapes.size() + " shapes from " + path);

				ShapeIcon sicon = new ShapeIcon(shapes);
				double w = sicon.getIconWidth();
				double h = sicon.getIconHeight();
				// Keep Aspect ratio
				double scale = Math.min(iconSize_ / w, iconSize_ / h);
				sicon.setScale(scale, scale);

				JButton b = new JButton(sicon);
				b.addActionListener(e ->
				{
					showShapes(path.toString(), shapes);
				});
				b.setOpaque(true);
				b.setBackground(Color.WHITE);
				b.setPreferredSize(new Dimension(iconSize_+2, iconSize_+2));
				// b.setMaximumSize(new Dimension(34,34));
				pane_.add(b, gc);
				gc.gridx++;
			}
		});

		revalidate();
		repaint();
	}

	void setIconSize(int s)
	{
		iconSize_ = s;
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
				double scale = Math.min(s / iw, s / ih);
				si.setScale(scale, scale);
				c.setPreferredSize(new Dimension(s + 2, s + 2));
			}
		}
		pane_.revalidate();
	}
}

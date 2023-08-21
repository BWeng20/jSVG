package com.bw.jtools;

import com.bw.jtools.shape.AbstractShape;
import com.bw.jtools.svg.SVGException;
import com.bw.jtools.ui.ShapeIcon;
import com.bw.jtools.ui.ShapePane;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.LookAndFeel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.border.Border;
import javax.swing.plaf.metal.MetalLookAndFeel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
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
import java.util.function.Consumer;
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
	protected javax.swing.ButtonGroup lafGroup_;

	protected boolean showName_ = false;
	protected boolean enabled_ = true;

	protected boolean showBorder_ = true;

	protected int iconSize_ = 32;
	private int[] iconSizes_ = {16, 32, 64, 128};

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
		gc.gridx = 0;
		gc.gridy = 0;
		gc.gridheight = 1;
		gc.gridwidth = 1;
		gc.insets = new Insets(0, 0, 5, 5);
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
			JRadioButtonMenuItem menuSizeX = new JRadioButtonMenuItem(String.format("%d", vsize));
			sizeGroup_.add(menuSizeX);
			viewMenu.add(menuSizeX);
			if (vsize == iconSize_)
				menuSizeX.setSelected(true);
			final int vsizeFinal = vsize;
			menuSizeX.addActionListener(e -> setIconSize(vsizeFinal));
		}

		viewMenu.addSeparator();

		final JCheckBoxMenuItem showName = new JCheckBoxMenuItem("Show Name");
		showName.setSelected(showName_);
		showName.addActionListener(e -> setShowName(showName.isSelected()));
		viewMenu.add(showName);

		final JCheckBoxMenuItem enabled = new JCheckBoxMenuItem("Enable Buttons");
		enabled.setSelected(enabled_);
		enabled.addActionListener(e -> setShowEnabled(enabled.isSelected()));
		viewMenu.add(enabled);

		final JCheckBoxMenuItem showBorder = new JCheckBoxMenuItem("Border");
		showBorder.setSelected(showBorder_);
		showBorder.addActionListener(e -> setShowBorder(showBorder.isSelected()));
		viewMenu.add(showBorder);

		JMenu lafMenu = new JMenu("Look & Feel");
		lafGroup_ = new ButtonGroup();

		LookAndFeel selectedLaf = UIManager.getLookAndFeel();

		System.out.println("Current Metal Theme: " + MetalLookAndFeel.getCurrentTheme()
																	 .getName());

		for (UIManager.LookAndFeelInfo laf : UIManager.getInstalledLookAndFeels())
		{
			JRadioButtonMenuItem l = new JRadioButtonMenuItem(laf.getName());
			lafGroup_.add(l);
			lafMenu.add(l);
			final UIManager.LookAndFeelInfo lafFinal = laf;
			if (selectedLaf.getName()
						   .equals(laf.getName()))
			{
				l.setSelected(true);
			}
			l.addActionListener(e -> setLaf(lafFinal));
		}

		viewMenu.add(lafMenu);

		menuBar.add(viewMenu);
		setJMenuBar(menuBar);

		setLocationByPlatform(true);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setMinimumSize(new Dimension(400, 300));
	}

	public int getIconSize()
	{
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


	protected void showShape(String name, AbstractShape shape)
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
		Rectangle2D.Double targetArea = new Rectangle2D.Double(0, 0, s.width - 4, s.height - 4);

		drawPane_.setShape(shape);
		drawPane_.setScale(1, 1);
		Rectangle2D.Double area = drawPane_.getPainter()
										   .getArea();

		double scale = Math.min(targetArea.width / area.width, targetArea.height / area.height);
		drawPane_.setScale(scale, scale);

		contentViewer_.setTitle(name);
		contentViewer_.setVisible(true);
	}

	protected void loadSVGs(Stream<Path> paths)
	{

		paths.sorted((p1, p2) -> p1.getFileName()
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
					 final AbstractShape shape = loadSVG(path);
					 System.out.println("Loaded shape from " + path);
					 addSVG(path, shape);
				 }
			 });

		pane_.revalidate();
		repaint();
	}

	protected synchronized void addSVG(Path name, AbstractShape shape)
	{
		if (gc.gridx >= 20)
		{
			gc.gridx = 0;
			gc.gridy++;
		}
		ShapeIcon sicon = new ShapeIcon(shape);
		sicon.setDescription(name.getFileName()
								 .toString());
		double w = sicon.getIconWidth();
		double h = sicon.getIconHeight();
		// Keep Aspect ratio
		double scale = Math.min(iconSize_ / w, iconSize_ / h);
		sicon.setScale(scale, scale);

		JButton b = new JButton(sicon);

		b.setBorder(BorderFactory.createLineBorder(Color.GRAY));
		b.setMargin(new Insets(0, 0, 0, 0));
		b.setIconTextGap(0);
		b.setContentAreaFilled(false);

		b.setToolTipText(name.toString());
		b.setVerticalTextPosition(JButton.BOTTOM);
		b.setHorizontalTextPosition(JButton.CENTER);

		b.setText(showName_ ? sicon.getDescription() : null);
		// sicon.setGray
		b.setEnabled(enabled_);
		b.addActionListener(e ->
		{
			showShape(name.toString(), shape);
		});
		b.setOpaque(false);
		b.setBackground(Color.WHITE);
		pane_.add(b, gc);
		gc.gridx++;
	}

	void setShowName(boolean showName)
	{
		if (showName_ != showName)
		{
			showName_ = showName;
			updateAllButtons(b -> b.setText(showName_ ? ((ShapeIcon) b.getIcon()).getDescription() : null));
		}
	}

	void setShowEnabled(boolean showEnabled)
	{
		if (enabled_ != showEnabled)
		{
			enabled_ = showEnabled;
			updateAllButtons(b -> b.setEnabled(enabled_));
		}
	}

	void setShowBorder(boolean showBorder)
	{
		if (showBorder_ != showBorder)
		{
			showBorder_ = showBorder;
			final Border border = showBorder_ ? UIManager.getBorder("Button.border") : null;

			updateAllButtons(b ->
			{
				b.setBorderPainted(showBorder_);
				b.setBorder(border);
			});
		}
	}


	void setIconSize(int s)
	{
		iconSize_ = s;
		updateAllButtons(b ->
		{
			ShapeIcon si = (ShapeIcon) b.getIcon();
			si.setScale(1, 1);
			double iw = si.getIconWidth();
			double ih = si.getIconHeight();
			// Keep Aspect ratio
			double scale = Math.min(s / iw, s / ih);
			si.setScale(scale, scale);
		});
	}

	void setLaf(UIManager.LookAndFeelInfo laf)
	{
		try
		{
			UIManager.setLookAndFeel(laf.getClassName());
			Window[] ws = JFrame.getWindows();
			for (Window w : ws)
			{
				SwingUtilities.updateComponentTreeUI(w);
			}
			showBorder_ = !showBorder_;
			setShowBorder(!showBorder_);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	void updateAllButtons(Consumer<JButton> consumer)
	{
		for (int ci = 0; ci < pane_.getComponentCount(); ++ci)
		{
			JComponent c = (JComponent) pane_.getComponent(ci);
			if (c instanceof JButton)
			{
				consumer.accept(((JButton) c));
			}
		}
		pane_.revalidate();
		repaint();

	}

}

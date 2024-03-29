package com.bw.jtools.examples;

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
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

/**
 * Demonstration- and Test-Utility to test SVGs on buttons.<br>
 * You can start the class from command-line with directories or files as arguments or open them via file-menu.
 */
public class SVGIconTester extends SVGAppBase
{
	/**
	 * The panel to show all icons.
	 */
	protected JPanel pane_;

	/**
	 * Scroll-pane inside the svg content viewer (if an icon button is pressed).
	 */
	protected JScrollPane contentViewerScrollPane_;

	/**
	 * Pre-defined icon-sizes to select.
	 */
	protected javax.swing.ButtonGroup sizeGroup_;

	/**
	 * Look-And-Feel button.
	 */
	protected javax.swing.ButtonGroup lafGroup_;

	/**
	 * True if the file name shall be shown below the icons.
	 */
	protected boolean showName_ = false;

	/**
	 * True if all icons shall be enabled.
	 */
	protected boolean enabled_ = true;

	/**
	 * True if a border shall be drawn.
	 */
	protected boolean showBorder_ = true;

	/**
	 * The current icon size
	 */
	protected int iconSize_ = 32;
	private int[] iconSizes_ = {16, 32, 64, 128};

	/**
	 * Shows a SVG file.
	 *
	 * @param args File name
	 * @throws SVGException          In case the file could not be parsed.
	 * @throws FileNotFoundException In case the files could not be found.
	 */
	public static void main(String[] args) throws FileNotFoundException, SVGException
	{
		SVGIconTester frame = new SVGIconTester(args);
		frame.pack();
		frame.setVisible(true);
	}

	/**
	 * Create a new SVGViewer.
	 * Caller has to call "pack" and "setVisible".
	 *
	 * @param paths The file to show or null.
	 */
	public SVGIconTester(String... paths)
	{
		gc.gridx = 0;
		gc.gridy = 0;
		gc.gridheight = 1;
		gc.gridwidth = 1;
		gc.insets = new Insets(0, 0, 5, 5);
		gc.anchor = GridBagConstraints.NORTHWEST;
		gc.fill = GridBagConstraints.NONE;

		pane_ = new JPanel(new GridBagLayout());
		if (paths != null && paths.length > 0)
		{
			Arrays.asList(paths)
				  .forEach(this::loadSVGsFromPathOrUri);
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

	/**
	 * Gets the icon size.
	 *
	 * @return The icon size.
	 */
	public int getIconSize()
	{
		return iconSize_;
	}

	final static Border br = BorderFactory.createLineBorder(Color.BLACK, 1);
	final static Insets is = new Insets(1, 1, 1, 1);

	JDialog contentViewer_;

	/**
	 * The pane to show the selected shape in full size.
	 */
	ShapePane contentViewerDrawPane_;

	/**
	 * GridBagConstraints instance for reuse.
	 */
	final GridBagConstraints gc = new GridBagConstraints();


	/**
	 * SHows the shape.
	 *
	 * @param name  The title.
	 * @param shape The shape to show.
	 */
	protected void showShape(String name, AbstractShape shape)
	{
		if (contentViewer_ == null)
		{
			contentViewer_ = new JDialog(this);
			contentViewer_.setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);

			contentViewerDrawPane_ = new ShapePane();
			contentViewerDrawPane_.setZoomByMetaMouseWheelEnabled(true);
			contentViewerDrawPane_.setMouseDragEnabled(true);
			contentViewerDrawPane_.setRotateByShiftMouseWheelEnabled(true);
			contentViewerDrawPane_.setContextMenuEnabled(true);
			contentViewerDrawPane_.setInlineBorder(true);

			contentViewerScrollPane_ = new JScrollPane(contentViewerDrawPane_);

			contentViewer_.setLayout(new BorderLayout());
			contentViewer_.add(BorderLayout.CENTER, contentViewerScrollPane_);
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
		Dimension s = contentViewerScrollPane_.getSize(null);
		Rectangle2D.Double targetArea = new Rectangle2D.Double(0, 0, s.width - 4, s.height - 4);

		contentViewerDrawPane_.setShape(shape);
		contentViewerDrawPane_.setScale(1, 1);
		Rectangle2D.Double area = contentViewerDrawPane_.getPainter()
														.getArea();

		double scale = Math.min(targetArea.width / area.width, targetArea.height / area.height);
		contentViewerDrawPane_.setScale(scale, scale);

		contentViewer_.setTitle(name);
		contentViewer_.setVisible(true);
	}

	/**
	 * Adds a shape to the view.
	 *
	 * @param shapeFile The shape to add.
	 */
	protected synchronized void addSVG(ShapeFile shapeFile)
	{
		if (gc.gridx >= 20)
		{
			gc.gridx = 0;
			gc.gridy++;
		}
		ShapeIcon sicon = new ShapeIcon(shapeFile.shape_);
		sicon.setDescription(shapeFile.path_.getFileName()
											.toString());
		double w = sicon.getIconWidth2D();
		double h = sicon.getIconHeight2D();
		// Keep Aspect ratio
		double scale = Math.min(iconSize_ / w, iconSize_ / h);
		sicon.setScale(scale, scale);

		JButton b = new JButton(sicon);

		b.setBorderPainted(showBorder_);
		b.setBorder(showBorder_ ? UIManager.getBorder("Button.border") : null);
		b.setMargin(new Insets(0, 0, 0, 0));
		b.setIconTextGap(0);
		b.setContentAreaFilled(false);

		b.setToolTipText(shapeFile.path_.toString());
		b.setVerticalTextPosition(JButton.BOTTOM);
		b.setHorizontalTextPosition(JButton.CENTER);

		b.setText(showName_ ? sicon.getDescription() : null);
		// sicon.setGray
		b.setEnabled(enabled_);
		b.addActionListener(e ->
		{
			showShape(shapeFile.path_.toString(), shapeFile.shape_);
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

	@Override
	protected void setShapes(List<ShapeFile> shapes)
	{
		shapes.forEach(this::addSVG);
		pane_.revalidate();
		repaint();
	}
}

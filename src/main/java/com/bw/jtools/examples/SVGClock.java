package com.bw.jtools.examples;

import com.bw.jtools.shape.AbstractPainterBase;
import com.bw.jtools.shape.AbstractShape;
import com.bw.jtools.shape.animation.Animator;
import com.bw.jtools.svg.SVGException;
import com.bw.jtools.ui.ShapePane;
import com.bw.jtools.ui.editor.AbstractAnimationOptionPanel;
import com.bw.jtools.ui.editor.RotationAnimationController;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.Timer;
import javax.swing.WindowConstants;
import javax.swing.text.JTextComponent;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Rectangle2D;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Demonstration- and Test-Utility to test animated SVGs.<br>
 */
public class SVGClock extends SVGAppBase
{

	protected Path svgPath_;
	protected ShapePane pane_;
	protected AbstractShape shape_;
	protected Timer clock_;
	protected Timer status_;
	protected Animator animator_;
	protected JCheckBox animate_;


	protected boolean enabled_ = true;

	/**
	 * Shows a SVG file.
	 *
	 * @param args File name
	 */
	public static void main(String[] args) throws FileNotFoundException, SVGException
	{
		String file = null;
		if (args.length > 0)
			file = args[0];
		SVGClock frame = new SVGClock(file);
		frame.pack();
		frame.setVisible(true);
	}

	final GridBagConstraints gc = new GridBagConstraints();

	/**
	 * Create a new SVGViewer.
	 * Caller has to call "pack" and "setVisible".
	 *
	 * @param path The file to show or null.
	 */
	public SVGClock(String path)
	{
		gc.gridx = 0;
		gc.gridy = 0;
		gc.gridheight = 1;
		gc.gridwidth = 1;
		gc.insets = new Insets(0, 0, 5, 5);
		gc.anchor = GridBagConstraints.NORTHWEST;
		gc.fill = GridBagConstraints.NONE;

		pane_ = new ShapePane();
		// ScrollPane viewport will clear on paint. No need to do it twice.
		pane_.setOpaque(false);
		// Let us zoom by ctrl-mouse-wheel...
		pane_.setZoomByMetaMouseWheelEnabled(true);
		// Let us drag by mouse...
		pane_.setMouseDragEnabled(true);
		// Let us rotate by shift-mouse-wheel...
		pane_.setRotateByShiftMouseWheelEnabled(true);

		// Activate time measurement in painter for the status bar.
		pane_.getPainter()
			 .setTimeMeasurementEnabled(true);

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
			fs.setFileSelectionMode(JFileChooser.FILES_ONLY);
			fs.setMultiSelectionEnabled(false);
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

		final JCheckBoxMenuItem enabled = new JCheckBoxMenuItem("Enable Component");
		enabled.setSelected(enabled_);
		enabled.addActionListener(e -> setShowEnabled(enabled.isSelected()));
		viewMenu.add(enabled);

		menuBar.add(viewMenu);
		setJMenuBar(menuBar);

		seconds_ = new RotationAnimationController("Seconds");
		minutes_ = new RotationAnimationController("Minutes");
		hour_ = new RotationAnimationController("Hour");

		animate_ = new JCheckBox("On");
		animate_.setSelected(false);
		animate_.addChangeListener(e ->
		{
			if (animator_ != null)
			{
				if (animate_.isSelected())
				{
					if (!animator_.isRunning())
					{
						animator_.start();
					}
				}
				else
				{
					animator_.stop();
				}
			}
		});

		JPanel options = new JPanel(new GridBagLayout());
		GridBagConstraints gc = new GridBagConstraints();
		gc.gridx = 0;
		gc.gridy = 0;
		gc.weightx = 0;
		gc.weighty = 0;
		gc.fill = GridBagConstraints.HORIZONTAL;
		gc.anchor = GridBagConstraints.WEST;

		options.add(animate_, gc);
		gc.gridy++;

		AbstractAnimationOptionPanel secondsOptions = seconds_.getOptionPane();
		secondsOptions.setBorder(BorderFactory.createTitledBorder("Seconds"));
		options.add(secondsOptions, gc);
		gc.gridy++;

		AbstractAnimationOptionPanel minutesOptions = minutes_.getOptionPane();
		minutesOptions.setBorder(BorderFactory.createTitledBorder("Minutes"));
		options.add(minutesOptions, gc);
		gc.gridy++;
		AbstractAnimationOptionPanel hoursOptions = hour_.getOptionPane();
		hoursOptions.setBorder(BorderFactory.createTitledBorder("Hours"));
		options.add(hoursOptions, gc);
		gc.gridy++;
		gc.weighty = 1;
		JButton runGC = new JButton("Run GC");
		runGC.addActionListener(e ->
		{
			System.gc();
		});
		options.add(runGC, gc);

		add(BorderLayout.EAST, options);

		if (path != null)
		{
			loadSVGsFromPathOrUri(path);
		}
		else
		{
			setTitle("jSVG Animation Demonstration");
		}

		setLocationByPlatform(true);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setMinimumSize(new Dimension(400, 300));

		pane_.setDoubleBuffered(false);

		addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowClosing(WindowEvent e)
			{
				// Stop animator otherwise app will not exit.
				//@TODO possibly we can do this from animator via component-listener?
				if ( clock_ != null)
					clock_.stop();
				if (animator_ != null)
					animator_.stop();
			}
		});

		startMeasurementTimer(status, pane_.getPainter());

		clock_ = new Timer(100, a ->
		{

			LocalTime localTime = LocalTime.now();

			double millis = localTime.getNano() / 1000000.0;

			localTime.plus(1, ChronoUnit.SECONDS);
			long animationTime = (long) (1000 - millis);

			seconds_.setTargetValue((360.0 / 60.0) * localTime.getSecond(), animationTime);
			minutes_.setTargetValue((360.0 / 60.0) * localTime.getMinute(), animationTime);
			hour_.setTargetValue((360.0 / 12.0) * localTime.getHour(), animationTime);

			int delayMS = (int) (1200.0 - millis);

			clock_.setInitialDelay(delayMS);
			clock_.restart();
		});
		LocalTime localTime = LocalTime.now();
		clock_.setInitialDelay(1500 - (localTime.getNano() / 1000000));
		clock_.setDelay(0);
		clock_.start();
	}

	void setShowEnabled(boolean showEnabled)
	{
		if (enabled_ != showEnabled)
		{
			enabled_ = showEnabled;
			pane_.setEnabled(enabled_);
			repaint();
		}
	}

	@Override
	protected void setShapes(List<ShapeFile> shapes)
	{
		if (!shapes.isEmpty())
		{
			ShapeFile f = shapes.get(0);
			svgPath_ = f.path_;
			shape_ = f.shape_;

			if (f.path_ != null)
				setTitle(f.path_.toString());
			else
				setTitle("jSVG Animation Demonstration");

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

			animator_ = new Animator(pane_, shape_);

			hour_.setShape(shape_, animator_);
			minutes_.setShape(shape_, animator_);
			seconds_.setShape(shape_, animator_);

			if (animate_.isSelected())
				animator_.start();
			else
				animator_.stop();
		}
	}

	@Override
	protected void statusUpdate(JTextComponent status, AbstractPainterBase painter)
	{
		long usedKB = (Runtime.getRuntime()
							  .totalMemory() - Runtime.getRuntime()
													  .freeMemory()) / (1024 * 1204);
		status.setText(usedKB + "MB of " + (Runtime.getRuntime()
												   .totalMemory() / (1024 * 1204)) + "MB used");
	}

	RotationAnimationController hour_;
	RotationAnimationController minutes_;
	RotationAnimationController seconds_;

}

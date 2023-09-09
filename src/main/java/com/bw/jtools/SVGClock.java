package com.bw.jtools;

import com.bw.jtools.shape.AbstractPainterBase;
import com.bw.jtools.shape.AbstractShape;
import com.bw.jtools.shape.ShapeGroup;
import com.bw.jtools.shape.animation.AnimationType;
import com.bw.jtools.shape.animation.Animator;
import com.bw.jtools.shape.animation.FillMode;
import com.bw.jtools.shape.animation.Rotation;
import com.bw.jtools.svg.SVGException;
import com.bw.jtools.ui.ShapePane;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.Timer;
import javax.swing.WindowConstants;
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
import java.util.Objects;

/**
 * Demonstration- and Test-Utility to test animated SVGs.<br>
 */
public class SVGClock extends SVGAppBase
{

	protected Path svgPath_;
	protected ShapePane pane_;
	protected AbstractShape shape_;
	protected Timer clock_;

	protected boolean enabled_ = true;

	protected final double RAD_PRO_UNIT = Math.toRadians(36f / 6f);
	protected final double DELAY_PRO_RAD = 500 / RAD_PRO_UNIT;

	protected static final double RAD_FULL_CIRCLE = Math.toRadians(360);


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

		seconds_ = new ClockAnimation("Seconds");
		minutes_ = new ClockAnimation("Minutes");
		hour_ = new ClockAnimation("Hour");

		JTabbedPane options = new JTabbedPane(JTabbedPane.TOP);
		options.addTab("Second", seconds_.optionPane_);
		options.addTab("Minutes", minutes_.optionPane_);
		options.addTab("Hour", hour_.optionPane_);

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
				clock_.stop();
				seconds_.stop();
				minutes_.stop();
				hour_.stop();
			}
		});

		// @TODO: SwingTimer are ugly un-accurate. Try some other... Eg.. Scheduler.
		clock_ = new Timer(100, a ->
		{

			LocalTime localTime = LocalTime.now();

			double millis = localTime.getNano() / 1000000.0;

			localTime.plus(1, ChronoUnit.SECONDS);
			long animationTime = (long) (1000 - millis);

			seconds_.setAngle((360.0 / 60.0) * localTime.getSecond(), animationTime);
			minutes_.setAngle((360.0 / 60.0) * localTime.getMinute(), animationTime);
			hour_.setAngle((360.0 / 12.0) * localTime.getHour(), animationTime);


			int delayMS = (int) (1200.0 - millis);

			System.out.println(localTime.toString() + " " + delayMS);

			clock_.setDelay(delayMS);


		});
		LocalTime localTime = LocalTime.now();
		clock_.setInitialDelay(1500 - (localTime.getNano() / 1000000));
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

			hour_.setShape(pane_);
			minutes_.setShape(pane_);
			seconds_.setShape(pane_);

		}
	}

	static class ClockAnimation
	{
		protected JComboBox<String> shapeIds_;
		protected JCheckBox animate_;
		protected JTextField centerX_;
		protected JTextField centerY_;

		protected Animator animator_;
		protected JComboBox<AnimationType> type_;

		protected double angle_ = 0;

		protected String lastId_;
		protected Rotation rotation_ = new Rotation(AnimationType.Spring, 0, 0);

		public JPanel optionPane_;

		public ClockAnimation(String name)
		{
			optionPane_ = new JPanel(new GridBagLayout());

			GridBagConstraints gc = new GridBagConstraints();
			gc.fill = GridBagConstraints.HORIZONTAL;
			gc.anchor = GridBagConstraints.WEST;
			gc.weighty = 0;
			gc.gridx = 0;
			gc.gridy = 0;
			gc.gridwidth = 2;
			gc.gridheight = 1;

			shapeIds_ = new JComboBox<>();
			shapeIds_.setToolTipText("Available Shape-Ids.");

			animate_ = new JCheckBox("On");
			animate_.setSelected(false);
			animate_.addChangeListener(e ->
			{
				if (animate_.isSelected())
				{
					if (!animator_.isRunning())
					{
						rotation_.setAnimation(0, rotation_.getValue(), FillMode.Freeze);
						updateAnimation(500);
						animator_.start();
						LocalTime localTime = LocalTime.now();
					}
				}
				else
				{
					animator_.stop();
					if (lastId_ != null)
					{
						animator_.removeAnimation(lastId_, rotation_);
						lastId_ = null;
					}

				}
			});

			optionPane_.add(shapeIds_, gc);
			gc.gridy = 1;
			gc.weighty = 0;
			optionPane_.add(animate_, gc);
			gc.gridwidth = 1;

			type_ = new JComboBox(AnimationType.values());
			type_.setSelectedItem(AnimationType.Spring);

			gc.gridy = 2;
			gc.gridx = 0;
			JLabel lt = new JLabel("Type");
			lt.setLabelFor(type_);
			optionPane_.add(lt, gc);
			gc.gridx = 1;
			optionPane_.add(type_, gc);

			type_.addActionListener(a ->
			{
				updateAnimation(500);
			});

			gc.gridy = 3;
			gc.gridx = 0;
			JLabel lx = new JLabel("X");
			optionPane_.add(lx, gc);
			gc.gridx = 1;
			centerX_ = new JTextField(10);
			centerX_.setText("200");
			lx.setLabelFor(centerX_);
			optionPane_.add(centerX_, gc);
			gc.gridy = 4;
			gc.gridx = 0;
			JLabel ly = new JLabel("Y");
			optionPane_.add(ly, gc);
			gc.gridx = 1;
			centerY_ = new JTextField(10);
			centerY_.setText("200");
			ly.setLabelFor(centerY_);
			optionPane_.add(centerY_, gc);

			gc.fill = GridBagConstraints.VERTICAL;
			gc.gridy++;
			gc.weighty = 1;
			optionPane_.add(new JLabel(""), gc);


			shapeIds_.addActionListener(a -> updateAnimation(500));
			centerX_.addActionListener(a -> updateAnimation(500));
			centerY_.addActionListener(a -> updateAnimation(500));

			updateAnimation(500);
		}

		/**
		 * Updates the animation according to current second-angle.
		 *
		 * @param delayMS The calculated time delay until the pointer shall reach the target angle.
		 */
		private void updateAnimation(long delayMS)
		{
			try
			{
				double x = Double.parseDouble(centerX_.getText());
				double y = Double.parseDouble(centerY_.getText());
				rotation_.setCenter(x, y);
			}
			catch (Exception e)
			{
			}

			if (animator_ != null)
			{
				String id = (String) shapeIds_.getSelectedItem();
				if (!Objects.equals(id, lastId_))
				{
					// Selected shape has changed. Remove old and add a new animation.
					if (lastId_ != null)
					{
						animator_.removeAnimation(lastId_, rotation_);
						lastId_ = null;
					}
					if (id != null)
					{
						animator_.addAnimation(id, rotation_);
						lastId_ = id;
					}
				}
			}
			// If the current value cross 360° we can adapt the range to stay in [0..360°].
			double value = rotation_.getValue();
			double rad = Math.toRadians(angle_);
			if (value != rad)
			{
				rotation_.setType((AnimationType) type_.getSelectedItem());
				rotation_.setAnimation(delayMS, rad, FillMode.Freeze);
			}
		}

		public void setShape(ShapePane pane)
		{
			AbstractShape shape = pane.getShape();
			shapeIds_.removeAllItems();
			addShapeInfo(shape);

			if (animator_ != null)
				animator_.stop();
			lastId_ = null;

			animator_ = new Animator(pane, shape);
			if (animate_.isSelected())
				animator_.start();
			else
				animator_.stop();
		}

		public void addShapeInfo(AbstractShape shape)
		{

			if (shape.id_ != null && !shape.id_.isEmpty())
			{
				shapeIds_.addItem(shape.id_);
				if (shape instanceof ShapeGroup)
				{
					for (AbstractShape s : ((ShapeGroup) shape).shapes_)
					{
						addShapeInfo(s);
					}
				}
			}
		}

		public void stop()
		{
			if (animator_ != null)
				animator_.stop();
		}

		void setAngle(double angle, long animationTime)
		{

			angle %= 360;
			if (angle_ > 360)
			{
				angle_ %= 360;
				rotation_.setValue(Math.toRadians(angle_));
			}
			angle_ = (angle_ <= angle) ? angle : 360 + angle;
			updateAnimation(animationTime);

		}
	}

	ClockAnimation hour_;
	ClockAnimation minutes_;
	ClockAnimation seconds_;

}

package com.bw.jtools.ui;

import com.bw.jtools.shape.AbstractPainterBase;
import com.bw.jtools.shape.AbstractShape;
import com.bw.jtools.shape.Context;
import com.bw.jtools.shape.ShapePainter;

import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Rectangle2D;

/**
 * A pane that uses a ShapePainter.
 */
public class ShapePane extends JComponent
{
	/**
	 * Creates a new ShapePane.
	 */
	public ShapePane()
	{
	}

	private boolean drawFrame_ = false;
	private Paint framePaint_ = Color.BLACK;
	private AbstractPainterBase painter_ = new ShapePainter();
	private boolean mouseWheelEnabled_ = false;
	private boolean mouseDragEnabled_ = false;
	private boolean mouseRotateEnabled_ = false;

	private double initialScaleX_ = 1;
	private double initialScaleY_ = 1;


	/**
	 * If set, the shape not rendered in gray-mode if component is disabled.
	 */
	private boolean renderGrayIfDisabled_ = true;

	private JPopupMenu contextPopupMenu_;

	private static JFileChooser pngFileChooser;


	/**
	 * Get the file chooser used for "save as PNG" actions.
	 */
	public static synchronized JFileChooser getPNGFileChooser()
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
	 * Saves the shape as PNG image.
	 */
	public void saveAsImage()
	{
		if (painter_ != null)
		{
			JFileChooser fs = getPNGFileChooser();
			int returnVal = fs.showSaveDialog(this);
			if (returnVal == JFileChooser.APPROVE_OPTION)
			{
				painter_.saveAsImage(fs.getSelectedFile());
			}
		}
	}


	private MouseWheelListener scaleWheelListener = we ->
	{
		int wheel = we.getWheelRotation();
		if (wheel != 0)
		{
			int mod = we.getModifiersEx();
			if ((mod & InputEvent.META_DOWN_MASK) != 0 || (mod & InputEvent.CTRL_DOWN_MASK) != 0)
			{
				double scale = -0.1 * wheel;
				double x = getXScale() + scale;
				double y = getYScale() + scale;
				if (x >= 0.1 && y >= 0.1)
				{
					painter_.setScale(x, y);
					refresh();
				}
			}
		}
	};

	private MouseAdapter dragListener = new MouseAdapter()
	{
		Point org_ = new Point(0, 0);

		@Override
		public void mousePressed(MouseEvent e)
		{
			org_ = new Point(e.getPoint());
		}

		@Override
		public void mouseDragged(MouseEvent e)
		{
			JViewport viewPort = (JViewport) SwingUtilities.getAncestorOfClass(JViewport.class, ShapePane.this);
			if (viewPort != null)
			{
				Point p = e.getPoint();
				int dx = org_.x - p.x;
				int dy = org_.y - p.y;
				Rectangle view = viewPort.getViewRect();
				scrollRectToVisible(new Rectangle(view.x + dx, view.y + dy, view.width, view.height));
			}
		}
	};

	private MouseWheelListener wheelRotateListener = we ->
	{
		int wheel = we.getWheelRotation();
		if (wheel != 0)
		{
			int mod = we.getModifiersEx();
			if (painter_ != null && (mod & InputEvent.SHIFT_DOWN_MASK) != 0)
			{
				painter_.setRotationAngleDegree((painter_.getRotationAngleDegree() + (0.5 * wheel)) % 360);
				revalidate();
				repaint();
			}
		}
	};


	/**
	 * Draws a border inside the panel with the default stroke and Color.BLACK.
	 */
	public void setInlineBorder(boolean draw)
	{
		setInlineBorder(draw, Color.BLACK);
	}

	/**
	 * Draws a border inside the panel with the default stroke.
	 */
	public void setInlineBorder(boolean draw, Paint color)
	{
		drawFrame_ = draw;
		framePaint_ = color;
		repaint();
	}

	/**
	 * Sets a new painter, including the shape in it.
	 */
	public void setPainter(AbstractPainterBase painter)
	{
		painter_ = painter;
		refresh();
	}

	public AbstractPainterBase getPainter()
	{
		return painter_;
	}

	/**
	 * Replaces the shape in the painter.
	 *
	 * @param shape The new shape.
	 */
	public void setShape(AbstractShape shape)
	{
		painter_.setShape(shape);
		refresh();
	}

	public AbstractShape getShape()
	{
		return painter_.getShape();
	}


	/**
	 * Gets X-Scale factor.
	 */
	public double getXScale()
	{
		return painter_.getXScale();
	}

	/**
	 * Gets Y-Scale factor.
	 */
	public double getYScale()
	{
		return painter_.getYScale();
	}

	/**
	 * Sets X- and Y-Scale factor.
	 */
	public void setScale(double scaleX, double scaleY)
	{
		initialScaleX_ = scaleX;
		initialScaleY_ = scaleY;
		painter_.setScale(scaleX, scaleY);
		refresh();
	}

	/**
	 * Returns true if the shape is rendered in gray if this component is disabled.
	 */
	public boolean isGrayIfDisabled()
	{
		return renderGrayIfDisabled_;
	}

	/**
	 * If set to true, the shape is rendered in gray if this component is disabled.
	 */
	public void setGrayIfDisabled(boolean grayIfDisabled)
	{
		this.renderGrayIfDisabled_ = grayIfDisabled;
	}


	@Override
	public Dimension getPreferredSize()
	{
		Rectangle2D area = painter_.getArea();
		return new Dimension((int) (0.5 + area.getX() + area.getWidth()), (int) (0.5 + area.getY() + area.getHeight()));
	}

	@Override
	protected void paintComponent(Graphics g)
	{
		if (painter_ == null)
			super.paintComponent(g);
		else
		{
			Graphics2D g2d = (Graphics2D) g;
			Context.initGraphics(g2d);

			if (drawFrame_)
			{
				final Paint p = g2d.getPaint();
				g2d.setPaint(framePaint_);
				g2d.draw(painter_.getArea());
				g2d.setPaint(p);
			}
			painter_.paint(g2d, getForeground(), getBackground(), isOpaque(), renderGrayIfDisabled_ && !isEnabled());
		}
	}

	private void refresh()
	{
		revalidate();
		repaint();
	}

	/**
	 * Installs a wheel-listener that zooms by mouse-wheel if Meta/Ctrl-Key is hold.
	 *
	 * @param wheelEnabled If true zoom by wheel is enabled.
	 *                     If false, zoom by wheel is disabled.
	 */
	public void setZoomByMetaMouseWheelEnabled(boolean wheelEnabled)
	{
		if (mouseWheelEnabled_ != wheelEnabled)
		{
			mouseWheelEnabled_ = wheelEnabled;
			if (wheelEnabled)
				addMouseWheelListener(scaleWheelListener);
			else
				removeMouseWheelListener(scaleWheelListener);
		}
	}

	/**
	 * Installs a mouse-listener that drags the image inside a scroll-pane.
	 *
	 * @param mouseDragEnabled If true user can drag the image.
	 */
	public void setMouseDragEnabled(boolean mouseDragEnabled)
	{
		if (mouseDragEnabled_ != mouseDragEnabled)
		{
			mouseDragEnabled_ = mouseDragEnabled;
			if (mouseDragEnabled_)
			{
				addMouseListener(dragListener);
				addMouseMotionListener(dragListener);
			}
			else
			{
				removeMouseListener(dragListener);
				removeMouseMotionListener(dragListener);
			}
		}
	}

	/**
	 * Installs a mouse-listener that rotates by mpouse-wheel if Shift-Key is hold.
	 *
	 * @param rotateEnabled If true rotate by shift-mouse-wheel is enabled.
	 *                      If false, rotate is disabled.
	 */
	public void setRotateByShiftMouseWheelEnabled(boolean rotateEnabled)
	{
		if (mouseRotateEnabled_ != rotateEnabled)
		{
			mouseRotateEnabled_ = rotateEnabled;
			if (mouseRotateEnabled_)
			{
				addMouseWheelListener(wheelRotateListener);
			}
			else
			{
				removeMouseWheelListener(wheelRotateListener);
			}
		}
	}

	public void setContextMenuEnabled(boolean menuEnabled)
	{
		setComponentPopupMenu(menuEnabled ? getPopupMenu() : null);
	}

	/**
	 * Gets the context pop-up menu that can be used.
	 * Remind that this pop-up menu is only set on this component if {@link #setContextMenuEnabled(boolean)} is set to true.<br>
	 * The pop-up menu return can be used on other components if needed.
	 */
	public synchronized JPopupMenu getPopupMenu()
	{
		if (contextPopupMenu_ == null)
		{

			JMenuItem saveAsImageMenuItem = new JMenuItem("Save as PNG...", KeyEvent.VK_P);
			saveAsImageMenuItem.setToolTipText("<html>Saves the SVG with the<br>current scale to a PNG image.</html>");
			saveAsImageMenuItem.addActionListener(e -> saveAsImage());

			JMenuItem resetMenuItem = new JMenuItem("Reset scale & rotation", KeyEvent.VK_R);
			resetMenuItem.setToolTipText("<html>Resets rotation and scale to defaults.</html>");
			resetMenuItem.addActionListener(e ->
			{
				AbstractPainterBase p = getPainter();
				p.setRotationAngleDegree(0);
				p.setScale(initialScaleX_, initialScaleY_);
				refresh();
			});


			contextPopupMenu_ = new JPopupMenu("Shape Menu");
			contextPopupMenu_.add(saveAsImageMenuItem);
			contextPopupMenu_.add(resetMenuItem);

		}
		return contextPopupMenu_;
	}

}

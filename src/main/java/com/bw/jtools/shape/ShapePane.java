package com.bw.jtools.shape;

import javax.swing.JComponent;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.event.InputEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Rectangle2D;
import java.util.Collection;

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
	private ShapePainter painter_ = new ShapePainter();
	private boolean mouseWheelEnabled = false;

	private MouseWheelListener wheelListener = we ->
	{
		if (we.getWheelRotation() != 0)
		{
			int mod = we.getModifiersEx();
			if ((mod & InputEvent.META_DOWN_MASK) != 0 || (mod & InputEvent.CTRL_DOWN_MASK) != 0)
			{
				double scale = -0.1 * we.getWheelRotation();
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
	 * Sets a new painter, including the shapes in it.
	 */
	public void setPainter(ShapePainter painter)
	{
		painter_ = painter;
		refresh();
	}

	public ShapePainter getPainter()
	{
		return painter_;
	}

	/**
	 * Replaces all shapes in the painter.
	 *
	 * @param shapes The new shapes.
	 */
	public void setShapes(Collection<AbstractShape> shapes)
	{
		painter_.clearShapes();
		painter_.addShapes(shapes);
		refresh();
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
	 * Adds a hsape to the painter.
	 *
	 * @param shape The new shape.
	 */
	public void addShape(ShapeGroup shape)
	{
		painter_.addShape(shape);
		refresh();
	}

	/**
	 * Sets X- and Y-Scale factor.
	 */
	public void setScale(double scaleX, double scaleY)
	{
		painter_.setScale(scaleX, scaleY);
		refresh();
	}

	@Override
	public Dimension getPreferredSize()
	{
		Rectangle2D.Double area = painter_.getArea();
		return new Dimension((int) (0.5 + area.x + area.width), (int) (0.5 + area.y + area.height));
	}

	@Override
	protected void paintComponent(Graphics g)
	{
		if (painter_ == null)
			super.paintComponent(g);
		else
		{
			Graphics2D g2d = (Graphics2D) g;
			if (drawFrame_)
			{
				final Paint p = g2d.getPaint();
				g2d.setPaint(framePaint_);
				g2d.draw(painter_.getArea());
				g2d.setPaint(p);
			}
			painter_.paintShapes(g2d, getForeground(), getBackground(), isOpaque());
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
	public void setZoomByMouseWheelEnabled(boolean wheelEnabled)
	{
		if (mouseWheelEnabled != wheelEnabled)
		{
			mouseWheelEnabled = wheelEnabled;
			if (wheelEnabled)
				addMouseWheelListener(wheelListener);
			else
				removeMouseWheelListener(wheelListener);
		}
	}
}

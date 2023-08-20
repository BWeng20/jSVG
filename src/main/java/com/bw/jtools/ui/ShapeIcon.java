package com.bw.jtools.ui;

import com.bw.jtools.shape.AbstractShape;
import com.bw.jtools.shape.Context;
import com.bw.jtools.shape.ShapePainter;

import javax.swing.Icon;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;

/**
 * Icon that use a ShapePainter to render.
 */
public class ShapeIcon implements Icon
{
	private boolean drawFrame_ = false;
	private Paint framePaint_ = Color.BLACK;
	private final ShapePainter painter_;

	/**
	 * Creates a new Shape Icon. <br>
	 * The shapes are drawn in the same order as added.
	 *
	 * @param shape Initial shapes to draw.
	 */
	public ShapeIcon(AbstractShape shape)
	{
		painter_ = new ShapePainter(shape);
	}

	/**
	 * Replaces all shapes in the painter.
	 *
	 * @param shape The new shapes.
	 */
	public void setShape(AbstractShape shape)
	{
		painter_.setShape(shape);
	}

	/**
	 * Draws a border inside the icon with the default stroke.
	 */
	public void setInlineBorder(boolean draw, Paint color)
	{
		drawFrame_ = draw;
		framePaint_ = color;
	}

	/**
	 * Draws a border inside the icon with the default stroke and Color.BLACK.
	 */
	public void setInlineBorder(boolean draw)
	{
		setInlineBorder(draw, Color.BLACK);
	}

	/**
	 * Sets X- and Y-Scale factor.
	 */
	public void setScale(double scaleX, double scaleY)
	{
		painter_.setScale(scaleX, scaleY);
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
	 * Paints the shapes.
	 */
	@Override
	public void paintIcon(Component c, Graphics g, int x, int y)
	{
		Graphics2D g2d = (Graphics2D) g.create();
		try
		{
			Context.initGraphics(g2d);
			g2d.translate(x, y);
			if (drawFrame_)
			{
				g2d.setPaint(framePaint_);
				g2d.draw(painter_.getArea());
			}
			painter_.paintShape(g2d, c.getForeground(), c.getBackground(), c.isOpaque());
		}
		finally
		{
			g2d.dispose();
		}
	}

	@Override
	public int getIconWidth()
	{
		return (int) Math.ceil(painter_.getAreaWidth());
	}

	@Override
	public int getIconHeight()
	{
		return (int) Math.ceil(painter_.getAreaHeight());
	}

	public ShapePainter getPainter()
	{
		return painter_;
	}

}

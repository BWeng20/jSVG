package com.bw.jtools.shape;

import com.bw.jtools.svg.ShapeHelper;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Holds and paints a list of shapes.<br>
 * <ul>
 * <li>See ShapeIcon for a usage as Icon.</li>
 * <li>See ShapePane for a usage as JComponent.</li>
 * </ul>
 */
public final class ShapePainter
{
	private Rectangle2D.Double area_ = null;
	private final List<AbstractShape> shapes_ = new ArrayList<>();
	private double scaleX_ = 1.0f;
	private double scaleY_ = 1.0f;
	private boolean adaptOffset_ = true;

	private boolean measureTime_ = false;
	private long lastMSNeeded_ = 0;

	private double offsetX_ = 0;
	private double offsetY_ = 0;

	/**
	 * Returns the covered area according to shapes and scale.
	 */
	public Rectangle2D.Double getArea()
	{
		if (area_ == null)
			return new Rectangle2D.Double(0, 0, 0, 0);
		else if (adaptOffset_)
			return new Rectangle2D.Double(0, 0, scaleX_ * area_.width, scaleY_ * area_.height);
		else
			return new Rectangle2D.Double(scaleX_ * area_.x, scaleY_ * area_.y, scaleX_ * area_.width, scaleY_ * area_.height);
	}

	/**
	 * Auto adapt origin. Manual offset is ignored.
	 * Upper-left corner is moved to 0,0.
	 */
	public void setAdaptOrigin(boolean on)
	{
		adaptOffset_ = on;
	}

	/**
	 * Sets manual offset.
	 */
	public void setOrigin(double x, double y)
	{
		offsetX_ = x;
		offsetY_ = y;
	}

	/**
	 * Gets the absolute width of the covered area.
	 */
	public double getAreaWidth()
	{
		return area_ == null ? 0 : scaleX_ * (adaptOffset_ ? area_.width : (area_.x + area_.width));
	}

	/**
	 * Gets the absolute height of the covered area.
	 */
	public double getAreaHeight()
	{
		return area_ == null ? 0 : scaleY_ * (adaptOffset_ ? area_.height : (area_.y + area_.height));
	}

	public void clearShapes()
	{
		shapes_.clear();
		area_ = null;
	}

	/**
	 * Adds a shape.
	 */
	public void addShape(AbstractShape shape)
	{
		shapes_.add(shape);

		Rectangle2D transRect = shape.getTransformedBounds();
		if (area_ == null)
			area_ = new Rectangle2D.Double(transRect.getX(), transRect.getY(), transRect.getWidth(), transRect.getHeight());
		else
			area_ = (Rectangle2D.Double) area_.createUnion(transRect);
	}

	/**
	 * Adds a number of shapes.
	 * Iterates across the collection and calls {@link #addShape(AbstractShape)}.
	 */
	public void addShapes(Collection<AbstractShape> shapes)
	{
		if (shapes != null)
			for (AbstractShape s : shapes)
				addShape(s);
	}

	/**
	 * Sets X- and Y-Scale factor.
	 */
	public void setScale(double scaleX, double scaleY)
	{
		scaleX_ = scaleX;
		scaleY_ = scaleY;
	}

	/**
	 * Gets X-Scale factor.
	 */
	public double getXScale()
	{
		return scaleX_;
	}

	/**
	 * Gets Y-Scale factor.
	 */
	public double getYScale()
	{
		return scaleY_;
	}

	/**
	 * Paints the shapes.
	 *
	 * @param ctx       Graphic context, will NOT be restored.
	 * @param clearArea If true the area of the shapes is cleared with the current color.
	 */
	public void paintShapes(Context ctx, boolean clearArea)
	{
		if (area_ == null)
			return;

		final long ms = (measureTime_) ? System.currentTimeMillis() : 0;

		Context lct = new Context(ctx, false);
		final Graphics2D g2D = lct.g2D_;

		g2D.scale(scaleX_, scaleY_);
		if (adaptOffset_)
			g2D.translate(-area_.x, -area_.y);
		else
			g2D.translate(offsetX_, offsetY_);

		if (clearArea)
			g2D.fill(area_);

		lct.aft_ = lct.g2D_.getTransform();
		for (AbstractShape shape : shapes_)
			shape.paint(lct);

		if (measureTime_)
			lastMSNeeded_ = System.currentTimeMillis() - ms;
	}

	/**
	 * Paints the shapes.
	 *
	 * @param g          Graphics, will not be changed.
	 * @param foreground The foreground paint to use.
	 * @param background The background paint to use.
	 * @param clearArea  If true the area of the shapes is cleared with the current color.
	 */
	public void paintShapes(Graphics g, Paint foreground, Paint background, boolean clearArea)
	{
		Context ctx = new Context(g);
		try
		{
			ctx.currentColor_ = foreground;
			ctx.currentBackground_ = background;
			paintShapes(ctx, clearArea);
		}
		finally
		{
			ctx.dispose();
		}
	}

	/**
	 * Paint the shapes along the outline of on other shape.<br>
	 * Identical to
	 * <pre>
	 * paintAlong(ctx, new ShapeHelper(outline), start, end, distance);
	 * </pre>
	 * Use this method only if Outline is not reused.
	 *
	 * @param ctx      The graphics context.
	 * @param outline  The shape for the outline.
	 * @param start    Start offset.
	 * @param end      End offset. Negative values describe offsets from end.
	 * @param distance The additional distance alone the outline.
	 */
	public void paintAlong(Context ctx, Shape outline,
						   double start, double end, final double distance)
	{
		paintAlong(ctx, new ShapeHelper(outline),
				start, end, distance);
	}

	/**
	 * Paint the shapes along the outline of on other shape.
	 *
	 * @param ctx      The graphics context.
	 * @param outline  The shape-helper for the outline.
	 * @param start    Start offset.
	 * @param end      End offset. Negative values describe offsets from end.
	 * @param distance The additional distance alone the outline.
	 */
	public void paintAlong(Context ctx, ShapeHelper outline,
						   double start, double end, final double distance)
	{
		if (outline != null)
		{
			if (end <= 0)
			{
				end = outline.getOutlineLength() + end;
			}
			else if (end > outline.getOutlineLength())
			{
				// Can't paint longer than the end.
				end = outline.getOutlineLength();
			}
			final double d = end;
			double pos = start;
			if (pos < 0)
			{
				pos = 0;
			}

			final Context gl = new Context(ctx);
			try
			{
				final AffineTransform t = gl.g2D_.getTransform();

				if (ctx.debug_)
				{
					// Debugging: Shows the path
					gl.g2D_.setPaint(ctx.debugPaint_);
					gl.g2D_.setStroke(ctx.debugStroke_);
					gl.g2D_.draw(outline.getShape());
				}

				ShapeHelper.PointOnPath pop1 = outline.pointAtLength(pos);
				while (pos < d)
				{
					pos += distance;
					ShapeHelper.PointOnPath pop2 = outline.pointAtLength(pos);
					gl.g2D_.setTransform(t);
					gl.g2D_.translate(pop1.x_, pop1.y_);
					if (pop2 == null)
					{
						gl.g2D_.rotate(pop1.angle_);
					}
					else
					{
						gl.g2D_.rotate(Math.atan2(pop2.y_ - pop1.y_, pop2.x_ - pop1.x_));
					}

					paintShapes(gl, false);

					if (gl.debug_)
					{
						gl.g2D_.setPaint(gl.debugPaint_);
						gl.g2D_.setStroke(gl.debugStroke_);
						gl.g2D_.draw(getArea());
						gl.g2D_.drawLine(0, -2, 0, 2);
					}
					pop1 = pop2;
				}
			}
			finally
			{
				gl.dispose();
			}
		}
	}


	public void setTimeMeasurementEnabled(boolean measureTime)
	{
		this.measureTime_ = measureTime;
	}

	public long getMeasuredTimeMS()
	{
		return lastMSNeeded_;
	}

}

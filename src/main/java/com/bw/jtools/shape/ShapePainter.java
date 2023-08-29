package com.bw.jtools.shape;

import com.bw.jtools.svg.ShapeHelper;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;

/**
 * Holds and paints a list of shapes.<br>
 * <ul>
 * <li>See ShapeIcon for a usage as Icon.</li>
 * <li>See ShapePane for a usage as JComponent.</li>
 * </ul>
 */
public final class ShapePainter extends AbstractPainterBase
{
	private AbstractShape shape_;


	public ShapePainter()
	{
	}

	public ShapePainter(AbstractShape shape)
	{
		setShape(shape);
	}

	/**
	 * Adds a shape.
	 */
	public final void setShape(AbstractShape shape)
	{
		shape_ = shape;
		area_ = null;
	}

	@Override
	protected void calculateArea()
	{
		if (shape_ == null)
		{
			area_ = new Rectangle2D.Double(0, 0, 1, 1);
		}
		else
		{
			Rectangle2D transRect = shape_.getTransformedBounds();
			area_ = new Rectangle2D.Double(transRect.getX(), transRect.getY(), transRect.getWidth(), transRect.getHeight());
		}
	}


	/**
	 * Paints the shapes.
	 *
	 * @param ctx       Graphic context, will NOT be restored.
	 * @param clearArea If true the area of the shapes is cleared with the current color.
	 */
	public void paint(Context ctx, boolean clearArea)
	{
		if (shape_ == null)
			return;

		final long ms = (measureTime_) ? System.currentTimeMillis() : 0;

		if (area_ == null)
			calculateArea();

		Context lct = new Context(ctx, false);
		final Graphics2D g2D = lct.g2D_;

		final AffineTransform rotation = getRotation();
		g2D.scale(scaleX_, scaleY_);

		if (rotation != null)
		{
			Rectangle2D a = rotation.createTransformedShape(area_)
									.getBounds2D();
			g2D.translate(-a.getX(), -a.getY());
		}
		else
		{
			g2D.translate(-area_.x, -area_.y);
		}

		if (clearArea)
		{
			g2D.setPaint(lct.currentBackground_);
			g2D.fill(area_);
		}

		if (rotation != null)
		{
			g2D.transform(rotation);
		}

		shape_.paint(lct);

		if (measureTime_)
			lastMSNeeded_ = System.currentTimeMillis() - ms;
	}

	/**
	 * Paints the shape.
	 *
	 * @param g          Graphics, will not be changed.
	 * @param foreground The foreground paint to use.
	 * @param background The background paint to use.
	 * @param clearArea  If true the area of the shapes is cleared with the current color.
	 * @param toGray     If true all colors are converted to gray.
	 */
	@Override
	public void paint(Graphics g, Paint foreground, Paint background, boolean clearArea, boolean toGray)
	{
		Context ctx = new Context(g);
		try
		{
			ctx.currentColor_ = foreground;
			ctx.currentBackground_ = background;
			ctx.translateColor2Gray_ = toGray;
			paint(ctx, clearArea);
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

					paint(gl, false);

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

	/**
	 * Gets the current rotation angle in degree.
	 */
	public double getRotationAngleDegree()
	{
		return rotationAngleDegree_;
	}

	public void setRotationAngleDegree(double angleDegree)
	{
		if (angleDegree != rotationAngleDegree_)
		{
			rotationAngleDegree_ = angleDegree;
		}
	}
}

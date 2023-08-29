package com.bw.jtools.shape;

import com.bw.jtools.svg.ShapeHelper;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

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
	private AbstractShape shape_;
	private double scaleX_ = 1.0f;
	private double scaleY_ = 1.0f;

	private boolean measureTime_ = false;
	private long lastMSNeeded_ = 0;

	private double rotationAngleDegree_ = 0;

	private boolean isRotationActive()
	{
		return (rotationAngleDegree_ < -0.1 || rotationAngleDegree_ > 0.1);
	}

	private AffineTransform getRotation()
	{
		if (isRotationActive())
			return AffineTransform.getRotateInstance(Math.toRadians(rotationAngleDegree_), area_.x + (area_.width / 2),
					area_.y + (area_.height / 2));
		else
			return null;
	}


	/**
	 * Returns the covered area according to shapes and scale.
	 */
	public Rectangle2D.Double getArea()
	{
		Rectangle2D area = area_;

		if (area != null)
		{
			AffineTransform rotation = getRotation();
			if (rotation != null)
				area = rotation.createTransformedShape(area)
							   .getBounds2D();
		}

		if (area == null)
			return new Rectangle2D.Double(0, 0, 0, 0);
		else
			return new Rectangle2D.Double(0, 0, scaleX_ * area.getWidth(), scaleY_ * area.getHeight());
	}

	/**
	 * Gets the absolute width of the covered area.
	 */
	public double getAreaWidth()
	{
		return area_ == null ? 0 : scaleX_ * area_.width;
	}

	/**
	 * Gets the absolute height of the covered area.
	 */
	public double getAreaHeight()
	{
		return area_ == null ? 0 : scaleY_ * area_.height;
	}

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
		if (shape != null)
		{
			Rectangle2D transRect = shape.getTransformedBounds();
			area_ = new Rectangle2D.Double(transRect.getX(), transRect.getY(), transRect.getWidth(), transRect.getHeight());
		}
		else
			area_ = null;
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
	public void paintShape(Context ctx, boolean clearArea)
	{
		if (area_ == null || shape_ == null)
			return;

		final long ms = (measureTime_) ? System.currentTimeMillis() : 0;

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
	 */
	public void paintShape(Graphics g, Paint foreground, Paint background, boolean clearArea )
	{
		paintShape(g,foreground,background,clearArea, false);
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
	public void paintShape(Graphics g, Paint foreground, Paint background, boolean clearArea, boolean toGray)
	{
		Context ctx = new Context(g);
		try
		{
			ctx.currentColor_ = foreground;
			ctx.currentBackground_ = background;
			ctx.translateColor2Gray_ = toGray;
			paintShape(ctx, clearArea);
		}
		finally
		{
			ctx.dispose();
		}
	}

	/**
	 * Draw the shapes to a buffered image with foreground black and background white.<br>
	 * If no shapes are loaded, nothing is drawn and if dst is null, a one pixel wide image is created.
	 *
	 * @param dst If null a new buffer, compatible with the current screen is created.
	 * @return dst or (if dst was null) a new created image.
	 */
	public BufferedImage paintShapeToBuffer(BufferedImage dst, boolean toGray)
	{
		return paintShapeToBuffer(dst, Color.BLACK, Color.WHITE, toGray);
	}

	/**
	 * Draw the shapes to a buffered image with foreground black and transparent background.<br>
	 * If no shapes are loaded, nothing is drawn and if dst is null, a one pixel wide image is created.
	 *
	 * @param dst If null a new buffer, compatible with the current screen is created.
	 * @return dst or (if dst was null) a new created image.
	 */
	public BufferedImage paintShapeToBufferTransparent(BufferedImage dst, boolean toGray)
	{
		return paintShapeToBuffer(dst, Color.BLACK, new Color(0, 0, 0, 0), toGray);
	}


	/**
	 * Draw the shapes to a buffered image.<br>
	 * If no shapes are loaded, nothing is drawn and if dst is null, a one pixel wide image is created.
	 *
	 * @param dst        If null a new buffer, compatible with the current screen is created.
	 * @param foreground The foreground color.
	 * @param background The background color.
	 * @return dst or (if dst was null) a new created image.
	 */
	public BufferedImage paintShapeToBuffer(BufferedImage dst, Paint foreground, Paint background, boolean toGray)
	{
		if (dst == null)
		{
			Rectangle2D area = getArea();
			if (area == null || area.getHeight() == 0 || area.getWidth() == 0)
				area = new Rectangle2D.Double(0, 0, 1, 1);

			dst = new BufferedImage((int) (0.5 + area.getWidth()),
					(int) (0.5 + area.getHeight()), BufferedImage.TYPE_INT_ARGB);
		}

		Graphics2D g2d = dst.createGraphics();
		Context.initGraphics(g2d);

		paintShape(g2d, foreground, background, true, toGray);
		return dst;
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

					paintShape(gl, false);

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

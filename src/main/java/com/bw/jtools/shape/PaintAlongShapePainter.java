package com.bw.jtools.shape;

import com.bw.jtools.svg.ShapeHelper;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Holds and paints a list of shapes.<br>
 * <ul>
 * <li>See ShapeIcon for a usage as Icon.</li>
 * <li>See ShapePane for a usage as JComponent.</li>
 * </ul>
 */
public final class PaintAlongShapePainter extends AbstractPainterBase
{

	private AbstractPainterBase painter_;
	private boolean paintOverlapped_ = true;

	private List<Shape> shapes_ = new ArrayList<>();
	private List<ShapeHelper> outlines_ = new ArrayList<>();

	public PaintAlongShapePainter()
	{

	}

	public void setPainter(AbstractPainterBase painter)
	{
		if (painter != painter_)
		{
			area_ = null;
			painter_ = painter;
		}
	}

	public void addOutline(Shape shape)
	{
		shapes_.add(shape);
		outlines_.add(new ShapeHelper(shape));
		area_ = null;
	}

	public void removeOutline(Shape shape)
	{
		int idx = shapes_.indexOf(shape);
		if (idx >= 0)
		{
			outlines_.remove(idx);
			shapes_.remove(idx);
			area_ = null;
		}
	}

	/**
	 * Sets the shape to paint along.
	 */
	@Override
	public final void setShape(AbstractShape shape)
	{
		if (painter_ != null)
		{
			painter_.setShape(shape);
			area_ = null;
		}
	}

	public boolean isPaintOverlapped()
	{
		return paintOverlapped_;
	}

	public void setPaintOverlapped(boolean paintOverlapped)
	{
		paintOverlapped_ = paintOverlapped;
	}

	protected void calculateArea()
	{
		if (painter_ != null && !shapes_.isEmpty())
		{
			Rectangle2D shapePainterArea = painter_.getArea();
			for (Shape s : shapes_)
			{
				Rectangle2D sBounds2D = s.getBounds2D();
				if (area_ == null)
				{
					area_ = new Rectangle2D.Double();
					area_.setRect(sBounds2D);
				}
				else
				{
					Rectangle2D.union(area_, sBounds2D, area_);
				}
			}
			// @TODO: do this more accurate
			area_.setRect(area_.x - shapePainterArea.getWidth(), area_.y - shapePainterArea.getHeight(), area_.width + (2 * shapePainterArea.getWidth()), area_.height + (2 * shapePainterArea.getHeight()));
		}
		else
			area_ = new Rectangle2D.Double(0, 0, 0, 0);
	}

	/**
	 * Paints the ShapePainter along the added shapes.
	 *
	 * @param ctx       Graphic context, Graphics inside will NOT be restored.
	 * @param clearArea If true the area of the shapes is cleared with the current color.
	 */
	@Override
	protected void paint(Context ctx, boolean clearArea)
	{
		if (shapes_.isEmpty())
			return;

		Context lct = new Context(ctx, false);
		try
		{
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
			Stroke outlineStroke = new BasicStroke(1f);

			for (int i = 0; i < shapes_.size(); ++i)
			{
				g2D.setStroke(outlineStroke);
				g2D.setPaint(Color.RED);
				g2D.draw(shapes_.get(i));
				paintAlong(lct, outlines_.get(i), 0, 0, 0);
			}
		}
		finally
		{
			lct.dispose();
		}
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
			double d = end;
			double pos = start;
			if (pos < 0)
			{
				pos = 0;
			}

			final Context gl = new Context(ctx, true);
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

				double shapeH = painter_.getAreaHeight();
				double shapeW = painter_.getAreaWidth();
				ShapeHelper.PointOnPath pop1 = outline.pointAtLength(pos);
				pos += distance + shapeW;
				if (paintOverlapped_)
					d += distance + shapeW;
				while (pos < d)
				{
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
					gl.g2D_.translate(0, -shapeH / 2);

					painter_.paint(gl, false);

					if (gl.debug_)
					{
						gl.g2D_.setPaint(gl.debugPaint_);
						gl.g2D_.setStroke(gl.debugStroke_);
						gl.g2D_.draw(getArea());
						gl.g2D_.drawLine(0, -2, 0, 2);
					}
					pop1 = pop2;
					pos += distance + shapeW;
				}
			}
			finally
			{
				gl.dispose();
			}
		}
	}

}

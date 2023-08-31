package com.bw.jtools.shape;

import com.bw.jtools.svg.ShapeHelper;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Paints a Painter along a list of paths.
 */
public final class PaintAlongShapePainter extends AbstractPainterBase
{

	private AbstractPainterBase tilePainter_;
	private boolean paintOverlapped_ = true;
	private boolean paintPaths_ = false;

	private double distance_ = 0;
	private double start_ = 0;
	private double end_ = 0;

	private static Stroke defaultPathStroke_ = new BasicStroke(1f);


	private Stroke pathStroke_ = defaultPathStroke_;
	private Paint pathPaint_ = Color.RED;


	private List<Shape> paths_ = new ArrayList<>();
	private List<ShapeHelper> shapeHelpers_ = new ArrayList<>();

	/**
	 * Creates an empty painter.<br>
	 * To show anything a tile-painter needs to be set and at least one path needs to be added.
	 *
	 * @see #setTilePainter(AbstractPainterBase)
	 * @see #addPath(Shape)
	 */
	public PaintAlongShapePainter()
	{
	}

	/**
	 * Sets "paint path".
	 * If true, the paths are painted below the tiles.
	 * If false paths are not painted. Can be used for debugging.
	 * Default is false.
	 *
	 * @see #setPathStroke(Stroke, Paint)
	 * @see #getPaintPaths()
	 */
	public void setPaintPaths(boolean paintPaths)
	{
		paintPaths_ = paintPaths;
	}

	/**
	 * Get the setting for "paint path".
	 *
	 * @see #setPaintPaths(boolean)
	 */
	public boolean getPaintPaths()
	{
		return paintPaths_;
	}

	/**
	 * Sets the stroke and paint of the paths, if {@link #setPaintPaths(boolean)} is true.
	 *
	 * @param pathStroke If null default BasicStroke(1) is restored.
	 * @param pathPaint  If null, default Color.RED is restored.
	 */
	public void setPathStroke(Stroke pathStroke, Paint pathPaint)
	{
		pathStroke_ = pathStroke == null ? defaultPathStroke_ : pathStroke;
		pathPaint_ = pathPaint == null ? Color.RED : pathPaint;
	}

	/**
	 * Sets the additional offset between tiles along the paths.
	 * Can be negative to skip empty space inside the viewBox.<br>
	 * Values less than negative width of the tile are ignored.
	 * Default is 0.
	 */
	public void setDistanceOffset(double distance)
	{
		distance_ = distance;
	}

	/**
	 * Gets the value of the distance-offset.
	 *
	 * @return The distance offset
	 * @see #setDistanceOffset(double)
	 */
	public double getDistanceOffset()
	{
		return distance_;
	}

	/**
	 * Sets the start offset from where tiles are drawn.
	 * Default is 0.
	 * If negative "0" will be used.
	 */
	public void setStartOffset(double start)
	{
		start_ = start < 0d ? 0d : start;
	}

	/**
	 * Gets values of "start offset".
	 *
	 * @return The start offset
	 * @see #setStartOffset(double)
	 */
	public double getStartOffset()
	{
		return start_;
	}

	/**
	 * Sets the end offset until tiles are drawn.
	 * Default is 0.<br>
	 * If negative or 0, the value is an offset to the length of the path.
	 * If positive the value counts from start of path.
	 */
	public void setEndOffset(double end)
	{
		end_ = end;
	}

	/**
	 * Gets values of "end offset".
	 *
	 * @return The end offset
	 * @see #setEndOffset(double)
	 */
	public double getEndOffset()
	{
		return end_;
	}

	/**
	 * Sets the tile painter.<br>
	 * Needs to be called at least once to set the tile to paint along.
	 */
	public void setTilePainter(AbstractPainterBase painter)
	{
		if (painter != tilePainter_)
		{
			area_ = null;
			tilePainter_ = painter;
		}
	}

	/**
	 * Gets the current tile painter.
	 */
	public AbstractPainterBase getTilePainter()
	{
		return tilePainter_;
	}


	/**
	 * Adds a path.
	 * Needs to be called at least once.
	 */
	public void addPath(Shape shape)
	{
		paths_.add(shape);
		shapeHelpers_.add(new ShapeHelper(shape));
		area_ = null;
	}

	/**
	 * Removes a path.<br>
	 * The instance is searched via "equals" calls.
	 * This method should be used with the same instance as used for {@link #addPath(Shape)}.
	 * Some Shape implementations have meaningfully "equals" implementation, other not (as Path).
	 *
	 * @param path The path to remove.
	 */
	public void removePath(Shape path)
	{
		int idx = paths_.indexOf(path);
		if (idx >= 0)
		{
			shapeHelpers_.remove(idx);
			paths_.remove(idx);
			area_ = null;
		}
	}

	/**
	 * Sets the shape to paint along.<br>
	 * Identical to {@link #getTilePainter()}.setShape()  with a following {@link #forceUpdateArea()}.
	 * Has no effect if tile-painter is not set.
	 */
	@Override
	public final void setShape(AbstractShape shape)
	{
		if (tilePainter_ != null)
		{
			tilePainter_.setShape(shape);
			forceUpdateArea();
		}
	}

	/**
	 * Sets setting "paint overlapped".
	 * <ul>
	 * <li>If true tiles are painted until the whole length of the path is covered, even if the last tile goes beyond the length.</li>
	 * <li>If false the tiles are painted as long as the length ios not reached, so a gap at the end may occur.</li>
	 * </ul>
	 * Default is true.
	 */
	public void setPaintOverlapped(boolean paintOverlapped)
	{
		paintOverlapped_ = paintOverlapped;
	}

	/**
	 * Gets setting "paint overlapped".
	 *
	 * @see #setPaintOverlapped(boolean)
	 */
	public boolean isPaintOverlapped()
	{
		return paintOverlapped_;
	}

	@Override
	protected void calculateArea()
	{
		if (tilePainter_ != null && !paths_.isEmpty())
		{
			Rectangle2D shapePainterArea = tilePainter_.getArea();
			for (Shape s : paths_)
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
		if (paths_.isEmpty())
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

			for (int i = 0; i < paths_.size(); ++i)
			{
				if (paintPaths_)
				{
					g2D.setStroke(pathStroke_);
					g2D.setPaint(pathPaint_);
					g2D.draw(paths_.get(i));
				}
				paintAlong(lct, shapeHelpers_.get(i), start_, end_, distance_);
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
	 * @param path     The shape-helper for the outline.
	 * @param start    Start offset.
	 * @param end      End offset. Negative values describe offsets from end.
	 * @param distance The additional distance alone the outline.
	 */
	public void paintAlong(Context ctx, ShapeHelper path,
						   double start, double end, double distance)
	{
		if (path != null)
		{
			final double len = path.getOutlineLength();

			if (end <= 0)
			{
				end = len + end;
			}
			else if (end > len)
			{
				// Can't paint longer than the end.
				end = len;
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
					gl.g2D_.draw(path.getShape());
				}

				double shapeH = tilePainter_.getAreaHeight();
				double shapeW = tilePainter_.getAreaWidth();

				// Ensure that we will not loop endless.
				if ((distance + shapeW) <= 0)
				{
					// shapeW < 0 is very unlikely. But to be sure...
					distance = (shapeW <= 0) ? (1 - shapeW) : 0;
				}

				ShapeHelper.PointOnPath pop1 = path.pointAtLength(pos);
				pos += distance + shapeW;
				if (paintOverlapped_)
					d += distance + shapeW;
				while (pos < d)
				{
					ShapeHelper.PointOnPath pop2;
					if (pos > len && path.isClosed())
					{
						// We reached the end of the outline.
						// For closed shapes it makes sense to follow the line from start. For open shapes this will fail.
						// As there is no method "isClosed" in any of the shape implementations,
						// we have to guess via "ShapeHelper.isClosed" above
						pop2 = path.pointAtLength(pos % len);
					}
					else
						pop2 = path.pointAtLength(pos);
					if (pop1 != null)
					{
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

						tilePainter_.paint(gl, false);

						if (gl.debug_)
						{
							gl.g2D_.setPaint(gl.debugPaint_);
							gl.g2D_.setStroke(gl.debugStroke_);
							gl.g2D_.draw(getArea());
							gl.g2D_.drawLine(0, -2, 0, 2);
						}
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

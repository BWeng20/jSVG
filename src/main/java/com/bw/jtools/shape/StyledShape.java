package com.bw.jtools.shape;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.Objects;

/**
 * A shape plus additional style information.
 */
public final class StyledShape extends AbstractShape
{
	public static final BasicStroke DEFAULT_STROKE = new BasicStroke(1f);

	/**
	 * The shape.
	 */
	public final Shape shape_;

	/**
	 * The stroke to render the outline.
	 */
	public Stroke stroke_;

	/**
	 * The Paint to render the outline.<br>
	 * Can be null.
	 */
	public final Paint paint_;

	/**
	 * The Paint to fill the shape. <br>
	 * Can be null.
	 */
	public final Paint fill_;

	public final Shape clipping_;

	private Rectangle2D transformedBounds_;


	/**
	 * Constructor to initialize,
	 */
	public StyledShape(String id, Shape shape, Stroke stroke, Paint paint, Paint fill,
					   Shape clipping, AffineTransform aft)
	{
		super(id);
		this.shape_ = shape;
		this.stroke_ = stroke;
		this.paint_ = paint;
		this.fill_ = fill;
		this.clipping_ = clipping;
		this.aft_ = aft == null ? new AffineTransform() : aft;
	}

	/**
	 * Get bounds of the transformed shape including stroke-width.
	 */
	@Override
	public Rectangle2D getTransformedBounds()
	{
		if (transformedBounds_ == null)
		{
			final double lw = ((stroke_ instanceof BasicStroke) ? (BasicStroke) stroke_ : DEFAULT_STROKE).getLineWidth();
			Rectangle2D r = shape_.getBounds2D();
			r = new Rectangle2D.Double(r.getX() - lw, r.getY() - lw, r.getWidth() + 2 * lw, r.getHeight() + 2 * lw);
			transformedBounds_ = aft_.createTransformedShape(r)
									 .getBounds2D();
		}
		return transformedBounds_;
	}

	@Override
	public void paint(Context ctx)
	{
		Shape orgClip = null;

		final Graphics2D g3D = ctx.g2D_;

		AffineTransform aold = g3D.getTransform();
		aftTemp_.setTransform(aold);
		aftTemp_.concatenate(aft_);
		if (clipping_ != null && enableClipping_)
		{
			orgClip = g3D.getClip();
			g3D.clip(clipping_);
		}
		g3D.setTransform(aftTemp_);

		Paint p = ctx.translatePaint(fill_);
		if (p != null)
		{
			g3D.setPaint(p);
			g3D.fill(shape_);
		}

		if (paint_ != null)
		{
			p = ctx.translatePaint(paint_);
			if (p != null)
			{
				g3D.setPaint(p);
				g3D.setStroke(stroke_);
				g3D.draw(shape_);
			}
		}
		g3D.setTransform(aold);
		if (clipping_ != null && enableClipping_)
		{
			g3D.setClip(orgClip);
		}
	}

	public AbstractShape getShapeById(String id)
	{
		return (Objects.equals(id_, id)) ? this : null;
	}

}

package com.bw.jtools.shape;

import java.awt.Graphics2D;
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
	 * Sets the shape to paint.
	 */
	@Override
	public final void setShape(AbstractShape shape)
	{
		shape_ = shape;
		area_ = null;
	}

	@Override
	public final AbstractShape getShape()
	{
		return shape_;
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
	 * @param ctx       Graphic context, Graphics inside will NOT be restored.
	 * @param clearArea If true the area of the shapes is cleared with the current color.
	 */
	@Override
	protected void paint(Context ctx, boolean clearArea)
	{
		if (shape_ == null)
			return;

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

		// If needed disable top-level-clipping.
		shape_.setClippingEnabled(enableClipping_);
		shape_.paint(lct);

	}

}

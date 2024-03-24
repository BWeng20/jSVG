package com.bw.jtools.shape;

import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;

/**
 * A abstract base for shapes.
 */
public abstract class AbstractShape
{
	protected static final AffineTransform ident_ = new AffineTransform();

	protected boolean enableClipping_ = true;

	/**
	 * Id to identify the shape group in some document.
	 */
	public final String id_;

	/**
	 * Transform to be applied to the graphics context.<br>
	 * Never null.
	 */
	public AffineTransform aft_;

	protected AffineTransform aftTemp_ = new AffineTransform();


	/**
	 * Constructor to initialize,
	 */
	protected AbstractShape(String id)
	{
		this.id_ = id;
	}

	/**
	 * Paints the shape.
	 *
	 * @param ctx The context to draw into.
	 */
	public abstract void paint(Context ctx);

	/**
	 * Get bounds of the transformed shape including stroke-width.
	 */
	public abstract Rectangle2D getTransformedBounds();

	/**
	 * Gives state of clipping.
	 *
	 * @return tree if clipping is enabled.
	 */
	public boolean isClippingEnabled()
	{
		return enableClipping_;
	}

	/**
	 * Activates clipping.
	 *
	 * @param enableClipping the new values.
	 */
	public void setClippingEnabled(boolean enableClipping)
	{
		this.enableClipping_ = enableClipping;
	}

	public abstract AbstractShape getShapeById(String id);

}

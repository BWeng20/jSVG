package com.bw.jtools.shape;

import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;

/**
 * A abstract base for shapes.
 */
public abstract class AbstractShape
{
	protected static final AffineTransform ident_ = new AffineTransform();

	/**
	 * Id to identify the shape group in the some document.
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

	public abstract void paint(Context ctx);

	/**
	 * Get bounds of the transformed shape including stroke-width.
	 */
	public abstract Rectangle2D getTransformedBounds();


}

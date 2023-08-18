package com.bw.jtools.svg;

import java.awt.Shape;
import java.awt.geom.AffineTransform;

/**
 * Collects all information about a shape that are needed to produce a final shape.
 */
public final class StyledShapeInfo extends ElementInfo
{
	/**
	 * The shape.
	 */
	public Shape shape_;

	/**
	 * The stroke to render the outline.
	 */
	public Stroke stroke_;

	/**
	 * The Paint to render the outline.<br>
	 * Can be null.
	 */
	public PaintWrapper paintWrapper_;


	/**
	 * The Paint to fill the shape. <br>
	 * Can be null.
	 */
	public PaintWrapper fillWrapper_;

	/**
	 * Transform to be applied to the graphics context.
	 */
	public AffineTransform aft_;

	public Shape clipping_;
	public FillRule fillRule_;

	/**
	 * Constructor to initialize,
	 */
	public StyledShapeInfo(Shape shape, Stroke stroke, PaintWrapper paint, PaintWrapper fill, Shape clipping)
	{
		this.shape_ = shape;
		this.stroke_ = stroke;
		this.paintWrapper_ = paint;
		this.fillWrapper_ = fill;
		this.clipping_ = clipping;
	}

	@Override
	public void applyTransform(AffineTransform aft)
	{
		if (aft_ == null)
			aft_ = new AffineTransform(aft);
		else
			aft_.preConcatenate(aft);
	}

	@Override
	public void applyPostTransform(AffineTransform aft)
	{
		if (aft != null)
		{
			if (aft_ == null)
				aft_ = new AffineTransform(aft);
			else
				aft_.concatenate(aft);
		}
	}
}

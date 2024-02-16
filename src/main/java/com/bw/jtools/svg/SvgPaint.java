package com.bw.jtools.svg;

import java.awt.Paint;
import java.awt.geom.AffineTransform;

/**
 * Abstract base of paint types.
 */
public abstract class SvgPaint
{
	/**
	 * The Id of the definition.
	 */
	public final String id_;

	/**
	 * The link of the definition.
	 */
	public String href_;

	/**
	 * The transformation from the definition.
	 */
	public AffineTransform aft_;

	protected SvgPaint(String id)
	{
		id_ = id;
	}

	/**
	 * Creates a Paint Wrapper for this element.
	 * @param svg The Converter to use for resolving references.
	 * @return The new Paint-Wrapper. Never null.
	 */
	public PaintWrapper getPaintWrapper(SVGConverter svg)
	{
		resolveHref(svg);
		return new PaintWrapper(this);
	}

	/**
	 * Creates adapted copy if opacity != 1.
	 * Returns this instance if opacity = 1.
	 * @param opacity The opacity
	 */
	public abstract SvgPaint adaptOpacity(float opacity);

	/**
	 *
	 * @param w The Wrapper to create paint for
	 * @return
	 */
	public abstract Paint createPaint(ElementWrapper w);


	public abstract void resolveHref(SVGConverter svg);

}

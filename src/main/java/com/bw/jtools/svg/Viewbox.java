package com.bw.jtools.svg;

import java.awt.geom.Rectangle2D;

/**
 * Parser for viewBox attribute.
 */
public class Viewbox extends Parser
{
	public Viewbox(String value)
	{
		super(value);

		minX = nextLengthPercentage();
		minY = nextLengthPercentage();
		width = nextLengthPercentage();
		height = nextLengthPercentage();
	}

	/**
	 * Get the viewBox rectangle.
	 *
	 * @return
	 */
	public Rectangle2D.Double getShape()
	{
		return new Rectangle2D.Double(minX.toPixel(null), minY.toPixel(null), width.toPixel(null), height.toPixel(null));
	}

	public final Length minX;
	public final Length minY;

	public final Length width;
	public final Length height;


}

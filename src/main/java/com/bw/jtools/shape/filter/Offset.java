package com.bw.jtools.shape.filter;

import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;

/**
 * Moves the source.
 */
public class Offset extends FilterBaseSingleSource
{

	/**
	 * The offset in X-direction.
	 */
	public double dx_;

	/**
	 * The offset in Y-direction.
	 */
	public double dy_;

	@Override
	protected Point2D.Double getOffset(double scaleX, double scaleY)
	{
		return new Point2D.Double(dx_ * scaleX, dy_ * scaleY);
	}

	@Override
	protected void render(PainterBuffers buffers, String targetName, BufferedImage src, BufferedImage target, double scaleX, double scaleY)
	{
		src.copyData(target.getRaster());
	}

	/**
	 * Initialize a new instance.
	 *
	 * @param source Name of source-buffer.
	 * @param target Name of target-buffer.
	 * @param dx     The offset in X-direction.
	 * @param dy     The offset in Y-direction.
	 */
	public Offset(String source, String target, double dx, double dy)
	{
		super(source, target);
		dx_ = dx;
		dy_ = dy;
	}
}


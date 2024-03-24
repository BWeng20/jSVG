package com.bw.jtools.shape.filter;

import java.awt.image.BufferedImage;

/**
 * Does nothing. Simply copies the source to target.
 */
public class Nop extends FilterBaseSingleSource
{

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
	 */
	public Nop(String source, String target)
	{
		super(source, target);
	}
}


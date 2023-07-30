package com.bw.jtools.shape.filter;

import java.awt.image.BufferedImage;

/**
 * Does nothing.
 */
public class Nop extends FilterBaseSingleSource
{

	@Override
	protected void render(PainterBuffers buffers, String targetName, BufferedImage src, BufferedImage target, double scaleX, double scaleY)
	{
		src.copyData(target.getRaster());
	}

	public Nop(String source, String target)
	{
		super(source, target);
	}
}


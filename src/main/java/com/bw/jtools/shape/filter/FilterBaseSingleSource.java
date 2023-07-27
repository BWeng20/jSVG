package com.bw.jtools.shape.filter;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.util.List;

/**
 * Base for filters.
 */
public abstract class FilterBaseSingleSource extends FilterBase
{
	protected void render(PainterBuffers buffers, String targetName, List<BufferedImage> src, BufferedImage target, double scaleX, double scaleY)
	{
		if (src.size() == 1)
			render(buffers, targetName, src.get(0), target, scaleX, scaleY);
	}

	protected abstract void render(PainterBuffers buffers, String targetName, BufferedImage src, BufferedImage target, double scaleX, double scaleY);

	protected Dimension getTargetDimension(List<BufferedImage> srcBuffers, double scaleX, double scaleY)
	{
		if (srcBuffers.isEmpty())
			return new Dimension(0, 0);
		else
		{
			BufferedImage src = srcBuffers.get(0);
			return getTargetDimension(src.getWidth(), src.getHeight(), scaleX, scaleY);
		}
	}

	protected Dimension getTargetDimension(int srcWidth, int srcHeight, double scaleX, double scaleY)
	{
		return new Dimension(srcWidth, srcHeight);
	}

	protected FilterBaseSingleSource(String source, String target)
	{
		super(source, target);
	}
}

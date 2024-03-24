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

	/**
	 * Renders the filter.
	 *
	 * @param buffers    The buffer-cache to use in case additional sources are needed.
	 * @param targetName The name of the target buffer.
	 * @param src        The source-buffer.
	 * @param target     The target image.
	 * @param scaleX     The scale in X-direction.
	 * @param scaleY     The scale in Y-direction.
	 */
	protected abstract void render(PainterBuffers buffers, String targetName, BufferedImage src, BufferedImage target, double scaleX, double scaleY);

	@Override
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


	/**
	 * Get the target dimension.<br>
	 * This base implementation simply returns the source dimension.
	 *
	 * @param srcWidth  The width of the source buffer
	 * @param srcHeight The height of the source buffer
	 * @param scaleX    The current scale in X-direction.
	 * @param scaleY    The current scale in Y-direction.
	 * @return The calculated target dimension. Never null.
	 */
	protected Dimension getTargetDimension(int srcWidth, int srcHeight, double scaleX, double scaleY)
	{
		return new Dimension(srcWidth, srcHeight);
	}

	/**
	 * Initializes a new instance.
	 *
	 * @param source The source buffer.
	 * @param target The target buffer.
	 */
	protected FilterBaseSingleSource(String source, String target)
	{
		super(source, target);
	}
}

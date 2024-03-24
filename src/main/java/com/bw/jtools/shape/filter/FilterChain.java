package com.bw.jtools.shape.filter;

import java.awt.Dimension;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * List of related filters.
 */
public class FilterChain
{
	private List<FilterBase> filters_;

	/**
	 * Renders the filter-chain.
	 *
	 * @param buffers The buffer-cache to use in case additional sources are needed.
	 * @param scaleX  The scale in X-direction.
	 * @param scaleY  The scale in Y-direction.
	 * @return The rendered image.
	 */
	public FilteredImage render(PainterBuffers buffers, double scaleX, double scaleY)
	{
		FilteredImage result = new FilteredImage();
		result.offset_ = new Point2D.Double(0, 0);
		for (FilterBase f : filters_)
		{
			List<BufferedImage> src = f.getSourceBuffers(buffers);
			if (!src.isEmpty())
			{
				Dimension d = f.getTargetDimension(src, scaleX, scaleY);
				result.image_ = buffers.getTargetBuffer(f.target_, d.width, d.height);
				Point2D.Double off = f.getOffset(scaleX, scaleY);
				result.offset_.x += off.x;
				result.offset_.y += off.y;
				f.render(buffers, f.target_, src, result.image_, scaleX, scaleY);
			}
		}
		return result;
	}

	/**
	 * Create a chain of filters.
	 *
	 * @param filter THe list of filters.
	 */
	public FilterChain(List<FilterBase> filter)
	{
		filters_ = new ArrayList<>(filter);
	}
}

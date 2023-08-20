package com.bw.jtools.shape.filter;

import java.awt.Dimension;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * Base for filters.
 */
public abstract class FilterBase
{
	/**
	 * Renders a filter.
	 *
	 * @param buffers Buffer manager.
	 * @param scaleX  Scale for the filter effect.
	 * @param scaleY  Scale for the filter effect.
	 * @return
	 */
	public FilteredImage render(PainterBuffers buffers, double scaleX, double scaleY)
	{
		FilteredImage result = new FilteredImage();
		List<BufferedImage> srcBuffers = getSourceBuffers(buffers);
		Dimension d = getTargetDimension(srcBuffers, scaleX, scaleY);
		result.image_ = buffers.getTargetBuffer(target_, d.width, d.height);
		render(buffers, target_, srcBuffers, result.image_, scaleX, scaleY);
		result.offset_ = getOffset(scaleX, scaleY);
		return result;
	}

	public List<BufferedImage> getSourceBuffers(PainterBuffers buffers)
	{
		List<BufferedImage> srcBuffers = new ArrayList<>(source_.size());
		for (String src : source_)
		{
			BufferedImage s = SOURCE_ALPHA.equals(src) ? buffers.getSourceAlphaBuffer(SOURCE) : buffers.getSourceBuffer(src);
			if (s != null)
				srcBuffers.add(s);
		}
		return srcBuffers;
	}

	protected final List<String> source_ = new ArrayList<>();
	protected final String target_;

	/**
	 * Predefined source buffer name for the base source buffer.
	 */
	public static final String SOURCE = "Source";
	public static final String SOURCE_ALPHA = "SourceAlpha";

	/**
	 * Get the target dimension.
	 *
	 * @param srcBuffers The sources
	 * @param scaleX     The current scale in X-direction.
	 * @param scaleY     The current scale in Y-direction.
	 */
	protected Dimension getTargetDimension(List<BufferedImage> srcBuffers, double scaleX, double scaleY)
	{
		final Dimension d = new Dimension(0, 0);
		for (BufferedImage b : srcBuffers)
		{
			final int w = b.getWidth();
			if (w > d.width) d.width = w;
			final int h = b.getHeight();
			if (h > d.height) d.height = h;
		}
		return d;
	}

	protected Point2D.Double getOffset(double scaleX, double scaleY)
	{
		return new Point2D.Double(0, 0);
	}

	protected abstract void render(PainterBuffers buffers, String targetName, List<BufferedImage> src, BufferedImage target, double scaleX, double scaleY);


	protected FilterBase(String source, String target)
	{
		if (source != null)
			source_.add(source);
		target_ = target;
	}

	/**
	 * Get the target pixel-units accorigin to the transformation.
	 * E.g. if the transformation is scale(2,4) you will get (2,4).
	 */
	public static Point2D.Double getUnits(final AffineTransform aft)
	{
		AffineTransform scaleAft = new AffineTransform(aft);
		Point2D.Double zero = new Point2D.Double(0, 0);
		scaleAft.deltaTransform(zero, zero);
		Point2D p = new Point2D.Double(1, 0);
		p = scaleAft.deltaTransform(p, p);
		double scaleX = p.distance(zero);

		p = new Point2D.Double(0, 1);
		p = aft.deltaTransform(p, p);
		double scaleY = p.distance(zero);

		return new Point2D.Double(scaleX, scaleY);
	}
}

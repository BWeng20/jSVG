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
	 * @return The resulting image, never null.
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

	/**
	 * Get the list of source buffers from the buffer-cache.
	 *
	 * @param buffers The buffer-cache to use.
	 * @return The source images.
	 */
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

	/**
	 * The names of the source buffers.
	 */
	protected final List<String> source_ = new ArrayList<>();


	/**
	 * The name of the target buffer.
	 */
	protected final String target_;

	/**
	 * Predefined source buffer name for the base source buffer.
	 */
	public static final String SOURCE = "Source";

	/**
	 * Predefined source buffer name for the Alpha-Source.
	 */
	public static final String SOURCE_ALPHA = "SourceAlpha";

	/**
	 * Get the target dimension.
	 *
	 * @param srcBuffers The sources
	 * @param scaleX     The current scale in X-direction.
	 * @param scaleY     The current scale in Y-direction.
	 * @return The calculated target dimension. Never null.
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

	/**
	 * Gets the offset of the filter.
	 * The default implementation returns 0,0.
	 *
	 * @param scaleX The scale in X-direction to use.
	 * @param scaleY The scale in Y-direction to use.
	 * @return The calculated point.
	 */
	protected Point2D.Double getOffset(double scaleX, double scaleY)
	{
		return new Point2D.Double(0, 0);
	}

	/**
	 * Renders the filter.
	 *
	 * @param buffers    The buffer-cache to use in case additional sources are needed.
	 * @param targetName The name of the target buffer.
	 * @param src        The list of source-buffer-names.
	 * @param target     The target image.
	 * @param scaleX     The scale in X-direction.
	 * @param scaleY     The scale in Y-direction.
	 */
	protected abstract void render(PainterBuffers buffers, String targetName, List<BufferedImage> src, BufferedImage target, double scaleX, double scaleY);


	/**
	 * C'tor for simple use cases with one source and one target.
	 *
	 * @param source The name of the source-buffer.
	 * @param target The name of the target-buffer.
	 */
	protected FilterBase(String source, String target)
	{
		if (source != null)
			source_.add(source);
		target_ = target;
	}

	/**
	 * Get the target pixel-units according to the transformation.
	 * E.g. if the transformation is scale(2,4) you will get (2,4).
	 *
	 * @param aft The Transformation to use. Must not be null.
	 * @return The X- and Y-scale for one unit.
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

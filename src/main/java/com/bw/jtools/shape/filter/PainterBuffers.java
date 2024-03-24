package com.bw.jtools.shape.filter;

import java.awt.GraphicsConfiguration;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.awt.image.RescaleOp;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Internal buffer-system to chain filters.<br>
 * Very memory consuming, ugly and slow. One reason against usage of filter at all.
 */
public class PainterBuffers
{
	private final Map<String, BufferedImage> buffers_ = new HashMap<>();
	private final Map<String, BufferedImage> tempBuffers_ = new HashMap<>();
	private GraphicsConfiguration cfg_;

	/**
	 * Debugging counter.
	 */
	public static long buffersCreated_;

	/**
	 * Initialize a new instance.
	 */
	public PainterBuffers()
	{
	}

	/**
	 * Sets the graphic configuration used to create buffers.
	 *
	 * @param cfg The configuration.
	 */
	public void setConfiguration(GraphicsConfiguration cfg)
	{
		cfg_ = cfg;
	}

	/**
	 * Clears all buffers.
	 */
	public void clear()
	{
		clearBuffers(buffers_);
		clearBuffers(tempBuffers_);
	}

	private static <T> void clearBuffers(Map<T, BufferedImage> buffers)
	{
		for (BufferedImage i : buffers.values())
			i.flush();
		buffers.clear();
	}

	/**
	 * Adds a buffer by name.
	 *
	 * @param name The name of the buffer.
	 * @param img  The Buffer-Image.
	 */
	public void addBuffer(String name, BufferedImage img)
	{
		buffers_.put(name, img);
	}

	/**
	 * Gets a buffer.
	 *
	 * @param name The name.
	 * @return The buffer or null.
	 */
	public BufferedImage getSourceBuffer(String name)
	{
		return buffers_.get(name);
	}

	/**
	 * Get the source buffer as alpha (rgba with rgb part black).
	 *
	 * @param name Name of buffer (without "alpha" in name).
	 * @return The buffer image or null.
	 */
	public BufferedImage getSourceAlphaBuffer(String name)
	{
		final String alphaName = name + "Alpha".intern();
		BufferedImage i = buffers_.get(alphaName);
		if (i == null)
		{
			i = getSourceBuffer(name);
			if (i != null)
			{
				Raster in = i.getData();
				++buffersCreated_;
				BufferedImage alpha = cfg_.createCompatibleImage(i.getWidth(), i.getHeight(), i.getTransparency());

				int N = in.getNumBands();
				final float[] scaleFactors = new float[N];
				final float[] offsets = new float[N];
				Arrays.fill(scaleFactors, 0);
				scaleFactors[N - 1] = 1f;
				Arrays.fill(offsets, 0);

				RescaleOp extractAlpha = new RescaleOp(scaleFactors, offsets, null);
				extractAlpha.filter(in, alpha.getRaster());

				buffers_.put(alphaName, alpha);
				i = alpha;
			}
		}
		return i;
	}

	/**
	 * Get a temporary buffer.
	 *
	 * @param idx    The index of the buffer. Each index gets it own buffer.
	 * @param width  The width of the buffer.
	 * @param height The height of the buffer.
	 * @return The buffer, never null.
	 */
	public BufferedImage getTemporaryBuffer(int idx, double width, double height)
	{
		String key = idx + ":"
				+ ((int) Math.ceil(width / 100) * 100) + ":"
				+ ((int) Math.ceil(height / 100) * 100);
		return getBuffer(key, tempBuffers_, width, height);
	}

	/**
	 * Get or create a target buffer.
	 *
	 * @param name   The name of the buffer.
	 * @param width  The width of the buffer.
	 * @param height The height of the buffer.
	 * @return The buffer, never null.
	 */
	public BufferedImage getTargetBuffer(String name, double width, double height)
	{
		return getBuffer(name, buffers_, width, height);
	}

	private <T> BufferedImage getBuffer(
			T key, Map<T, BufferedImage> buffer, double width, double height)
	{
		BufferedImage i = buffer.get(key);
		if (i == null || i.getWidth() < width || i.getHeight() < height)
		{
			if (width <= 0.5) width = 1;
			if (height <= 0.5) height = 1;
			++buffersCreated_;
			i = cfg_.createCompatibleImage((int) width, (int) height, Transparency.TRANSLUCENT);
			buffer.put(key, i);
		}


		return i;
	}

}

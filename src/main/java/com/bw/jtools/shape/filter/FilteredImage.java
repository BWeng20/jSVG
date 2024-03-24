package com.bw.jtools.shape.filter;

import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;

/**
 * Helper class to hold a filter output.
 */
public class FilteredImage
{
	/**
	 * The rendered images.
	 */
	public BufferedImage image_;

	/**
	 * The resulting offset of the image.
	 */
	public Point2D.Double offset_;
}

package com.bw.jtools.svg;

import java.awt.MultipleGradientPaint;
import java.util.ArrayList;
import java.util.List;

/**
 * Base for SVG Filter Primitive implementations.
 */
public abstract class FilterPrimitive
{
	/**
	 * Default for x value of region definitions.
	 */
	public static final Length xDefault = new Length(0, LengthUnit.percent);
	/**
	 * Default for y value of region definitions.
	 */
	public static final Length yDefault = new Length(0, LengthUnit.percent);
	/**
	 * Default for width value of region definitions.
	 */
	public static final Length widthDefault = new Length(100, LengthUnit.percent);
	/**
	 * Default for height value of region definitions.
	 */
	public static final Length heightDefault = new Length(100, LengthUnit.percent);

	/**
	 * SVG tag of the filter primitive.
	 */
	public final SvgTagType type_;

	/**
	 * The color interpolation from attribute "color-interpolation-filters".
	 */
	public MultipleGradientPaint.ColorSpaceType colorInterpolation_;

	/**
	 * The effective region of the filter.
	 */
	public Length x_, y_, width_, height_;

	/**
	 * Names of source buffers.
	 */
	public final List<String> in_ = new ArrayList<>();
	/**
	 * Name of result buffer.
	 */
	public String result_;

	/**
	 * Get number of input buffers.
	 */
	public abstract int numberOfInputs();

	/**
	 * Check if a SVG tag is a filter primitive.
	 */
	public static boolean isFilterPrimitive(SvgTagType type)
	{
		return type.ordinal() >= SvgTagType.feBlend.ordinal();
	}

	protected FilterPrimitive(SvgTagType filterType)
	{
		type_ = filterType;
	}
}

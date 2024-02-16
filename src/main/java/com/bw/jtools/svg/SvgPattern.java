package com.bw.jtools.svg;

import java.awt.Color;
import java.awt.Paint;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a SVG Pattern definition.
 */
public class SvgPattern extends SvgPaint
{
	/**
	 * Reference box.
	 */
	public Length x_, y_, width_, height_;

	/**
	 * Shapes of the pattern.
	 */
	public final List<ElementInfo> shapes_ = new ArrayList<>();

	/**
	 * Creates a new pattern descriptor.
	 * @param id The XML id.
	 */
	public SvgPattern(String id) {
		super(id);
	}

	@Override
	public SvgPaint adaptOpacity(float opacity)
	{
		// @TODO: Implement this
		return this;
	}

	@Override
	public Paint createPaint(ElementWrapper w)
	{
		// Can't create now
		return Color.BLACK;
	}

	@Override
	public void resolveHref(SVGConverter svg)
	{
		if (href_ != null)
		{
			SvgPaint hrefGradient = svg.getSvgPaint(href_);
			href_ = null;
			if (hrefGradient instanceof SvgPattern)
			{
				hrefGradient.resolveHref(svg);
				copyFromTemplate((SvgPattern)hrefGradient);
			}
		}
	}

	/**
	 * Copies yet undefined elements from template.
	 * @param template The template. Must not be null.
	 */
	public void copyFromTemplate(SvgPattern template)
	{
		//@TODO: Aggregate or copy? Check specs!
		if (aft_ == null) aft_ = template.aft_;

		if ( x_ == null) x_ = template.x_;
		if ( y_ == null) y_ = template.y_;
		if ( width_ == null) y_ = template.width_;
		if ( height_ == null) y_ = template.height_;

		if ( shapes_.isEmpty() ) {
			shapes_.addAll(template.shapes_);
		}

	}


}

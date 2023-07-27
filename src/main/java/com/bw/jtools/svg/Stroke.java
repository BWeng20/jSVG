package com.bw.jtools.svg;

import java.awt.BasicStroke;

/**
 * Holds all information about a stroke.
 */
public class Stroke
{
	public Stroke(
			Color color,
			Length width,
			LengthList dasharray,
			Double dashoffset,
			LineCap linecap,
			LineJoin linejoin,
			Double miterlimit)
	{
		cap = BasicStroke.CAP_BUTT;
		if (linecap != null)
			switch (linecap)
			{
				case butt:
					break;
				case round:
					cap = BasicStroke.CAP_ROUND;
					break;
				case square:
					cap = BasicStroke.CAP_SQUARE;
					break;
			}

		join = BasicStroke.JOIN_MITER;
		if (linejoin != null)
			switch (linejoin)
			{
				case bevel:
					join = BasicStroke.JOIN_BEVEL;
					break;
				case round:
					join = BasicStroke.JOIN_ROUND;
					break;
				case arcs:
				case miter:
				case miter_clip:
					break;
			}

		width_ = width == null ? defaultWidth_ : width;
		miterlimit_ = miterlimit == null ? 4f : miterlimit.floatValue();
		dasharray_ = dasharray;
		dashoffset_ = dashoffset == null ? 0f : dashoffset.floatValue();

		paint_ = color == null ? null : color.getPaintWrapper();
	}

	private PaintWrapper paint_;
	private int cap;
	private int join;
	private Length width_;
	private float miterlimit_;
	private LengthList dasharray_;
	private float dashoffset_;
	private static final Length defaultWidth_ = new Length(1, LengthUnit.px);

	/**
	 * Applies the stroke to an element and creates an awt stroke instance for it.
	 */
	public java.awt.Stroke createStroke(ElementWrapper w)
	{
		final Double vpLength = w.getViewPortLength();
		return new BasicStroke(
				(float) width_.toPixel(vpLength), cap, join, miterlimit_,
				dasharray_ == null ? null : dasharray_.toFloatPixel(vpLength),
				dashoffset_);

	}

	public PaintWrapper getPaintWrapper()
	{
		return paint_;
	}

}

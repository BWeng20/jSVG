package com.bw.jtools.svg;

import com.bw.jtools.shape.Context;

/**
 * Encapsulates color and gradients definitions to adapt gradients if needed.
 * The class is invariant. Any modification creates a new instance.
 */
public final class PaintWrapper
{
	enum Mode
	{
		/**
		 * Pattern or some gradient
		 */
		Paint,
		Color,
		ContextFill,
		ContextStroke
	}

	private Object value_;
	private final Mode mode_;

	private PaintWrapper(Mode mode)
	{
		mode_ = mode;
	}

	public PaintWrapper(PaintWrapper other)
	{
		if (other == null)
		{
			mode_ = Mode.Color;
			value_ = Context.NONE;
		}
		else
		{
			mode_ = other.mode_;
			value_ = other.value_;
		}
	}

	public PaintWrapper(java.awt.Color color)
	{
		mode_ = Mode.Color;
		value_ = color;
	}

	public PaintWrapper(SvgPaint paint)
	{
		mode_ = Mode.Paint;
		value_ = paint;
	}

	public static PaintWrapper contextFill()
	{
		return new PaintWrapper(Mode.ContextFill);
	}

	public static PaintWrapper contextStroke()
	{
		return new PaintWrapper(Mode.ContextStroke);
	}

	public PaintWrapper adaptOpacity(float opacity)
	{
		if (opacity == 1f)
			return this;
		PaintWrapper pw = new PaintWrapper(mode_);
		if (mode_ == Mode.Color)
			pw.value_ = SvgColor.adaptOpacity((java.awt.Color) value_, opacity);
		else if (mode_ == Mode.Paint)
			pw.value_ = ((SvgPaint) value_).adaptOpacity(opacity);
		else
			// @TODO: What to do for context-modes?
			pw.value_ = value_;
		return pw;
	}

	public java.awt.Color getColor()
	{
		return mode_ == Mode.Color ? (java.awt.Color) value_ : null;
	}

	public java.awt.Paint createPaint(ElementWrapper w)
	{
		switch (mode_)
		{
			case Color:
				return (java.awt.Color) value_;
			case Paint:
				return ((SvgPaint) value_).createPaint(w);
			case ContextFill:
				// @TODO
				return null;
			case ContextStroke:
				// @TODO
				return null;
		}
		return null;
	}

}

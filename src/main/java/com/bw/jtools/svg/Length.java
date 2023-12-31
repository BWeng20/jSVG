package com.bw.jtools.svg;

import java.awt.Toolkit;

/**
 * A length with unit.
 */
public final class Length
{
	public final double value_;
	public final LengthUnit unit_;

	public Length(double value, LengthUnit unit)
	{
		value_ = value;
		unit_ = unit;
	}

	private static final double pixelPerInch_;
	private static final double pixelPerPoint_;
	private static final double pixelPerM_;
	private static final double pixelPerCM_;
	private static final double pixelPerMM_;
	private static final double pixelPerPica_;

	// @TODO: font height
	private static final double pixelPerEM_ = 12;


	// @TODO: font x-height (height of small letters)
	private static final double pixelPerEX_ = 8;

	static
	{
		double ppi = 72;
		try
		{
			ppi = Toolkit.getDefaultToolkit()
						 .getScreenResolution();
		}
		catch (Exception ex)
		{
		}
		pixelPerInch_ = ppi;
		pixelPerPoint_ = ppi / 72d;
		pixelPerM_ = 39.37d * ppi;
		pixelPerCM_ = 0.3937d * ppi;
		pixelPerMM_ = 0.03937d * ppi;
		pixelPerPica_ = ppi / 6d;
	}

	/**
	 * Returns a new Length that is the sum of this and v.
	 *
	 * @param v The other value to add.
	 */
	public Length add(Length v)
	{
		if (v != null)
		{
			if (v.unit_ == unit_)
			{
				return new Length(value_ + v.value_, unit_);
			}
			else
			{
				return new Length(toPixel(null) + v.toPixel(null), LengthUnit.px);
			}
		}
		else
			return new Length(value_, unit_);
	}

	/**
	 * Conversion to pixel.
	 *
	 * @param absValue Absolute value in case length is percentage. Can be null.
	 */
	public double toPixel(Double absValue)
	{
		if (unit_ == null)
			return value_ * pixelPerPoint_;
		switch (unit_)
		{
			case pt:
				return value_ * pixelPerPoint_;
			case px:
				return value_;
			case in:
				return value_ * pixelPerInch_;
			case m:
				return value_ * pixelPerM_;
			case cm:
				return value_ * pixelPerCM_;
			case mm:
				return value_ * pixelPerMM_;
			case pc:
				return value_ * pixelPerPica_;
			case rem:
			case em:
				return value_ * pixelPerEM_;
			case ex:
				return value_ * pixelPerEX_;
			case percent:
				return absValue == null ? value_ : (absValue * (value_ / 100d));
		}
		return value_;
	}

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder(20);
		sb.append(value_);
		if (unit_ != null)
			sb.append(unit_);
		return sb.toString();
	}
}

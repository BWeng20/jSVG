package com.bw.jtools.svg;

/**
 * Represent values of the file-rule attribute.
 */
public enum FillRule
{
	nonzero, evenodd;

	public static FillRule fromString(String val)
	{
		if (val != null)
		{
			try
			{
				FillRule r = FillRule.valueOf(val.toLowerCase());
				return r;
			}
			catch (IllegalArgumentException i)
			{
				SVGConverter.warn("Unknown file-rule value %s", val);
			}
		}
		return nonzero;
	}
}

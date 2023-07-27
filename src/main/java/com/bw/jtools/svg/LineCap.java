package com.bw.jtools.svg;

public enum LineCap
{
	butt,
	round,
	square;

	public static LineCap fromString(String val)
	{
		if (val != null)
		{
			try
			{
				return LineCap.valueOf(val.toLowerCase());
			}
			catch (IllegalArgumentException i)
			{
				SVGConverter.error("Unknown line-cap value '%s'", val);
			}
		}
		return butt;
	}

}

package com.bw.jtools.svg;

public enum LineJoin
{
	arcs, bevel, miter, miter_clip, round;

	public static LineJoin fromString(String val)
	{
		if (val != null)
		{
			try
			{
				return LineJoin.valueOf(val.replace('-', '_')
										   .toLowerCase());
			}
			catch (IllegalArgumentException i)
			{
				SVGConverter.warn("Unknown line-join mode %s", val);
			}
		}
		return miter;
	}
}

package com.bw.jtools.svg;

import java.awt.geom.Path2D;

public class Polyline extends Parser
{
	public Polyline(String points)
	{
		super(points);

		Double x, y;
		boolean first = true;
		do
		{
			x = nextDouble(Double.NaN);
			y = nextDouble(Double.NaN);
			if (Double.isNaN(x) || Double.isNaN(y))
				break;
			if (first)
			{
				path_.moveTo(x, y);
				first = false;
			}
			else
				path_.lineTo(x, y);
		} while (true);
	}

	Path2D.Double path_ = new Path2D.Double();

	public Path2D getPath()
	{
		return path_;
	}

	public Path2D toPolygon()
	{
		path_.closePath();
		return path_;
	}
}

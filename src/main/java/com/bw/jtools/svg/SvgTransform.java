package com.bw.jtools.svg;

import com.bw.jtools.shape.StyledShape;

import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A SVG Transformation
 */
public class SvgTransform extends Parser
{
	private AffineTransform transform_;
	private static final Pattern transformRE = Pattern.compile("([a-z,A-Z ]+)\\(([^\\(]+)\\)", Pattern.CASE_INSENSITIVE);

	public SvgTransform(StyledShape s, String transform)
	{
		super();
		Matcher m = transformRE.matcher(transform);
		double w = 0, h = 0;
		if (s != null && s.shape_ != null)
		{
			Rectangle2D r = s.shape_.getBounds2D();
			w = r.getWidth();
			h = r.getHeight();
		}
		else
		{
			w = 100;
			h = 100;
		}
		try
		{
			while (m.find())
			{
				String transformation = m.group(1)
										 .trim()
										 .toLowerCase();
				setContent(m.group(2));

				AffineTransform t;

				if ("none".equals(transformation))
					t = null;
				else if ("translate".equals(transformation))
				{
					Length x = nextLengthPercentage();
					Length y = nextLengthPercentage();
					t = AffineTransform.getTranslateInstance(x.toPixel(w), y == null ? 0 : y.toPixel(h));
				}
				else if ("translateX".equals(transformation))
					t = AffineTransform.getTranslateInstance(nextLengthPercentage().toPixel(w), 0);
				else if ("translateY".equals(transformation))
					t = AffineTransform.getTranslateInstance(0, nextLengthPercentage().toPixel(h));
				else if ("matrix".equals(transformation))
				{
					double[] matrix = new double[6];
					matrix[0] = nextDouble();
					matrix[1] = nextDouble();
					matrix[2] = nextDouble();
					matrix[3] = nextDouble();
					matrix[4] = nextDouble();
					matrix[5] = nextDouble();
					t = new AffineTransform(matrix);
				}
				else if ("scale".equals(transformation))
				{
					double x = nextDouble();
					double y = nextDouble();
					if (y == 0) y = x;
					t = AffineTransform.getScaleInstance(x, y);
				}
				else if ("scaleX".equals(transformation))
				{
					t = AffineTransform.getScaleInstance(nextDouble(), 1);
				}
				else if ("scaleY".equals(transformation))
				{
					t = AffineTransform.getScaleInstance(1, nextDouble());
				}
				else if ("rotate".equals(transformation))
				{
					double angle = nextAngle(0);
					double x = nextDouble(Double.NaN);
					double y = nextDouble(Double.NaN);
					if (x == x && y == y)
						t = AffineTransform.getRotateInstance(angle, x, y);
					else
						t = AffineTransform.getRotateInstance(angle);
				}
				else if ("skewX".equals(transformation))
				{
					t = null;
					// @TODO
				}
				else if ("skewY".equals(transformation))
				{
					t = null;
					// @TODO
				}
				else
					t = null;

				if (t != null)
				{
					if (transform_ != null)
						transform_.concatenate(t);
					else
						transform_ = t;
				}
			}
		}
		catch (Exception e)
		{
			SVGConverter.error("Failed to parse transform", e);
			transform_ = new AffineTransform();
		}
	}

	AffineTransform getTransform()
	{
		return transform_;
	}


}

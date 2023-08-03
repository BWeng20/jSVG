package com.bw.jtools.svg;

import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import static com.bw.jtools.svg.ElementWrapper.isNotEmpty;

public class Marker extends ElementInfo
{
	Rectangle2D.Double viewBox_;

	/**
	 * Reference point, mapped to marker position.
	 * Using viewport coordinates.
	 */
	public Length refX_;
	public Length refY_;

	/**
	 * Size of markers in
	 */
	public double markerWidth_ = 3;
	public double markerHeight_ = 3;

	/**
	 * If auto is active, orientation is reverse for marker-start.
	 */
	public boolean autoReverse_;

	/**
	 * If angle is null, auto is active
	 */
	public Double angle_;

	public MarkerUnit unit_;

	public List<ElementInfo> shapes_ = new ArrayList<>();

	public Marker(ElementWrapper w)
	{
		viewBox_ = w.getViewBox();
		if (viewBox_ == null) viewBox_ = new Rectangle2D.Double(0, 0, 1, 1);

		refX_ = toRefLength(w.attr("refX"));
		refY_ = toRefLength(w.attr("refY"));

		Length marker = w.toLength("markerWidth");
		if (marker != null) markerWidth_ = marker.toPixel(null);
		marker = w.toLength("markerHeight");
		if (marker != null) markerHeight_ = marker.toPixel(null);

		String orient = w.attr("orient");
		if ("auto".equalsIgnoreCase(orient))
		{
			autoReverse_ = false;
			angle_ = null;
		}
		else if ("auto-start-reverse".equalsIgnoreCase(orient))
		{
			autoReverse_ = true;
			angle_ = null;
		}
		else
		{
			Length orientL = w.parseLength(orient);
			angle_ = orientL == null ? 0 : orientL.toPixel(null);
		}
		MarkerUnit unit = MarkerUnit.fromString(w.attr("markerUnits"));
		if (unit != null)
			unit_ = unit;
	}

	private Length toRefLength(String value)
	{
		if (isNotEmpty(value))
		{
			Length l;
			if ("left".equalsIgnoreCase(value) || "top".equalsIgnoreCase(value))
				l = new Length(0, LengthUnit.percent);
			else if ("center".equalsIgnoreCase(value))
				l = new Length(50, LengthUnit.percent);
			else if ("right".equalsIgnoreCase(value) || "bottom".equalsIgnoreCase(value))
				l = new Length(100, LengthUnit.percent);
			else
				l = ElementWrapper.parseLength(value);
			return l;
		}
		return null;
	}


	@Override
	public void applyTransform(AffineTransform aft)
	{
		for (ElementInfo e : shapes_)
			e.applyTransform(aft);
	}
}

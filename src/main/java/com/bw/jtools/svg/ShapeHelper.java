package com.bw.jtools.svg;

import java.awt.Shape;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Provides geometric and other helper functions on a shape.
 */
public final class ShapeHelper
{

	private final Shape shape_;
	private double outlineLength_;
	private List<Segment> segments_;
	private Rectangle2D bounds_;

	private boolean isClosed_;

	public ShapeHelper(Shape shape)
	{
		shape_ = shape;
	}

	public Shape getShape()
	{
		return shape_;
	}

	public Rectangle2D getBoundingBox()
	{
		if (bounds_ == null)
		{
			bounds_ = shape_.getBounds2D();
		}
		return bounds_;
	}

	public double getOutlineLength()
	{
		initialiseIfNeeded();
		return outlineLength_;
	}

	public PointOnPath pointAtLength(double length)
	{
		Segment upper = findSegmentAtLength(length);
		if (upper != null)
		{
			Segment lower = getSegmentAtIndex(upper.index_ - 1);
			PointOnPath p = new PointOnPath();
			if (lower != null)
			{
				double offset = length - lower.length_;
				p.angle_ = Math.atan2(upper.y_ - lower.y_, upper.x_ - lower.x_);
				p.x_ = lower.x_ + (offset * Math.cos(p.angle_));
				p.y_ = lower.y_ + (offset * Math.sin(p.angle_));
			}
			else
			{
				Segment next = getSegmentAtIndex(upper.index_ + 1);
				if (next != null)
				{
					p.angle_ = Math.atan2(next.y_ - upper.y_, next.x_ - upper.x_);
					p.x_ = upper.x_ + (length * Math.cos(p.angle_));
					p.y_ = upper.y_ + (length * Math.sin(p.angle_));
				}
				else
				{
					p.x_ = upper.x_;
					p.y_ = upper.y_;
					p.angle_ = 0;
				}
			}
			return p;
		}
		return null;
	}

	public PointOnPath pointAtX(double xpos)
	{
		Segment upper = findSegmentAtX(xpos);
		if (upper != null)
		{
			Segment lower = getSegmentAtIndex(upper.index_ - 1);
			PointOnPath p = new PointOnPath();
			if (lower != null)
			{
				double offset = xpos - lower.x_;
				p.angle_ = Math.atan2(upper.y_ - lower.y_, upper.x_ - lower.x_);
				p.x_ = lower.x_ + (offset * Math.cos(p.angle_));
				p.y_ = lower.y_ + (offset * Math.sin(p.angle_));
			}
			else
			{
				Segment next = getSegmentAtIndex(upper.index_ + 1);
				if (next != null)
				{
					p.angle_ = Math.atan2(next.y_ - upper.y_, next.x_ - upper.x_);
					double offset = xpos - upper.x_;
					p.x_ = upper.x_ + (offset * Math.cos(p.angle_));
					p.y_ = upper.y_ + (offset * Math.sin(p.angle_));
				}
				else
				{
					p.x_ = upper.x_;
					p.y_ = upper.y_;
					p.angle_ = 0;
				}
			}
			return p;
		}
		return null;
	}

	public Segment getSegmentAtIndex(int index)
	{
		return (index >= 0 && index < segments_.size()) ? segments_.get(index) : null;
	}

	public Segment findSegmentAtLength(double length)
	{
		initialiseIfNeeded();

		if (length < 0 || length > outlineLength_)
		{
			return null;
		}

		for (Segment s : segments_)
		{
			if (s.length_ >= length)
			{
				return s;
			}
		}
		return null;
	}

	public Segment findSegmentAtX(double x)
	{
		initialiseIfNeeded();

		for (Segment s : segments_)
		{
			if (s.x_ >= x)
			{
				return s;
			}
		}
		return null;
	}

	private void initialiseIfNeeded()
	{
		if (segments_ == null)
		{
			outlineLength_ = 0d;

			PathIterator pi = shape_.getPathIterator(null, 0.1d);
			segments_ = new ArrayList<>(10);

			double x = 0;
			double y = 0;
			double cx = 0;
			double cy = 0;
			double[] seg = new double[6];

			while (!pi.isDone())
			{
				final int type = pi.currentSegment(seg);
				switch (type)
				{
					case PathIterator.SEG_MOVETO:
						x = seg[0];
						y = seg[1];
						cx = x;
						cy = y;
						segments_.add(new Segment(PathIterator.SEG_MOVETO, x, y, outlineLength_, segments_.size()));
						break;
					case PathIterator.SEG_LINETO:
						outlineLength_ += Point2D.distance(x, y, seg[0], seg[1]);
						x = seg[0];
						y = seg[1];
						segments_.add(new Segment(PathIterator.SEG_LINETO, x, y, outlineLength_, segments_.size()));
						break;
					case PathIterator.SEG_CLOSE:
						outlineLength_ += Point2D.distance(x, y, cx, cy);
						segments_.add(new Segment(PathIterator.SEG_CLOSE, cx, cy, outlineLength_, segments_.size()));
						x = cx;
						y = cy;
						break;
				}
				pi.next();
			}
			if (segments_.size()>1) {
				// This logic is a bit fuzzy, as a path CAN contain multiple "MOVE" at start.
				// we assume that the first segment is a real one...
				Segment f = segments_.get(0);
				Segment l = segments_.get(segments_.size()-1);
				isClosed_ = Point2D.distance(f.x_, f.y_, l.x_, l.y_) < 0.1;
			} else {
				// A Shape with only one segment can not be closed (or how?).
				isClosed_ = false;
			}

		}
	}

	public Shape getSegmentPath()
	{
		initialiseIfNeeded();
		Path2D.Double path = new Path2D.Double();
		for (Segment s : segments_)
		{
			switch (s.type_)
			{
				case PathIterator.SEG_MOVETO:
					path.moveTo(s.x_, s.y_);
					break;
				case PathIterator.SEG_LINETO:
					path.lineTo(s.x_, s.y_);
					break;
				case PathIterator.SEG_CLOSE:
					path.closePath();
					break;
			}
		}
		return path;
	}

	/**
	 * Guess (from start and end point) if this shape is "closed".
	 * A path with different sub-paths is assumed "closed" if the coordinates of the first and last segments a near enough.
	 */
	public boolean isClosed()
	{
		return isClosed_;
	}


	public static class PointOnPath
	{

		public double x_;
		public double y_;
		public double angle_;
	}

	public static class Segment
	{

		public final int type_;
		public final double x_;
		public final double y_;
		public final double length_;
		public final int index_;

		public Segment(int type, double x, double y, double length, int index)
		{
			type_ = type;
			x_ = x;
			y_ = y;
			length_ = length;
			index_ = index;
		}
	}
}

package com.bw.jtools.svg;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.List;

/**
 * Collects all information about a group of shapes.
 * Used in case a group of shapes has a common filter.
 */
public class GroupInfo extends ElementInfo
{
	public List<ElementInfo> shapes_ = new ArrayList<>();
	public Filter filter_;
	public Shape clipPath_;

	/**
	 * Constructor to initialize,
	 */
	public GroupInfo(String id)
	{
		id_ = id;
	}

	@Override
	public void applyTransform(AffineTransform aft)
	{
		for (ElementInfo e : shapes_)
			e.applyTransform(aft);
	}

	@Override
	public void applyPostTransform(AffineTransform aft)
	{
		for (ElementInfo e : shapes_)
			e.applyPostTransform(aft);
	}
}

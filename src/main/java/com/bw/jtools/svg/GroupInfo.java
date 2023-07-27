package com.bw.jtools.svg;

import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.List;

/**
 * Collects all information about a group of shapes.
 * Used in case a group of shapes has a common filter.
 */
public final class GroupInfo extends ElementInfo
{
	public List<ElementInfo> shapes_ = new ArrayList<>();
	public Filter filter_;

	/**
	 * Constructor to initialize,
	 */
	public GroupInfo(String id, Filter filter)
	{
		id_ = id;
		filter_ = filter;
	}

	@Override
	public void applyTransform(AffineTransform aft)
	{
		for (ElementInfo e : shapes_)
			e.applyTransform(aft);
	}

}

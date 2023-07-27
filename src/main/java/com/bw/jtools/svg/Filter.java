package com.bw.jtools.svg;

import java.util.List;

/**
 * Declaration of a filter.
 */
public class Filter
{
	public final String id_;
	public final Type type_;

	public Unit filterUnits_;
	public Unit primitiveUnits_;

	public Length x_, y_, width_, height_;

	public List<FilterPrimitive> primitives_;

	public Filter(String id, Type type)
	{
		id_ = id;
		type_ = type;
	}
}

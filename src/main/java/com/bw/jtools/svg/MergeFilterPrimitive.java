package com.bw.jtools.svg;

import java.util.ArrayList;
import java.util.List;

/**
 * Merge filter primitive.
 */
public class MergeFilterPrimitive extends FilterPrimitive
{
	public final List<MergeFilterNode> nodes_ = new ArrayList<>();

	@Override
	public int numberOfInputs()
	{
		return in_.size();
	}

	public MergeFilterPrimitive()
	{
		super(SvgTagType.feMerge);
	}
}

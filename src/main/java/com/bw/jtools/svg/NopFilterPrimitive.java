package com.bw.jtools.svg;

/**
 * Offset filter primitive.
 */
public class NopFilterPrimitive extends FilterPrimitive
{
	@Override
	public int numberOfInputs()
	{
		return 1;
	}

	public NopFilterPrimitive()
	{
		super(Type.feNop);
	}
}

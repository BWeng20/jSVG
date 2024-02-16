package com.bw.jtools.svg;

/**
 * Offset filter primitive.
 */
public class OffsetFilterPrimitive extends FilterPrimitive
{
	public final Length dx_;
	public final Length dy_;

	@Override
	public int numberOfInputs()
	{
		return 1;
	}

	public OffsetFilterPrimitive(Length dx, Length dy)
	{
		super(SvgTagType.feOffset);
		this.dx_ = dx;
		this.dy_ = dy;
	}
}

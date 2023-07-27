package com.bw.jtools.shape.filter;

import com.bw.jtools.CaseInsensitiveEnum;

/**
 * Porter-Duff compositing mode.
 */
public enum CompositeOperator
{
	over,
	in, out,
	atop,
	xor,
	arithmetic;

	private static final CaseInsensitiveEnum<CompositeOperator> mapper_ = new CaseInsensitiveEnum(CompositeOperator.values());

	public static CompositeOperator fromString(String val)
	{
		return mapper_.fromString(val);
	}

}

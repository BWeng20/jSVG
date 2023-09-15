package com.bw.jtools.svg;

public enum MarkerUnit
{
	userSpaceOnUse,
	strokeWidth;

	private static final CaseInsensitiveEnum<MarkerUnit> mapper_ = new CaseInsensitiveEnum(MarkerUnit.values());

	public static MarkerUnit fromString(String val)
	{
		return mapper_.fromString(val);
	}
}

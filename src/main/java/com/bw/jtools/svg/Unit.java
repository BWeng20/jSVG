package com.bw.jtools.svg;

public enum Unit
{
	userSpaceOnUse,
	objectBoundingBox;

	private static final CaseInsensitiveEnum<Unit> mapper_ = new CaseInsensitiveEnum(Unit.values());

	public static Unit fromString(String val)
	{
		return mapper_.fromString(val);
	}
}

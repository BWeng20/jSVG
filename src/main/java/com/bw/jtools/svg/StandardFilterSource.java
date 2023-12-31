package com.bw.jtools.svg;

public enum StandardFilterSource
{
	SourceGraphic,
	SourceAlpha,
	BackgroundImage,
	BackgroundAlpha,
	FillPaint,
	StrokePaint;

	private static final CaseInsensitiveEnum<StandardFilterSource> mapper_ = new CaseInsensitiveEnum(StandardFilterSource.values());

	public static StandardFilterSource fromString(String val)
	{
		return mapper_.fromString(val);
	}

}

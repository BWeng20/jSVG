package com.bw.jtools.svg;

import java.util.HashMap;
import java.util.Map;

public enum LengthUnit
{
	em,
	ex,
	px,
	in,
	m,
	cm,
	mm,
	pt,
	pc,
	percent,
	rem;

	private static final Map<String, LengthUnit> lowerCaseMap_;

	static
	{
		lowerCaseMap_ = new HashMap<>();
		for (LengthUnit gu : LengthUnit.values())
			if (gu == percent)
				lowerCaseMap_.put("%", gu);
			else
				lowerCaseMap_.put(gu.name()
									.toLowerCase(), gu);
	}


	public static LengthUnit fromString(String val)
	{
		if (val != null)
			return lowerCaseMap_.get(val.trim()
										.toLowerCase());
		return null;
	}


}

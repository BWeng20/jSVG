package com.bw.jtools;

import java.util.HashMap;
import java.util.Map;

public final class CaseInsensitiveEnum<T extends Enum>
{
	public Map<String, T> lowerCaseMap_;

	public CaseInsensitiveEnum(T[] values)
	{
		lowerCaseMap_ = new HashMap<>();
		for (T gu : values)
			lowerCaseMap_.put(gu.name()
								.toLowerCase(), gu);
	}

	public T fromString(String val)
	{
		if (val != null)
			return lowerCaseMap_.get(val.trim()
										.toLowerCase());
		return null;
	}
}

package com.bw.jtools.svg;

import java.util.HashMap;
import java.util.Map;

/**
 * Helper class to handle enums in a case-insensitive way.
 *
 * @param <T> The actual Enum type.
 */
public final class CaseInsensitiveEnum<T extends Enum>
{

	private Map<String, T> lowerCaseMap_;

	public CaseInsensitiveEnum(T[] values)
	{
		lowerCaseMap_ = new HashMap<>();
		for (T gu : values)
			lowerCaseMap_.put(gu.name()
								.toLowerCase(), gu);
	}

	/**
	 * Get the matching enum value.
	 *
	 * @param val The value to convert.
	 */
	public T fromString(String val)
	{
		if (val != null)
			return lowerCaseMap_.get(val.trim()
										.toLowerCase());
		return null;
	}
}

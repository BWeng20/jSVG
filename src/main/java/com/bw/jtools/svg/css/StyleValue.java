package com.bw.jtools.svg.css;

/**
 * A style value with specificity.
 */
public class StyleValue
{
	/**
	 * The specificity.
	 */
	public Specificity specificity_;

	/**
	 * The value.
	 */
	public String value_;

	/**
	 * Create a new StyleValue.
	 *
	 * @param value       Value for the new instance.
	 * @param specificity Specificity for the new instance.
	 */
	public StyleValue(String value, Specificity specificity)
	{
		value_ = value;
		specificity_ = specificity;
	}
}

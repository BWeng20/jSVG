package com.bw.jtools.svg.css;

public class StyleValue
{
	public Specificity specificity_;
	public String value_;

	public StyleValue(String value, Specificity specificity)
	{
		value_ = value;
		specificity_ = specificity;
	}
}

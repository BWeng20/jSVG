package com.bw.jtools.svg.css;

/**
 * CSS selector.
 */
public class Selector
{
	/**
	 * Type of selector
	 */
	public SelectorType type_ = SelectorType.TAG;

	/**
	 * Id
	 */
	public String id_;

	/**
	 * Additional selector to combine or null.
	 */
	public Selector combinate_;

	/**
	 * Type of combination if combinate_ is not null.
	 */
	public CombinatorType combinateType_;
}

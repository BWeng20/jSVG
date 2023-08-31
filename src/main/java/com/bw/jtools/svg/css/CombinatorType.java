package com.bw.jtools.svg.css;

/**
 * Possible combination values for CSS selectors.
 */
public enum CombinatorType
{
	/**
	 * Space
	 */
	DESCENDANT,

	/**
	 * &gt;
	 */
	CHILD,

	/**
	 * ~
	 */
	SIBLING,

	/**
	 * +
	 */
	ADJACENT_SIBLING,

	/**
	 * || - not supported!
	 */
	COLUMN_SIBLING

}

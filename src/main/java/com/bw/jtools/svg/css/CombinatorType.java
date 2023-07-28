package com.bw.jtools.svg.css;

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

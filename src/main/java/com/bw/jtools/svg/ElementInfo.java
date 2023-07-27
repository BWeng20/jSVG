package com.bw.jtools.svg;

import java.awt.geom.AffineTransform;

/**
 * Base for shapes and groups.
 */
public abstract class ElementInfo
{
	/**
	 * Id to identify the shape in the some document.
	 */
	public String id_;


	/**
	 * Constructor to initialize,
	 */
	protected ElementInfo()
	{
	}

	public abstract void applyTransform(AffineTransform aft);
}

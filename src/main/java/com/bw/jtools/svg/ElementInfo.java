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
	public AffineTransform aft_;


	/**
	 * Constructor to initialize,
	 */
	protected ElementInfo()
	{
	}

	public void applyTransform(AffineTransform aft)
	{
		if (aft != null)
		{
			if (aft_ == null)
				aft_ = new AffineTransform(aft);
			else
				aft_.preConcatenate(aft);
		}
	}

	public void applyPostTransform(AffineTransform aft)
	{
		if (aft != null)
		{
			if (aft_ == null)
				aft_ = new AffineTransform(aft);
			else
				aft_.concatenate(aft);
		}
	}

}

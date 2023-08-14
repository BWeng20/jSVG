package com.bw.jtools.shape;

import java.awt.Color;
import java.awt.Paint;
import java.awt.geom.Rectangle2D;

/**
 * A abstract base for shapes.
 */
public abstract class AbstractShape
{
	/**
	 * Placeholder for "currentColor". The color from caller-perspective.
	 */
	public static final Color CURRENT_COLOR = new Color(0, 0, 0);

	/**
	 * Placeholder for "background", an internal extension to access the background of the painting component.
	 */
	public static final Color CURRENT_BACKGROUND = new Color(0xce, 0xce, 0xce);

	/**
	 * Placeholder for "none" color.
	 */
	public static final Color NONE = new Color(0, 0, 0, 0);

	/**
	 * Id to identify the shape group in the some document.
	 */
	public final String id_;

	/**
	 * Constructor to initialize,
	 */
	protected AbstractShape(String id)
	{
		this.id_ = id;
	}

	public abstract void paint(Context ctx);

	/**
	 * Get bounds of the transformed shape including stroke-width.
	 */
	public abstract Rectangle2D getTransformedBounds();

	/**
	 * Translates special paints to values.
	 */
	protected Paint translatePaint(Context ctx, Paint p)
	{
		if (p == null)
			return Color.BLACK;
		else if (p == NONE)
			return null;
		else if (p == CURRENT_COLOR)
			return ctx.currentColor_;
		else if (p == CURRENT_BACKGROUND)
			return ctx.currentBackground_;
		else
			return p;
	}

}

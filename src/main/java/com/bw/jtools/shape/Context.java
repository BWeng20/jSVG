package com.bw.jtools.shape;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

/**
 * The drawing context.
 * THis class provides logic for dynamic calculation of colors and transformations.
 */
public class Context
{
	public Graphics2D g2D_;
	public AffineTransform aft_;
	public Paint currentColor_;
	public Paint currentBackground_;
	private final boolean newContext_;

	public boolean debug_ = false;
	/**
	 * Stroke for debug lines.
	 */
	public static Stroke debugStroke_ = new BasicStroke(0.5f);
	/**
	 * Paint for debug lines.
	 */
	public static Paint debugPaint_ = Color.RED;

	public static Object renderingHint_Antialias_ = RenderingHints.VALUE_ANTIALIAS_ON;


	public Context(Context ctx)
	{
		this(ctx, true);
	}

	public Context(Context ctx, boolean createNewContext)
	{
		newContext_ = createNewContext;
		if ( createNewContext )
		{
			this.g2D_ = (Graphics2D) ctx.g2D_.create();
		}
		else
			this.g2D_ = ctx.g2D_;
		aft_ = ctx.aft_;
		currentColor_ = ctx.currentColor_;
		currentBackground_ = ctx.currentBackground_;
		debug_ = ctx.debug_;
	}

	public Context(Graphics g2D)
	{
		this(g2D, true);
	}

	public Context(Graphics g2D, boolean createNewContext)
	{
		this.newContext_ = createNewContext;
		if (createNewContext)
		{
			this.g2D_ = (Graphics2D) g2D.create();
		}
		else
		{
			this.g2D_ = (Graphics2D) g2D;
		}
		this.aft_ = this.g2D_.getTransform();
		this.currentColor_ = this.g2D_.getPaint();
	}

	public Context(BufferedImage source, Context ctx)
	{
		newContext_ = true;
		g2D_ = source.createGraphics();
		initGraphics(g2D_);

		aft_ = ctx.aft_;
		currentColor_ = ctx.currentColor_;
		currentBackground_ = ctx.currentBackground_;
		debug_ = ctx.debug_;
	}

	/**
	 * Sets rendering hints.
	 */
	public static void initGraphics(Graphics2D g2d)
	{
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, Context.renderingHint_Antialias_);
	}

	public void dispose()
	{
		if (newContext_)
		{
			g2D_.dispose();
			g2D_ = null;
		}
	}
}

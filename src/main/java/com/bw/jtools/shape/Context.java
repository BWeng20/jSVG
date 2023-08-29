package com.bw.jtools.shape;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.Paint;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.image.BufferedImage;

/**
 * The drawing context.
 * THis class provides logic for dynamic calculation of colors and transformations.
 */
public class Context
{
	public Graphics2D g2D_;
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

	public boolean translateColor2Gray_ = false;


	/**
	 * Minimum gray value if colors are translated to gray.
	 */
	public float translateColor2GrayMin_ = 90;

	/**
	 * Maximum gray value if colors are translated to gray.
	 */
	public float translateColor2GrayMax_ = 255;

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


	public Context(Context ctx)
	{
		this(ctx, true);
	}

	public Context(Context ctx, boolean createNewContext)
	{
		newContext_ = createNewContext;
		if (createNewContext)
		{
			this.g2D_ = (Graphics2D) ctx.g2D_.create();
		}
		else
			this.g2D_ = ctx.g2D_;
		currentColor_ = ctx.currentColor_;
		currentBackground_ = ctx.currentBackground_;
		translateColor2Gray_ = ctx.translateColor2Gray_;
		translateColor2GrayMin_ = ctx.translateColor2GrayMin_;
		translateColor2GrayMax_ = ctx.translateColor2GrayMax_;
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
		this.currentColor_ = this.g2D_.getPaint();
	}

	public Context(BufferedImage source, Context ctx)
	{
		newContext_ = true;
		g2D_ = source.createGraphics();
		initGraphics(g2D_);

		currentColor_ = ctx.currentColor_;
		currentBackground_ = ctx.currentBackground_;
		translateColor2Gray_ = ctx.translateColor2Gray_;
		translateColor2GrayMin_ = ctx.translateColor2GrayMin_;
		translateColor2GrayMax_ = ctx.translateColor2GrayMax_;
		debug_ = ctx.debug_;
	}

	/**
	 * Sets rendering hints.
	 */
	public static void initGraphics(Graphics2D g2d)
	{
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, Context.renderingHint_Antialias_);
	}


	/**
	 * Translates special paints to values and handled gray-mode.
	 */
	public Paint translatePaint(Paint p)
	{
		if (p == null)
			p = Color.BLACK;
		else if (p == NONE)
			p = null;
		else if (p == CURRENT_COLOR)
			p = currentColor_;
		else if (p == CURRENT_BACKGROUND)
			p = currentBackground_;

		if (translateColor2Gray_)
		{
			if (p instanceof Color)
			{
				float factor = (translateColor2GrayMax_ - translateColor2GrayMin_) / 255f;

				int rgb = ((Color) p).getRGB();

				// Using weights from https://en.wikipedia.org/wiki/Grayscale
				int gray = (int)
						(((0.2126f * ((rgb >> 16) & 0xFF)) + (0.7152f * ((rgb >> 8) & 0xFF)) + (0.0722f * (rgb & 0xFF)) * factor) + translateColor2GrayMin_ + 0.5f);
				if (gray > 255)
					gray = 255;

				p = new Color((rgb & 0xff000000) | (gray << 16) | (gray << 8) | gray);
			}
			else if (p instanceof RadialGradientPaint)
			{
				RadialGradientPaint rp = (RadialGradientPaint) p;

				Color[] colors = rp.getColors();
				for (int ci = 0; ci < colors.length; ++ci)
				{
					colors[ci] = (Color) translatePaint(colors[ci]);
				}
				p = new RadialGradientPaint(rp.getCenterPoint(), rp.getRadius(), rp.getFocusPoint(), rp.getFractions(), colors, rp.getCycleMethod(), rp.getColorSpace(), rp.getTransform());

			}
			else if (p instanceof LinearGradientPaint)
			{
				LinearGradientPaint lp = (LinearGradientPaint) p;

				Color[] colors = lp.getColors();
				for (int ci = 0; ci < colors.length; ++ci)
				{
					colors[ci] = (Color) translatePaint(colors[ci]);
				}

				p = new LinearGradientPaint(
						lp.getStartPoint(), lp.getEndPoint(),
						lp.getFractions(), colors,
						lp.getCycleMethod(), lp.getColorSpace(), lp.getTransform());

			}
		}

		return p;
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

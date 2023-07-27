/*
 * (c) copyright 2022 Bernd Wengenroth
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.bw.jtools.shape;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
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
	public Stroke debugStroke_ = new BasicStroke(0.5f);
	/**
	 * Paint for debug lines.
	 */
	public Paint debugPaint_ = Color.RED;


	public Context(Context ctx)
	{
		this(ctx, true);
	}

	public Context(Context ctx, boolean createNewContext)
	{
		newContext_ = createNewContext;
		g2D_ = createNewContext ? (Graphics2D) ctx.g2D_.create() : ctx.g2D_;
		aft_ = ctx.aft_;
		currentColor_ = ctx.currentColor_;
		currentBackground_ = ctx.currentBackground_;
		debug_ = ctx.debug_;
		if (debug_)
		{
			debugStroke_ = ctx.debugStroke_;
			debugPaint_ = ctx.debugPaint_;
		}
	}

	public Context(Graphics g2D)
	{
		this(g2D, true);
	}

	public Context(Graphics g2D, boolean createNewContext)
	{
		this.newContext_ = createNewContext;
		this.g2D_ = (Graphics2D) (createNewContext ? g2D.create() : g2D);
		this.aft_ = this.g2D_.getTransform();
		this.currentColor_ = this.g2D_.getPaint();
	}

	public Context(BufferedImage source, Context ctx)
	{
		newContext_ = true;
		g2D_ = source.createGraphics();
		aft_ = ctx.aft_;
		currentColor_ = ctx.currentColor_;
		currentBackground_ = ctx.currentBackground_;
		debug_ = ctx.debug_;
		if (debug_)
		{
			debugPaint_ = ctx.debugPaint_;
			debugStroke_ = ctx.debugStroke_;
		}
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

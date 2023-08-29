package com.bw.jtools.shape;

import com.bw.jtools.shape.filter.FilterBase;
import com.bw.jtools.shape.filter.FilterChain;
import com.bw.jtools.shape.filter.FilteredImage;
import com.bw.jtools.shape.filter.PainterBuffers;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * A group of shapes plus optional filter.
 */
public final class ShapeGroup extends AbstractShape
{
	/**
	 * The shapes.
	 */
	public List<AbstractShape> shapes_ = new ArrayList<>();

	public FilterChain filter_;

	public Point2D.Double units_;

	private PainterBuffers buffers_;
	private Rectangle2D transformedBounds_;
	private Shape clipping_;


	/**
	 * Constructor to initialize,
	 */
	public ShapeGroup(String id, FilterChain filter, Shape clipPath, AffineTransform aft)
	{
		super(id);
		this.filter_ = filter;
		this.clipping_ = clipPath;
		this.aft_ = aft;
	}

	/**
	 * The transformation of this group that leads to the buffer-raster.
	 * Combination of
	 * the transformation on the graphics at start
	 * the final transformation on the group itself.
	 * translate-transform to move the group to the Null-point of the buffer.
	 */
	AffineTransform bufferAft_;

	/**
	 * Draws to buffer. The resulting image will be based on the transformed bounds.
	 * To get the correct target point use {@link #getBasePoint(AffineTransform, Point2D)}
	 */
	public void draw2Buffer(Context ctx)
	{

		Rectangle2D r = getTransformedBounds();

		AffineTransform aft = ctx.g2D_.getTransform();
		r = aft.createTransformedShape(r)
			   .getBounds2D();

		AffineTransform bAft = new AffineTransform(aft);
		AffineTransform tr = AffineTransform.getTranslateInstance(-r.getX(), -r.getY());
		bAft.preConcatenate(tr);

		if (buffers_ == null || bufferAft_ == null || 0.001 < transformDifference(bAft, bufferAft_))
		{
			if (buffers_ == null)
			{
				buffers_ = new PainterBuffers();
				buffers_.setConfiguration(ctx.g2D_.getDeviceConfiguration());
			}
			else
			{
				buffers_.clear();
			}

			BufferedImage source = buffers_.getTargetBuffer(FilterBase.SOURCE, r.getWidth(), r.getHeight());
			Context bctx = new Context(source, ctx);
			try
			{
				bufferAft_ = bAft;
				paintInternal(bctx);
			}
			finally
			{
				bctx.dispose();
			}
		}
	}

	@Override
	public void paint(Context ctx)
	{
		if (filter_ == null)
		{
			paintInternal(ctx);
		}
		else
		{
			draw2Buffer(ctx);
			PainterBuffers buffers = getBuffers();
			if (buffers != null)
			{
				Point2D targetPoint = new Point2D.Double(0, 0);

				ctx.g2D_.setTransform(ident_);
				AffineTransform aft = ctx.g2D_.getTransform();
				getBasePoint(aft, targetPoint);
				Point2D.Double units = FilterBase.getUnits(aft);

				FilteredImage image = filter_.render(buffers,
						units_.x * units.x, units_.y * units.y);

				ctx.g2D_.drawImage(image.image_,
						(int) (0.5 + targetPoint.getX() + image.offset_.getX()),
						(int) (0.5 + targetPoint.getY() + image.offset_.getY()), null);

				if (ctx.debug_)
				{
					ctx.g2D_.setColor(Color.RED);
					ctx.g2D_.drawRect(
							(int) (0.5 + targetPoint.getX() + image.offset_.getX()),
							(int) (0.5 + targetPoint.getY() + image.offset_.getY()),
							image.image_.getWidth(), image.image_.getHeight());
				}
			}
		}
	}

	protected void paintInternal(Context ctx)
	{
		final Graphics2D g2D = ctx.g2D_;

		Shape orgClip = null;
		if (clipping_ != null && enableClipping_)
		{
			orgClip = g2D.getClip();
			g2D.clip(clipping_);
		}
		AffineTransform orgAft = g2D.getTransform();
		aftTemp_.setTransform(orgAft);
		if (aft_ != null)
			aftTemp_.concatenate(aft_);

		g2D.setTransform(aftTemp_);

		for (AbstractShape shape : shapes_)
			shape.paint(ctx);

		g2D.setTransform(orgAft);
		if (clipping_ != null && enableClipping_)
			ctx.g2D_.setClip(orgClip);
	}

	/**
	 * Calculate the difference between the matrices of two transformations.
	 */
	protected double transformDifference(AffineTransform a1, AffineTransform a2)
	{
		double[] m1 = new double[6];
		double[] m2 = new double[6];
		a1.getMatrix(m1);
		a2.getMatrix(m2);
		double d = 0;
		for (int i = 0; i < 6; ++i) d += Math.abs(m1[i] - m2[i]);

		return d;
	}

	public PainterBuffers getBuffers()
	{
		return buffers_;
	}

	/**
	 * Get bounds of the transformed shape including stroke-width.
	 */
	@Override
	public Rectangle2D getTransformedBounds()
	{
		if (transformedBounds_ == null)
		{
			if (clipping_ != null)
			{
				Area area = new Area(clipping_);
				if (aft_ != null)
					transformedBounds_ = aft_.createTransformedShape(clipping_)
											 .getBounds2D();
				else
					transformedBounds_ = clipping_.getBounds2D();
			}
			else
			{
				// @TODO: fix it
				for (AbstractShape shape : shapes_)
				{
					Rectangle2D r = shape.getTransformedBounds();
					if (transformedBounds_ == null)
						transformedBounds_ = r.getBounds2D();
					else
						transformedBounds_.add(r);
				}
				if (transformedBounds_ == null)
					transformedBounds_ = new Rectangle2D.Double(0, 0, 0, 0);
				if (clipping_ != null)
				{
					Area area = new Area(transformedBounds_);
					area.intersect(new Area(clipping_));
					transformedBounds_ = area.getBounds2D();
				}
			}
		}
		return transformedBounds_;
	}

	/**
	 * Get the base zero point in target coordinates.
	 */
	public Point2D getBasePoint(AffineTransform aft, Point2D dst)
	{
		Rectangle2D r = getTransformedBounds();
		return aft.transform(new Point2D.Double(r.getX(), r.getY()), dst);
	}

}

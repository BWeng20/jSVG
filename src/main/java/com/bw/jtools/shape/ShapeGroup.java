package com.bw.jtools.shape;

import com.bw.jtools.shape.filter.FilterBase;
import com.bw.jtools.shape.filter.FilterChain;
import com.bw.jtools.shape.filter.FilteredImage;
import com.bw.jtools.shape.filter.PainterBuffers;

import java.awt.Color;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
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
	public ShapeGroup(String id, FilterChain filter, Shape clipPath)
	{
		super(id);
		this.filter_ = filter;
		this.clipping_ = clipPath;
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
		r = ctx.aft_.createTransformedShape(r)
					.getBounds2D();

		AffineTransform bAft = new AffineTransform(ctx.aft_);
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
				bctx.aft_ = bufferAft_ = bAft;
				paintInternal(bctx);
			}
			finally
			{
				bctx.dispose();
			}
		}
	}

	static final AffineTransform ident = new AffineTransform();

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

				ctx.g2D_.setTransform(ident);
				getBasePoint(ctx.aft_, targetPoint);
				Point2D.Double units = FilterBase.getUnits(ctx.aft_);

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
		Shape oldClip = null;
		if (clipping_ != null)
		{
			oldClip = ctx.g2D_.getClip();
			ctx.g2D_.clip(clipping_);
		}
		for (AbstractShape shape : shapes_)
			shape.paint(ctx);

		if (clipping_ != null)
			ctx.g2D_.setClip(oldClip);
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
			for (AbstractShape shape : shapes_)
			{
				Rectangle2D r = shape.getTransformedBounds();
				if (transformedBounds_ == null)
					transformedBounds_ = r.getBounds2D();
				else
					transformedBounds_.add(r);
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

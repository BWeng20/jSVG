package com.bw.jtools.shape;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

public abstract class AbstractPainterBase
{
	/**
	 * Area covered by the shapes, without rotation and scale.
	 *
	 * @see #calculateArea()
	 */
	protected Rectangle2D.Double area_ = null;
	protected double scaleX_ = 1.0f;
	protected double scaleY_ = 1.0f;

	protected boolean measureTime_ = false;
	protected long lastMSNeeded_ = 0;

	protected double rotationAngleDegree_ = 0;

	protected boolean isRotationActive()
	{
		return (rotationAngleDegree_ < -0.1 || rotationAngleDegree_ > 0.1);
	}

	protected AffineTransform getRotation()
	{
		if (isRotationActive())
			return AffineTransform.getRotateInstance(Math.toRadians(rotationAngleDegree_), area_.x + (area_.width / 2),
					area_.y + (area_.height / 2));
		else
			return null;
	}

	protected void ensureArea()
	{
		if (area_ == null)
			calculateArea();
	}


	/**
	 * Returns the covered area according to shapes and scale.
	 */
	public Rectangle2D.Double getArea()
	{
		ensureArea();
		Rectangle2D area = area_;

		if (area != null)
		{
			AffineTransform rotation = getRotation();
			if (rotation != null)
				area = rotation.createTransformedShape(area)
							   .getBounds2D();
		}

		if (area == null)
			return new Rectangle2D.Double(0, 0, 0, 0);
		else
			return new Rectangle2D.Double(0, 0, scaleX_ * area.getWidth(), scaleY_ * area.getHeight());
	}

	/**
	 * Gets the absolute width of the covered area.
	 */
	public double getAreaWidth()
	{
		ensureArea();
		return scaleX_ * area_.width;
	}

	/**
	 * Gets the absolute height of the covered area.
	 */
	public double getAreaHeight()
	{
		ensureArea();
		return scaleY_ * area_.height;
	}

	/**
	 * Sets X- and Y-Scale factor.
	 */
	public void setScale(double scaleX, double scaleY)
	{
		scaleX_ = scaleX;
		scaleY_ = scaleY;
	}

	/**
	 * Gets X-Scale factor.
	 */
	public double getXScale()
	{
		return scaleX_;
	}

	/**
	 * Gets Y-Scale factor.
	 */
	public double getYScale()
	{
		return scaleY_;
	}

	/**
	 * Draw the shapes to a buffered image with foreground black and background white.<br>
	 * If no shapes are loaded, nothing is drawn and if dst is null, a one pixel wide image is created.
	 *
	 * @param dst If null a new buffer, compatible with the current screen is created.
	 * @return dst or (if dst was null) a new created image.
	 */
	public BufferedImage paintShapeToBuffer(BufferedImage dst, boolean toGray)
	{
		return paintShapeToBuffer(dst, Color.BLACK, Color.WHITE, toGray);
	}

	/**
	 * Draw the shapes to a buffered image with foreground black and transparent background.<br>
	 * If no shapes are loaded, nothing is drawn and if dst is null, a one pixel wide image is created.
	 *
	 * @param dst If null a new buffer, compatible with the current screen is created.
	 * @return dst or (if dst was null) a new created image.
	 */
	public BufferedImage paintShapeToBufferTransparent(BufferedImage dst, boolean toGray)
	{
		return paintShapeToBuffer(dst, Color.BLACK, new Color(0, 0, 0, 0), toGray);
	}


	/**
	 * Draw the shapes to a buffered image.<br>
	 * If no shapes are loaded, nothing is drawn and if dst is null, a one pixel wide image is created.
	 *
	 * @param dst        If null a new buffer, compatible with the current screen is created.
	 * @param foreground The foreground color.
	 * @param background The background color.
	 * @return dst or (if dst was null) a new created image.
	 */
	public BufferedImage paintShapeToBuffer(BufferedImage dst, Paint foreground, Paint background, boolean toGray)
	{
		if (dst == null)
		{
			Rectangle2D area = getArea();
			if (area == null || area.getHeight() == 0 || area.getWidth() == 0)
				area = new Rectangle2D.Double(0, 0, 1, 1);

			dst = new BufferedImage((int) (0.5 + area.getWidth()),
					(int) (0.5 + area.getHeight()), BufferedImage.TYPE_INT_ARGB);
		}

		Graphics2D g2d = dst.createGraphics();
		Context.initGraphics(g2d);

		paint(g2d, foreground, background, true, toGray);
		return dst;
	}

	public abstract void paint(Graphics g2d, Paint foreground, Paint background, boolean clearArea, boolean toGray);

	/**
	 * Paints the shape.
	 *
	 * @param g          Graphics, will not be changed.
	 * @param foreground The foreground paint to use.
	 * @param background The background paint to use.
	 * @param clearArea  If true the area of the shapes is cleared with the current color.
	 */
	public void paint(Graphics g, Paint foreground, Paint background, boolean clearArea)
	{
		paint(g, foreground, background, clearArea, false);
	}

	/**
	 * Implementations have to set member {@link #area_} to the covered area, without the rotation and scale of this instance.
	 */
	protected abstract void calculateArea();
}

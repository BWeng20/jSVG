package com.bw.jtools.shape;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;

public abstract class AbstractPainterBase
{
	/**
	 * Area covered by the shapes, without rotation and scale.
	 *
	 * @see #calculateArea()
	 */
	protected Rectangle2D.Double area_ = null;

	/**
	 * Area covert by the transformed shape. Including scale and rotation.
	 */
	protected Rectangle2D.Double areaTransformed_ = null;
	protected double scaleX_ = 1.0f;
	protected double scaleY_ = 1.0f;

	protected boolean measureTime_ = false;
	protected long lastMSNeeded_ = 0;

	protected double rotationAngleDegree_ = 0;

	/**
	 * If true, clipping of the top-most view is applied.
	 */
	protected boolean enableClipping_ = true;

	/**
	 * Enable paint time measurement.
	 *
	 * @param measureTime true if time measurement shall be enabled.
	 */
	public void setTimeMeasurementEnabled(boolean measureTime)
	{
		this.measureTime_ = measureTime;
	}

	/**
	 * Get time in milliseconds of the last paint.
	 *
	 * @return time in milliseconds.
	 */
	public long getMeasuredTimeMS()
	{
		return lastMSNeeded_;
	}


	/**
	 * Checks if a rotation is in effect.
	 *
	 * @return true if rotation is set, false if not.
	 */
	protected boolean isRotationActive()
	{
		return (rotationAngleDegree_ < -0.1 || rotationAngleDegree_ > 0.1);
	}

	/**
	 * Gets the current rotation angle in degree.
	 *
	 * @return the rotation angle in degrees.
	 */
	public double getRotationAngleDegree()
	{
		return rotationAngleDegree_;
	}

	/**
	 * Sets the rotation angle.
	 *
	 * @param angleDegree New angle in degree.
	 */
	public void setRotationAngleDegree(double angleDegree)
	{
		if (angleDegree != rotationAngleDegree_)
		{
			rotationAngleDegree_ = angleDegree;
			areaTransformed_ = null;
		}
	}

	/**
	 * Get the current rotation angle as transform.
	 *
	 * @return The rotation transformation if rotation is active, null if rotation is off.
	 * @see #setRotationAngleDegree(double)
	 */
	protected AffineTransform getRotation()
	{
		if (isRotationActive())
			return AffineTransform.getRotateInstance(Math.toRadians(rotationAngleDegree_), area_.x + (area_.width / 2),
					area_.y + (area_.height / 2));
		else
			return null;
	}

	/**
	 * Ensures that members "area_" and "areaTransformed_" are available.
	 */
	protected void ensureArea()
	{
		if (area_ == null)
		{
			calculateArea();
			areaTransformed_ = null;
		}
		if (areaTransformed_ == null)
		{
			areaTransformed_ = new Rectangle2D.Double(0, 0, area_.width * scaleX_, area_.height * scaleY_);
		}
	}

	/**
	 * Enforce an update of the area.
	 */
	public void forceUpdateArea()
	{
		area_ = null;
	}

	/**
	 * Gets the covered area according to shape and scale.
	 *
	 * @return the area.
	 */
	public Rectangle2D.Double getArea()
	{
		ensureArea();
		return areaTransformed_;
	}

	/**
	 * Gets the width of the covered area.
	 *
	 * @return The width.
	 */
	public double getAreaWidth()
	{
		ensureArea();
		return areaTransformed_.width;
	}

	/**
	 * Gets the absolute height of the covered area.
	 *
	 * @return The height of the output area.
	 */
	public double getAreaHeight()
	{
		ensureArea();
		return areaTransformed_.height;
	}

	/**
	 * Sets X- and Y-Scale factor.
	 *
	 * @param scaleX The new X-scale.
	 * @param scaleY The new Y-scale.
	 */
	public void setScale(double scaleX, double scaleY)
	{
		scaleX_ = scaleX;
		scaleY_ = scaleY;
		areaTransformed_ = null;
	}

	/**
	 * Gets X-Scale factor.
	 *
	 * @return The current x-scale.
	 */
	public double getXScale()
	{
		return scaleX_;
	}

	/**
	 * Gets Y-Scale factor.
	 *
	 * @return The current y-scale.
	 */
	public double getYScale()
	{
		return scaleY_;
	}


	/**
	 * Checks if clipping of the top-most view-box is applied.
	 *
	 * @return true if clipping is enabled.
	 */
	public boolean isClippingEnabled()
	{
		return enableClipping_;
	}

	/**
	 * Sets clipping of the top-most view-box.
	 *
	 * @param enabled if true, clipping is enabled.
	 */
	public void setClippingEnabled(boolean enabled)
	{
		this.enableClipping_ = enabled;
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
	 * Paints the shape.
	 *
	 * @param g          Graphics, will not be changed.
	 * @param foreground The foreground paint to use.
	 * @param background The background paint to use.
	 * @param clearArea  If true the area of the shapes is cleared with the current color.
	 * @param toGray     If true all colors are converted to gray.
	 */
	public void paint(Graphics g, Paint foreground, Paint background, boolean clearArea, boolean toGray)
	{
		final long ms = (measureTime_) ? System.currentTimeMillis() : 0;
		Context ctx = new Context(g);
		try
		{
			if (area_ == null)
				calculateArea();
			ctx.currentColor_ = foreground;
			ctx.currentBackground_ = background;
			ctx.translateColor2Gray_ = toGray;
			paint(ctx, clearArea);
		}
		finally
		{
			ctx.dispose();
			if (measureTime_)
			{
				lastMSNeeded_ = System.currentTimeMillis() - ms;
			}
		}
	}

	/**
	 * Save the shape as PNG bitmap with the current scale.
	 *
	 * @param pngFile The File to store to. If extension is missing or empty, ".png" is added.
	 */
	public void saveAsImage(File pngFile)
	{
		saveAsImage(pngFile, false);
	}

	/**
	 * Save the shape as PNG bitmap with the current scale.
	 *
	 * @param pngFile The File to store to. If extension is missing or empty, ".png" is added.
	 * @param toGray  If true the image is rendered in gray-scale.
	 */
	public void saveAsImage(File pngFile, boolean toGray)
	{
		if (pngFile != null)
		{
			BufferedImage image = paintShapeToBufferTransparent(null, toGray);
			if (image != null)
			{
				try
				{
					String fileName = pngFile.getName();
					int i = fileName.lastIndexOf('.');
					if (i < 0 || i == fileName.length() - 1)
					{
						if (i < 0)
						{
							fileName += ".";
						}
						fileName += "png";
						String dir = pngFile.getParent();
						if (dir == null)
							dir = "";
						if (!dir.isEmpty())
							dir += File.separator;
						pngFile = new File(dir + fileName);
					}
					ImageIO.write(image, "png", pngFile);
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * Paints the shapes.
	 *
	 * @param ctx       Graphic context, will NOT be restored.
	 * @param clearArea If true the area of the shapes is cleared with the current color.
	 */
	protected abstract void paint(Context ctx, boolean clearArea);

	/**
	 * Implementations have to set member {@link #area_} to the covered area, without the rotation and scale of this instance.
	 */
	protected abstract void calculateArea();

	/**
	 * Sets the abstract shape to draw.
	 */
	public abstract void setShape(AbstractShape shape);

	/**
	 * Get the current shape.
	 */
	public abstract AbstractShape getShape();

}

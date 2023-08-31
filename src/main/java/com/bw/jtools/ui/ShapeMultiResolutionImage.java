package com.bw.jtools.ui;

import com.bw.jtools.shape.AbstractShape;
import com.bw.jtools.shape.ShapePainter;

import java.awt.Image;
import java.awt.geom.Rectangle2D;
import java.awt.image.AbstractMultiResolutionImage;
import java.awt.image.BufferedImage;
import java.util.Collections;
import java.util.List;
import java.util.WeakHashMap;

/**
 * See {@link java.awt.image.MultiResolutionImage} for details about the concept.<br>
 * This implementation is indented to be used as frame-icons. See {@link javax.swing.JFrame#setIconImage(Image)}.
 * Such images are used os-dependent in different sizes for the frame-icon, the task-bar e.t.c.<br>
 * This implementation creates the requested sizes from the SVG-shapes on the fly.<br>
 * The images are cached in a weak-hash-map.
 */
public class ShapeMultiResolutionImage extends AbstractMultiResolutionImage
{
	protected final ShapePainter painter_;

	protected BufferedImage defaultImage_;
	protected final WeakHashMap<String, BufferedImage> images_ = new WeakHashMap<>();
	protected boolean keepAspectRatio = true;

	protected synchronized BufferedImage getDefaultImage()
	{
		if (defaultImage_ == null)
		{
			defaultImage_ = painter_.paintShapeToBufferTransparent(null, false);
		}
		return defaultImage_;
	}

	/**
	 * Creates a multi-res image from a ShapePainter.
	 */
	public ShapeMultiResolutionImage(ShapePainter painter)
	{
		this.painter_ = painter;
	}

	/**
	 * Creates a multi-res image from SVG-Shapes.
	 */
	public ShapeMultiResolutionImage(AbstractShape shape)
	{
		this.painter_ = new ShapePainter(shape);
	}

	@Override
	public Image getResolutionVariant(double destImageWidth, double destImageHeight)
	{
		BufferedImage img;
		synchronized (this)
		{
			String key = String.format("%.3fx%.3f", destImageWidth, destImageHeight);
			img = images_.get(key);
			if (img == null)
			{
				painter_.setScale(1, 1);
				Rectangle2D area = painter_.getArea();
				double scaleX = destImageWidth / area.getWidth();
				double scaleY = destImageHeight / area.getHeight();
				if (keepAspectRatio)
				{
					double scale = Math.min(scaleX, scaleY);
					painter_.setScale(scale, scale);
				}
				else
				{
					painter_.setScale(scaleX, scaleY);
				}
				img = painter_.paintShapeToBufferTransparent(null, false);
				images_.put(key, img);
			}
		}
		return img;
	}

	@Override
	public List<Image> getResolutionVariants()
	{
		return Collections.singletonList(getDefaultImage());
	}


	@Override
	protected Image getBaseImage()
	{
		return getDefaultImage();
	}
}

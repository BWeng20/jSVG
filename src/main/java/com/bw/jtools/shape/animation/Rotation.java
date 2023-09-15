package com.bw.jtools.shape.animation;

import com.bw.jtools.shape.AbstractShape;

import java.awt.geom.Point2D;

/**
 * Animation that applies an additional rotation-transformation on the shape.
 */
public class Rotation extends Animation
{

	/**
	 * Creates a new rotation aniumation.
	 *
	 * @param type    The type of animation.
	 * @param centerX The x-coordinate of the center in userspace units of the shape.
	 * @param centerY The y-coordinate of the center in userspace units of the shape.
	 */
	public Rotation(AnimationType type, double centerX, double centerY)
	{
		super(type);
		centerX_ = centerX;
		centerY_ = centerY;
	}

	private double centerX_;
	private double centerY_;

	protected void apply(AbstractShape shape)
	{
		if (value_ == value_)
		{
			shape.aft_.rotate(value_, centerX_, centerY_);
		}
	}

	/**
	 * Sets the coordinates of the center in userspace units of the shape.
	 *
	 * @param centerX The x-coordinate.
	 * @param centerY The y-coordinate.
	 */
	public void setCenter(double centerX, double centerY)
	{
		this.centerX_ = centerX;
		this.centerY_ = centerY;
	}

	/**
	 * Sets the coordinates of the center in userspace units of the shape.
	 *
	 * @param center The coordinates.
	 */
	public void setCenter(Point2D.Double center)
	{
		this.centerX_ = center.x;
		this.centerY_ = center.y;
	}


	/**
	 * Gets the coordinates of the center in userspace units of the shape.
	 *
	 * @return The coordinates.
	 */
	public Point2D.Double getCenter()
	{
		return new Point2D.Double(centerX_, centerY_);
	}

}

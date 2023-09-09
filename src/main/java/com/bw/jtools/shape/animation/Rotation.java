package com.bw.jtools.shape.animation;

import com.bw.jtools.shape.AbstractShape;

import java.awt.geom.Point2D;

public class Rotation extends Animation
{

	public Rotation(AnimationType type, double centerX, double centerY)
	{
		super(type);
		centerX_ = centerX;
		centerY_ = centerY;
	}

	public double getCenterX_()
	{
		return centerX_;
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

	public void setCenter(double centerX, double centerY)
	{
		this.centerX_ = centerX;
		this.centerY_ = centerY;
	}

	public void setCenter(Point2D.Double center)
	{
		this.centerX_ = center.x;
		this.centerY_ = center.y;
	}


	public Point2D.Double getCenter()
	{
		return new Point2D.Double(centerX_, centerY_);
	}

}

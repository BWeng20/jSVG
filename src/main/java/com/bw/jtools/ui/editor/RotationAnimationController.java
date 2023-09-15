package com.bw.jtools.ui.editor;

import com.bw.jtools.shape.animation.AnimationType;
import com.bw.jtools.shape.animation.FillMode;
import com.bw.jtools.shape.animation.Rotation;

/**
 * Controller for rotation animation.
 * Adapts the rotation angle to keep the value inside 360°.
 */
public class RotationAnimationController extends AbstractAnimationController<Rotation>
{
	private double angle_ = 0;

	/**
	 * Creates a new Rotation animation controller
	 * @param name The user-friendly name of the animation.
	 */
	public RotationAnimationController(String name)
	{
		super(name, new Rotation(AnimationType.Spring, 0, 0));
	}

	@Override
	protected RotationAnimationOptionPanel createOptionPanel()
	{
		return new RotationAnimationOptionPanel(this);
	}

	@Override
	public void setTargetValue(double angle, long animationTime)
	{
		angle %= 360;
		if (angle_ > 360)
		{
			angle_ %= 360;
			animation_.setValue(Math.toRadians(angle_));
		}
		angle_ = (angle_ <= angle) ? angle : 360 + angle;
		updateAnimation(animationTime);
	}

	protected void updateAnimation(long animationTime)
	{
		// If the current value cross 360° we can adapt the range to stay in [0..360°].
		double value = animation_.getValue();
		double rad = Math.toRadians(angle_);
		if (value != rad)
		{
			animation_.setAnimation(animationTime, rad, FillMode.Freeze);
		}
	}


}

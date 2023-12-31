package com.bw.jtools.ui.editor;

import com.bw.jtools.shape.AbstractShape;
import com.bw.jtools.shape.animation.Animation;
import com.bw.jtools.shape.animation.Animator;

/**
 * Abstract base for Animation-Controllers.
 * Applies values and provide an option user-interface.
 */
public abstract class AbstractAnimationController<T extends Animation>
{

	/**
	 * The shared root of the shape-hierarchy.
	 */
	protected AbstractShape shape_;

	/**
	 * The shared animator that manages the animations.
	 */
	protected Animator animator_;

	/**
	 * The option panel to show the controls.
	 */
	protected AbstractAnimationOptionPanel optionPane_;

	/**
	 * The user-friendly name of the animation.
	 */
	protected String name_;

	/**
	 * The animation to control.
	 */
	protected T animation_;

	/**
	 * To be called by implementations to set user-friendly name and the specific animation.
	 *
	 * @param name      The user-friendly name.
	 * @param animation The animation instance to use.
	 */
	protected AbstractAnimationController(String name, T animation)
	{
		this.name_ = name;
		this.animation_ = animation;
	}

	/**
	 * Gets the option UI for this animation.
	 *
	 * @return The panel, never null.
	 */
	public AbstractAnimationOptionPanel getOptionPane()
	{
		if (optionPane_ == null)
		{
			optionPane_ = createOptionPanel();
		}
		return optionPane_;
	}

	/**
	 * Implementation have to return a new instance of the option-UI.
	 *
	 * @return The panel to show the options.
	 */
	protected abstract AbstractAnimationOptionPanel createOptionPanel();

	/**
	 * Sets a new shape and the associated animator.
	 *
	 * @param shape    The shape to handle.
	 * @param animator THe animator.
	 */
	public void setShape(AbstractShape shape, Animator animator)
	{
		shape_ = shape;

		if (animator_ != null)
			animator_.stop();

		if (animator_ != animator)
		{
			animator_ = animator;
		}

		if (optionPane_ != null)
			optionPane_.setShape(shape_);
	}

	/**
	 * Sets a new animations target value.
	 *
	 * @param value         The new target value.
	 * @param animationTime The time the animation shall run.
	 */
	public abstract void setTargetValue(double value, long animationTime);

	/**
	 * Gets the shape.
	 *
	 * @return The current shape.
	 */
	public AbstractShape getShape()
	{
		return shape_;
	}

	/**
	 * Gets the animator.
	 *
	 * @return The current Animator. Can be null.
	 */
	public Animator getAnimator()
	{
		return animator_;
	}

	/**
	 * Gets the animation.
	 *
	 * @return The animation. Never null.
	 */
	public T getAnimation()
	{
		return animation_;
	}

}

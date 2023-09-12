package com.bw.jtools.shape.animation;

import com.bw.jtools.shape.AbstractShape;

import java.time.LocalTime;

/**
 * Abstract base class for animations.
 */
public abstract class Animation
{
	/**
	 * Creates a new animation object.
	 * Uses by implementations to initialize type and defaults.
	 */
	protected Animation(AnimationType type)
	{
		type_ = type;
		setSpringDamping(3);
		setSpringFrequency(20);
	}

	/**
	 * Needs to be implemented by actual animations to apply the value to the target attribute.
	 *
	 * @param shape The shape to apply to.
	 */
	protected abstract void apply(AbstractShape shape);


	/**
	 * Sets the damping value for spring animation.
	 * Has no effect if another type is set.
	 */
	public void setSpringDamping(double damping)
	{
		springDamping_ = Math.pow(20, damping) / 10.0;
	}

	/**
	 * Sets the frequency value for spring animation.
	 * Has no effect if another type is set.
	 */
	public void setSpringFrequency(double frequency)
	{
		springFrequency = Math.max(1, frequency);
	}

	protected double spring(double time)
	{
		return 1 - (Math.pow(springDamping_, -time) * (1 - time) * Math.cos(springFrequency * time));
	}

	protected double linear(double time)
	{
		return time;
	}

	/**
	 * Sets the animation to the time-frame.
	 *
	 * @param time The current time frame.
	 * @return true if some value was changed (and a repaint is needed).
	 */
	public boolean tick(long time)
	{
		long endTime = startTime_ + durationMS_;
		if (startTime_ <= time)
		{
			if (endTime > time)
			{
				double timeRelative = (((double) (time - startTime_)) / (endTime - startTime_));
				double v;
				switch (type_)
				{
					case Spring:
						v = spring(timeRelative);
						break;
					default:
						v = linear(timeRelative);
						break;
				}
				v = startValue_ + (v * (targetValue_ - startValue_));
				if (v != value_)
				{
					value_ = v;
					return true;
				}
			}
			else
			{
				if (fill_ == FillMode.Freeze)
				{
					if (value_ != targetValue_)
					{
						value_ = targetValue_;
						return true;
					}
				}
				else if (value_ == value_)
				{
					value_ = Double.NaN;
					return true;
				}
			}
		}
		else if (value_ != startValue_)
		{
			// Check for NaN
			if (!(value_ != value_ && startValue_ != startValue_))
			{
				value_ = startValue_;
				return true;
			}
		}
		return false;
	}

	/**
	 * Sets animation values.
	 *
	 * @param durationMS  The time in milliseconds the animation shall run.
	 * @param targetValue The target value that shall be reached at the end.
	 * @param fill        What shall happen if the animation is finished.
	 */
	public void setAnimation(long durationMS, double targetValue, FillMode fill)
	{
		durationMS_ = durationMS;
		targetValue_ = targetValue;
		fill_ = fill;
		start();
	}

	/**
	 * Gets the current value.
	 */
	public double getValue()
	{
		return value_;
	}

	/**
	 * Sets the current value.
	 */
	public void setValue(double value)
	{
		value_ = value;
	}

	/**
	 * Sets the type of animation.
	 * If called during an animation the value will "jump" to the new position as if the type was active from start.
	 *
	 * @param type The new type.
	 */
	public void setType(AnimationType type)
	{
		type_ = type;
	}


	/**
	 * Initialize start-time and start-value.
	 */
	public void start()
	{
		startTime_ = System.currentTimeMillis();
		startValue_ = value_;
		if (startValue_ != startValue_)
			startValue_ = 0;
	}

	private long startTime_;
	private long durationMS_;

	private double startValue_;
	private double targetValue_;

	protected double value_;

	private FillMode fill_;

	private double springDamping_;
	private double springFrequency;

	private AnimationType type_;

}

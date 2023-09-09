package com.bw.jtools.shape.animation;

import com.bw.jtools.shape.AbstractShape;

import javax.swing.SwingUtilities;
import javax.swing.Timer;
import java.awt.Component;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Animation manager.
 */
public class Animator
{

	protected final AbstractShape shape_;
	protected final Component component_;
	protected Timer timer_;

	protected int timerTick_ = 20;

	protected final Map<String, AnimationItem> animations_ = new HashMap<>();

	private static class AnimationItem
	{

		public List<Animation> animation_ = new ArrayList<>();
		public final AbstractShape shape_;
		public final AffineTransform orgAft_;

		public AnimationItem(AbstractShape shape)
		{
			shape_ = shape;
			orgAft_ = (shape_.aft_ == null) ? new AffineTransform() : new AffineTransform(shape_.aft_);
		}
	}

	public Animator(Component comp, AbstractShape shape)
	{
		shape_ = shape;
		component_ = comp;
	}

	/**
	 * Adds an animation.
	 *
	 * @param id        The uniqe id of the shape to animate.
	 * @param animation The animation.
	 */
	public void addAnimation(String id, Animation animation)
	{
		AbstractShape toAnimate = shape_.getShapeById(id);
		if (toAnimate != null)
		{
			AnimationItem item = animations_.computeIfAbsent(id, i -> new AnimationItem(toAnimate));
			item.animation_.add(animation);
		}
	}

	/**
	 * Remove an animation and resets the shape to original state.
	 *
	 * @param id        The unique id of the shape that is animated.
	 * @param animation The animation.
	 * @return true if the animation was found and removed.
	 */
	public boolean removeAnimation(String id, Animation animation)
	{
		AnimationItem item = animations_.get(id);
		if (item != null)
		{
			boolean found = item.animation_.remove(animation);
			if (found)
			{
				if (item.shape_.aft_ != null)
				{
					// @TODO: restore ....
					item.shape_.aft_.setTransform(item.orgAft_);
					for (Animation a : item.animation_)
					{
						a.apply(item.shape_);
					}
					SwingUtilities.invokeLater(component_::repaint);
				}
			}
			return found;
		}
		return false;
	}

	public void stop()
	{
		if (timer_ != null)
		{
			timer_.stop();
			timer_ = null;
		}
	}


	/**
	 * (Re-)Starts the animations.
	 */
	public void start()
	{
		stop();

		for (AnimationItem i : animations_.values())
			i.animation_.forEach(Animation::start);

		timer_ = new Timer(timerTick_, e ->
		{
			long nextTick;
			boolean repaint = false;
			long tick = System.currentTimeMillis();
			for (Map.Entry<String, AnimationItem> i : animations_.entrySet())
			{
				AnimationItem s = i.getValue();
				for (Animation a : s.animation_)
				{
					if (a.tick(tick))
					{
						repaint = true;
					}
				}
				if (repaint)
				{
					if (s.shape_.aft_ == null)
						s.shape_.aft_ = new AffineTransform(s.orgAft_);
					else
						s.shape_.aft_.setTransform(s.orgAft_);
					s.animation_.forEach(a -> a.apply(s.shape_));
				}

			}
			if (repaint)
				component_.repaint();
			nextTick = timerTick_ - (System.currentTimeMillis() - tick);
			if (nextTick <= 0)
				nextTick = 1;
			timer_.setDelay((int) nextTick);
		});
		timer_.start();
	}

	public boolean isRunning()
	{
		return timer_ != null && timer_.isRunning();
	}

}

package com.bw.jtools.shape.animation;

import com.bw.jtools.shape.AbstractShape;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import java.awt.Component;
import java.awt.Toolkit;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Animation manager.<br>
 * Manages several animations on one shape-hierarchy.
 */
public class Animator
{

	/**
	 * The root of the shape-hierarchy to animate.
	 */
	protected final AbstractShape shape_;

	/**
	 * The component that display the shapes.
	 */
	protected final Component component_;

	/**
	 * Timer to create animation-frames.
	 */
	protected int animationThreadId_;

	/**
	 * The default timer-delay.
	 */
	protected int timerTick_ = 20;

	private static int animatorCount_ = 0;


	/**
	 * Map of shape-ids to animations.
	 */
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

	/**
	 * Creates a new animation.
	 *
	 * @param comp  The component that displays the shape.
	 * @param shape The shape-root.
	 */
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
			return item.animation_.remove(animation);
		}
		return false;
	}

	/**
	 * Stops the animation. Value will keep the current value.
	 */
	public synchronized void stop()
	{
		animationThreadId_ = 0;
	}


	/**
	 * (Re-)Starts the animations.
	 */
	public synchronized void start()
	{
		stop();

		for (AnimationItem i : animations_.values())
			i.animation_.forEach(Animation::start);

		do
		{
			animationThreadId_ = ++animatorCount_;
		} while (animationThreadId_ == 0);
		final int animatorId = animationThreadId_;


			Thread animationThread = new Thread(() ->
			{
				while (animatorId == animationThreadId_)
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
					{
						SwingUtilities.invokeLater(() ->
						{
							if ( component_ instanceof JComponent)
							{
								((JComponent) component_).paintImmediately(0, 0, component_.getWidth(), component_.getHeight());
								Toolkit.getDefaultToolkit()
									   .sync();
							}
							else
								component_.repaint();
						});
					}
					nextTick = timerTick_ - (System.currentTimeMillis() - tick);
					if (nextTick <= 0)
						nextTick = 1;
					try
					{
						Thread.sleep((int) nextTick);
					}
					catch (Exception e)
					{
					}
				}
				;
			}, "Animator-" + animatorId);
			animationThread.start();
	}

	/**
	 * Check if the animation is still running.
	 *
	 * @return True if the animation is still running.
	 */
	public boolean isRunning()
	{
		return 0 != animationThreadId_;
	}

}

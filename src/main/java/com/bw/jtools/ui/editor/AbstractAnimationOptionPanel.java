package com.bw.jtools.ui.editor;

import com.bw.jtools.shape.AbstractShape;

import javax.swing.JPanel;
import java.awt.LayoutManager;

/**
 * Abstract base class for Option-UIs for Animations.
 *
 * @see AbstractAnimationController#getOptionPane()
 */
public abstract class AbstractAnimationOptionPanel<T extends AbstractAnimationController> extends JPanel
{
	protected T ctrl_;

	/**
	 * Called by implementations. Calls {@link #createUI()} and {@link #setShape(AbstractShape)}.
	 *
	 * @param layout The layout to use (for JPanel c'tor).
	 * @param ctrl   The Controller to bind.
	 */
	protected AbstractAnimationOptionPanel(LayoutManager layout, T ctrl)
	{
		super(layout);

		ctrl_ = ctrl;

		// @TODO: Calling virtual methods from base class. Bad idea.
		createUI();
		setShape(ctrl_.getShape());
	}

	/**
	 * To be implemented to create the UI elements.<br>
	 * Remind that members of the implementing class will be zero-initialized but c'tor is not finished if called.
	 */
	protected abstract void createUI();

	/**
	 * Sets a new shape. E.g. to fill the id-combos.<br>
	 * Remind that members of the implementing class will be zero-initialized but c'tor is possible not finished if called.
	 * @param shape The new shape to use.
	 */
	public abstract void setShape(AbstractShape shape);

}

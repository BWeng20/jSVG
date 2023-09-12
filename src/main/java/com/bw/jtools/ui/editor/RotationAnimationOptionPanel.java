package com.bw.jtools.ui.editor;

import com.bw.jtools.shape.AbstractShape;
import com.bw.jtools.shape.ShapeGroup;
import com.bw.jtools.shape.animation.AnimationType;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTextField;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.Objects;

/**
 * Options-UI for a Rotation Animation.
 *
 * @see RotationAnimationController#getOptionPane()
 */
class RotationAnimationOptionPanel extends AbstractAnimationOptionPanel<RotationAnimationController>
{
	protected JComboBox<String> shapeIds_;
	protected JTextField centerX_;
	protected JTextField centerY_;
	protected JComboBox<AnimationType> type_;
	protected String lastId_;

	/**
	 * Creates a new Panel.
	 *
	 * @see RotationAnimationController#createOptionPanel()
	 */
	protected RotationAnimationOptionPanel(RotationAnimationController ctrl)
	{
		super(new GridBagLayout(), ctrl);
	}

	@Override
	protected void createUI()
	{
		shapeIds_ = new JComboBox<>();
		shapeIds_.setToolTipText("Available Shape-Ids.");

		JLabel lt = new JLabel("Type");
		type_ = new JComboBox(AnimationType.values());
		type_.setSelectedItem(AnimationType.Spring);
		lt.setLabelFor(type_);

		centerX_ = new JTextField(10);
		centerX_.setText("0");
		JLabel lx = new JLabel("X");
		lx.setLabelFor(centerX_);

		JLabel ly = new JLabel("Y");
		centerY_ = new JTextField(10);
		centerY_.setText("0");
		ly.setLabelFor(centerY_);

		GridBagConstraints gc = new GridBagConstraints();
		gc.fill = GridBagConstraints.HORIZONTAL;
		gc.anchor = GridBagConstraints.WEST;
		gc.weighty = 0;
		gc.gridx = 0;
		gc.gridy = 0;
		gc.gridwidth = 2;
		gc.gridheight = 1;
		add(shapeIds_, gc);
		gc.gridy++;
		gc.gridwidth = 1;
		add(lt, gc);
		gc.gridx++;
		add(type_, gc);

		gc.gridy++;
		gc.gridx = 0;
		add(lx, gc);
		gc.gridx = 1;
		add(centerX_, gc);
		gc.gridy = 4;
		gc.gridx = 0;
		add(ly, gc);
		gc.gridx = 1;
		add(centerY_, gc);

		gc.fill = GridBagConstraints.VERTICAL;
		gc.gridy++;
		gc.weighty = 1;
		add(new JLabel(""), gc);

		shapeIds_.addActionListener(a -> updateSettings());
		centerX_.addActionListener(a -> updateSettings());
		centerY_.addActionListener(a -> updateSettings());
		type_.addActionListener(a -> updateSettings());
	}

	private void updateSettings()
	{
		try
		{
			double x = Double.parseDouble(centerX_.getText());
			double y = Double.parseDouble(centerY_.getText());
			ctrl_.getAnimation()
				 .setCenter(x, y);
		}
		catch (Exception e)
		{
		}

		String id = (String) shapeIds_.getSelectedItem();
		if (!Objects.equals(id, lastId_))
		{
			// Selected shape has changed. Remove old and add a new animation.
			if (lastId_ != null)
			{
				ctrl_.getAnimator()
					 .removeAnimation(lastId_, ctrl_.getAnimation());
				lastId_ = null;
			}
			if (id != null && !id.isEmpty())
			{
				ctrl_.getAnimator()
					 .addAnimation(id, ctrl_.getAnimation());
				lastId_ = id;
			}
		}

		ctrl_.getAnimation()
			 .setType((AnimationType) type_.getSelectedItem());
		ctrl_.updateAnimation(500);
	}


	/**
	 * Sets a new shape. Mainly to fill the id-combo.
	 */
	public void setShape(AbstractShape shape)
	{
		shapeIds_.removeAllItems();
		shapeIds_.addItem("");
		if (shape != null)
			addShapeInfo(shape);
		lastId_ = null;
	}

	protected void addShapeInfo(AbstractShape shape)
	{
		if (shape.id_ != null && !shape.id_.isEmpty())
		{
			shapeIds_.addItem(shape.id_);
			if (shape instanceof ShapeGroup)
			{
				for (AbstractShape s : ((ShapeGroup) shape).shapes_)
				{
					addShapeInfo(s);
				}
			}
		}
	}
}

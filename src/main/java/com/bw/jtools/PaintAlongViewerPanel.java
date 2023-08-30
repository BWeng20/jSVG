package com.bw.jtools;

import com.bw.jtools.shape.AbstractPainterBase;
import com.bw.jtools.shape.AbstractShape;
import com.bw.jtools.shape.PaintAlongShapePainter;
import com.bw.jtools.ui.ShapePane;

import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Shape;

/**
 * Demonstration- and Test-Utility draw SVG files along shapes.
 */
public class PaintAlongViewerPanel extends JPanel
{

	protected ShapePane paintAlongViewerDrawPane_;
	protected JScrollPane paintAlongScrollPane_;
	protected PaintAlongShapePainter paintAlongViewerPainter_;

	protected JSlider distance_;

	/**
	 * Create a new SVGViewer.
	 * Caller has to call "pack" and "setVisible".
	 *
	 * @param file The file to show or null.
	 */
	public PaintAlongViewerPanel()
	{
		paintAlongViewerDrawPane_ = new ShapePane();
		paintAlongViewerPainter_ = new PaintAlongShapePainter();
		paintAlongViewerPainter_.setPaintOutlines(true);
		paintAlongViewerDrawPane_.setPainter(paintAlongViewerPainter_);
		paintAlongViewerDrawPane_.setZoomByMetaMouseWheelEnabled(true);
		paintAlongViewerDrawPane_.setMouseDragEnabled(true);
		paintAlongViewerDrawPane_.setRotateByShiftMouseWheelEnabled(true);
		paintAlongViewerDrawPane_.setContextMenuEnabled(true);
		paintAlongViewerDrawPane_.setInlineBorder(true);
		paintAlongViewerDrawPane_.setScale(1, 1);
		paintAlongViewerDrawPane_.setOpaque(false);

		paintAlongScrollPane_ = new JScrollPane(paintAlongViewerDrawPane_);

		setLayout(new BorderLayout());
		add(BorderLayout.CENTER, paintAlongScrollPane_);

		JPanel optionPane = new JPanel(new GridBagLayout());

		GridBagConstraints gc = new GridBagConstraints();
		gc.fill = GridBagConstraints.VERTICAL;
		gc.anchor = GridBagConstraints.NORTHWEST;
		gc.weighty = 1;
		gc.gridx = 0;
		gc.gridy = 0;
		gc.gridwidth = 1;
		gc.gridheight = 1;

		distance_ = new JSlider(JSlider.VERTICAL, -100, 100, 0);
		distance_.setToolTipText("Additional offset between tiles.");
		distance_.setMajorTickSpacing(10);
		distance_.setMinorTickSpacing(1);
		distance_.setPaintTicks(true);
		distance_.setPaintLabels(true);
		distance_.addChangeListener(e ->
		{
			paintAlongViewerPainter_.setDistanceOffset(distance_.getValue());
			repaint();
		});

		JSlider start = new JSlider(JSlider.VERTICAL, 0, 200, 0);
		start.setToolTipText("Start offset on path");
		start.setMajorTickSpacing(10);
		start.setMinorTickSpacing(1);
		start.setPaintTicks(true);
		start.setPaintLabels(true);
		start.addChangeListener(e ->
		{
			paintAlongViewerPainter_.setStartOffset(start.getValue());
			repaint();
		});

		JSlider end = new JSlider(JSlider.VERTICAL, -200, 0, 0);
		end.setToolTipText("End offset on path. Negative values count from end of path.");
		end.setMajorTickSpacing(10);
		end.setMinorTickSpacing(1);
		end.setPaintTicks(true);
		end.setPaintLabels(true);
		end.addChangeListener(e ->
		{
			paintAlongViewerPainter_.setEndOffset(end.getValue());
			repaint();
		});

		JCheckBox showOutlines = new JCheckBox("Outlines");
		showOutlines.setSelected(true);
		showOutlines.addChangeListener(e ->
		{
			paintAlongViewerPainter_.setPaintOutlines(showOutlines.isSelected());
			repaint();
		});

		JCheckBox overlapped = new JCheckBox("Overlapped");
		overlapped.setSelected(true);
		overlapped.addChangeListener(e ->
		{
			paintAlongViewerPainter_.setPaintOverlapped(overlapped.isSelected());
			repaint();
		});


		JCheckBox autoScale = new JCheckBox("Autoscale");

		optionPane.add(distance_, gc);
		gc.gridx = 1;
		optionPane.add(start, gc);
		gc.gridx = 2;
		optionPane.add(end, gc);
		gc.gridy = GridBagConstraints.RELATIVE;
		gc.gridx = 0;
		gc.weighty = 0;
		gc.gridwidth = 3;
		optionPane.add(showOutlines, gc);
		optionPane.add(overlapped, gc);
		// optionPane.add(autoScale,gc);

		add(BorderLayout.EAST, optionPane);
		setPreferredSize(new Dimension(600, 600));
	}

	public void setTileShape(AbstractShape shape)
	{
		paintAlongViewerDrawPane_.setShape(shape);
		updateSliders();
	}

	public void setTilePainter(AbstractPainterBase painter)
	{
		paintAlongViewerPainter_.setTilePainter(painter);
		updateSliders();
	}

	public void addOutline(Shape outline)
	{
		paintAlongViewerPainter_.addPath(outline);
	}

	public void updateTilePainter()
	{
		paintAlongViewerPainter_.forceUpdateArea();
		updateSliders();
		paintAlongViewerDrawPane_.repaint();
	}

	private void updateSliders()
	{
		double w = paintAlongViewerPainter_.getTilePainter()
										   .getAreaWidth();
		distance_.setMinimum(-((int) w - 1));
	}


}

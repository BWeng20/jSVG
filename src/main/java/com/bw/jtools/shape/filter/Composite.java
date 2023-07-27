package com.bw.jtools.shape.filter;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.List;

/**
 * Combination of two input images using a Porter-Duff compositing operator.
 */
public class Composite extends FilterBaseSingleSource
{
	public CompositeOperator operator_;
	public List<Double> k_;

	@Override
	protected void render(PainterBuffers buffers, String targetName, BufferedImage src, BufferedImage target, double scaleX, double scaleY)
	{
		Graphics2D g2d = (Graphics2D) target.getGraphics();
		try
		{
			// @TODO Implement this filter
			g2d.drawImage(src, 0, 0, null);

			// @TODO Removed debugging stuff if finished.
			g2d.setColor(Color.ORANGE);
			g2d.drawRect(0, 0, src.getWidth(), src.getHeight());

		}
		finally
		{
			g2d.dispose();
		}
	}

	public Composite(String source, String target,
					 CompositeOperator operator,
					 List<Double> k)
	{
		super(source, target);
		operator_ = operator;
		k_ = k;
	}
}


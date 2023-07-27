package com.bw.jtools.shape.filter;

import com.bw.jtools.svg.MergeFilterNode;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * Merges sources by painting them in the specified order to the target buffer.
 */
public class Merge extends FilterBase
{

	public Merge(List<MergeFilterNode> nodes, String target)
	{
		super(null, target);

		for (MergeFilterNode n : nodes)
		{
			source_.add(n.in_);
		}
	}

	@Override
	protected void render(PainterBuffers buffers, String targetName, List<BufferedImage> src, BufferedImage target, double scaleX, double scaleY)
	{
		Graphics2D g2d = (Graphics2D) target.getGraphics();
		try
		{
			for (BufferedImage s : src)
			{
				g2d.drawImage(s, 0, 0, null);
			}
		}
		finally
		{
			g2d.dispose();
		}
	}
}


package com.bw.jtools.shape.filter;

import com.bw.jtools.shape.Context;
import com.bw.jtools.svg.MergeFilterNode;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.List;

/**
 * Moves the source.
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
	protected void render(PainterBuffers buffers, String targetName, List<BufferedImage> srcs, BufferedImage target, double scaleX, double scaleY)
	{

		Graphics2D g = target.createGraphics();
		Context.initGraphics(g);
		try
		{
			for (BufferedImage src : srcs)
			{
				g.drawImage(src, 0, 0, null);
			}
		}
		finally
		{
			g.dispose();
		}

	}
}


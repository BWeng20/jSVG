package com.bw.jtools.svg;

import java.util.ArrayList;
import java.util.List;

public final class LengthList extends Parser
{
	List<Length> lengthList_ = new ArrayList<>(4);

	public LengthList(String list)
	{
		super(list);
		Length l;
		do
		{
			l = nextLengthPercentage();
			if (l != null)
				lengthList_.add(l);
		} while (l != null);
	}

	public List<Length> getLengthList()
	{
		return lengthList_;
	}

	public float[] toFloatPixel(Double absValue)
	{
		final int N = lengthList_.size();
		float fa[] = new float[N];
		for (int i = 0; i < N; ++i)
		{
			fa[i] = (float) lengthList_.get(i)
									   .toPixel(absValue);
		}
		return fa;
	}

	public boolean isEmpty()
	{
		return lengthList_.isEmpty();
	}
}

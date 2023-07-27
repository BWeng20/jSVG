package com.bw.jtools.svg;

import java.util.List;

/**
 * Gaussian blur filter primitive.
 */
public class GaussianBlurFilterPrimitive extends FilterPrimitive
{
	public final List<Double> stdDeviation_;

	@Override
	public int numberOfInputs()
	{
		return 1;
	}

	public GaussianBlurFilterPrimitive(List<Double> stdDeviation)
	{
		super(Type.feGaussianBlur);
		this.stdDeviation_ = stdDeviation;
	}
}

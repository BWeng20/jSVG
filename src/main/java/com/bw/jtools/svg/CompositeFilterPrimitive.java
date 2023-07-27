package com.bw.jtools.svg;

import com.bw.jtools.shape.filter.CompositeOperator;

import java.util.List;

/**
 * Composite filter primitive.
 */
public class CompositeFilterPrimitive extends FilterPrimitive
{
	public final CompositeOperator operator_;

	public final List<Double> k_;

	@Override
	public int numberOfInputs()
	{
		return 2;
	}

	public CompositeFilterPrimitive(CompositeOperator operator, List<Double> k)
	{
		super(Type.feComposite);
		this.operator_ = operator;
		this.k_ = k;
	}
}

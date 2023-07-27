package com.bw.jtools.svg;

import com.bw.jtools.shape.filter.LightSource;

/**
 * Specular-Lighting filter primitive.
 */
public class SpecularLightingFilterPrimitive extends FilterPrimitive
{
	public final double surfaceScale_;
	public final double specularConstant_;
	public final double specularExponent_;
	public final Double dx_;
	public final Double dy_;

	public final LightSource light_;

	public SpecularLightingFilterPrimitive(double surfaceScale, double specularConstant, double specularExponent, Double dx, Double dy, LightSource light)
	{
		super(Type.feSpecularLighting);
		surfaceScale_ = surfaceScale;
		specularConstant_ = specularConstant;
		specularExponent_ = specularExponent;
		dx_ = dx;
		dy_ = (dy == null) ? dx : dy;
		light_ = light;
	}

	@Override
	public int numberOfInputs()
	{
		return 1;
	}

}

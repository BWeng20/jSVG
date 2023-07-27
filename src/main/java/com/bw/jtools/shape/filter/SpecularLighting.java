package com.bw.jtools.shape.filter;

import java.awt.Color;
import java.awt.image.BufferedImage;

public class SpecularLighting extends FilterBaseSingleSource
{

	public double surfaceScale_;
	public double specularConstant_;
	public double specularExponent_;
	public Double dx_;
	public Double dy_;

	public Color color_;

	public LightSource light_;

	@Override
	protected void render(PainterBuffers buffers, String targetName, BufferedImage src, BufferedImage target, double scaleX, double scaleY)
	{

		// @TODO: implement this crazy filter. Ask mathematically gifted relatives for this!
		src.copyData(target.getRaster());
	}

	public SpecularLighting(String source, String target,
							double surfaceScale,
							double specularConstant,
							double specularExponent,
							Double dx, Double dy,
							LightSource light)
	{
		super(source, target);

		surfaceScale_ = surfaceScale;
		specularConstant_ = specularConstant;
		specularExponent_ = specularExponent;
		dx_ = dx;
		dy_ = dy;
		light_ = light;
	}


}




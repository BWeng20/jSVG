package com.bw.jtools.svg;

import java.util.HashMap;

public enum Type
{
	g,
	clipPath,
	path, rect, circle, ellipse,
	text, textPath,
	line, polyline, polygon,
	use, style,
	defs, linearGradient, radialGradient,
	filter, marker,

	title,
	desc,

	// Sub elements for feComponentTransfer
	feFuncA, feFuncB, feFuncG, feFuncR,

	// Sub element for feMerge
	feMergeNode,

	// Filter primitive elements
	feBlend, feColorMatrix, feComponentTransfer,
	feComposite, feConvolveMatrix, feDiffuseLighting,
	feDisplacementMap, feDropShadow, feFlood,
	feGaussianBlur, feImage, feMerge, feMorphology, feOffset, feSpecularLighting,
	feTile, feTurbulence,

	// Sub elements of feDiffuseLighting and feSpecularLighting
	feDistantLight, fePointLight, feSpotLight;


	private final static HashMap<String, Type> types_ = new HashMap<>();

	// Use map instead of "valueOf" to avoid exceptions for unknown values
	static
	{
		for (Type t : values())
			types_.put(t.name(), t);
	}

	public static Type valueFrom(String typeName)
	{
		return types_.get(typeName);
	}
}

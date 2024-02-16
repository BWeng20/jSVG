package com.bw.jtools.svg;

import java.util.HashMap;

/**
 * SVG XML Tag Types used.
 */
public enum SvgTagType
{
	/**
	 * A Group. See <a href="https://www.w3.org/TR/SVG2/struct.html#Groups">W3C Groups</a>
	 */
	g,

	/**
	 * A external link. See <a href="https://www.w3.org/TR/SVG2/linking.html#Links">W3C Linking</a>
	 */
	a,
	clipPath,
	path, rect, circle, ellipse,
	text, textPath, tspan,
	line, polyline, polygon,
	use, style,

	/**
	 * Definitions,  See <a href="https://www.w3.org/TR/SVG2/struct.html#Head">W3C Defs Element</a>
	 */
	defs,
	linearGradient, radialGradient,
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
	feNop,
	feTile, feTurbulence,

	// Sub elements of feDiffuseLighting and feSpecularLighting
	feDistantLight, fePointLight, feSpotLight,
	metadata,

	/**
	 *  Pattern definition, see <a href="https://www.w3.org/TR/SVG2/pservers.html#PatternElement">W3C Pattern Element</a>
	 */
	pattern;

	private final static HashMap<String, SvgTagType> types_ = new HashMap<>();

	// Use map instead of "valueOf" to avoid exceptions for unknown values
	static
	{
		for (SvgTagType t : values())
			types_.put(t.name(), t);
	}

	public static SvgTagType valueFrom(String typeName)
	{
		return types_.get(typeName);
	}
}

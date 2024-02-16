package com.bw.jtools.svg;

import java.util.HashMap;

public enum Attribute
{
	Class("class"),
	Color("color"),
	BackgroundColor("background-color"),
	ViewBox("viewBox"),
	ClipPath("clip-path"),
	D("d"),
	Opacity("opacity"),
	Marker_Start("marker-start"),
	Marker_Mid("marker-mid"),
	Marker_End("marker-end"),
	Font_Weight("font-weight"),

	Rx("rx"),
	Ry("ry"),
	Stroke("stroke"),
	Stroke_Opacity("stroke-opacity"),
	Stroke_Width("stroke-width"),
	Stroke_DashArray("stroke-dasharray"),
	Stroke_DashOffset("stroke-dashoffset"),
	Stroke_LineCap("stroke-linecap"),
	Stroke_LineJoin("stroke-linejoin"),
	Stroke_MiterLimit("stroke-miterlimit"),
	Style("style"),
	Transform("transform"),
	Width("width"),
	Height("height"),
	FilterUnits("filterUnits"),
	PrimitiveUnits("primitiveUnits"),

	ColorInterpolationFilters("color-interpolation-filters"),

	In("in"),
	In2("in2"),
	Result("result"),
	Dx("dx"), Dy("dy"),
	X("x"), Y("y"),
	SpreadMethod("spreadMethod"),

	FillOpacity("fill-opacity"),
	StdDeviation("stdDeviation"),

	GradientUnits("gradientUnits"),
	GradientTransform("gradientTransform"),
	Offset("offset"),
	StopColor("stop-color"),
	StopOpacity("stop-opacity"),
	Fill("fill"),
	FillRule("fill-rule"),

	X1("x1"),
	Y1("y1"),
	X2("x2"),
	Y2("y2"),

	Points("points"),

	Cx("cx"),
	Cy("cy"),
	R("r"),

	WhiteSpace("white-space"),
	XmlSpace("xml:space"),
	FontSize("font-size"),
	FontFamily("font-family"),

	RefX("refX"),
	RefY("refY"),

	MarkerWidth("markerWidth"),
	MarkerHeight("markerHeight"),
	MarkerUnits("markerUnits"),
	Orient("orient"),

	StartOffset("startOffset"),
	TextLength("textLength"),
	LengthAdjust("lengthAdjust"),
	TextAnchor("text-anchor"),
	TextDecoration("text-decoration"),

	Fx("fx"),
	Fy("fy"),
	Fr("fr"),

	PatternTransform("patternTransform" ),
	PatternUnits( "patternUnits" ),
	PreserveAspectRatio("preserveAspectRatio" );

	private final static HashMap<String, Attribute> attributes_ = new HashMap<>();

	// Use map instead of "valueOf" to avoid exceptions for unknown values
	static
	{
		for (Attribute t : values())
			attributes_.put(t.xmlName(), t);
	}

	private final String xmlName_;

	Attribute(String xmlName)
	{
		xmlName_ = xmlName;
	}

	public static Attribute valueFrom(String attributeName)
	{
		return attributes_.get(attributeName);
	}

	public String xmlName()
	{
		return xmlName_;
	}
}

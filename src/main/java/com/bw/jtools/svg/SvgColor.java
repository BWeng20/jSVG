package com.bw.jtools.svg;

import com.bw.jtools.shape.Context;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SvgColor
{
	private static final HashMap<String, PaintWrapper> name2color_ = new HashMap<>();

	static
	{
		name2color_.put("black", new PaintWrapper(new java.awt.Color(0x00, 0x00, 0x00)));
		name2color_.put("navy", new PaintWrapper(new java.awt.Color(0x00, 0x00, 0x80)));
		name2color_.put("darkblue", new PaintWrapper(new java.awt.Color(0x00, 0x00, 0x8B)));
		name2color_.put("mediumblue", new PaintWrapper(new java.awt.Color(0x00, 0x00, 0xCD)));
		name2color_.put("blue", new PaintWrapper(new java.awt.Color(0x00, 0x00, 0xFF)));
		name2color_.put("darkgreen", new PaintWrapper(new java.awt.Color(0x00, 0x64, 0x00)));
		name2color_.put("green", new PaintWrapper(new java.awt.Color(0x00, 0x80, 0x00)));
		name2color_.put("teal", new PaintWrapper(new java.awt.Color(0x00, 0x80, 0x80)));
		name2color_.put("darkcyan", new PaintWrapper(new java.awt.Color(0x00, 0x8B, 0x8B)));
		name2color_.put("deepskyblue", new PaintWrapper(new java.awt.Color(0x00, 0xBF, 0xFF)));
		name2color_.put("darkturquoise", new PaintWrapper(new java.awt.Color(0x00, 0xCE, 0xD1)));
		name2color_.put("mediumspringgreen", new PaintWrapper(new java.awt.Color(0x00, 0xFA, 0x9A)));
		name2color_.put("lime", new PaintWrapper(new java.awt.Color(0x00, 0xFF, 0x00)));
		name2color_.put("springgreen", new PaintWrapper(new java.awt.Color(0x00, 0xFF, 0x7F)));
		name2color_.put("cyan(Safe 16=aqua Hex3)", new PaintWrapper(new java.awt.Color(0x00, 0xFF, 0xFF)));
		name2color_.put("aqua", new PaintWrapper(new java.awt.Color(0x00, 0xFF, 0xFF)));
		name2color_.put("midnightblue", new PaintWrapper(new java.awt.Color(0x19, 0x19, 0x70)));
		name2color_.put("dodgerblue", new PaintWrapper(new java.awt.Color(0x1E, 0x90, 0xFF)));
		name2color_.put("lightseagreen", new PaintWrapper(new java.awt.Color(0x20, 0xB2, 0xAA)));
		name2color_.put("forestgreen", new PaintWrapper(new java.awt.Color(0x22, 0x8B, 0x22)));
		name2color_.put("seagreen", new PaintWrapper(new java.awt.Color(0x2E, 0x8B, 0x57)));
		name2color_.put("darkslategray", new PaintWrapper(new java.awt.Color(0x2F, 0x4F, 0x4F)));
		name2color_.put("darkslategrey", new PaintWrapper(new java.awt.Color(0x2F, 0x4F, 0x4F)));
		name2color_.put("limegreen", new PaintWrapper(new java.awt.Color(0x32, 0xCD, 0x32)));
		name2color_.put("mediumseagreen", new PaintWrapper(new java.awt.Color(0x3C, 0xB3, 0x71)));
		name2color_.put("turquoise", new PaintWrapper(new java.awt.Color(0x40, 0xE0, 0xD0)));
		name2color_.put("royalblue", new PaintWrapper(new java.awt.Color(0x41, 0x69, 0xE1)));
		name2color_.put("steelblue", new PaintWrapper(new java.awt.Color(0x46, 0x82, 0xB4)));
		name2color_.put("darkslateblue", new PaintWrapper(new java.awt.Color(0x48, 0x3D, 0x8B)));
		name2color_.put("mediumturquoise", new PaintWrapper(new java.awt.Color(0x48, 0xD1, 0xCC)));
		name2color_.put("indigo", new PaintWrapper(new java.awt.Color(0x4B, 0x00, 0x82)));
		name2color_.put("darkolivegreen", new PaintWrapper(new java.awt.Color(0x55, 0x6B, 0x2F)));
		name2color_.put("cadetblue", new PaintWrapper(new java.awt.Color(0x5F, 0x9E, 0xA0)));
		name2color_.put("cornflowerblue", new PaintWrapper(new java.awt.Color(0x64, 0x95, 0xED)));
		name2color_.put("mediumaquamarine", new PaintWrapper(new java.awt.Color(0x66, 0xCD, 0xAA)));
		name2color_.put("dimgrey", new PaintWrapper(new java.awt.Color(0x69, 0x69, 0x69)));
		name2color_.put("dimgray", new PaintWrapper(new java.awt.Color(0x69, 0x69, 0x69)));
		name2color_.put("slateblue", new PaintWrapper(new java.awt.Color(0x6A, 0x5A, 0xCD)));
		name2color_.put("olivedrab", new PaintWrapper(new java.awt.Color(0x6B, 0x8E, 0x23)));
		name2color_.put("slategrey", new PaintWrapper(new java.awt.Color(0x70, 0x80, 0x90)));
		name2color_.put("slategray", new PaintWrapper(new java.awt.Color(0x70, 0x80, 0x90)));
		name2color_.put("lightslategray", new PaintWrapper(new java.awt.Color(0x77, 0x88, 0x99)));
		name2color_.put("lightslategrey", new PaintWrapper(new java.awt.Color(0x77, 0x88, 0x99)));
		name2color_.put("mediumslateblue", new PaintWrapper(new java.awt.Color(0x7B, 0x68, 0xEE)));
		name2color_.put("lawngreen", new PaintWrapper(new java.awt.Color(0x7C, 0xFC, 0x00)));
		name2color_.put("chartreuse", new PaintWrapper(new java.awt.Color(0x7F, 0xFF, 0x00)));
		name2color_.put("aquamarine", new PaintWrapper(new java.awt.Color(0x7F, 0xFF, 0xD4)));
		name2color_.put("maroon", new PaintWrapper(new java.awt.Color(0x80, 0x00, 0x00)));
		name2color_.put("purple", new PaintWrapper(new java.awt.Color(0x80, 0x00, 0x80)));
		name2color_.put("olive", new PaintWrapper(new java.awt.Color(0x80, 0x80, 0x00)));
		name2color_.put("gray", new PaintWrapper(new java.awt.Color(0x80, 0x80, 0x80)));
		name2color_.put("grey", new PaintWrapper(new java.awt.Color(0x80, 0x80, 0x80)));
		name2color_.put("skyblue", new PaintWrapper(new java.awt.Color(0x87, 0xCE, 0xEB)));
		name2color_.put("lightskyblue", new PaintWrapper(new java.awt.Color(0x87, 0xCE, 0xFA)));
		name2color_.put("blueviolet", new PaintWrapper(new java.awt.Color(0x8A, 0x2B, 0xE2)));
		name2color_.put("darkred", new PaintWrapper(new java.awt.Color(0x8B, 0x00, 0x00)));
		name2color_.put("darkmagenta", new PaintWrapper(new java.awt.Color(0x8B, 0x00, 0x8B)));
		name2color_.put("saddlebrown", new PaintWrapper(new java.awt.Color(0x8B, 0x45, 0x13)));
		name2color_.put("darkseagreen", new PaintWrapper(new java.awt.Color(0x8F, 0xBC, 0x8F)));
		name2color_.put("lightgreen", new PaintWrapper(new java.awt.Color(0x90, 0xEE, 0x90)));
		name2color_.put("mediumpurple", new PaintWrapper(new java.awt.Color(0x93, 0x70, 0xDB)));
		name2color_.put("darkviolet", new PaintWrapper(new java.awt.Color(0x94, 0x00, 0xD3)));
		name2color_.put("palegreen", new PaintWrapper(new java.awt.Color(0x98, 0xFB, 0x98)));
		name2color_.put("darkorchid", new PaintWrapper(new java.awt.Color(0x99, 0x32, 0xCC)));
		name2color_.put("yellowgreen", new PaintWrapper(new java.awt.Color(0x9A, 0xCD, 0x32)));
		name2color_.put("sienna", new PaintWrapper(new java.awt.Color(0xA0, 0x52, 0x2D)));
		name2color_.put("brown", new PaintWrapper(new java.awt.Color(0xA5, 0x2A, 0x2A)));
		name2color_.put("darkgray", new PaintWrapper(new java.awt.Color(0xA9, 0xA9, 0xA9)));
		name2color_.put("darkgrey", new PaintWrapper(new java.awt.Color(0xA9, 0xA9, 0xA9)));
		name2color_.put("lightblue", new PaintWrapper(new java.awt.Color(0xAD, 0xD8, 0xE6)));
		name2color_.put("greenyellow", new PaintWrapper(new java.awt.Color(0xAD, 0xFF, 0x2F)));
		name2color_.put("paleturquoise", new PaintWrapper(new java.awt.Color(0xAF, 0xEE, 0xEE)));
		name2color_.put("lightsteelblue", new PaintWrapper(new java.awt.Color(0xB0, 0xC4, 0xDE)));
		name2color_.put("powderblue", new PaintWrapper(new java.awt.Color(0xB0, 0xE0, 0xE6)));
		name2color_.put("firebrick", new PaintWrapper(new java.awt.Color(0xB2, 0x22, 0x22)));
		name2color_.put("darkgoldenrod", new PaintWrapper(new java.awt.Color(0xB8, 0x86, 0x0B)));
		name2color_.put("mediumorchid", new PaintWrapper(new java.awt.Color(0xBA, 0x55, 0xD3)));
		name2color_.put("rosybrown", new PaintWrapper(new java.awt.Color(0xBC, 0x8F, 0x8F)));
		name2color_.put("darkkhaki", new PaintWrapper(new java.awt.Color(0xBD, 0xB7, 0x6B)));
		name2color_.put("silver", new PaintWrapper(new java.awt.Color(0xC0, 0xC0, 0xC0)));
		name2color_.put("mediumvioletred", new PaintWrapper(new java.awt.Color(0xC7, 0x15, 0x85)));
		name2color_.put("indianred", new PaintWrapper(new java.awt.Color(0xCD, 0x5C, 0x5C)));
		name2color_.put("peru", new PaintWrapper(new java.awt.Color(0xCD, 0x85, 0x3F)));
		name2color_.put("chocolate", new PaintWrapper(new java.awt.Color(0xD2, 0x69, 0x1E)));
		name2color_.put("tan", new PaintWrapper(new java.awt.Color(0xD2, 0xB4, 0x8C)));
		name2color_.put("lightgray", new PaintWrapper(new java.awt.Color(0xD3, 0xD3, 0xD3)));
		name2color_.put("lightgrey", new PaintWrapper(new java.awt.Color(0xD3, 0xD3, 0xD3)));
		name2color_.put("thistle", new PaintWrapper(new java.awt.Color(0xD8, 0xBF, 0xD8)));
		name2color_.put("orchid", new PaintWrapper(new java.awt.Color(0xDA, 0x70, 0xD6)));
		name2color_.put("goldenrod", new PaintWrapper(new java.awt.Color(0xDA, 0xA5, 0x20)));
		name2color_.put("palevioletred", new PaintWrapper(new java.awt.Color(0xDB, 0x70, 0x93)));
		name2color_.put("crimson", new PaintWrapper(new java.awt.Color(0xDC, 0x14, 0x3C)));
		name2color_.put("gainsboro", new PaintWrapper(new java.awt.Color(0xDC, 0xDC, 0xDC)));
		name2color_.put("plum", new PaintWrapper(new java.awt.Color(0xDD, 0xA0, 0xDD)));
		name2color_.put("burlywood", new PaintWrapper(new java.awt.Color(0xDE, 0xB8, 0x87)));
		name2color_.put("lightcyan", new PaintWrapper(new java.awt.Color(0xE0, 0xFF, 0xFF)));
		name2color_.put("lavender", new PaintWrapper(new java.awt.Color(0xE6, 0xE6, 0xFA)));
		name2color_.put("darksalmon", new PaintWrapper(new java.awt.Color(0xE9, 0x96, 0x7A)));
		name2color_.put("violet", new PaintWrapper(new java.awt.Color(0xEE, 0x82, 0xEE)));
		name2color_.put("palegoldenrod", new PaintWrapper(new java.awt.Color(0xEE, 0xE8, 0xAA)));
		name2color_.put("lightcoral", new PaintWrapper(new java.awt.Color(0xF0, 0x80, 0x80)));
		name2color_.put("khaki", new PaintWrapper(new java.awt.Color(0xF0, 0xE6, 0x8C)));
		name2color_.put("aliceblue", new PaintWrapper(new java.awt.Color(0xF0, 0xF8, 0xFF)));
		name2color_.put("honeydew", new PaintWrapper(new java.awt.Color(0xF0, 0xFF, 0xF0)));
		name2color_.put("azure", new PaintWrapper(new java.awt.Color(0xF0, 0xFF, 0xFF)));
		name2color_.put("sandybrown", new PaintWrapper(new java.awt.Color(0xF4, 0xA4, 0x60)));
		name2color_.put("wheat", new PaintWrapper(new java.awt.Color(0xF5, 0xDE, 0xB3)));
		name2color_.put("beige", new PaintWrapper(new java.awt.Color(0xF5, 0xF5, 0xDC)));
		name2color_.put("whitesmoke", new PaintWrapper(new java.awt.Color(0xF5, 0xF5, 0xF5)));
		name2color_.put("mintcream", new PaintWrapper(new java.awt.Color(0xF5, 0xFF, 0xFA)));
		name2color_.put("ghostwhite", new PaintWrapper(new java.awt.Color(0xF8, 0xF8, 0xFF)));
		name2color_.put("salmon", new PaintWrapper(new java.awt.Color(0xFA, 0x80, 0x72)));
		name2color_.put("antiquewhite", new PaintWrapper(new java.awt.Color(0xFA, 0xEB, 0xD7)));
		name2color_.put("linen", new PaintWrapper(new java.awt.Color(0xFA, 0xF0, 0xE6)));
		name2color_.put("lightgoldenrodyellow", new PaintWrapper(new java.awt.Color(0xFA, 0xFA, 0xD2)));
		name2color_.put("oldlace", new PaintWrapper(new java.awt.Color(0xFD, 0xF5, 0xE6)));
		name2color_.put("red", new PaintWrapper(new java.awt.Color(0xFF, 0x00, 0x00)));
		name2color_.put("fuchsia", new PaintWrapper(new java.awt.Color(0xFF, 0x00, 0xFF)));
		name2color_.put("magenta(Safe 16=fuchsia Hex3)", new PaintWrapper(new java.awt.Color(0xFF, 0x00, 0xFF)));
		name2color_.put("deeppink", new PaintWrapper(new java.awt.Color(0xFF, 0x14, 0x93)));
		name2color_.put("orangered", new PaintWrapper(new java.awt.Color(0xFF, 0x45, 0x00)));
		name2color_.put("tomato", new PaintWrapper(new java.awt.Color(0xFF, 0x63, 0x47)));
		name2color_.put("hotpink", new PaintWrapper(new java.awt.Color(0xFF, 0x69, 0xB4)));
		name2color_.put("coral", new PaintWrapper(new java.awt.Color(0xFF, 0x7F, 0x50)));
		name2color_.put("darkorange", new PaintWrapper(new java.awt.Color(0xFF, 0x8C, 0x00)));
		name2color_.put("lightsalmon", new PaintWrapper(new java.awt.Color(0xFF, 0xA0, 0x7A)));
		name2color_.put("orange", new PaintWrapper(new java.awt.Color(0xFF, 0xA5, 0x00)));
		name2color_.put("lightpink", new PaintWrapper(new java.awt.Color(0xFF, 0xB6, 0xC1)));
		name2color_.put("pink", new PaintWrapper(new java.awt.Color(0xFF, 0xC0, 0xCB)));
		name2color_.put("gold", new PaintWrapper(new java.awt.Color(0xFF, 0xD7, 0x00)));
		name2color_.put("peachpuff", new PaintWrapper(new java.awt.Color(0xFF, 0xDA, 0xB9)));
		name2color_.put("navajowhite", new PaintWrapper(new java.awt.Color(0xFF, 0xDE, 0xAD)));
		name2color_.put("moccasin", new PaintWrapper(new java.awt.Color(0xFF, 0xE4, 0xB5)));
		name2color_.put("bisque", new PaintWrapper(new java.awt.Color(0xFF, 0xE4, 0xC4)));
		name2color_.put("mistyrose", new PaintWrapper(new java.awt.Color(0xFF, 0xE4, 0xE1)));
		name2color_.put("blanchedalmond", new PaintWrapper(new java.awt.Color(0xFF, 0xEB, 0xCD)));
		name2color_.put("papayawhip", new PaintWrapper(new java.awt.Color(0xFF, 0xEF, 0xD5)));
		name2color_.put("lavenderblush", new PaintWrapper(new java.awt.Color(0xFF, 0xF0, 0xF5)));
		name2color_.put("seashell", new PaintWrapper(new java.awt.Color(0xFF, 0xF5, 0xEE)));
		name2color_.put("cornsilk", new PaintWrapper(new java.awt.Color(0xFF, 0xF8, 0xDC)));
		name2color_.put("lemonchiffon", new PaintWrapper(new java.awt.Color(0xFF, 0xFA, 0xCD)));
		name2color_.put("floralwhite", new PaintWrapper(new java.awt.Color(0xFF, 0xFA, 0xF0)));
		name2color_.put("snow", new PaintWrapper(new java.awt.Color(0xFF, 0xFA, 0xFA)));
		name2color_.put("yellow", new PaintWrapper(new java.awt.Color(0xFF, 0xFF, 0x00)));
		name2color_.put("lightyellow", new PaintWrapper(new java.awt.Color(0xFF, 0xFF, 0xE0)));
		name2color_.put("ivory", new PaintWrapper(new java.awt.Color(0xFF, 0xFF, 0xF0)));
		name2color_.put("white", new PaintWrapper(java.awt.Color.WHITE));
		name2color_.put("none", new PaintWrapper(Context.NONE));
		name2color_.put("transparent", new PaintWrapper(new java.awt.Color(0, 0, 0, 0)));
		name2color_.put("currentcolor", new PaintWrapper(Context.CURRENT_COLOR));
		name2color_.put("context-fill", PaintWrapper.contextFill());
		name2color_.put("context-stroke", PaintWrapper.contextStroke());
		// Internal extension to access the background color of the painting component.
		name2color_.put("background", new PaintWrapper(Context.CURRENT_BACKGROUND));
		name2color_.put("currentbackground", new PaintWrapper(Context.CURRENT_BACKGROUND));
	}

	private PaintWrapper paintWrapper_;

	private static final Pattern rgbRegEx_ = Pattern.compile("rgb\\(\\s*([\\+\\-\\d+]+%?)\\s*,\\s*([\\+\\-\\d+]+%?)\\s*,\\s*([\\+\\-\\d+]+%?)\\s*\\)", Pattern.CASE_INSENSITIVE);

	public SvgColor(SVGConverter svg, String color, double opacity)
	{
		if (opacity < 0)
			opacity = 0;
		else if (opacity > 1f)
			opacity = 1f;

		if (color != null)
		{
			color = color.trim();
			if (color.startsWith("#"))
			{
				if (color.length() < 5)
				{
					if (color.length() > 3)
					{
						char r = color.charAt(1);
						char g = color.charAt(2);
						char b = color.charAt(3);
						color = new StringBuilder(7).append('#')
													.append(r)
													.append(r)
													.append(g)
													.append(g)
													.append(b)
													.append(b)
													.toString();
					}
					else
						color = "#000000";
				}

				try
				{
					paintWrapper_ = new PaintWrapper(java.awt.Color.decode(color));
				}
				catch (NumberFormatException ne)
				{
					SVGConverter.error("Illegal color value '%s'", color);
					paintWrapper_ = new PaintWrapper(java.awt.Color.BLACK);
				}
			}
			else
			{
				Matcher m = rgbRegEx_.matcher(color);
				if (m.matches())
				{
					int r = decodeRGBValue(m.group(1));
					int g = decodeRGBValue(m.group(2));
					int b = decodeRGBValue(m.group(3));
					paintWrapper_ = new PaintWrapper(new java.awt.Color(r, g, b));
				}
				else
				{
					String[] ref = ElementWrapper.urlRef(color);
					if (ref != null)
					{
						paintWrapper_ = svg.getPaint(ref[0]);
						if (paintWrapper_ == null)
						{
							// Use fallback if reference doesn't exist.
							paintWrapper_ = new SvgColor(svg, ref[1], opacity).paintWrapper_;
							opacity = 1f;
						}
					}
					else
					{
						PaintWrapper predef = getPredefinedPaintWrapper(color);
						if (predef == null)
						{
							predef = getPredefinedPaintWrapper("black");
						}
						paintWrapper_ = new PaintWrapper(predef);
					}
				}
			}
			// Adapt Paint if opacity could not be included.
			if (opacity != 1f)
				paintWrapper_ = paintWrapper_.adaptOpacity((float) opacity);
		}
		else
			paintWrapper_ = null;
	}

	public static PaintWrapper getPredefinedPaintWrapper(String colorName)
	{
		return colorName == null ? null : name2color_.get(colorName.toLowerCase());
	}

	/**
	 * Decode a values from a rgb(r,g,b) expression.
	 */
	private static int decodeRGBValue(String v)
	{
		try
		{
			boolean perc = v.endsWith("%");
			if (perc) v = v.substring(0, v.length() - 1);
			int val = Integer.parseInt(v);
			return (perc) ? (int) (0.5 + 2.55f * val) : val;
		}
		catch (Exception e)
		{
			return 0;
		}
	}

	public static java.awt.Color adaptOpacity(java.awt.Color color, double opacity)
	{
		if (color == Context.NONE)
			return Context.NONE;
		else
			return new java.awt.Color(color.getRed(), color.getGreen(), color.getBlue(), (int) (color.getAlpha() * opacity + 0.5));
	}

	public PaintWrapper getPaintWrapper()
	{
		return paintWrapper_;
	}

}

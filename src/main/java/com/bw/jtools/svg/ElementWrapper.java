package com.bw.jtools.svg;

import com.bw.jtools.svg.css.CSSParser;
import com.bw.jtools.svg.css.Specificity;
import com.bw.jtools.svg.css.StyleValue;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.Shape;
import java.awt.font.TextAttribute;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Holds a svg element. Handles attributes.
 */
public final class ElementWrapper
{
	private Map<String, StyleValue> attributes_;
	private Map<String, String> overrides_;
	private boolean isShadow_;
	private Set<String> classes_;
	private final Element node_;
	private ElementWrapper parent_;
	private final Type type_;
	private Boolean preserveSpace_;
	private ShapeHelper shape_;
	private AffineTransform aft_;
	private ElementCache elementCache_;
	private Rectangle2D.Double viewPort_;
	private boolean viewBoxRetrieved_;
	private Rectangle2D.Double viewBox_;
	private Double viewPortLength_;
	private Double opacity_;
	private Double effectiveOpacity_;

	private static final double SQRT2 = Math.sqrt(2d);

	private static final Pattern unitRegExp_ = Pattern.compile("(\\s*[+-]?[\\d\\.]+(?:e[+-]?\\d+)?)\\s*(rem|pt|px|em|%|in|cm|mm|m|ex|pc)", Pattern.CASE_INSENSITIVE);
	private static final Pattern urlRegExp = Pattern.compile("url\\(['\"]?\\s*#([^\\\"')]+)['\"]?\\)(.*)", Pattern.CASE_INSENSITIVE);

	private static final HashMap<String, Float> fontWeights_ = new HashMap<>();
	private static final float FONT_WEIGHT_FACTOR = TextAttribute.WEIGHT_BOLD / 700;

	static
	{
		fontWeights_.put("normal", TextAttribute.WEIGHT_REGULAR);
		fontWeights_.put("bold", TextAttribute.WEIGHT_BOLD);
		// @ODO: make it relative
		fontWeights_.put("lighter", TextAttribute.WEIGHT_LIGHT);
		// @ODO: make it relative
		fontWeights_.put("bolder", TextAttribute.WEIGHT_DEMIBOLD);
	}

	/**
	 * Checks a string for null or empty.
	 */
	protected static boolean isEmpty(String v)
	{
		return v == null || v.isEmpty();
	}

	/**
	 * Checks if string is not null or empty.
	 */
	protected static boolean isNotEmpty(String v)
	{
		return v != null && !v.isEmpty();
	}

	/**
	 * Extract id reference from a "url(#id)" expression.
	 *
	 * @return null if the expression is not a url. Otherwise the id will be in #0 and any remaining text in #1.
	 */
	protected static String[] urlRef(String ref)
	{
		Matcher m = urlRegExp.matcher(ref);
		return (m.matches()) ? new String[]{m.group(1).trim(), m.group(2)} : null;
	}

	/**
	 * Parse a length value.
	 */
	public static Length parseLength(String val)
	{
		if (isNotEmpty(val))
			try
			{
				Matcher m = unitRegExp_.matcher(val);
				if (m.matches())
					return new Length(parseDouble(m.group(1)), LengthUnit.fromString(m.group(2)));
				else
					return new Length(parseDouble(val), LengthUnit.px);
			}
			catch (Exception e)
			{
				SVGConverter.error(e, "Can't parse length '%s'", val);
			}
		return null;
	}

	/**
	 * Parse a double mote error tolerant.
	 *
	 * @param val The value as string
	 * @return The converted value.
	 */
	protected static double parseDouble(String val)
	{
		if (isEmpty(val)) return 0;
		return Double.parseDouble(val);
	}


	/**
	 * Converts a number or length value to double.
	 */
	protected static Double convDouble(String val)
	{
		if (isNotEmpty(val))
			try
			{
				Matcher m = unitRegExp_.matcher(val);
				if (m.matches())
					return new Length(parseDouble(m.group(1)), LengthUnit.fromString(m.group(2))).toPixel(null);
				else
					return parseDouble(val);
			}
			catch (Exception e)
			{
				SVGConverter.error(e, "Can't parse '%s'", val);
			}
		return null;
	}

	public void setShape(Shape shape)
	{
		if (shape != null)
		{
			shape_ = new ShapeHelper(shape);
		}
		else
			shape_ = null;
	}

	public ShapeHelper getShape()
	{
		return shape_;
	}

	public ElementWrapper(ElementCache cache, Element node, boolean isShadow)
	{
		elementCache_ = cache;
		node_ = node;
		isShadow_ = isShadow;

		Node parentNode = node.getParentNode();
		while (parent_ == null && parentNode != null)
		{
			parent_ = elementCache_.getElementWrapper(parentNode);
			parentNode = parentNode.getParentNode();
		}
		type_ = Type.valueFrom(node.getTagName());
	}

	public Type getType()
	{
		return type_;
	}

	public String getTagName()
	{
		return node_.getTagName();
	}

	public boolean hasClass(String clazz)
	{
		return getClasses().contains(clazz);
	}

	public Set<String> getClasses()
	{
		if (classes_ == null)
		{
			classes_ = new HashSet<>();
			String clazz = attr("class", false);
			if (clazz != null)
			{
				Scanner s = new Scanner(clazz);
				while (s.hasNext())
					classes_.add(s.next());
			}
		}
		return classes_;
	}

	/**
	 * Get id attribute.
	 */
	public String id()
	{
		return node_.getAttribute("id");
	}

	/**
	 * Get href or xlink:href attribute.
	 */
	public String href()
	{
		String href = node_.getAttribute("href")
						   .trim();
		if (isEmpty(href))
			href = node_.getAttribute("xlink:href")
						.trim();
		if (isNotEmpty(href))
		{
			if (href.startsWith("#"))
				href = href.substring(1);
			return href;
		}
		return null;
	}

	/**
	 * Get clip-path attribute.
	 */
	public String clipPath()
	{
		String v = node_.getAttribute("clip-path")
						.trim();
		if (isNotEmpty(v))
		{
			String ref[] = urlRef(v);
			if (ref != null)
				return ref[0];
		}
		return null;
	}

	/**
	 * Gets the by "marker-mid" referenced marker.
	 *
	 * @return Id of marker or null
	 */
	public String markerMid()
	{
		String v = attr("marker-mid", true);
		if (isNotEmpty(v) && !"none".equals(v))
		{
			String ref[] = urlRef(v);
			if (ref != null)
				return ref[0];
		}
		return null;
	}

	/**
	 * Gets the by "marker-start" referenced marker.
	 *
	 * @return Id of marker or null
	 */
	public String markerStart()
	{
		String v = attr("marker-start", true);
		if (isNotEmpty(v) && !"none".equals(v))
		{
			String ref[] = urlRef(v);
			if (ref != null)
				return ref[0];
		}
		return null;
	}

	/**
	 * Gets the by "marker-end" referenced marker.
	 *
	 * @return Id of marker or null
	 */
	public String markerEnd()
	{
		String v = attr("marker-end", true);
		if (isNotEmpty(v) && !"none".equals(v))
		{
			String ref[] = urlRef(v);
			if (ref != null)
				return ref[0];
		}
		return null;
	}

	/**
	 * Get the filter id from the filter attribute.
	 */
	public String filter()
	{
		String v = node_.getAttribute("filter")
						.trim();
		if (isNotEmpty(v))
		{
			String ref[] = urlRef(v);
			if (ref != null)
				return ref[0];
		}
		return null;
	}

	/**
	 * Gets the java font-weight-value from "font-weight"-attribute expressed as {@link TextAttribute#WEIGHT}.
	 */
	public double fontWeight()
	{
		String w = attr("font-weight", true);
		if (isEmpty(w))
			return TextAttribute.WEIGHT_REGULAR;

		Float pref = fontWeights_.get(w.trim()
									   .toLowerCase());
		if (pref != null)
			return pref;
		Double d = convDouble(w);
		return (d == null) ? TextAttribute.WEIGHT_REGULAR : (d * FONT_WEIGHT_FACTOR);
	}

	/**
	 * Get the length value of a none-inherited xml- or style-attribute.
	 *
	 * @return The length or null if the attribute doesn't exists.
	 */
	public Length toLength(String attributeName)
	{
		return toLength(attributeName, false);
	}

	/**
	 * Get the length value of a xml- or style-attribute.
	 *
	 * @return The length or null if the attribute doesn't exists.
	 */
	public Length toLength(String attributeName, boolean inherited)
	{
		return parseLength(attr(attributeName, inherited));
	}


	/**
	 * Get the double value of a none-inherited xml- or style-attribute as Double.
	 *
	 * @return The double or null if the attribute doesn't exists.
	 */
	public Double toDouble(String attributeName)
	{
		return convDouble(attr(attributeName, false));
	}

	/**
	 * Get the primitive double value of a none-inherited xml- or style-attribute.
	 *
	 * @return The double or 0 if the attribute doesn't exists.
	 */
	public double toPDouble(String attributeName)
	{
		return toPDouble(attributeName, false);
	}

	/**
	 * Get the double value of a xml- or style-attribute.
	 *
	 * @param inherited If true and the attribute doesn't exists also the parent nodes are scanned.
	 * @return The double or null if the attribute doesn't exists.
	 */
	public Double toDouble(String attributeName, boolean inherited)
	{
		return convDouble(attr(attributeName, inherited));
	}

	/**
	 * Get the primitive double value of a xml- or style-attribute.
	 * Default is 0.
	 *
	 * @param inherited If true and the attribute doesn't exists also the parent nodes are scanned.
	 * @return The double or 0 if the attribute doesn't exists.
	 */
	public double toPDouble(String attributeName, boolean inherited)
	{
		return toPDouble(attributeName, 0d, inherited);
	}

	/**
	 * Get the primitive double value of a xml- or style-attribute.
	 *
	 * @param inherited If true and the attribute doesn't exists also the parent nodes are scanned.
	 * @return The double or 0 if the attribute doesn't exists.
	 */
	public double toPDouble(String attributeName, double defaultValue, boolean inherited)
	{
		Double d = convDouble(attr(attributeName, inherited));
		return d == null ? defaultValue : d;
	}

	/**
	 * Get list of doubles
	 *
	 * @return List, never null but possible empty.
	 */
	public List<Double> toPDoubleList(String attributeName, boolean inherited)
	{
		final String val = attr(attributeName, inherited);
		LengthList l;
		if (val != null)
			return new LengthList(val).getLengthList()
									  .stream()
									  .map(length -> length.value_)
									  .collect(Collectors.toList());
		else
			return Collections.emptyList();
	}


	public LengthList toLengthList(String attributeName, boolean inherited)
	{
		final String val = attr(attributeName, inherited);
		LengthList l;
		if (val != null)
		{
			l = new LengthList(val);
			if (l.isEmpty())
				l = null;
		}
		else
			l = null;
		return l;
	}

	public static double convPDouble(String value)
	{
		Double d = convDouble(value);
		return d == null ? 0d : d;
	}

	/**
	 * Get the text content - to be used for text-elements.
	 */
	public String text()
	{
		return text(node_);
	}

	/**
	 * Get the text content - to be used for text-elements.
	 */
	public String text(Node node)
	{
		String text = node.getTextContent();
		// @TODO: Support all modes of "white-space" correctly.
		if (text != null && !preserveSpace())
		{
			// Trim WS
			StringBuilder sb = new StringBuilder(text.length());
			text.chars()
				.forEach(new IntConsumer()
				{
					boolean lastWasWS = false;

					@Override
					public void accept(int ch)
					{
						if (Character.isWhitespace(ch))
						{
							if (!lastWasWS)
							{
								sb.append((char) ch);
								lastWasWS = true;
							}
						}
						else
						{
							sb.append((char) ch);
							lastWasWS = false;
						}
					}
				});
			text = sb.toString();
		}
		return text;
	}

	/**
	 * Gets the transform on this ele.
	 */
	public AffineTransform transform()
	{
		if (aft_ == null)
		{
			String transform = attr("transform", false);
			if (isNotEmpty(transform))
				aft_ = new Transform(null, transform).getTransform();
			else
				aft_ = new AffineTransform();
		}
		return aft_.isIdentity() ? null : aft_;
	}

	/**
	 * Creates a shadow copy of this element, copies attributes from the "usingElement" to
	 * the copy. Other styles and attributes are inherited from the original.
	 *
	 * @param usingElement The referencing element.
	 * @return The shadow copy.
	 */
	public ElementWrapper createReferenceShadow(ElementWrapper usingElement)
	{
		ElementWrapper uw = new ElementWrapper(elementCache_, getNode(), true);
		uw.parent_ = usingElement;
		String tag = uw.getTagName();
		if (tag.equals("svg") || tag.equals("symbol"))
		{
			// @TODO: set width and height of viewbox
		}

		NamedNodeMap attributes = usingElement.getNode()
											  .getAttributes();
		int nAttr = attributes.getLength();
		for (int iAttr = 0; iAttr < nAttr; ++iAttr)
		{
			Node attrNode = attributes.item(iAttr);
			String attrName = attrNode.getNodeName();
			if (attrName != null)
				uw.override(attrName, attrNode.getNodeValue());
		}
		Map<String, StyleValue> styleAttributes = usingElement.getStyleAttributes();
		for (Map.Entry<String, StyleValue> styleAttr : styleAttributes.entrySet())
		{
			String attrName = styleAttr.getKey();
			uw.override(attrName, styleAttr.getValue().value_);
		}
		return uw;
	}

	/**
	 * Get the base for percentages-values bases on the viewport that are not x- or y-bound.
	 *
	 * @see <a href="https://www.w3.org/TR/SVG11/coords.html#Units">https://www.w3.org/TR/SVG11/coords.html#Units</a>
	 */
	public double getViewPortLength()
	{
		if (viewPortLength_ == null)
		{
			if (viewPort_ == null) getViewPort();
			viewPortLength_ = Math.sqrt((viewPort_.width * viewPort_.width) + (viewPort_.height * viewPort_.height)) / SQRT2;
		}
		return viewPortLength_;
	}

	public Rectangle2D.Double getViewPort()
	{
		if (viewPort_ == null)
		{
			Length width = toLength("width", true);
			Length height = toLength("height", true);

			if (width == null) width = new Length(100, LengthUnit.px);
			if (height == null) height = new Length(100, LengthUnit.px);

			viewPort_ = new Rectangle2D.Double(0, 0, width.toPixel(null), height.toPixel(null));
		}
		return viewPort_;
	}

	public Rectangle2D.Double getViewBox()
	{
		if (!viewBoxRetrieved_)
		{
			viewBoxRetrieved_ = true;
			LengthList l = toLengthList("viewBox", true);
			if (l != null)
			{
				List<Length> ll = l.getLengthList();
				if (ll.size() >= 4)
				{
					Rectangle2D.Double vp = getViewPort();
					viewPort_ = new Rectangle2D.Double(
							ll.get(0)
							  .toPixel(vp.width),
							ll.get(1)
							  .toPixel(vp.height),
							ll.get(2)
							  .toPixel(vp.width),
							ll.get(3)
							  .toPixel(vp.height)
					);
				}
			}
		}
		return viewBox_;
	}


	private static Map<String, String> systemFontFamilies_;

	/**
	 * Handles font related attributes and returns the calculated font.
	 */
	protected Font font(Font defaultFont)
	{
		Length fontSize = toLength("font-size", true);
		String fontFamily = attr("font-family", true);
		double fontWeight = fontWeight();

		if (fontSize == null) fontSize = new Length(12, LengthUnit.pt);
		if (ElementWrapper.isEmpty(fontFamily))
			fontFamily = defaultFont.getFamily();
		else
		{
			synchronized (ElementWrapper.class)
			{
				if (systemFontFamilies_ == null)
				{
					systemFontFamilies_ = new HashMap<>();
					try
					{
						String sysfams[] = GraphicsEnvironment.getLocalGraphicsEnvironment()
															  .getAvailableFontFamilyNames();
						for (String sysFam : sysfams)
							systemFontFamilies_.put(sysFam.toLowerCase(), sysFam);
					}
					catch (Exception e)
					{
					}
				}
			}
			final String[] fams = fontFamily.split(",");
			if (fams.length > 0)
			{
				String sysFF = null;
				for (String fam : fams)
				{
					sysFF = systemFontFamilies_.get(fam.trim()
													   .toLowerCase());
					if (sysFF != null)
						break;
				}
				fontFamily = sysFF == null ? fams[0] : sysFF;
			}
		}

		Map<TextAttribute, Object> attributes = new HashMap<>();
		attributes.put(TextAttribute.FAMILY, fontFamily);
		attributes.put(TextAttribute.WEIGHT, fontWeight);

		// font-size seems to use also pixel as unit.
		attributes.put(TextAttribute.SIZE, fontSize.toPixel(null));

		return Font.getFont(attributes);
	}

	/**
	 * Gets attribute opacity (none inherited)
	 */
	public float opacity()
	{
		if (opacity_ == null)
		{
			opacity_ = toPDouble("opacity", 1.0d, false);
		}
		return opacity_.floatValue();
	}

	/**
	 * Gets the effective opacity, combined from this element and ancestors.
	 */
	public double effectiveOpacity()
	{
		if (effectiveOpacity_ == null)
		{
			effectiveOpacity_ = opacity() * ((parent_ == null) ? 1d : parent_.effectiveOpacity());
		}
		return effectiveOpacity_.doubleValue();
	}


	/**
	 * Checks if the node has white-space-preservation on.
	 */
	public boolean preserveSpace()
	{
		if (preserveSpace_ == null)
		{
			final String ws = attr("white-space");
			preserveSpace_ = "preserve".equals(attr("xml:space")) || (ws != null && ws.startsWith("pre"));
		}
		return preserveSpace_;
	}

	/**
	 * Gets an attribute from this element or ancestors.<br>
	 * The value can be specified directly or via
	 * style-attribute.
	 */
	public String attr(String attributeName)
	{
		return attr(attributeName, true);
	}

	/**
	 * Gets an attribute from this element.<br>
	 * The value can be specified directly or via
	 * style-attribute.<br>
	 * Values from "style" (direct style-attribute or via style-sheet) have higher priority than attributes.
	 *
	 * @param inherited If true the attribute canbe inherited.
	 */
	public String attr(String attributeName, boolean inherited)
	{
		if (overrides_ != null)
		{
			String ov = overrides_.get(attributeName);
			if (isNotEmpty(ov))
				return ov;
		}
		String v = getStyleValue(attributeName);
		if (isEmpty(v))
		{
			v = node_.getAttribute(attributeName);
		}
		if (isEmpty(v) && inherited)
		{
			v = inherited(attributeName);
			if (isNotEmpty(v))
				attributes_.put(attributeName, new StyleValue(v, Specificity.MIN));
		}
		return v;
	}

	/**
	 * Local style attributes have higher priority than style-sheet-values.
	 */
	public String getStyleValue(String attributeName)
	{
		StyleValue sv = getStyleAttributes().get(attributeName);
		return sv == null ? null : sv.value_;
	}

	/**
	 * Get all attributes from the local style-attribute.<br>
	 */
	public Map<String, StyleValue> getStyleAttributes()
	{
		if (attributes_ == null)
		{
			Map<String, String> attrs = CSSParser.parseStyle(node_.getAttribute("style"));
			attributes_ = new HashMap<>();
			for (Map.Entry<String, String> e : attrs.entrySet())
			{
				//@TODO: Handle "!important"
				attributes_.put(e.getKey(), new StyleValue(e.getValue(), Specificity.MAX));
			}
		}
		return attributes_;
	}

	/**
	 * Overrides an attribute.
	 */
	public void override(String attributeName, String value)
	{
		if (value != null && isEmpty(attr(attributeName, false)))
		{
			if (overrides_ == null)
				overrides_ = new HashMap<>();
			overrides_.put(attributeName, value);
		}
	}

	/**
	 * Gets an attribute from parents.
	 */
	protected String inherited(String attributeName)
	{
		String v = null;
		ElementWrapper ancestor = parent_;
		while (v == null && ancestor != null)
		{
			v = ancestor.attr(attributeName);
			ancestor = ancestor.parent_;
		}
		return v;
	}

	public ElementCache getCache()
	{
		return elementCache_;
	}

	public Element getNode()
	{
		return node_;
	}

	/**
	 * Calls a function on each element of this sub-tree.
	 * Includes this element and all sub-elements.
	 *
	 * @param consumer The function to call.
	 */
	public void forSubTree(Consumer<ElementWrapper> consumer)
	{
		elementCache_.forSubTree(node_, consumer);
	}

	/**
	 * Get human-readable name of this node to identify it in the source.
	 */
	public String nodeName()
	{
		StringBuilder sb = new StringBuilder();
		sb.append(type_.name());
		final String id = id();
		if (!ElementCache.isGenerated(id))
			sb.append(' ')
			  .append(id);
		return sb.toString();
	}

	public List<ElementWrapper> getChildren()
	{
		List<ElementWrapper> children = new ArrayList<>();
		Node child = node_.getFirstChild();
		while (child != null)
		{
			while (child != null && child.getNodeType() != Node.ELEMENT_NODE)
				child = child.getNextSibling();
			if (child != null)
			{
				ElementWrapper w = elementCache_.getElementWrapper(child);
				if (isShadow_)
				{
					w = w.createReferenceShadow(this);
				}
				children.add(w);
				child = child.getNextSibling();
			}
		}
		return children;
	}
}

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
	static class OverrideItem
	{

		OverrideItem(String value, boolean localAttributeAdded)
		{
			value_ = value;
			localAttributeAdded_ = localAttributeAdded;
		}

		@Override
		public String toString()
		{
			return localAttributeAdded_ ? value_ + "*" : value_;
		}

		final String value_;
		final boolean localAttributeAdded_;
	}

	private Map<Attribute, StyleValue> attributes_;
	private Map<Attribute, OverrideItem> overrides_;
	private boolean isShadow_;
	private Set<String> classes_;
	private final Element node_;
	private ElementWrapper parent_;
	private final SvgTagType type_;
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
	protected static Double convDouble(String val, double absVal)
	{
		if (isNotEmpty(val))
			try
			{
				Matcher m = unitRegExp_.matcher(val);
				if (m.matches())
					return new Length(parseDouble(m.group(1)), LengthUnit.fromString(m.group(2))).toPixel(absVal);
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
		SvgTagType t = SvgTagType.valueFrom(node.getTagName());
		if (t == null)
		{
			// @TODO: Currently we don't care about real namespaces....
			String tn = node.getTagName();
			int nsIdx = tn.indexOf(':');
			if (nsIdx >= 0)
			{
				t = SvgTagType.valueFrom(tn.substring(nsIdx + 1));
			}
		}
		type_ = t;
	}

	public boolean isShadow()
	{
		return isShadow_;
	}

	public SvgTagType getType()
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
			String clazz = attr(Attribute.Class, false);
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
		String v = attr(Attribute.ClipPath, false).trim();
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
		String v = attr(Attribute.Marker_Mid, true);
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
		String v = attr(Attribute.Marker_Start, true);
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
		String v = attr(Attribute.Marker_End, true);
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
		String w = attr(Attribute.Font_Weight, true);
		if (isEmpty(w))
			return TextAttribute.WEIGHT_REGULAR;

		Float pref = fontWeights_.get(w.trim()
									   .toLowerCase());
		if (pref != null)
			return pref;
		Double d = convDouble(w, 1);
		return (d == null) ? TextAttribute.WEIGHT_REGULAR : (d * FONT_WEIGHT_FACTOR);
	}

	/**
	 * Get the length value of a none-inherited xml- or style-attribute.
	 *
	 * @return The length or null if the attribute doesn't exists.
	 */
	public Length toLength(Attribute attribute)
	{
		return toLength(attribute, false);
	}

	/**
	 * Get the length value of a xml- or style-attribute.
	 *
	 * @return The length or null if the attribute doesn't exists.
	 */
	public Length toLength(Attribute attribute, boolean inherited)
	{
		return parseLength(attr(attribute, inherited));
	}


	/**
	 * Get the double value of a none-inherited xml- or style-attribute as Double.
	 *
	 * @return The double or null if the attribute doesn't exists.
	 */
	public Double toDouble(Attribute attribute, double absVal)
	{
		return convDouble(attr(attribute, false), absVal);
	}

	/**
	 * Get the primitive double value of a none-inherited xml- or style-attribute.
	 *
	 * @return The double or 0 if the attribute doesn't exists.
	 */
	public double toPDouble(Attribute attribute, double absVal)
	{
		return toPDouble(attribute, absVal, false);
	}

	/**
	 * Get the double value of a xml- or style-attribute.
	 *
	 * @param inherited If true and the attribute doesn't exists also the parent nodes are scanned.
	 * @return The double or null if the attribute doesn't exists.
	 */
	public Double toDouble(Attribute attribute, double absVal, boolean inherited)
	{
		return convDouble(attr(attribute, inherited), absVal);
	}

	/**
	 * Get the primitive double value of a xml- or style-attribute.
	 * Default is 0.
	 *
	 * @param inherited If true and the attribute doesn't exists also the parent nodes are scanned.
	 * @return The double or 0 if the attribute doesn't exists.
	 */
	public double toPDouble(Attribute attribute, double absVal, boolean inherited)
	{
		return toPDouble(attribute, 0d, absVal, inherited);
	}

	/**
	 * Get the primitive double value of a xml- or style-attribute.
	 *
	 * @param inherited If true and the attribute doesn't exists also the parent nodes are scanned.
	 * @return The double or 0 if the attribute doesn't exists.
	 */
	public double toPDouble(Attribute attribute, double defaultValue, double absVal, boolean inherited)
	{
		Double d = convDouble(attr(attribute, inherited), absVal);
		return d == null ? defaultValue : d;
	}

	/**
	 * Get list of doubles
	 *
	 * @return List, never null but possible empty.
	 */
	public List<Double> toPDoubleList(Attribute attribute, final double absVal, boolean inherited)
	{
		final String val = attr(attribute, inherited);
		LengthList l;
		if (val != null)
			return new LengthList(val).getLengthList()
									  .stream()
									  .map(length -> length.toPixel(absVal))
									  .collect(Collectors.toList());
		else
			return Collections.emptyList();
	}


	public LengthList toLengthList(Attribute attribute, boolean inherited)
	{
		final String val = attr(attribute, inherited);
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

	public static double convPDouble(String value, double absVal)
	{
		Double d = convDouble(value, absVal);
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
		return normalizeText(node.getTextContent());
	}

	public String normalizeText(String text)
	{

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
								sb.append(' ');
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
	 * Gets the transform on this element.
	 */
	public AffineTransform transform()
	{
		if (aft_ == null)
		{
			String transform = attr(Attribute.Transform, false);
			if (isNotEmpty(transform))
				aft_ = new SvgTransform(null, transform).getTransform();
			else
				aft_ = new AffineTransform();
		}
		return (aft_ == null || aft_.isIdentity()) ? null : aft_;
	}

	public void applyTransform(AffineTransform aft)
	{
		transform();
		aft_.concatenate(aft);
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
		uw.parent_ = usingElement.parent_;
		String tag = uw.getTagName();
		if (tag.equals("svg") || tag.equals("symbol"))
		{
			// @TODO: set width and height of viewbox
		}

		// Take over overrides from using element...
		if (usingElement.overrides_ != null)
		{
			for (Map.Entry<Attribute, OverrideItem> i : usingElement.overrides_.entrySet())
			{
				Attribute attr = i.getKey();
				uw.override(attr, i.getValue().value_, false);
			}
		}

		// Take over attributes of using element
		NamedNodeMap attributes = usingElement.getNode()
											  .getAttributes();
		int nAttr = attributes.getLength();
		for (int iAttr = 0; iAttr < nAttr; ++iAttr)
		{
			Node attrNode = attributes.item(iAttr);
			Attribute attr = Attribute.valueFrom(attrNode.getNodeName());
			if (attr != null)
			{
				if (attr != Attribute.Transform && attr != Attribute.ClipPath && attr != Attribute.X && attr != Attribute.Y)
				{
					if (!usingElement.isOverrideFromAttribute(attr))
						// Add any attribute that is not already in overrides
						uw.override(attr, attrNode.getNodeValue(), false);
				}
			}
		}

		Map<Attribute, StyleValue> styleAttributes = usingElement.getStyleAttributes();
		for (Map.Entry<Attribute, StyleValue> styleAttr : styleAttributes.entrySet())
		{
			Attribute attr = styleAttr.getKey();
			uw.override(attr, styleAttr.getValue().value_, false);
		}
		return uw;
	}

	public ElementWrapper createReferenceShadowChild(ElementWrapper parent)
	{
		ElementWrapper uw = new ElementWrapper(elementCache_, getNode(), true);
		uw.parent_ = parent;
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

	public double getViewPortWidth()
	{
		if (viewPort_ == null) getViewPort();
		return viewPort_.width;
	}

	public double getViewPortHeight()
	{
		if (viewPort_ == null) getViewPort();
		return viewPort_.height;
	}


	public Rectangle2D.Double getViewPort()
	{
		if (viewPort_ == null)
		{
			Length x = toLength(Attribute.X, true);
			Length y = toLength(Attribute.Y, true);
			Length width = toLength(Attribute.Width, true);
			Length height = toLength(Attribute.Height, true);

			String viewBox = attr(Attribute.ViewBox, true);
			if (viewBox != null)
			{
				Viewbox vb = new Viewbox(viewBox);
				if (width == null) width = vb.width;
				if (height == null) height = vb.height;
			}

			if (x == null) x = new Length(0, LengthUnit.px);
			if (y == null) y = new Length(0, LengthUnit.px);

			if (width == null) width = new Length(100, LengthUnit.px);
			if (height == null) height = new Length(100, LengthUnit.px);

			Double absW = (parent_ != null) ? parent_.getViewPortWidth() : null;
			Double absH = (parent_ != null) ? parent_.getViewPortHeight() : null;
			viewPort_ = new Rectangle2D.Double(x.toPixel(absW), y.toPixel(absH), width.toPixel(absW), height.toPixel(absH));
		}
		return viewPort_;
	}

	public Rectangle2D.Double getViewBox()
	{
		if (!viewBoxRetrieved_)
		{
			viewBoxRetrieved_ = true;
			LengthList l = toLengthList(Attribute.ViewBox, true);
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
		Length fontSize = toLength(Attribute.FontSize, true);
		String fontFamily = attr(Attribute.FontFamily, true);
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
			opacity_ = toPDouble(Attribute.Opacity, 1.0d, 1.0d, false);
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
			final String ws = attr(Attribute.WhiteSpace);
			preserveSpace_ = "preserve".equals(attr(Attribute.XmlSpace)) || (ws != null && ws.startsWith("pre"));
		}
		return preserveSpace_;
	}

	/**
	 * Gets an attribute from this element or ancestors.<br>
	 * The value can be specified directly or via
	 * style-attribute.
	 */
	public String attr(Attribute attribute)
	{
		return attr(attribute, true);
	}


	/**
	 * Gets an attribute from this element.<br>
	 * The value can be specified directly or via
	 * style-attribute.<br>
	 * Values from "style" (direct style-attribute or via style-sheet) have higher priority than attributes.
	 *
	 * @param inherited If true the attribute can be inherited.
	 */
	public String attr(Attribute attribute, boolean inherited)
	{
		String r = getOverride(attribute);
		if (isEmpty(r))
			r = attrWithoutOverrides(attribute, inherited);
		return r;
	}

	protected String getOverride(Attribute attribute)
	{
		OverrideItem i = (overrides_ != null) ? overrides_.get(attribute) : null;
		if (i == null)
			return null;
		else
			return i.value_;
	}

	protected boolean isOverrideFromAttribute(Attribute attribute)
	{
		OverrideItem i = (overrides_ != null) ? overrides_.get(attribute) : null;
		return (i == null) ? false : i.localAttributeAdded_;

	}

	private String attrWithoutOverrides(Attribute attribute, boolean inherited)
	{
		String v = getStyleValue(attribute);
		if (isEmpty(v))
		{
			v = node_.getAttribute(attribute.xmlName());
		}
		if (isEmpty(v) && inherited)
		{
			v = inherited(attribute);
			if (isNotEmpty(v))
				attributes_.put(attribute, new StyleValue(v, Specificity.MIN));
		}
		return v;
	}


	/**
	 * Local style attributes have higher priority than style-sheet-values.
	 */
	public String getStyleValue(Attribute attribute)
	{
		StyleValue sv = getStyleAttributes().get(attribute);
		return sv == null ? null : sv.value_;
	}

	/**
	 * Get all attributes from the local style-attribute.<br>
	 */
	public Map<Attribute, StyleValue> getStyleAttributes()
	{
		if (attributes_ == null)
		{
			Map<Attribute, String> attrs = CSSParser.parseStyle(node_.getAttribute(Attribute.Style.xmlName()));
			attributes_ = new HashMap<>();
			for (Map.Entry<Attribute, String> e : attrs.entrySet())
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
	public void override(Attribute attribute, String value, boolean fromAttribute)
	{
		if (isNotEmpty(value))
		{
			String orgValue = attrWithoutOverrides(attribute, false);
			boolean aggregate =
					(attribute == Attribute.X ||
							attribute == Attribute.Y ||
							attribute == Attribute.Transform);

			if (aggregate)
			{
				if (overrides_ != null)
				{
					OverrideItem inherited = overrides_.get(attribute);
					if (inherited != null)
					{
						fromAttribute = inherited.localAttributeAdded_;
						if (fromAttribute)
						{
							// Values was already set from local attribute.
							value = inherited.value_;
						}
					}
				}
				if (isNotEmpty(orgValue))
				{
					fromAttribute = true;
					orgValue = null;
				}
			}

			if (value != null && isEmpty(orgValue))
			{
				if (overrides_ == null)
					overrides_ = new HashMap<>();
				OverrideItem ovi = new OverrideItem(value, fromAttribute);
				overrides_.put(attribute, ovi);
			}
		}
	}

	/**
	 * Gets an attribute from parents.
	 */
	protected String inherited(Attribute attribute)
	{
		String v = null;
		ElementWrapper ancestor = parent_;
		while (v == null && ancestor != null)
		{
			v = ancestor.attr(attribute);
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
				if ((!elementCache_.isNamespaceAware()) || SVGConverter.SVG_NAME_SPACE.equals(child.getNamespaceURI()))
				{
					ElementWrapper w = elementCache_.getElementWrapper(child);
					if (isShadow_)
					{
						w = w.createReferenceShadowChild(this);
						w.parent_ = this;
					}
					children.add(w);
				}
				child = child.getNextSibling();
			}
		}
		return children;
	}

	public ElementWrapper getParent()
	{
		return parent_;
	}
}

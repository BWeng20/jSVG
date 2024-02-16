package com.bw.jtools.svg;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.awt.Font;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphMetrics;
import java.awt.font.GlyphVector;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class SvgText extends Parser
{
	private static final FontRenderContext frc_ = new FontRenderContext(null, false, false);

	protected final double x_;
	protected final double y_;


	public enum TextAnchor
	{
		start,
		middle,
		end;

		public static TextAnchor valueFrom(String anchor)
		{
			if (anchor != null)
			{
				anchor = anchor.toLowerCase();
				if (anchor.equals("start")) return start;
				if (anchor.equals("middle")) return middle;
				if (anchor.equals("end")) return end;
			}
			return null;

		}
	}

	public enum TextDecoration
	{
		Underline,
		LineThrough,
		Overline,
		None;

		public static TextDecoration valueFrom(String textDeco)
		{
			if (textDeco != null)
			{
				textDeco = textDeco.trim()
								   .toLowerCase();
				if (textDeco.equals("underline")) return Underline;
				if (textDeco.equals("line-through")) return LineThrough;
				if (textDeco.equals("overline")) return Overline;
				if (textDeco.equals("none")) return None;
			}
			return null;
		}

		public static List<TextDecoration> valueFromList(String textDecoList)
		{
			if (textDecoList == null)
				return Collections.emptyList();
			String[] parts = textDecoList.split("\\s");
			List<TextDecoration> result = new ArrayList<>(parts.length);
			for (String p : parts)
			{
				TextDecoration deco = valueFrom(p);
				if (deco != null)
					result.add(deco);
			}
			return result;
		}

	}


	public enum LengthAdjust
	{
		spacing,
		spacingAndGlyphs;

		public static LengthAdjust valueFrom(String lengthAdjust)
		{
			if (lengthAdjust != null)
			{
				lengthAdjust = lengthAdjust.toLowerCase();
				if (lengthAdjust.equals("spacing")) return spacing;
				if (lengthAdjust.equals("spacingandglyphs")) return spacingAndGlyphs;
			}
			return null;

		}
	}

	/**
	 * Helper for processing outlines of text fragments.
	 */
	protected static final class TextShape
	{

		final String text;
		Shape outline;

		/**
		 * Addition space needed at the end of the outline.
		 */
		final double additionalSpace;

		/**
		 * Creates a new Text Shape.
		 *
		 * @param text The text.
		 * @param font The fint to use.
		 */
		TextShape(String text, List<TextDecoration> decorations, Font font)
		{
			this.text = text;
			if (decorations.isEmpty() && (!text.isEmpty()) && ' ' == text.charAt(text.length() - 1))
			{
				additionalSpace = font.getStringBounds(" ", frc_)
									  .getWidth();
			}
			else
			{
				additionalSpace = 0;
			}
			outline = createTextOutline(font, decorations, text);
		}
	}

	public SvgText(SVGConverter svg, ElementWrapper w, Font defaultFont, List<ElementInfo> shapes)
	{
		super();

		// @TODO: Multiple values for x/y
		x_ = w.toPDouble(Attribute.X, w.getViewPortWidth(), false) + w.toPDouble(Attribute.Dx, w.getViewPortWidth(), false);
		y_ = w.toPDouble(Attribute.Y, w.getViewPortHeight(), false) + w.toPDouble(Attribute.Dy, w.getViewPortHeight(), false);

		Rectangle2D.Double box = w.getViewPort();

		NodeList nodes = w.getNode()
						  .getChildNodes();

		Node childNode;

		Point2D.Double pos = new Point2D.Double(x_, y_);
		final ElementCache cache = w.getCache();
		final Font font = w.font(defaultFont);

		StringBuilder textContent = new StringBuilder();

		// tspans aggregate following text content
		ElementWrapper lastTSpanElement = null;
		ElementWrapper lastTextPathElement = null;

		for (int idx = 0; idx < nodes.getLength(); ++idx)
		{
			childNode = nodes.item(idx);
			switch (childNode.getNodeType())
			{
				case Node.TEXT_NODE:
					textContent.append(w.text(childNode));
					break;
				case Node.ELEMENT_NODE:
					if (lastTSpanElement != null)
					{
						parseTSpan(svg, w, lastTSpanElement, textContent, font, pos, box, shapes);
					}
					else if (lastTextPathElement != null)
					{
						parseTextPath(svg, w, lastTextPathElement, textContent, font, shapes, pos);
						// Text Path resets position accumulation.
						pos.x = 0;
						pos.y = 0;
					}
					else
					{
						parseTextContent(svg, w, textContent, font, pos, box, shapes);
					}
					lastTSpanElement = null;
					lastTextPathElement = null;

					ElementWrapper cw = cache.getElementWrapper(childNode);
					SvgTagType t = cw.getType();
					if (t == SvgTagType.tspan)
					{
						lastTSpanElement = cw;
					}
					else if (t == SvgTagType.textPath)
					{
						lastTextPathElement = cw;
					}
			}
		}
		if (textContent.length() > 0)
		{
			if (lastTSpanElement != null)
			{
				parseTSpan(svg, w, lastTSpanElement, textContent, font, pos, box, shapes);
			}
			else if (lastTextPathElement != null)
			{
				parseTextPath(svg, w, lastTextPathElement, textContent, font, shapes, pos);
			}
			else
			{
				parseTextContent(svg, w, textContent, font, pos, box, shapes);
			}
		}
	}

	protected void parseTextContent(SVGConverter svg, ElementWrapper ew, StringBuilder textContent, Font defaultFont, Point2D.Double pos, Rectangle2D.Double box, List<ElementInfo> shapes)
	{
		Length dx = ew.toLength(Attribute.Dx);
		Length dy = ew.toLength(Attribute.Dy);
		TextAnchor anchor = TextAnchor.valueFrom(ew.attr(Attribute.TextAnchor, true));
		List<TextDecoration> decorations = decorations(ew);


		// @TODO: x,y,dx,dy: can contain a list of values

		// @TODO: rotate
		// @TODO: textLength
		// @TODO: lengthAdjust

		if (dx != null)
			pos.x += dx.toPixel(box.getWidth());
		if (dy != null)
			pos.y += dy.toPixel(box.getHeight());

		final TextShape textShape = new TextShape(ew.normalizeText(textContent.toString()), decorations, ew.font(defaultFont));
		adaptPositionForTextOutline(List.of(textShape), pos, anchor);
		ew.setShape(textShape.outline);
		shapes.add(svg.createShapeInfo(ew));

		textContent.setLength(0);
	}

	protected void parseTSpan(SVGConverter svg, ElementWrapper parent, ElementWrapper ew, StringBuilder additionalContent, Font defaultFont, Point2D.Double pos, Rectangle2D.Double box, List<ElementInfo> shapes)
	{
		Length x = ew.toLength(Attribute.X);
		Length y = ew.toLength(Attribute.Y);
		Length dx = ew.toLength(Attribute.Dx);
		Length dy = ew.toLength(Attribute.Dy);
		TextAnchor anchor = TextAnchor.valueFrom(ew.attr(Attribute.TextAnchor, true));
		List<TextDecoration> decorations = decorations(ew);


		// @TODO: x,y,dx,dy: can contain a list of values

		// @TODO: rotate
		// @TODO: textLength
		// @TODO: lengthAdjust

		if (x == null)
		{
			// On X relative mode force anchor to "start"
			anchor = TextAnchor.start;
		}
		else
		{
			pos.x = x.toPixel(box.getWidth());
		}
		if (y != null)
		{
			pos.y = y.toPixel(box.getHeight());
		}

		if (dx != null)
		{
			pos.x += dx.toPixel(box.getWidth());
		}
		if (dy != null)
		{
			pos.y += dy.toPixel(box.getHeight());
		}

		TextShape textShape = new TextShape(ew.normalizeText(ew.text()), decorations, ew.font(defaultFont));
		TextShape additionTextShape;

		List<TextShape> textShapeList = new ArrayList<>(2);
		textShapeList.add(textShape);

		String additionalText = ew.normalizeText(additionalContent.toString());
		if (!additionalText.isEmpty())
		{
			textShapeList.add(new TextShape(additionalText, decorations(parent), parent.font(defaultFont)));
		}
		additionalContent.setLength(0);

		adaptPositionForTextOutline(textShapeList, pos, anchor);

		shapes.add(svg.createShapeInfo(ew, new ShapeHelper(textShapeList.get(0).outline)));
		if (textShapeList.size() > 1)
			shapes.add(svg.createShapeInfo(parent, new ShapeHelper(textShapeList.get(1).outline)));

	}

	protected void parseTextPath(SVGConverter svg, ElementWrapper parent, ElementWrapper ew,
								 StringBuilder additionalContent,
								 Font defaultFont, List<ElementInfo> shapes, Point2D.Double pos)
	{
		String href = ew.href();
		if (ElementWrapper.isNotEmpty(href))
		{
			ElementWrapper pw = ew.getCache()
								  .getElementWrapperById(href);
			if (pw != null && SvgTagType.path == pw.getType())
			{
				pw = pw.createReferenceShadow(ew);

				ShapeHelper path = pw.getShape();
				if (path == null)
				{
					pw.setShape(new Path(pw.attr(Attribute.D, false)).getPath());
					path = pw.getShape();
				}
				AffineTransform aft = pw.transform();
				if (aft != null)
					path = new ShapeHelper(aft.createTransformedShape(path.getShape()));

				Double startOffset = ew.toDouble(Attribute.StartOffset, 1, false);
				Double textLength = ew.toDouble(Attribute.TextLength, 1, false);
				LengthAdjust adjust = LengthAdjust.valueFrom(ew.attr(Attribute.LengthAdjust, true));
				// @TODO spacing

				TextAnchor anchor = TextAnchor.valueFrom(ew.attr(Attribute.TextAnchor, true));
				List<TextDecoration> decorations = decorations(ew);

				final Font font = ew.font(defaultFont);

				Shape textLayout = layoutText(font, ew.text(), decorations, path, anchor,
						startOffset == null ? 0 : startOffset.doubleValue(),
						textLength == null ? 0 : textLength.doubleValue(), adjust);
				shapes.add(svg.createShapeInfo(ew, new ShapeHelper(textLayout)));

				ShapeHelper.PointOnPath endPoint = path.pointAtLength(path.getOutlineLength() - 0.001);

				String additionalText = ew.normalizeText(additionalContent.toString());
				if (!additionalText.isEmpty())
				{
					TextShape additionTextShape = new TextShape(additionalText, decorations(parent), font);
					adaptPositionForTextOutline(List.of(additionTextShape), new Point2D.Double(endPoint.x_, endPoint.y_), TextAnchor.start);
					shapes.add(svg.createShapeInfo(parent, new ShapeHelper(additionTextShape.outline)));
				}
				additionalContent.setLength(0);
			}
		}
	}

	protected List<TextDecoration> decorations(ElementWrapper ew)
	{
		return TextDecoration.valueFromList(ew.attr(Attribute.TextDecoration, true));
	}

	protected static Map<TextAttribute, Object> decorationsToTextAttribute(List<TextDecoration> decorations)
	{
		Map<TextAttribute, Object> attributes = new HashMap<>();
		for (TextDecoration decoration : decorations)
		{
			switch (decoration)
			{
				case LineThrough:
					attributes.put(TextAttribute.STRIKETHROUGH, TextAttribute.STRIKETHROUGH_ON);
					break;
				case Overline:
					// @TODO: do it yourself?
					break;
				case Underline:
					attributes.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
					break;
				case None:
					break;
			}
		}
		return attributes;
	}


	/**
	 * Moved the text outlines to calculated positions.
	 *
	 * @param textShapes List of text outlines parts
	 * @param pos        Base position to move to. Will be adapted
	 * @param anchor     Anchor mode.
	 */
	protected void adaptPositionForTextOutline(List<TextShape> textShapes, Point2D.Double pos, TextAnchor anchor)
	{
		double width = 0;
		for (TextShape s : textShapes)
		{
			width += s.outline.getBounds2D()
							  .getWidth();

		}
		if (anchor != null)
		{
			double offset;
			switch (anchor)
			{
				case end:
					offset = width;
					break;
				case middle:
					offset = width / 2;
					break;
				default: // start
					offset = 0;
					break;
			}
			pos.x -= offset;
		}
		for (TextShape s : textShapes)
		{
			AffineTransform aft = AffineTransform.getTranslateInstance(pos.x, pos.y);
			s.outline = aft.createTransformedShape(s.outline);
			pos.x += s.outline.getBounds2D()
							  .getWidth() + s.additionalSpace;
		}
	}


	protected static Shape createTextOutline(Font font, List<TextDecoration> decorations, String text)
	{
		if (text == null || text.isEmpty())
		{
			return new Rectangle2D.Double(0, 0, 0, 0);
		}
		else
		{
			Map<TextAttribute, Object> attributes = decorationsToTextAttribute(decorations);
			attributes.put(TextAttribute.FONT, font);
			return new TextLayout(text, attributes, frc_).getOutline(null);
		}
	}

	/**
	 * Creates text along a path
	 */
	public static Shape layoutText(Font font, String text, List<TextDecoration> decorations,
								   ShapeHelper path, TextAnchor align,
								   double startOffset,
								   double textLength,
								   LengthAdjust lengthAdjustMode)
	{
		Path2D.Double newPath = new Path2D.Double();

		if (text == null || font == null || path == null || path.getOutlineLength() == 0d)
			return newPath;

		// TextAttribute seems not to work via GlyphVector and TextLayout can't be used here.

		GlyphVector glyphs = font.createGlyphVector(frc_, text);
		if (glyphs == null || glyphs.getNumGlyphs() == 0)
			return newPath;

		double glyphsLength = glyphs.getVisualBounds()
									.getWidth();
		if (glyphsLength == 0d)
			return newPath;

		if (textLength == 0) textLength = glyphsLength;
		double pathLength = path.getOutlineLength();

		double charScale = textLength / glyphsLength;

		double currentPosition = startOffset;

		if (align == TextAnchor.end)
			currentPosition -= textLength;
		else if (align == TextAnchor.middle)
			currentPosition -= textLength / 2;

		AffineTransform glyphTrans = new AffineTransform();

		for (int i = 0; i < glyphs.getNumGlyphs(); ++i)
		{
			Shape glyph = glyphs.getGlyphOutline(i);
			Point2D p = glyphs.getGlyphPosition(i);
			GlyphMetrics gm = glyphs.getGlyphMetrics(i);

			float advance = gm.getAdvance();

			if (lengthAdjustMode == LengthAdjust.spacingAndGlyphs)
			{
				AffineTransform scale = AffineTransform.getScaleInstance(charScale, 1.0f);
				glyph = scale.createTransformedShape(glyph);
				advance *= charScale;
			}

			Rectangle2D bounds = glyph.getBounds2D();
			double glyphWidth = bounds.getWidth();
			double charMidPos = currentPosition + (advance / 2d);

			final ShapeHelper.PointOnPath point = path.pointAtLength(charMidPos);

			// To trace the reference points on the path:
			// Rectangle2D dot = new Rectangle2D.Double(point.x_-1, point.y_-1,2, 2);
			// newPath.append( dot, false);

			if (point != null)
			{
				glyphTrans.setToTranslation(point.x_, point.y_);
				glyphTrans.rotate(point.angle_);
				glyphTrans.translate(-p.getX() - advance / 2d, -p.getY());
				newPath.append(glyphTrans.createTransformedShape(glyph), false);
			}
			currentPosition += (lengthAdjustMode == LengthAdjust.spacing) ? (advance * charScale) : advance;
		}

		// @TODO: Implement somehow decorations.

		return newPath;
	}

}

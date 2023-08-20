package com.bw.jtools.svg;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.awt.Font;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphMetrics;
import java.awt.font.GlyphVector;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.List;


public class Text extends Parser
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

	public Text(SVGConverter svg, ElementWrapper w, Font defaultFont, List<ElementInfo> shapes)
	{
		super();

		// @TODO: Multiple values for x/y
		x_ = w.toPDouble(Attribute.X, w.getViewPortWidth(), false);
		y_ = w.toPDouble(Attribute.Y, w.getViewPortHeight(), false);

		Rectangle2D.Double box = w.getViewPort();

		NodeList nodes = w.getNode()
						  .getChildNodes();

		Node childNode;

		Point2D.Double pos = new Point2D.Double(x_, y_);
		final ElementCache cache = w.getCache();
		final Font font = w.font(defaultFont);

		for (int idx = 0; idx < nodes.getLength(); ++idx)
		{
			childNode = nodes.item(idx);
			switch (childNode.getNodeType())
			{
				case Node.TEXT_NODE:
					String text = w.text(childNode);
					if (ElementWrapper.isNotEmpty(text))
					{
						w.setShape(createText(font, text, pos));
						shapes.add(svg.createShapeInfo(w));
					}
					break;
				case Node.ELEMENT_NODE:
					ElementWrapper cw = cache.getElementWrapper(childNode);
					final String tag = cw.getTagName();
					if ("tspan".equals(tag))
						parseTSpan(svg, cw, font, pos, box, shapes);
					else if ("textPath".equals(tag))
						parseTextPath(svg, cw, font, shapes);
			}
		}
	}

	protected void parseTSpan(SVGConverter svg, ElementWrapper ew, Font defaultFont, Point2D.Double pos, Rectangle2D.Double box, List<ElementInfo> shapes)
	{
		Length x = ew.toLength(Attribute.X);
		Length y = ew.toLength(Attribute.Y);
		Length dx = ew.toLength(Attribute.Dx);
		Length dy = ew.toLength(Attribute.Dy);

		// @TODO: x,y,dx,dy: can contain a list of values

		// @TODO: rotate
		// @TODO: textLength
		// @TODO: lengthAdjust

		if (x != null)
			pos.x = x.toPixel(box.getWidth());
		if (y != null)
			pos.y = y.toPixel(box.getHeight());

		if (dx != null)
			pos.x += dx.toPixel(box.getWidth());
		if (dy != null)
			pos.y += dy.toPixel(box.getHeight());

		ew.setShape(createText(ew.font(defaultFont), ew.text(), pos));
		shapes.add(svg.createShapeInfo(ew));
	}

	protected void parseTextPath(SVGConverter svg, ElementWrapper ew, Font defaultFont, List<ElementInfo> shapes)
	{
		String href = ew.href();
		if (ElementWrapper.isNotEmpty(href))
		{
			ElementWrapper pw = ew.getCache()
								  .getElementWrapperById(href);
			if (pw != null && Type.path == pw.getType())
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

				// @TODO: Adapt pos
				ew.setShape(layoutText(ew.font(defaultFont), ew.text(), path, anchor,
						startOffset == null ? x_ : x_ + startOffset.doubleValue(),
						textLength == null ? 0 : textLength.doubleValue(), adjust));
				shapes.add(svg.createShapeInfo(ew));
			}
		}
	}

	protected Shape createText(Font font, String text, Point2D.Double pos)
	{
		if (text == null || text.isEmpty())
		{
			return new Rectangle2D.Double(pos.x, pos.y, 0, 0);
		}
		else
		{
			TextLayout tl = new TextLayout(text, font, frc_);
			Rectangle2D bounds = tl.getBounds();
			Shape shape = tl.getOutline(AffineTransform.getTranslateInstance(pos.x, pos.y));
			pos.x += bounds.getWidth();
			return shape;
		}
	}

	/**
	 * Creates text along a path
	 */
	public static Shape layoutText(Font font, String text,
								   ShapeHelper path, TextAnchor align,
								   double startOffset,
								   double textLength,
								   LengthAdjust lengthAdjustMode)
	{
		Path2D.Double newPath = new Path2D.Double();

		if (text == null || font == null || path == null || path.getOutlineLength() == 0d)
			return newPath;

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
			currentPosition += pathLength - textLength;
		else if (align == TextAnchor.middle)
			currentPosition += (pathLength - textLength) / 2;

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

		return newPath;
	}

}

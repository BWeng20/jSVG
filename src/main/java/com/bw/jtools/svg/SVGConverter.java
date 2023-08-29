package com.bw.jtools.svg;

import com.bw.jtools.shape.AbstractShape;
import com.bw.jtools.shape.ShapeGroup;
import com.bw.jtools.shape.StyledShape;
import com.bw.jtools.shape.filter.FilterBase;
import com.bw.jtools.shape.filter.FilterChain;
import com.bw.jtools.shape.filter.GaussianBlur;
import com.bw.jtools.shape.filter.Merge;
import com.bw.jtools.shape.filter.Nop;
import com.bw.jtools.shape.filter.Offset;
import com.bw.jtools.svg.css.CSSParser;
import com.bw.jtools.svg.css.CssStyleSelector;
import com.bw.jtools.ui.ShapeIcon;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.awt.Font;
import java.awt.MultipleGradientPaint;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RectangularShape;
import java.awt.geom.RoundRectangle2D;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.bw.jtools.svg.ElementWrapper.isNotEmpty;

/**
 * Parses and converts an SVG document into Java2D-shapes plus basic style-information (see {@link StyledShape}).<br>
 * <br>
 * Currently supported features:
 * <ul>
 * <li>g</li>
 * <li>path</li>
 * <li>rect</li>
 * <li>circle</li>
 * <li>ellipse</li>
 * <li>line</li>
 * <li>polyline</li>
 * <li>polygon</li>
 * <li>text (with tspan and textPath, only single x,y,dx,dy length values, no textLength, no lengthAdjust, no rotate, no content area)</li>
 * <li>use</li>
 * <li>linearGradient (without stop-opacity)</li>
 * <li>radialGradient (without stop-opacity)</li>
 * <li>clipPath (only direct use by attribute clip-path, no inheritance)</li>
 * <li>filter (partial and only simple scenarios)</li>
 * </ul>
 * Filter stuff that needs offline-rendering (like blur) is very slow.
 * Filter stuff that needs offline-rendering (like blur) is very slow.
 * Don't use them if you need to render fast (or draw to an off-screen-buffer).<br>
 * The SVG specification contains a lot of filter cases, but most SVG graphics doesn't
 * use such stuff. So the conversion to Java2D shapes is an efficient way to draw
 * simple scalable graphics. Drawing these shapes is very fast. They can also be drawn with any transformation without loss in quality.<br>
 * See {@link com.bw.jtools.shape.ShapePainter} and {@link ShapeIcon}.<br>
 * For usage see the example {@link com.bw.jtools.SVGViewer}.
 */
public class SVGConverter
{

	/**
	 * Helper method to dump SVG warnings that may need user-attention.<br>
	 * Can be used by other classes.
	 */
	public static void warn(String s, Object... params)
	{
		System.out.print("SVG Warning: ");
		System.out.printf(s, params);
		System.out.println();
	}

	/**
	 * Helper method to dump SVG errors that may need user-attention.<br>
	 * Can be used by other classes.
	 */
	public static void error(Throwable t, String s, Object... params)
	{
		error(s, params);
		if (t != null)
		{
			if (detailedErrorInformation_)
				t.printStackTrace(System.err);
			else
				System.err.println(t.getClass()
									.getSimpleName() + ": " + t.getMessage());
		}
	}

	/**
	 * Helper method to dump SVG errors that may need user-attention.<br>
	 * Can be used by other classes.
	 */
	public static void error(String s, Object... params)
	{
		System.err.print("SVG Error: ");
		System.err.printf(s, params);
		System.err.println();
	}

	private ShapeGroup finalShape_;
	private Map<String, Gradient> paintServer_ = new HashMap<>();
	private Map<String, PaintWrapper> paints_ = new HashMap<>();
	private Font defaultFont_ = Font.decode("Arial-PLAIN-12");
	private final ElementCache elementCache_ = new ElementCache();

	public static final boolean addPathSegments_ = false;

	public static boolean detailedErrorInformation_ = false;

	/**
	 * If true experimental features are enabled.
	 */
	public static boolean experimentalFeaturesEnables_ = false;

	/**
	 * Parse an SVG document and creates shapes.
	 * After creation call {@link #getShape()} to retrieve the resulting shapes.<br>
	 *
	 * @param xml The svg document.
	 */
	public SVGConverter(final String xml) throws SVGException
	{
		this(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
	}

	/**
	 * Parse an SVG document and creates shapes.
	 * After creation call {@link #getShape()} to retrieve the resulting shapes.<br>
	 *
	 * @param in Input-Stream to the svg document.
	 */
	public SVGConverter(final InputStream in) throws SVGException
	{
		try
		{
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			dbf.setValidating(false);
			dbf.setIgnoringComments(true);
			dbf.setIgnoringElementContentWhitespace(true);

			dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);

			// Loading a DTD/Schema will slow down processing by several seconds (if schema is specified).
			// Suppress loading of references dtd/schema. This will also deactivate validation and
			// id processing (see scanForIDs call below).
			dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
			dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse(in);

			// Without schema processing (see above), "id" attributes will not be detected as key
			// and "getElementById" will not work. So we have to collect the Ids manually.
			elementCache_.scanForIds(doc);

			// Parse styles and apply them
			NodeList styles = doc.getElementsByTagName(Type.style.name());
			if (styles.getLength() > 0)
			{
				CSSParser cssParser = new CSSParser();
				CssStyleSelector cssStyleSelector = new CssStyleSelector();
				for (int i = 0; i < styles.getLength(); ++i)
				{
					cssParser.parse(styles.item(i)
										  .getTextContent(), null, cssStyleSelector);
				}
				cssStyleSelector.apply(doc.getDocumentElement(), elementCache_);
			}

			// @TODO patterns
			// NodeList patterns = doc.getElementsByTagName("pattern");

			ElementWrapper svg = getCache().getElementWrapper(doc.getElementsByTagName("svg")
																 .item(0));

			List<ElementInfo> shapes = new ArrayList<>();
			parseChildren(shapes, svg);

			// Create an enclosing group and set the viewBox as clip-path.
			// "Height" and "Width" is currently not supported.

			String viewBox = svg.attr(Attribute.ViewBox);

			finalShape_ = new ShapeGroup(svg.id(), null, (viewBox == null) ? null : new Viewbox(viewBox).getShape(), null);

			for (ElementInfo s : shapes)
				finalShape_.shapes_.add(finish(s));
			shapes.clear();

		}
		catch (Exception e)
		{
			throw new SVGException("Failed to parse SVG", e);
		}
	}

	/**
	 * Convenience replacement for <i>new SVGConverter(is).getShapes()</i>.
	 *
	 * @param is The insvg document.
	 * @return The converted shapes.
	 * @throws SVGException In case of any error.
	 */
	public static AbstractShape convert(final InputStream is) throws SVGException
	{
		return new SVGConverter(is).getShape();
	}

	/**
	 * Convenience replacement for <i>new SVGConverter(xml).getShapes()</i>.
	 *
	 * @param xml The svg document.
	 * @return The converted shapes.
	 * @throws SVGException In case of any error.
	 */
	public static AbstractShape convert(final String xml) throws SVGException
	{
		return new SVGConverter(xml).getShape();
	}


	private void parseChildren(List<ElementInfo> shapes, ElementWrapper parent)
	{
		for (ElementWrapper child : parent.getChildren())
		{
			parseElement(shapes, child);
		}
	}

	private void parseElement(List<ElementInfo> shapes, ElementWrapper w)
	{
		final String e = w.getTagName();
		Type typ = w.getType();

		if (typ == null)
			warn("Unknown command %s", e);
		else switch (typ)
		{
			case g:
			case a:
			{
				List<ElementInfo> g = new ArrayList<>();
				parseChildren(g, w);
				addShapeGroup(w, g, shapes);
			}
			break;
			case path:
			{
				if (w.getShape() == null)
					w.setShape(new Path(w.attr(Attribute.D, false)).getPath());
				shapes.add(createShapeInfo(w));

				// Debugging feature
				if (addPathSegments_)
				{
					Stroke s = new Stroke(new Color(this, "yellow", 1d),
							null, null, null, null, null, null);

					shapes.add(new StyledShapeInfo(w.getShape()
													.getSegmentPath(), s, s.getPaintWrapper(),
							null, null));
				}
			}
			break;
			case rect:
			{
				if (w.getShape() == null)
				{
					float x = (float) w.toPDouble(Attribute.X, w.getParent()
																.getViewPortWidth());
					float y = (float) w.toPDouble(Attribute.Y, w.getParent()
																.getViewPortHeight());
					float width = (float) w.toPDouble(Attribute.Width, w.getParent()
																		.getViewPortWidth());
					float height = (float) w.toPDouble(Attribute.Height, w.getParent()
																		  .getViewPortHeight());
					Double rx = w.toDouble(Attribute.Rx, w.getParent()
														  .getViewPortWidth(), false);
					Double ry = w.toDouble(Attribute.Ry, w.getParent()
														  .getViewPortHeight(), false);

					RectangularShape rec;

					if (rx != null || ry != null)
						rec = new RoundRectangle2D.Double(x, y, width, height, 2d * (rx == null ? ry : rx), 2d * (ry == null ? rx : ry));
					else
						rec = new Rectangle2D.Double(x, y, width, height);

					w.setShape(rec);
				}
				shapes.add(createShapeInfo(w));
			}
			break;
			case ellipse:
			{
				float cx = (float) w.toPDouble(Attribute.Cx, w.getViewPortWidth(), false);
				float cy = (float) w.toPDouble(Attribute.Cy, w.getViewPortHeight(), false);
				float rx = (float) w.toPDouble(Attribute.Rx, w.getViewPortWidth(), false);
				float ry = (float) w.toPDouble(Attribute.Ry, w.getViewPortHeight(), false);

				w.setShape(new Ellipse2D.Double(cx - rx, cy - ry, 2d * rx, 2d * ry));
				shapes.add(createShapeInfo(w));
			}
			break;
			case text:
			{
				List<ElementInfo> g = new ArrayList<>();
				new Text(this, w, defaultFont_, g);
				addShapeGroup(w, g, shapes);
			}
			break;
			case use:
			{
				// We need to create a group with transformations from the use-element that contains the referenced element.
				String href = w.href();
				if (isNotEmpty(href))
				{
					ElementWrapper refOrgW = elementCache_.getElementWrapperById(href);
					if (refOrgW != null)
					{
						GroupInfo group = new GroupInfo(w.id());

						ElementWrapper uw = refOrgW.createReferenceShadow(w);
						{
							List<ElementInfo> g = new ArrayList<>();
							parseElement(g, uw);
							addShapeGroup(w, g, group.shapes_);
						}

						Length x = w.toLength(Attribute.X, false);
						Length y = w.toLength(Attribute.Y, false);
						if (x != null || y != null)
						{
							AffineTransform posAft =
									AffineTransform.getTranslateInstance(
											x == null ? 0 : x.toPixel(null),
											y == null ? 0 : y.toPixel(null));
							group.shapes_.get(0)
										 .applyPostTransform(posAft);
						}

						group.filter_ = filter(w);
						group.clipPath_ = clipPath(w);
						shapes.add(group);
					}
				}
			}
			break;
			case circle:
			{
				double x1 = w.toPDouble(Attribute.Cx, w.getViewPortWidth());
				double y1 = w.toPDouble(Attribute.Cy, w.getViewPortHeight());
				double r = w.toPDouble(Attribute.R, w.getViewPortLength());

				w.setShape(new Ellipse2D.Double(x1 - r, y1 - r, 2 * r, 2 * r));
				shapes.add(createShapeInfo(w));
			}
			break;
			case line:
			{
				double x1 = w.toPDouble(Attribute.X1, w.getViewPortWidth());
				double y1 = w.toPDouble(Attribute.Y1, w.getViewPortHeight());
				double x2 = w.toPDouble(Attribute.X2, w.getViewPortWidth());
				double y2 = w.toPDouble(Attribute.Y2, w.getViewPortHeight());

				w.setShape(new Line2D.Double(x1, y1, x2, y2));
				shapes.add(createShapeInfo(w));
			}
			break;
			case polyline:
			{
				// @TODO
				w.setShape(new Polyline(w.attr(Attribute.Points)).getPath());
				shapes.add(createShapeInfo(w));
			}
			break;
			case polygon:
			{
				w.setShape(new Polyline(w.attr(Attribute.Points)).toPolygon());
				shapes.add(createShapeInfo(w));
			}
			break;
			case metadata:
			{
				// @TODO: Add this somehow to the data?
			}
			break;

			// Others are parsed on demand
		}
	}

	/**
	 * Finally create shapes from the elements.
	 */
	private AbstractShape finish(ElementInfo si)
	{
		ElementWrapper w = elementCache_.getElementWrapperById(si.id_);

		if (si instanceof StyledShapeInfo)
		{
			StyledShapeInfo s = (StyledShapeInfo) si;
			if (s.shape_ instanceof Path2D)
			{
				int windingRule = s.fillRule_ == FillRule.evenodd ? Path2D.WIND_EVEN_ODD : Path2D.WIND_NON_ZERO;
				((Path2D) s.shape_).setWindingRule(windingRule);
			}

			StyledShape sws = new StyledShape(
					s.id_,
					s.shape_,
					s.stroke_ == null ? null : s.stroke_.createStroke(w),
					s.paintWrapper_ == null ? null : s.paintWrapper_.createPaint(w),
					s.fillWrapper_ == null ? null : s.fillWrapper_.createPaint(w),
					s.clipping_,
					s.aft_
			);
			return sws;
		}
		else
		{
			GroupInfo g = (GroupInfo) si;
			ShapeGroup gr = new ShapeGroup(g.id_, createFilterChain(g.filter_), g.clipPath_, g.aft_);
			for (ElementInfo e : g.shapes_)
				gr.shapes_.add(finish(e));
			// @TODO
			gr.units_ = new Point2D.Double(1, 1);
			return gr;
		}
	}

	protected String mapSvgBufferName(String svgBufferName)
	{
		if (StandardFilterSource.SourceGraphic.name()
											  .equals(svgBufferName))
			return FilterBase.SOURCE;
		else
			return svgBufferName;
	}

	protected FilterChain createFilterChain(Filter filter)
	{
		FilterChain filterChain;
		String src;
		if (filter != null && !filter.primitives_.isEmpty())
		{
			List<FilterPrimitive> primitives = filter.primitives_;

			// Set-up default in/result-linkage
			Map<String, List<FilterPrimitive>> filterMap = new HashMap<>();
			final String sourceGraphic = StandardFilterSource.SourceGraphic.name();
			int sN;
			for (int fi = 0; fi < primitives.size(); ++fi)
			{
				FilterPrimitive fp = primitives.get(fi);
				if (fp.result_ == null) fp.result_ = "FilterBuffer" + filter.id_ + "-" + fi;
				sN = fp.numberOfInputs();
				if (sN > 0)
				{
					if (fp.in_.isEmpty())
						fp.in_.add(fi > 0 ? primitives.get(fi - 1).result_ : sourceGraphic);
					if (sN > 1 && fp.in_.size() < 2)
						fp.in_.add(fi > 0 ? primitives.get(fi - 1).result_ : sourceGraphic);
				}
				filterMap.computeIfAbsent(fp.result_, s -> new ArrayList<>())
						 .add(fp);
			}
			// Build primary filter tree
			List<FilterPrimitive> chain = new ArrayList<>();
			// Start with root of primary filter tree
			List<FilterPrimitive> needed = new ArrayList<>();
			needed.add(primitives.get(primitives.size() - 1));
			HashSet<String> provided = new HashSet<>();
			while (!needed.isEmpty())
			{
				// Add sources until needed sources are empty
				FilterPrimitive fp = needed.remove(0);
				chain.add(0, fp);
				provided.add(fp.result_);
				sN = fp.in_.size();
				for (int sI = 0; sI < sN; ++sI)
				{
					src = fp.in_.get(sI);
					if (StandardFilterSource.fromString(src) == null &&
							!(provided.contains(src)))
					{
						List<FilterPrimitive> srcs = filterMap.get(src);
						if (srcs == null)
						{
							warn("Filter %s has missing input: %s", filter.id_, src);
						}
						else if (!srcs.isEmpty())
						{
							FilterPrimitive fpN = srcs.remove(srcs.size() - 1);
							if (fp != fpN && !(
									chain.contains(fpN) || needed.contains(fpN)))
							{
								needed.add(fpN);
							}
						}
					}
				}
			}
			filterChain = new FilterChain(
					chain.stream()
						 .map(f ->
						 {

							 final String resultBuffer = mapSvgBufferName(f.result_);
							 final String inBuffer = f.in_.isEmpty() ? null : mapSvgBufferName(f.in_.get(0));

							 switch (f.type_)
							 {
								 case feGaussianBlur:
									 GaussianBlurFilterPrimitive gf = (GaussianBlurFilterPrimitive) f;
									 double stdDevX = 0;
									 double stdDevY = 0;
									 if (!gf.stdDeviation_.isEmpty())
									 {
										 stdDevX = gf.stdDeviation_.get(0);
										 stdDevY = (gf.stdDeviation_.size() > 1) ? gf.stdDeviation_.get(1) : stdDevX;
									 }
									 return new GaussianBlur(inBuffer, resultBuffer,
											 stdDevX, stdDevY);
								 case feOffset:
									 OffsetFilterPrimitive of = (OffsetFilterPrimitive) f;
									 return new Offset(inBuffer, resultBuffer,
											 of.dx_.toPixel(null), of.dy_.toPixel(null));
								 case feNop:
									 if (Objects.equals(inBuffer, resultBuffer))
										 return null;
									 else
										 return new Nop(inBuffer, resultBuffer);
								 case feMerge:
									 MergeFilterPrimitive mf = (MergeFilterPrimitive) f;
									 return new Merge(mf.nodes_, resultBuffer);
								 case feComposite:
								 case feSpecularLighting:
								 default:
									 return null;
							 }
						 })
						 .filter(Objects::nonNull)
						 .collect(Collectors.toList()));
		}
		else
			filterChain = null;
		return filterChain;
	}

	/**
	 * Get a pre-defined gradient.
	 *
	 * @param id The Id of the definition.
	 * @return The gradient of null.
	 */
	public Gradient getPaintServer(String id)
	{
		Gradient g = paintServer_.get(id);
		if (g == null)
		{
			ElementWrapper w = elementCache_.getElementWrapperById(id);
			if (w != null)
			{
				if ("linearGradient".equals(w.getTagName()))
					g = parseLinearGradient(w);
				else if ("radialGradient".equals(w.getTagName()))
					g = parseRadialGradient(w);
			}
			if (g != null)
				paintServer_.put(g.id_, g);
		}
		return g;
	}

	/**
	 * Get a pre-defined paint.
	 *
	 * @param id The Id of the definition.
	 * @return The paint of null.
	 */
	public PaintWrapper getPaint(String id)
	{
		PaintWrapper pt = paints_.get(id);
		if (pt == null)
		{
			Gradient g = getPaintServer(id);
			if (g != null)
				pt = g.getPaintWrapper(this);
			if (pt == null)
				pt = new PaintWrapper(java.awt.Color.BLACK);
			paints_.put(id, pt);
		}
		return pt;
	}

	public Shape getClipPath(String id)
	{
		// @TODO: use clip-rule for clipPath elements.

		ElementWrapper w = elementCache_.getElementWrapperById(id);
		if (w != null)
		{
			if (w.getType() != Type.clipPath)
			{
				warn("%s is not a clipPath", w.nodeName());
			}
			else
			{
				ShapeHelper shape = w.getShape();
				if (shape == null)
				{
					List<ElementInfo> g = new ArrayList<>();
					parseChildren(g, w);

					Path2D.Double clipPath = new Path2D.Double();
					for (ElementInfo si : g)
					{
						// @TODO: Can Clip paths contain groups?
						if (si instanceof StyledShapeInfo)
						{
							StyledShapeInfo s = (StyledShapeInfo) si;
							if (s.clipping_ == null)
							{
								clipPath.append(s.shape_, false);
							}
							else
							{
								// Intersect according to spec.
								Area area = new Area(s.shape_);
								area.intersect(new Area(s.clipping_));
								clipPath.append(area, false);
							}
						}
					}
					g.clear();
					w.setShape(clipPath);
					shape = w.getShape();
				}
				return shape.getShape();
			}
		}
		return null;
	}

	/**
	 * Gets a marker wrapper.
	 */
	public Marker getMarker(String id)
	{
		Marker m = elementCache_.getMarkerById(id);
		if (m == null)
		{
			ElementWrapper w = elementCache_.getElementWrapperById(id);
			if (w == null)
				warn("%s is unknown", id);
			else if (w.getType() != Type.marker)
				warn("%s is not a marker", w.nodeName());
			else
			{
				m = new Marker(w);
				List<ElementInfo> shapes = new ArrayList<>();
				parseChildren(shapes, w);
				addShapeGroup(w, shapes, m.shapes_);
				elementCache_.addMarker(id, m);
			}
		}
		return m;
	}

	/**
	 * Gets the filter wrapper for an element.
	 */
	public Filter filter(ElementWrapper w)
	{
		w = w == null ? null : elementCache_.getElementWrapperById(w.filter());
		if (w != null)
		{
			if (experimentalFeaturesEnables_)
			{
				Filter f = new Filter(w.id(), w.getType());
				// @TODO: handle href references for filters (same as for gradients).
				if (f.type_ != Type.filter)
					warn("%s is not a filter", f.id_);

				f.x_ = w.toLength(Attribute.X);
				f.y_ = w.toLength(Attribute.Y);
				f.width_ = w.toLength(Attribute.Width);
				f.height_ = w.toLength(Attribute.Height);

				// @TODO: filterRes
				f.filterUnits_ = Unit.fromString(w.attr(Attribute.FilterUnits));
				f.primitiveUnits_ = Unit.fromString(w.attr(Attribute.PrimitiveUnits));

				f.primitives_ = new ArrayList<>();

				// Collect all Sub-Primitives of the filter.
				elementCache_.forSubTree(w.getNode(), e ->
				{
					FilterPrimitive fp = filterPrimitive(e);
					if (fp != null)
						f.primitives_.add(fp);
				});
				return f;
			}
			else
			{
				warn("Filter are experimental and not enabled.");
			}
		}
		return null;
	}

	public FilterPrimitive filterPrimitive(ElementWrapper w)
	{
		Type t = w.getType();
		if (FilterPrimitive.isFilterPrimitive(t))
		{
			FilterPrimitive fp;
			switch (t)
			{
				case feGaussianBlur:
					fp = new GaussianBlurFilterPrimitive(w.toPDoubleList(Attribute.StdDeviation, w.getViewPortLength(), false));
					break;
				case feOffset:
					fp = new OffsetFilterPrimitive(w.toLength(Attribute.Dx), w.toLength(Attribute.Dy));
					break;
				case feMerge:
				{
					MergeFilterPrimitive merge = new MergeFilterPrimitive();
					elementCache_.forSubTree(w.getNode(), e ->
					{
						if (e.getType() == Type.feMergeNode)
						{
							MergeFilterNode node = new MergeFilterNode();
							node.id_ = e.id();
							node.in_ = e.attr(Attribute.In, false);
							merge.nodes_.add(node);
							merge.in_.add(node.in_);
						}
					});
					fp = merge;
					break;
				}
				case fePointLight:
					warn("Filter primitive %s not yet supported", t.name());
					return null;
				default:
					warn("Filter primitive %s not yet supported", t.name());
					fp = new NopFilterPrimitive();
					break;
			}
			if (fp != null)
				parseCommonFilterPrimitive(fp, w);
			return fp;
		}
		else
			return null;
	}

	private static final Map<String, MultipleGradientPaint.ColorSpaceType> colorInterpolationTypes_ =
			Map.of( // @TODO: Check for correct value for "auto"
					"auto", MultipleGradientPaint.ColorSpaceType.LINEAR_RGB,
					"sRGB ", MultipleGradientPaint.ColorSpaceType.SRGB,
					"linearRGB", MultipleGradientPaint.ColorSpaceType.LINEAR_RGB);

	public void parseCommonFilterPrimitive(FilterPrimitive fp, ElementWrapper w)
	{
		String in = w.attr(Attribute.In, false);
		if (isNotEmpty(in))
			fp.in_.add(in);
		in = w.attr(Attribute.In2, false);
		if (isNotEmpty(in))
			fp.in_.add(in);

		String colorInterpolationFilters = w.attr(Attribute.ColorInterpolationFilters, true);
		fp.colorInterpolation_ = colorInterpolationFilters == null ? MultipleGradientPaint.ColorSpaceType.LINEAR_RGB :
				colorInterpolationTypes_.getOrDefault(colorInterpolationFilters, MultipleGradientPaint.ColorSpaceType.LINEAR_RGB);

		fp.x_ = w.toLength(Attribute.X);
		fp.y_ = w.toLength(Attribute.Y);
		fp.width_ = w.toLength(Attribute.Width);
		fp.height_ = w.toLength(Attribute.Height);
		fp.result_ = w.attr(Attribute.Result, false);
	}

	/**
	 * Get all parsed shapes.
	 */
	public AbstractShape getShape()
	{
		return finalShape_;
	}

	/**
	 * Gets the internal element cache.
	 *
	 * @return Never null.
	 */
	public ElementCache getCache()
	{
		return elementCache_;
	}

	private RadialGradient parseRadialGradient(ElementWrapper w)
	{
		String id = w.id();
		if (id != null && !id.isEmpty())
		{
			RadialGradient rg = new RadialGradient(id);

			rg.cx = w.toLength(Attribute.Cx);
			rg.cy = w.toLength(Attribute.Cy);
			rg.r = w.toLength(Attribute.R);

			rg.fx = w.toLength(Attribute.Fx);
			rg.fy = w.toLength(Attribute.Fy);
			rg.fr = w.toLength(Attribute.Fr);

			parseCommonGradient(rg, w);

			return rg;
		}
		else
			return null;
	}

	private LinearGradient parseLinearGradient(ElementWrapper w)
	{
		String id = w.id();
		if (id != null && !id.isEmpty())
		{
			LinearGradient lg = new LinearGradient(id);

			lg.x1 = w.toLength(Attribute.X1);
			lg.y1 = w.toLength(Attribute.Y1);
			lg.x2 = w.toLength(Attribute.X2);
			lg.y2 = w.toLength(Attribute.Y2);

			parseCommonGradient(lg, w);

			return lg;
		}
		else
			return null;
	}


	protected void addShapeGroup(ElementWrapper w, List<ElementInfo> shapes, List<ElementInfo> target)
	{
		Filter f = filter(w);

		GroupInfo group = new GroupInfo(w.id());
		group.shapes_.addAll(shapes);
		group.filter_ = filter(w);
		group.clipPath_ = clipPath(w);
		group.applyTransform(w.transform());

		target.add(group);
		shapes.clear();
	}

	/**
	 * Helper to handle common presentation attributes and create a ShapeInfo-instance.
	 */
	protected ElementInfo createShapeInfo(ElementWrapper w)
	{
		Stroke stroke = stroke(w);
		Color fill = fill(w);
		Shape clipPath = clipPath(w);

		StyledShapeInfo styledShapeInfo = new StyledShapeInfo(w.getShape()
															   .getShape(),
				stroke.getPaintWrapper() == null ? null : stroke,
				stroke.getPaintWrapper(),
				fill.getPaintWrapper(),
				clipPath
		);
		styledShapeInfo.fillRule_ = fillRule(w);

		ElementInfo sinfo = styledShapeInfo;
		sinfo.id_ = w.id();
		transform(styledShapeInfo, w);

		GroupInfo g = null;

		Filter f = filter(w);
		if (f != null)
		{
			g = new GroupInfo(sinfo.id_);
			g.filter_ = f;
			g.shapes_.add(sinfo);
		}

		String markerMid = w.markerMid();
		if (markerMid != null)
		{
			Marker mMid = getMarker(markerMid);
			if (mMid != null)
			{
				if (g == null)
				{
					g = new GroupInfo(sinfo.id_);
					g.shapes_.add(sinfo);
				}
				ElementWrapper markerElement = elementCache_.getElementWrapperById(markerMid);
				PathIterator p = styledShapeInfo.shape_.getPathIterator(styledShapeInfo.aft_);
				p.next();
				double[] coords = new double[2];
				while (!p.isDone())
				{
					int mode = p.currentSegment(coords);
					p.next();
					if (p.isDone())
						break;
					switch (mode)
					{
						case PathIterator.SEG_LINETO:
						case PathIterator.SEG_QUADTO:
							// Convert the marker element inside the target context
							// and add them at the target position
							List<ElementInfo> markerElements = new ArrayList<>();
							parseChildren(markerElements, markerElement);
							List<ElementInfo> targetElements = new ArrayList<>();
							addShapeGroup(w, markerElements, targetElements);
							AffineTransform aft = AffineTransform.getTranslateInstance(coords[0], coords[1]);
							// @TODO angle, auto-reverse, marker-width/height, e.t.c.
							for (ElementInfo markerEI : targetElements)
							{
								markerEI.applyTransform(aft);
								g.shapes_.add(markerEI);
							}
							break;
						default:
					}
				}
			}
		}
		return g == null ? sinfo : g;
	}

	private void parseCommonGradient(Gradient g, ElementWrapper w)
	{
		g.href_ = w.href();

		String spreadMethod = w.attr(Attribute.SpreadMethod);
		if (isNotEmpty(spreadMethod))
		{
			spreadMethod = spreadMethod.trim()
									   .toLowerCase();
			if ("reflect".equals(spreadMethod))
				g.cycleMethod_ = MultipleGradientPaint.CycleMethod.REFLECT;
			else if ("repeat".equals(spreadMethod))
				g.cycleMethod_ = MultipleGradientPaint.CycleMethod.REPEAT;
		}

		g.gradientUnit_ = Unit.fromString(w.attr(Attribute.GradientUnits));

		String gradientTransform = w.attr(Attribute.GradientTransform, false);
		if (isNotEmpty(gradientTransform))
			g.aft_ = new Transform(null, gradientTransform).getTransform();

		NodeList stops = w.getNode()
						  .getElementsByTagName("stop");

		float f;
		int sN = stops.getLength();
		if (sN > 0)
		{
			g.fractions_ = new float[sN];
			g.colors_ = new java.awt.Color[sN];
			for (int i = 0; i < sN; ++i)
			{
				Element stop = (Element) stops.item(i);
				ElementWrapper wrapper = new ElementWrapper(elementCache_, stop, false);
				// Shadow tree?

				String offset = wrapper.attr(Attribute.Offset);
				if (offset != null)
				{
					f = (float) ElementWrapper.convPDouble(offset, 1);
					if (f < 0)
						f = 0;
					else if (f > 1.0f)
						f = 1.0f;
				}
				else
					f = i > 0 ? g.fractions_[i - 1] : 0;

				// Java2D enforce strictly increasing fractions.
				// SVG allows it - see https://svgwg.org/svg2-draft/pservers.html#StopNotes
				if (i > 0 && f <= g.fractions_[i - 1])
				{
					f = g.fractions_[i - 1] + .0000000001f;
				}
				g.fractions_[i] = f;

				final Color cp = new Color(this, wrapper.attr(Attribute.StopColor),
						wrapper.toPDouble(Attribute.StopOpacity, 1d, 1d, false));
				PaintWrapper pw = cp.getPaintWrapper();
				g.colors_[i] = (pw != null && pw.getColor() != null) ? pw.getColor() : java.awt.Color.BLACK;
			}
		}
	}


	/**
	 * Handles "Transform" attribute.
	 */
	protected final void transform(StyledShapeInfo s, ElementWrapper w)
	{
		AffineTransform t = w.transform();
		if (t != null)
			s.aft_ = t;
	}

	protected Color fill(ElementWrapper w)
	{
		String color = w.attr(Attribute.Fill, true);
		return new Color(this, color == null ? "black" : color, w.effectiveOpacity() * w.toPDouble(Attribute.FillOpacity, 1.0d, 1.0d, true));
	}

	protected FillRule fillRule(ElementWrapper w)
	{
		return FillRule.fromString(w.attr(Attribute.FillRule, true));
	}


	protected Stroke stroke(ElementWrapper w)
	{
		Stroke stroke = new Stroke(
				new Color(this, w.attr(Attribute.Stroke, true),
						w.effectiveOpacity() * w.toPDouble(Attribute.Stroke_Opacity, 1.0d, 1.0d, true)),
				w.toLength(Attribute.Stroke_Width, true),
				w.toLengthList(Attribute.Stroke_DashArray, true),
				// @TODO: relative widths
				w.toDouble(Attribute.Stroke_DashOffset, 1, true),
				LineCap.fromString(w.attr(Attribute.Stroke_LineCap, true)),
				LineJoin.fromString(w.attr(Attribute.Stroke_LineJoin, true)),
				// @TODO: relative limits?
				w.toDouble(Attribute.Stroke_MiterLimit, 1, true)
		);
		return stroke;
	}

	protected Shape clipPath(ElementWrapper w)
	{
		// @TODO intersect inherited clip-paths.
		Shape clipPath = getClipPath(w.clipPath());
		if (clipPath != null)
		{
			AffineTransform aft = w.transform();
			if (aft != null)
				clipPath = aft.createTransformedShape(clipPath);
		}
		return clipPath;
	}

}

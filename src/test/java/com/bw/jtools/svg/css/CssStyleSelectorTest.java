package com.bw.jtools.svg.css;

import com.bw.jtools.svg.Attribute;
import com.bw.jtools.svg.ElementCache;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CssStyleSelectorTest
{

	DocumentBuilder db;
	ElementCache elementCache_ = new ElementCache();

	@BeforeEach
	protected void initParser() throws ParserConfigurationException
	{
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		db = dbf.newDocumentBuilder();
	}

	String svg = "" +
			"<svg class='svgc' id='s1'>" +
			"  <rect class='rect-c1 rect-c2' id='r1' x='20' y='20' width='25' height='50'/>" +
			"  <circle id='c1' cx='10' cy='10' r='20'/>" +
			"</svg>";

	@Test
	public void applyTag() throws IOException, SAXException
	{
		CssStyleSelector cssStyleSelector = CSSParser.parse("rect { color:red } svg { color: blue }", "text/css");

		ByteArrayInputStream in = new ByteArrayInputStream(svg.getBytes(StandardCharsets.UTF_8));
		Document doc = db.parse(in);
		elementCache_.scanForIds(doc);

		cssStyleSelector.apply(doc, elementCache_);

		assertEquals("red", elementCache_.getElementWrapperById("r1")
										 .attr(Attribute.Color),
				"Rectangle shall get the more specific color from tag selector");

		assertEquals("blue", elementCache_.getElementWrapperById("c1")
										  .attr(Attribute.Color),
				"Circle shall inherit parent color");

		assertEquals("blue", elementCache_.getElementWrapperById("s1")
										  .attr(Attribute.Color),
				"svg element shall get color from tag-selector.");
	}

	@Test
	public void applyClass() throws IOException, SAXException
	{
		CssStyleSelector cssStyleSelector = CSSParser.parse(".svgc { color:red } .rect-c2 { color:green }", "text/css");

		ByteArrayInputStream in = new ByteArrayInputStream(svg.getBytes(StandardCharsets.UTF_8));
		Document doc = db.parse(in);
		elementCache_.scanForIds(doc);

		cssStyleSelector.apply(doc, elementCache_);

		assertEquals("green", elementCache_.getElementWrapperById("r1")
										   .attr(Attribute.Color),
				"Rectangle shall get the more specific color from class selector");

		assertEquals("red", elementCache_.getElementWrapperById("c1")
										 .attr(Attribute.Color),
				"Circle shall inherit parent color");

		assertEquals("red", elementCache_.getElementWrapperById("s1")
										 .attr(Attribute.Color),
				"svg element shall get color from class-selector.");
	}

}
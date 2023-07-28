package com.bw.jtools.svg.css;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CSSParserTest
{
	@Test
	void parse() throws IOException
	{
		CSSParser parser = new CSSParser();

		String css = "svg, div span { color:red;} .a-b+c .xyz { color: blue }";
		CssStyleSelector selector = parser.parse(css, "text/css");
		assertEquals(2, selector.rules_.size());
		assertEquals(2, selector.rules_.get(0).selectors_.size());
		assertEquals("svg", selector.rules_.get(0).selectors_.get(0).id_);
		assertEquals("div", selector.rules_.get(0).selectors_.get(1).id_);
		assertEquals("span", selector.rules_.get(0).selectors_.get(1).combinate_.id_);
		assertEquals(1, selector.rules_.get(0).styles_.size());
		assertEquals(SelectorType.TAG, selector.rules_.get(0).selectors_.get(0).type_, "Tag selector was not detected");
		assertEquals("red", selector.rules_.get(0).styles_.get("color"));

		assertEquals("blue", selector.rules_.get(1).styles_.get("color"));
		assertEquals(1, selector.rules_.get(1).selectors_.size());
		assertEquals("a-b+c", selector.rules_.get(1).selectors_.get(0).id_);
		assertEquals(SelectorType.CLASS, selector.rules_.get(1).selectors_.get(0).type_, "Class selector was not detected");

	}
}